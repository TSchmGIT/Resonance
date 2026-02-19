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
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.storage.EchoStorageComponent;
import com.tschm.resonance.metadata.EchoWandMetaData;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

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
        var compStorageEssence = ComponentHelper.findComponentAt(world, targetPos, EssenceStorageComponent.getComponentType());

        var compStorageEcho = ComponentHelper.findComponentAt(world, targetPos, EchoStorageComponent.getComponentType());
        var itemContainerState = ComponentHelper.findBlockStateAt(world, targetPos, ItemContainerState.class);
        if (compGenerator == null && compStorageEssence == null && compStorageEcho == null && itemContainerState == null) {
            return;
        }

        EchoWandMetaData metaData = itemStack.getFromMetadataOrDefault("EchoWandMetaData", EchoWandMetaData.CODEC);

        String interactionErrorInfoString = "No Interaction executed";
        if (compGenerator != null)
            interactionErrorInfoString = interactWithGenerator(targetPos, metaData);

        if (interactionErrorInfoString != null && compStorageEssence != null)
            interactionErrorInfoString = interactWithEssenceStorage(world, targetPos, compStorageEssence, metaData);

        if (interactionErrorInfoString != null)
            interactionErrorInfoString = interactWithEchoStorage(world, targetPos, compStorageEcho, metaData);

        if (interactionErrorInfoString != null) {
            metaData.resetBinding();
            DebugHelper.Print(interactionErrorInfoString);
        }

        ItemStack newItemStack = itemStack.withMetadata(EchoWandMetaData.KEYED_CODEC, metaData);
        if (newItemStack.equals(itemStack))
            return;

        ItemContainer heldItemContainer = context.getHeldItemContainer();
        byte heldSlot = context.getHeldItemSlot();
        heldItemContainer.replaceItemStackInSlot(heldSlot, itemStack, newItemStack);
    }

    private static String interactWithGenerator(@NonNullDecl Vector3i targetPos, EchoWandMetaData metaData) {
        switch (metaData.getWandState()) {
            case None -> {
                boolean success = metaData.bindTo(targetPos, EchoWandMetaData.EchoWandBindingType.Generator);
                if (success) {
                    DebugHelper.Print("Bound wand to " + targetPos.toString());
                    return null;
                } else {
                    metaData.resetBinding();
                    return "Cannot bind to generator!";
                }
            }
            case Bound -> {
                metaData.resetBinding();
                return "Can only bind to RE container!";
            }
        }

        return "Unknown generate interaction!";
    }

    private static String interactWithEssenceStorage(@NonNullDecl World world, @NonNullDecl Vector3i targetPos, EssenceStorageComponent compStorageEssence, EchoWandMetaData metaData) {
        List<Vector3i> alreadyBoundPositions = compStorageEssence.getBoundSenderList();

        switch (metaData.getWandState()) {
            case None -> {
                return "Click on generator to start binding";
            }
            case Bound -> {
                if (metaData.getBindingType() != EchoWandMetaData.EchoWandBindingType.Generator) {
                    return "Cannot bind to " + metaData.getBindingType().toString() + "!";
                }

                if (!compStorageEssence.canReceive) {
                    return "Essence Storage cannot receive essence!";
                }

                Vector3i boundPos = metaData.getBoundPosition();
                assert boundPos != null;

                var boundGenerator = ComponentHelper.findGeneratorComponentAt(world, boundPos);
                if (boundGenerator != null) {
                    // Remove old storage from generator
                    Vector3i oldStoragePos = boundGenerator.boundStoragePos;
                    if (oldStoragePos != null) {
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

                metaData.resetBinding();
            }
        }

        return null;
    }

    private static String interactWithEchoStorage(@Nonnull World world, @Nonnull Vector3i targetPos, @Nullable EchoStorageComponent compStorageEcho, EchoWandMetaData metaData) {
        switch (metaData.getWandState()) {
            case None -> {
                if (compStorageEcho == null) {
                    return "Select an Echo Storage first!";
                }

                if (!compStorageEcho.canSend()) {
                    return "This echo storage cannot retrieve essence!";
                }

                boolean success = metaData.bindTo(targetPos, EchoWandMetaData.EchoWandBindingType.EchoStorage);
                if (!success) {
                    return "Cannot bind to echo storage!";
                } else {
                    DebugHelper.Print("Cached Echo Storage at " + targetPos.toString());
                    return null;
                }
            }
            case Bound -> {
                if (metaData.getBindingType() != EchoWandMetaData.EchoWandBindingType.EchoStorage) {
                    return "Cannot bind to " + metaData.getBindingType().toString() + "!";
                }

                Vector3i boundPos = metaData.getBoundPosition();
                assert boundPos != null;

                EchoStorageComponent boundEchoStorage = ComponentHelper.findComponentAt(world, boundPos, EchoStorageComponent.getComponentType());
                if (boundEchoStorage == null) {
                    return "No echo storage at bound position " + boundPos.toString();
                }

                if (!boundEchoStorage.canSend() || (compStorageEcho != null && !compStorageEcho.canReceive())) {
                    if (!boundEchoStorage.canSend())
                        return "Bound Echo storage cannot retrieve essence!";
                    if (compStorageEcho != null && !compStorageEcho.canReceive())
                        return "Selected Echo storage cannot store essence!";
                }

                if (compStorageEcho != null)
                    createBinding(world, boundEchoStorage, boundPos, compStorageEcho, targetPos);
                else
                    createBinding(world, boundEchoStorage, targetPos);
            }
        }

        return null;
    }

    private static void createBinding(World world, EchoStorageComponent compSender, Vector3i posSender, EchoStorageComponent compReceiver, Vector3i posReceiver) {
        // Clean up old references
        if (compSender.getBoundPositionSend() != null) {
            Vector3i posReceiverOld = compSender.getBoundPositionSend();
            EchoStorageComponent compReceiverOld = ComponentHelper.findComponentAt(world, posReceiverOld, EchoStorageComponent.getComponentType());
            if (compReceiverOld != null)
                compReceiverOld.setBoundPositionReceive(null);
        }
        if (compReceiver.getBoundPositionReceive() != null) {
            Vector3i posSenderOld = compReceiver.getBoundPositionReceive();
            EchoStorageComponent compSenderOld = ComponentHelper.findComponentAt(world, posSenderOld, EchoStorageComponent.getComponentType());
            if (compSenderOld != null)
                compSenderOld.setBoundPositionSend(null);
        }

        compSender.setBoundPositionSend(posReceiver);
        compReceiver.setBoundPositionReceive(posSender);
        DebugHelper.Print("Bound two Echo Storages!");
    }

    private static void createBinding(World world, EchoStorageComponent compSender, Vector3i posReceiver) {
        // Clean up old references
        if (compSender.getBoundPositionSend() != null) {
            Vector3i posReceiverOld = compSender.getBoundPositionSend();
            EchoStorageComponent compReceiverOld = ComponentHelper.findComponentAt(world, posReceiverOld, EchoStorageComponent.getComponentType());
            if (compReceiverOld != null)
                compReceiverOld.setBoundPositionReceive(null);
        }

        compSender.setBoundPositionSend(posReceiver);
        DebugHelper.Print("Bound Echo Storage to ItemContainerState at " + posReceiver.toString());
    }

    @Override
    protected void simulateInteractWithBlock(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl World world, @NonNullDecl Vector3i vector3i) {
    }
}
