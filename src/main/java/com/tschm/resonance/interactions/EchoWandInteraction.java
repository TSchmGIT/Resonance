package com.tschm.resonance.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.metadata.EchoWandMetaData;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EchoWandInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<EchoWandInteraction> CODEC = BuilderCodec.builder(
            EchoWandInteraction.class, EchoWandInteraction::new, SimpleBlockInteraction.CODEC
    ).build();

    @Override
    protected void interactWithBlock(
            @Nonnull World world,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InteractionType interactionType,
            @Nonnull InteractionContext context,
            @Nullable ItemStack itemStack,
            @Nonnull Vector3i targetPos,
            @Nonnull CooldownHandler cooldownHandler) {
        if (itemStack == null || !itemStack.getItemId().equals("Echo_Wand") || context.getHeldItemContainer() == null)
            return;

        var compGenerator = ComponentHelper.findGeneratorComponentAt(world, targetPos);
        var compStorage = ComponentHelper.findComponentAt(world, targetPos, EssenceStorageComponent.getComponentType());
        if (compGenerator == null && compStorage == null)
            return;

        EchoWandMetaData metaData = itemStack.getFromMetadataOrDefault("EchoWandMetaData", EchoWandMetaData.CODEC);

        if (compGenerator != null) {
            switch (metaData.wandState) {
                case None -> {
                    metaData.boundPosition = targetPos;
                    metaData.wandState = EchoWandMetaData.EchoWandState.Bound;
                    DebugHelper.Print("Bound wand to " + targetPos.toString());
                }
                case Bound -> {
                    metaData.boundPosition = null;
                    metaData.wandState = EchoWandMetaData.EchoWandState.None;
                    DebugHelper.Print("Can only bind to RE container!");
                }
            }
        } else {
            List<Vector3i> alreadyBoundPositions = compStorage.getBoundSenderList();

            switch (metaData.wandState) {
                case None -> {
                    DebugHelper.Print("Click on generator to start binding");
                    metaData.boundPosition = null;
                }
                case Bound -> {
                    Vector3i boundPos = metaData.boundPosition;
                    var boundGenerator = ComponentHelper.findGeneratorComponentAt(world, boundPos);
                    if (boundGenerator != null) {
                        // Remove old storage from generator
                        Vector3i oldStoragePos = boundGenerator.boundStoragePos;
                        if (oldStoragePos != null){
                            var compOldStorage = ComponentHelper.findComponentAt(world, oldStoragePos, EssenceStorageComponent.getComponentType());
                            assert compOldStorage != null;
                            compOldStorage.getBoundSenderList().remove(boundPos);
                        }

                        if (alreadyBoundPositions.contains(boundPos)) {
                            alreadyBoundPositions.remove(boundPos);
                            boundGenerator.boundStoragePos = null;
                        } else {
                            alreadyBoundPositions.add(boundPos);
                            boundGenerator.boundStoragePos = targetPos;
                        }
                    } else
                        DebugHelper.Print("Cannot find generator at bound position " + boundPos.toString());

                    metaData.wandState = EchoWandMetaData.EchoWandState.None;
                    metaData.boundPosition = null;
                }
            }
        }

        ItemStack newItemStack = itemStack.withMetadata(EchoWandMetaData.KEYED_CODEC, metaData);
        if (newItemStack.equals(itemStack))
            return;

        ItemContainer heldItemContainer = context.getHeldItemContainer();
        byte heldSlot = context.getHeldItemSlot();
        heldItemContainer.replaceItemStackInSlot(heldSlot, itemStack, newItemStack);
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull ItemStack itemStack, @Nonnull World world, @Nonnull Vector3i vector3i) {
    }
}
