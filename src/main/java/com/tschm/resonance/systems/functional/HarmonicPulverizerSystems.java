package com.tschm.resonance.systems.functional;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.functional.HarmonicPulverizerComponent;
import com.tschm.resonance.util.BlockHelper;
import com.tschm.resonance.util.CraftingHelper;
import com.tschm.resonance.util.SystemsHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class HarmonicPulverizerSystems {
    public static class TickingSystem extends EntityTickingSystem<ChunkStore> {

        @Override
        public void tick(float dt, int idx, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            HarmonicPulverizerComponent comp = archetypeChunk.getComponent(idx, HarmonicPulverizerComponent.getComponentType());
            assert comp != null;

            // Fallback handling when processing item is unexpectedly null
            if (comp.processingRecipe == null)
                comp.remainingProcessingTicks = 0;

            if (isProcessingItem(comp)) {
                processItem(comp);
            } else {
                startNewOperation(comp);
            }

            Vector3i targetPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            assert targetPos != null;

            World world = commandBuffer.getExternalData().getWorld();
            Store<EntityStore> entityStore = world.getEntityStore().getStore();
            for (short i = (short) 0; i < comp.outputContainer.getCapacity(); ++i) {
                ItemStack itemStack = comp.outputContainer.getItemStack(i);
                if (itemStack == null)
                    continue;

                Holder<EntityStore> holder = ItemComponent.generateItemDrop(entityStore, itemStack, targetPos.toVector3d(), Vector3f.ZERO, 0, 0, 0);
                if (holder != null)
                    world.execute(() -> entityStore.addEntity(holder, AddReason.SPAWN));
            }
            comp.outputContainer.clear();

            // Block State Update
            BlockHelper.activateBlockState(isProcessingItem(comp) ? "On" : "Off", commandBuffer.getExternalData().getWorld(), targetPos);
        }

        private static boolean isProcessingItem(HarmonicPulverizerComponent comp) {
            return comp.processingRecipe != null && comp.remainingProcessingTicks > 0;
        }

        private static void processItem(HarmonicPulverizerComponent comp) {
            comp.remainingProcessingTicks--;

            // Return if we are still processing
            if (comp.remainingProcessingTicks > 0)
                return;

            CraftingRecipe processingRecipe = comp.processingRecipe;
            ItemContainer outputContainer = comp.outputContainer;

            // Try to move item to output container
            boolean transactionsSucceeded = true;
            for (MaterialQuantity output : processingRecipe.getOutputs()) {
                ItemStack itemStack = output.toItemStack();
                if (itemStack == null)
                    continue;

                ItemStackTransaction transaction = outputContainer.addItemStack(itemStack, true, false, false);

                if (!transaction.succeeded()) {
                    transactionsSucceeded = false;
                    break;
                }
            }

            if (transactionsSucceeded) {
                // Set timer to 0 to prepare a new operation
                comp.remainingProcessingTicks = 0;
                comp.processingRecipe = null;
            } else {
                // We remain at the end of the processed state
                comp.remainingProcessingTicks = 1;
            }
        }

        private static void startNewOperation(HarmonicPulverizerComponent comp) {
            ItemContainer inputContainer = comp.inputContainer;
            if (inputContainer.isEmpty() || comp.processingRecipe != null)
                return;

            for (short i = (short) 0; i < inputContainer.getCapacity(); ++i) {
                ItemStack itemStack = inputContainer.getItemStack(i);
                if (itemStack == null || itemStack.isEmpty())
                    continue;

                CraftingRecipe recipe = CraftingHelper.findMatchingRecipe(BenchType.Crafting, "Harmonic_Pulverizer", itemStack);
                if (recipe == null)
                    continue;

                ItemStackSlotTransaction transaction = inputContainer.removeItemStackFromSlot(i, 1);
                if (!transaction.succeeded())
                    continue;

                comp.processingRecipe = recipe;
                comp.remainingProcessingTicks = comp.ticksPerOperation;
                break;
            }
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return HarmonicPulverizerComponent.getComponentType();
        }
    }
}
