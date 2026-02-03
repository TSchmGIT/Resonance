package com.tschm.resonance.interactions;

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
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionEntry;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.RitualStoneComponent;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.ItemHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class RitualStoneInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<RitualStoneInteraction> CODEC = BuilderCodec.builder(
            RitualStoneInteraction.class, RitualStoneInteraction::new, SimpleBlockInteraction.CODEC
    ).build();

    @Override
    protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                                     @Nonnull InteractionType type, @Nonnull InteractionContext context,
                                     @Nullable ItemStack itemStack, @Nonnull Vector3i targetBlock,
                                     @Nonnull CooldownHandler cooldownHandler) {
        RitualStoneComponent ritualStoneComponent = ComponentHelper.findChunkComponentAt(world, targetBlock, RitualStoneComponent.getComponentType());
        if (ritualStoneComponent == null) {
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
            CraftingRecipe craftingRecipe = ritualStoneComponent.findMatchingCraftingRecipe();
            DebugHelper.Print(craftingRecipe != null ? "This would yield " + craftingRecipe.getPrimaryOutput().toString() : "There's nothing this would yield!");

            // Return if no recipe was found. Optional: Give feedback to player (sound, cfx, text, etc.)
            if (craftingRecipe == null)
                return;

            // Empty/Consume ritual stone inputs first
            RitualStoneComponent.Slot filledSlot = null;
            while ((filledSlot = ritualStoneComponent.getTopmostFilledSlot()) != null){
                // Remove spawned preview entity
                UUID oldUUID = ritualStoneComponent.getEntityUUID(filledSlot);
                Ref<EntityStore> previewItemEntity = oldUUID != null ? world.getEntityRef(oldUUID) : null;
                if (previewItemEntity != null) {
                    commandBuffer.removeEntity(previewItemEntity, RemoveReason.REMOVE);
                }
                ritualStoneComponent.setItem(filledSlot, null);
                ritualStoneComponent.setItemUUID(filledSlot, null);
            }

            // Spawn new entity and update component
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

            Vector3d spawnOffset = new Vector3d(0, 36, 0).scale(1.0f / 32.0);
            spawnOffset.rotateY((float) rotation.getRadians());
            spawnOffset.add(0.5f, 0.0f, 0.5f);

            MaterialQuantity primaryOutput = craftingRecipe.getPrimaryOutput();
            ItemStack newItem = primaryOutput.toItemStack();

            UUID spawnedUUID = ItemHelper.spawnStaticItemModel(commandBuffer, holder,
                    targetBlock.toVector3d().add(spawnOffset),
                    new Vector3f(0.0f, rotation.getDegrees(), 0.0f),
                    0, newItem, 12.0f / 32.0f);

            ritualStoneComponent.setMainInput(newItem);
            ritualStoneComponent.setMainInputUUID(spawnedUUID);
            return;
        }

        // Empty hand pulls out the most recently inserted item.
        if (heldItem == null) {
            RitualStoneComponent.Slot slot = ritualStoneComponent.getTopmostFilledSlot();
            if (slot == null) {
                return;
            }

            ItemStack item = ritualStoneComponent.getItem(slot);
            UUID oldUUID = ritualStoneComponent.getEntityUUID(slot);
            ritualStoneComponent.setItem(slot, null); //< remove from Ritual Stone

            // Add to item container
            heldItemContainer.addItemStackToSlot(heldSlot, item);

            // Remove spawned preview entity
            Ref<EntityStore> previewItemEntity = oldUUID != null ? world.getEntityRef(oldUUID) : null;
            if (previewItemEntity != null) {
                commandBuffer.removeEntity(previewItemEntity, RemoveReason.REMOVE);
            }

            // Update data references
            ritualStoneComponent.setItemUUID(slot, null);
        }
        // Otherwise insert if a slot is available.
        else {
            RitualStoneComponent.Slot slot = ritualStoneComponent.getFirstEmptySlot();
            if (slot == null) {
                return;
            }

            ItemStack singleItem = heldItem.withQuantity(1);
            ritualStoneComponent.setItem(slot, singleItem); //< add to Ritual Stone

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
            ritualStoneComponent.setItemUUID(slot, spawnedUUID);
        }
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType type,
                                             @Nonnull InteractionContext interactionContext,
                                             @Nullable ItemStack itemStack, @Nonnull World world,
                                             @Nonnull Vector3i targetBlock) {

    }

    public static String printInteractionContext(InteractionContext ctx) {
        if (ctx == null) {
            return "InteractionContext <null>";
        }

        StringBuilder sb = new StringBuilder("InteractionContext{");

        // --- Entity info ---
        sb.append("entity=");
        sb.append(ctx.getEntity() != null ? ctx.getEntity() : "<none>");

        sb.append(", owner=");
        sb.append(ctx.getOwningEntity() != null ? ctx.getOwningEntity() : "<none>");

        // --- Interaction chain ---
        InteractionChain chain = ctx.getChain();
        if (chain != null) {
            sb.append(", chainType=").append(chain.getType());
            sb.append(", chainId=").append(chain.getChainId());
            sb.append(", baseType=").append(chain.getBaseType());
            sb.append(", predicted=").append(chain.isPredicted());
        } else {
            sb.append(", chain=<null>");
        }

        // --- Entry / execution ---
        InteractionEntry entry = ctx.getEntry();
        if (entry != null) {
            sb.append(", entryIdx=").append(entry.getIndex());
            sb.append(", op=").append(ctx.getOperationCounter());
            sb.append(", useSim=").append(entry.isUseSimulationState());
        }

        // --- Held item ---
        sb.append(", heldSection=").append(ctx.getHeldItemSectionId());
        sb.append(", heldSlot=").append(ctx.getHeldItemSlot());

        ItemStack heldItem = ctx.getHeldItem();
        if (heldItem != null) {
            sb.append(", heldItem=").append(heldItem.getItem().getId());
            sb.append(" x").append(heldItem.getQuantity());
        } else {
            sb.append(", heldItem=<empty>");
        }

        // --- Original item (important for interaction resolution) ---
        if (ctx.getOriginalItemType() != null) {
            sb.append(", originalItem=").append(ctx.getOriginalItemType().getId());
        }

        // --- Targets ---
        Ref<EntityStore> targetEntity = ctx.getTargetEntity();
        if (targetEntity != null && targetEntity.isValid()) {
            sb.append(", targetEntity=").append(targetEntity);
        }

        BlockPosition targetBlock = ctx.getTargetBlock();
        if (targetBlock != null) {
            sb.append(", targetBlock=").append(targetBlock);
        }

        // --- Sync state ---
        InteractionSyncData state = ctx.getState();
        if (state != null) {
            sb.append(", state=").append(state);
        }

        InteractionSyncData clientState = ctx.getClientState();
        if (clientState != null && clientState != state) {
            sb.append(", clientState=").append(clientState);
        }

        // --- Labels ---
        if (ctx.hasLabels()) {
            sb.append(", labels=").append(ctx.hasLabels());
        }

        sb.append('}');
        return sb.toString();
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
