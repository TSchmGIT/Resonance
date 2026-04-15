package com.tschm.resonance.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.functional.EchoConjurerComponent;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EchoConjurerInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<EchoConjurerInteraction> CODEC = BuilderCodec.builder(
            EchoConjurerInteraction.class, EchoConjurerInteraction::new, SimpleBlockInteraction.CODEC
    ).build();

    @Override
    protected void interactWithBlock(
            @Nonnull World world,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InteractionType interactionType,
            @Nonnull InteractionContext ctx,
            @Nullable ItemStack itemStack,
            @Nonnull Vector3i targetBlock,
            @Nonnull CooldownHandler cooldownHandler) {
        ItemContainer heldItemContainer = ctx.getHeldItemContainer();
        if (itemStack == null || itemStack.isEmpty() || heldItemContainer == null) {
            ctx.getState().state = InteractionState.Failed;
            return;
        }

        EchoConjurerComponent comp = ComponentHelper.findComponentAt(world, targetBlock, EchoConjurerComponent.getComponentType());
        if (comp == null) {
            ctx.getState().state = InteractionState.Failed;
            return;
        }

        // Only accept the configured Life Essence item
        if (!comp.lifeEssenceItemId.equals(itemStack.getItemId())) {
            ctx.getState().state = InteractionState.Failed;
            return;
        }

        SimpleItemContainer inputContainer = comp.lifeEssenceContainer;
        MoveTransaction<ItemStackTransaction> transaction = heldItemContainer.moveItemStackFromSlot(ctx.getHeldItemSlot(), 1, inputContainer, true, false);
        if (!transaction.succeeded()) {
            DebugHelper.Print("EchoConjurer: Failed to move Life Essence from held container to input container");
            ctx.getState().state = InteractionState.Failed;
            return;
        }
    }

    @Override
    protected void simulateInteractWithBlock(
            @Nonnull InteractionType interactionType,
            @Nonnull InteractionContext interactionContext,
            @Nullable ItemStack itemStack,
            @Nonnull World world,
            @Nonnull Vector3i vector3i) {
    }
}
