package com.tschm.resonance.systems.functional;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.functional.HarmonicProcessorComponent;
import com.tschm.resonance.util.*;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class HarmonicProcessorSystems {
    public static class TickingSystem extends EntityTickingSystem<ChunkStore> {

        @Override
        public void tick(float dt, int idx, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            final HarmonicProcessorComponent comp = archetypeChunk.getComponent(idx, HarmonicProcessorComponent.getComponentType());
            final EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            final Vector3i targetPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            assert comp != null && compStorage != null && targetPos != null;

            final World world = commandBuffer.getExternalData().getWorld();

            tickProcessing(comp, compStorage, world, targetPos);
            tickItemTransfer(comp, world, targetPos);
        }

        private static void tickProcessing(HarmonicProcessorComponent comp, EssenceStorageComponent compStorage, World world, Vector3i targetPos) {
            // Fallback handling when processing item is unexpectedly null
            if (comp.processingRecipe == null && comp.remainingProcessingTicks > 0) {
                DebugHelper.PrintOnce("processor_null_recipe", "HarmonicProcessor: processingRecipe was null while remainingProcessingTicks > 0, resetting ticks");
                comp.remainingProcessingTicks = 0;
            }

            // Update progression or start a new operation
            BlockType blockType = world.getBlockType(targetPos);
            assert blockType != null;
            Bench bench = blockType.getBench();
            boolean hasPendingOperation = comp.processingRecipe != null && comp.remainingProcessingTicks > 0;
            boolean isProcessing = hasPendingOperation ? processItem(comp, compStorage) : startNewOperation(comp, bench);

            // Block State Update - "On" only when actively processing with RE
            BlockHelper.activateBlockState(isProcessing ? "On" : "Off", world, targetPos);
        }

        private static void tickItemTransfer(HarmonicProcessorComponent comp, World world, Vector3i targetPos) {
            // Item transfer to/from adjacent ItemContainerBlock blocks
            comp.remainingTransferTicks = Math.max(0, comp.remainingTransferTicks - 1);
            if (comp.remainingTransferTicks > 0)
                return;

            // Try to fill adjacent inventory first
            BlockType blockType = world.getBlockType(targetPos);
            assert blockType != null;
            Bench bench = blockType.getBench();
            if (bench != null) {
                List<CraftingRecipe> allowedRecipes = CraftingPlugin.getBenchRecipes(BenchType.Crafting, bench.getId());
                if (allowedRecipes.isEmpty())
                    DebugHelper.PrintOnce("processor_no_recipes_" + bench.getId(), "HarmonicProcessor: No crafting recipes found for bench: " + bench.getId());

                // Transfer items from and to adjacent containers
                TransferMode[] sideConfigs = ItemTransferHelper.toSideArray(comp.sideUp, comp.sideDown, comp.sideNorth, comp.sideSouth, comp.sideWest, comp.sideEast);
                ItemTransferHelper.processTransfers(world, targetPos, comp.inputContainer, comp.outputContainer, sideConfigs, allowedRecipes);

                comp.remainingTransferTicks = comp.ticksPerTransfer;
            } else {
                DebugHelper.PrintOnce("processor_no_bench_" + blockType.getId(), "HarmonicProcessor: No bench found for block: " + blockType.getId());
            }

            // Drop any remaining output items into the world
            Store<EntityStore> entityStore = world.getEntityStore().getStore();
            for (short i = (short) 0; i < comp.outputContainer.getCapacity(); ++i) {
                ItemStack itemStack = comp.outputContainer.getItemStack(i);
                if (itemStack == null)
                    continue;

                DebugHelper.PrintTimed("processor_drop_" + itemStack.getItemId(), "HarmonicProcessor: Dropping item: " + itemStack.getItemId());
                Holder<EntityStore> holder = ItemComponent.generateItemDrop(entityStore, itemStack, targetPos.toVector3d(), Vector3f.ZERO, 0, 0, 0);
                if (holder != null)
                    world.execute(() -> entityStore.addEntity(holder, AddReason.SPAWN));
            }
            comp.outputContainer.clear();
        }

        /**
         * @return true if actively processing (RE was available), false if paused
         */
        private static boolean processItem(HarmonicProcessorComponent comp, EssenceStorageComponent compStorage) {
            // Check if we have enough RE to process this tick
            long requiredEssence = comp.essencePerTick;
            long simulated = compStorage.removeEssence(requiredEssence, true);
            if (simulated < requiredEssence) {
                // Not enough RE - pause processing without losing progress
                return false;
            }

            // Consume RE
            compStorage.removeEssence(requiredEssence, false);
            comp.remainingProcessingTicks--;

            // Return if we are still processing
            if (comp.remainingProcessingTicks == 0) {
                boolean outputProduced = createCraftingResult(comp);
                if (outputProduced) {
                    // Reset timer to 0 and clear current recipe
                    comp.remainingProcessingTicks = 0;
                    comp.processingRecipe = null;
                } else {
                    // If any output failed to be placed, cancel the operation
                    // Reset the timer to 1 tick to prepare a new operation
                    comp.remainingProcessingTicks = 1;
                }
            }

            // Return true if we are still processing, false if we are done
            return comp.remainingProcessingTicks > 0;
        }

        private static boolean createCraftingResult(HarmonicProcessorComponent comp) {
            CraftingRecipe processingRecipe = comp.processingRecipe;
            ItemContainer outputContainer = comp.outputContainer;

            // Try to move outputs to output container
            Stream<ItemStack> newItemStacksStream = Arrays.stream(processingRecipe.getOutputs()).map(MaterialQuantity::toItemStack).dropWhile(Objects::isNull);
            ListTransaction<ItemStackTransaction> transaction = outputContainer.addItemStacks(newItemStacksStream.toList(), true, false, false);

            return transaction.succeeded();
        }

        private static boolean startNewOperation(HarmonicProcessorComponent comp, Bench bench) {
            if (comp.processingRecipe != null || comp.remainingProcessingTicks > 0)
                return false;

            ItemContainer inputContainer = comp.inputContainer;
            if (inputContainer.isEmpty())
                return false;

            for (short i = (short) 0; i < inputContainer.getCapacity(); ++i) {
                ItemStack itemStack = inputContainer.getItemStack(i);
                if (itemStack == null || itemStack.isEmpty())
                    continue;

                CraftingRecipe recipe = CraftingHelper.findMatchingRecipe(BenchType.Crafting, "Harmonic_Pulverizer", itemStack);
                if (recipe == null)
                    continue;

                ItemStackSlotTransaction transaction = inputContainer.removeItemStackFromSlot(i, 1);
                if (!transaction.succeeded()) {
                    DebugHelper.PrintOnce("processor_input_remove_fail", "HarmonicProcessor: Failed to remove item from input slot " + i);
                    continue;
                }

                comp.processingRecipe = recipe;
                comp.remainingProcessingTicks = comp.ticksPerOperation;
                return true;
            }

            return false;
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(HarmonicProcessorComponent.getComponentType(), EssenceStorageComponent.getComponentType());
        }
    }

    public static class BreakSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

        public BreakSystem() {
            super(BreakBlockEvent.class);
        }

        @Override
        public void handle(int i,
                           @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull CommandBuffer<EntityStore> commandBuffer,
                           @Nonnull BreakBlockEvent breakBlockEvent) {

            Vector3i pos = breakBlockEvent.getTargetBlock();
            World world = commandBuffer.getExternalData().getWorld();

            HarmonicProcessorComponent comp = ComponentHelper.findComponentAt(world, pos, HarmonicProcessorComponent.getComponentType());
            if (comp == null)
                return;

            // Drop all items from both containers
            Vector3d dropPos = pos.toVector3d();
            dropContainerItems(commandBuffer, dropPos, comp.inputContainer);
            dropContainerItems(commandBuffer, dropPos, comp.outputContainer);
        }

        private void dropContainerItems(CommandBuffer<EntityStore> commandBuffer, Vector3d pos, ItemContainer container) {
            List<ItemStack> items = container.dropAllItemStacks();
            Holder<EntityStore>[] holders = ItemComponent.generateItemDrops(commandBuffer, items, pos, Vector3f.ZERO);
            if (holders.length > 0)
                commandBuffer.addEntities(holders, AddReason.SPAWN);
        }

        @Nullable
        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
    }
}
