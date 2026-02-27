package com.tschm.resonance.interactions;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionEntry;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.RitualStoneComponent;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.ItemHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class RitualStoneInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<RitualStoneInteraction> CODEC = BuilderCodec.builder(
            RitualStoneInteraction.class, RitualStoneInteraction::new, SimpleBlockInteraction.CODEC
    ).build();

    @Override
    protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                                     @Nonnull InteractionType type, @Nonnull InteractionContext context,
                                     @Nullable ItemStack itemStack, @Nonnull Vector3i targetBlock,
                                     @Nonnull CooldownHandler cooldownHandler) {
        RitualStoneComponent compRitualStone = ComponentHelper.findComponentAt(world, targetBlock, RitualStoneComponent.getComponentType());
        EssenceStorageComponent compStorage = ComponentHelper.findComponentAt(world, targetBlock, EssenceStorageComponent.getComponentType());
        if (compRitualStone == null || compStorage == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        ItemContainer heldItemContainer = context.getHeldItemContainer();
        if (heldItemContainer == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        ItemStack heldItem = context.getHeldItem();
        byte heldSlot = context.getHeldItemSlot();

        // Check if we initiated a crafting interaction
        if (heldItem != null && heldItem.getItemId().equals("Echo_Wand")) {
            final boolean itemCrafted = processRecipeInteraction(world, commandBuffer, context, targetBlock, compRitualStone, compStorage);
            return;
        }

        // Empty hand pulls out the most recently inserted item.
        if (heldItem == null) {
            RitualStoneComponent.Slot slot = compRitualStone.getTopmostFilledSlot();
            if (slot == null) {
                return;
            }

            ItemStack item = compRitualStone.getItem(slot);
            UUID oldUUID = compRitualStone.getEntityUUID(slot);
            compRitualStone.setItem(slot, null); //< remove from Ritual Stone

            // Add to item container
            heldItemContainer.addItemStackToSlot(heldSlot, item);

            // Remove spawned preview entity
            Ref<EntityStore> previewItemEntity = oldUUID != null ? world.getEntityRef(oldUUID) : null;
            if (previewItemEntity != null) {
                commandBuffer.removeEntity(previewItemEntity, RemoveReason.REMOVE);
            }

            // Update data references
            compRitualStone.setItemUUID(slot, null);
        }
        // Otherwise insert if a slot is available.
        else {
            RitualStoneComponent.Slot slot = compRitualStone.getFirstEmptySlot();
            if (slot == null) {
                return;
            }

            ItemStack singleItem = heldItem.withQuantity(1);
            compRitualStone.setItem(slot, singleItem); //< add to Ritual Stone

            Vector3d spawnOffset;
            float scale;
            switch (slot) {
                case MAIN -> {
                    spawnOffset = new Vector3d(0, 36, 0).scale(1.0f / 32.0);
                    scale = 12.0f / 32.0f;
                }
                case CATALYST_1 -> {
                    spawnOffset = new Vector3d(32, 33, 0).scale(1.0f / 32.0);
                    scale = 6.0f / 32.0f;
                }
                case CATALYST_2 -> {
                    spawnOffset = new Vector3d(-32, 33, 0).scale(1.0f / 32.0);
                    scale = 6.0f / 32.0f;
                }
                default -> throw new IllegalStateException("Unexpected slot: " + slot);
            }

            // Remove from Item Container
            heldItemContainer.removeItemStackFromSlot(heldSlot, singleItem, 1);

            // Spawn preview entity at the rotated local offset of the ritual stone.
            Rotation rotation = Rotation.None;
            WorldChunk worldChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
            if (worldChunk == null || worldChunk.getBlockType(targetBlock) == null) {
                context.getState().state = InteractionState.Failed;
                return;
            }
            BlockChunk blockChunk = worldChunk.getBlockChunk();
            if (blockChunk != null) {
                BlockSection blockSection = blockChunk.getSectionAtBlockY(targetBlock.y);
                if (blockSection != null) {
                    RotationTuple rotationTuple = blockSection.getRotation(targetBlock.x, targetBlock.y, targetBlock.z);
                    rotation = rotationTuple.yaw();
                }
            }

            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

            spawnOffset.rotateY((float) rotation.getRadians());
            spawnOffset.add(0.5f, 0.0f, 0.5f);

            UUID spawnedUUID = ItemHelper.spawnStaticItemModel(commandBuffer, holder,
                    targetBlock.toVector3d().add(spawnOffset),
                    new Vector3f(0.0f, rotation.getDegrees(), 0.0f),
                    0, singleItem, scale);
            compRitualStone.setItemUUID(slot, spawnedUUID);
        }
    }

    private static boolean processRecipeInteraction(@NonNullDecl World world, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl InteractionContext context, @NonNullDecl Vector3i targetBlock, RitualStoneComponent compRitualStone, EssenceStorageComponent compStorage) {
        CraftingRecipe craftingRecipe = findMatchingCraftingRecipe(compRitualStone, compStorage);
        DebugHelper.Print(craftingRecipe != null ? "This would yield " + craftingRecipe.getPrimaryOutput().toString() : "There's nothing this would yield!");

        // Return if no recipe was found. Optional: Give feedback to player (sound, cfx, text, etc.)
        if (craftingRecipe == null)
            return false;

        // Empty/Consume ritual stone inputs first
        RitualStoneComponent.Slot filledSlot = null;
        while ((filledSlot = compRitualStone.getTopmostFilledSlot()) != null) {
            // Remove spawned preview entity
            UUID oldUUID = compRitualStone.getEntityUUID(filledSlot);
            Ref<EntityStore> previewItemEntity = oldUUID != null ? world.getEntityRef(oldUUID) : null;
            if (previewItemEntity != null) {
                commandBuffer.removeEntity(previewItemEntity, RemoveReason.REMOVE);
            }
            compRitualStone.setItem(filledSlot, null);
            compRitualStone.setItemUUID(filledSlot, null);
        }

        // Spawn new entity and update component
        Rotation rotation = Rotation.None;
        WorldChunk worldChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
        if (worldChunk == null || worldChunk.getBlockType(targetBlock) == null) {
            context.getState().state = InteractionState.Failed;
            return false;
        }
        BlockChunk blockChunk = worldChunk.getBlockChunk();
        if (blockChunk != null) {
            BlockSection blockSection = blockChunk.getSectionAtBlockY(targetBlock.y);
            if (blockSection != null) {
                RotationTuple rotationTuple = blockSection.getRotation(targetBlock.x, targetBlock.y, targetBlock.z);
                rotation = rotationTuple.yaw();
            }
        }
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        Vector3d spawnOffset = new Vector3d(0, 36, 0).scale(1.0f / 32.0);
        spawnOffset.rotateY((float) rotation.getRadians());
        spawnOffset.add(0.5f, 0.0f, 0.5f);

        MaterialQuantity primaryOutput = craftingRecipe.getPrimaryOutput();
        ItemStack newItem = primaryOutput.toItemStack();

        UUID spawnedUUID = ItemHelper.spawnStaticItemModel(commandBuffer, holder,
                targetBlock.toVector3d().add(spawnOffset),
                new Vector3f(0.0f, rotation.getDegrees(), 0.0f),
                0, newItem, 12.0f / 32.0f);

        compRitualStone.setMainInput(newItem);
        compRitualStone.setMainInputUUID(spawnedUUID);

        // Update essence
        for (MaterialQuantity input : craftingRecipe.getInput()) {
            if (Objects.equals(input.getResourceTypeId(), "Resonant_Essence")) {
                final long extractedEssence = compStorage.extractEssence((long) input.getQuantity(), true);
                if (extractedEssence < input.getQuantity())
                    return false;
                compStorage.extractEssence(extractedEssence, false);
                break;
            }
        }

        return true;
    }

    @Nullable
    public static CraftingRecipe findMatchingCraftingRecipe(RitualStoneComponent compRitualStone, EssenceStorageComponent compStorage) {
        // Find matching recipe for current inputs
        List<CraftingRecipe> benchRecipes = CraftingPlugin.getBenchRecipes(BenchType.Crafting, "Ritual_Stone");

        for (CraftingRecipe recipe : benchRecipes) {
            Set<RitualStoneComponent.Slot> availableSlots = new HashSet<>(Arrays.asList(RitualStoneComponent.Slot.values()));
            boolean recipeRequirementsMet = true;

            // Iterate inputs to check availability
            for (MaterialQuantity input : recipe.getInput()) {

                // If required, check if we have enough essence
                if (Objects.equals(input.getResourceTypeId(), "Resonant_Essence")) {
                    if (compStorage == null || compStorage.getEssenceStored() < input.getQuantity()) {
                        DebugHelper.Print("Not enough essence!");
                        recipeRequirementsMet = false;
                        break;
                    }
                    continue;
                }

                // Check if any available slot contains required inpt
                RitualStoneComponent.Slot matchingSlot = null;
                for (RitualStoneComponent.Slot availableSlot : availableSlots) {

                    // If item -> check item requirements
                    if (input.getItemId() != null) {
                        ItemStack inputStack = input.toItemStack();
                        ItemStack slotStack = compRitualStone.getItem(availableSlot);
                        if (ItemStack.isEquivalentType(inputStack, slotStack)) {
                            matchingSlot = availableSlot;
                            break;
                        }
                    }
                    // If tag -> check tag requirements
                    else if (input.getTagIndex() != Integer.MIN_VALUE) {
                        /* NYI */
                    }
                    // Else resource -> check resource requirements
                    else {
                        ResourceQuantity resource = input.toResource();
                        ItemStack slotItemStack = compRitualStone.getItem(availableSlot);
                        if (!ItemStack.isEmpty(slotItemStack)) {
                            Item slotItem = slotItemStack.getItem();
                            ItemResourceType resourceType = resource.getResourceType(slotItem);
                            if (resourceType != null) {
                                matchingSlot = availableSlot;
                                break;
                            }
                        }
                    }
                }

                // No available slot has the required input
                if (matchingSlot == null) {
                    recipeRequirementsMet = false;
                    break;
                }

                // A slot cannot be used twice
                // This accounts for recipes with multiples items of the same id
                availableSlots.remove(matchingSlot);
            }

            // If still true, we found a recipe
            if (recipeRequirementsMet) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType type,
                                             @Nonnull InteractionContext interactionContext,
                                             @Nullable ItemStack itemStack, @Nonnull World world,
                                             @Nonnull Vector3i targetBlock) {

    }

    public String printInteractionSyncData(InteractionSyncData data) {
        StringBuilder sb = new StringBuilder("InteractionSyncData{");

        sb.append("state=").append(data.state);
        sb.append(", progress=").append(String.format("%.2f", data.progress));
        sb.append(", opCounter=").append(data.operationCounter);
        sb.append(", root=").append(data.rootInteraction);
        sb.append(", entityId=").append(data.entityId);

        if (data.blockPosition != null) {
            sb.append(", blockPos=").append(data.blockPosition);
            sb.append(", face=").append(data.blockFace);
            if (data.blockRotation != null) {
                sb.append(", rot=").append(data.blockRotation);
            }
        }

        if (data.placedBlockId != Integer.MIN_VALUE) {
            sb.append(", placedBlockId=").append(data.placedBlockId);
        }

        if (data.chargeValue >= 0) {
            sb.append(", charge=").append(String.format("%.2f", data.chargeValue));
        }

        if (data.forkCounts != null && !data.forkCounts.isEmpty()) {
            sb.append(", forks=").append(data.forkCounts);
        }

        if (data.hitEntities != null) {
            sb.append(", hitEntities=").append(data.hitEntities.length);
        }

        if (data.attackerPos != null) {
            sb.append(", attackerPos=").append(data.attackerPos);
        }

        if (data.raycastHit != null) {
            sb.append(", rayHit=").append(data.raycastHit);
            sb.append(", dist=").append(String.format("%.2f", data.raycastDistance));
        }

        sb.append(", moveDir=").append(data.movementDirection);
        sb.append(", force=").append(data.applyForceState);
        sb.append(", nextLabel=").append(data.nextLabel);

        if (data.generatedUUID != null) {
            sb.append(", uuid=").append(data.generatedUUID);
        }

        sb.append('}');
        return sb.toString();
    }

}
