package com.tschm.resonance.interactions.echo_wand;

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
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.storage.EchoStorageComponent;
import com.tschm.resonance.interactions.echo_wand.handler.*;
import com.tschm.resonance.metadata.EchoWandMetaData;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class EchoWandInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<EchoWandInteraction> CODEC = BuilderCodec.builder(
            EchoWandInteraction.class, EchoWandInteraction::new, SimpleBlockInteraction.CODEC
    ).build();

    /**
     * All components found at the interaction target, bundled for handler dispatch.
     */
    public record WandTarget(
            @Nullable EssenceGeneratorComponent generator,
            @Nullable EssenceStorageComponent essenceStorage,
            @Nullable EchoStorageComponent echoStorage,
            @Nullable ItemContainerState itemContainer) {
    }

    private static final List<WandInteractionHandler> HANDLERS = List.of(
            new EssenceStorageHandler(),
            new EchoStorageHandler()
    );

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------
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

        WandTarget target = new WandTarget(
                ComponentHelper.findGeneratorComponentAt(world, targetPos),
                ComponentHelper.findComponentAt(world, targetPos, EssenceStorageComponent.getComponentType()),
                ComponentHelper.findComponentAt(world, targetPos, EchoStorageComponent.getComponentType()),
                ComponentHelper.findBlockStateAt(world, targetPos, ItemContainerState.class));

        if (target.generator() == null && target.essenceStorage() == null
                && target.echoStorage() == null && target.itemContainer() == null)
            return;

        EchoWandMetaData metaData = itemStack.getFromMetadataOrDefault("EchoWandMetaData", EchoWandMetaData.CODEC);

        String lastError = "No Interaction executed";
        for (WandInteractionHandler handler : HANDLERS) {
            if (!handler.isApplicable(target))
                continue;

            Optional<String> result = handler.handle(world, targetPos, metaData, target);
            if (result.isEmpty()) {
                lastError = null;
                break;
            }
            lastError = result.get();
        }

        if (lastError != null) {
            metaData.resetBinding();
            DebugHelper.Print(lastError);
        }

        ItemStack newItemStack = itemStack.withMetadata(EchoWandMetaData.KEYED_CODEC, metaData);
        if (newItemStack.equals(itemStack))
            return;

        ItemContainer heldItemContainer = context.getHeldItemContainer();
        byte heldSlot = context.getHeldItemSlot();
        heldItemContainer.replaceItemStackInSlot(heldSlot, itemStack, newItemStack);
    }

// -------------------------------------------------------------------------
// Shared utilities
// -------------------------------------------------------------------------
    @Override
    protected void simulateInteractWithBlock(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl World world, @NonNullDecl Vector3i vector3i) {
    }
}
