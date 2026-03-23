package com.tschm.resonance.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.functional.HarmonicPulverizerComponent;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.CraftingHelper;
import com.tschm.resonance.util.DebugHelper;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

public class HarmonicPulverizerInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<HarmonicPulverizerInteraction> CODEC = BuilderCodec.builder(
            HarmonicPulverizerInteraction.class, HarmonicPulverizerInteraction::new, SimpleBlockInteraction.CODEC
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

        var comp = ComponentHelper.findComponentAt(world, targetBlock, HarmonicPulverizerComponent.getComponentType());
        if (comp == null) {
            ctx.getState().state = InteractionState.Failed;
            return;
        }

        SimpleItemContainer inputContainer = comp.inputContainer;

        ItemStack processingItemStack = itemStack.withQuantity(1);
        assert processingItemStack != null;

        if (!inputContainer.canAddItemStack(processingItemStack, true, false) ||
                !heldItemContainer.canRemoveItemStack(processingItemStack)) {
            DebugHelper.Print("Cannot add item to input container or remove item from held item container");
            ctx.getState().state = InteractionState.Failed;
            return;
        }

        CraftingRecipe recipe = CraftingHelper.findMatchingRecipe(BenchType.Crafting, "Harmonic_Pulverizer", itemStack);
        if (recipe == null) {
            DebugHelper.Print("No matching recipe found for " + itemStack.getItemId());
            ctx.getState().state = InteractionState.Failed;
            return;
        }

        // Remove from held item container
        ItemStackTransaction transaction = heldItemContainer.removeItemStack(processingItemStack);
        if (!transaction.succeeded()) {
            DebugHelper.Print("Failed to remove item from held item container");
            ctx.getState().state = InteractionState.Failed;
            return;
        }

        // Add to input container of Harmonic Pulverizer
        // Systems will handle processing of those items
        transaction = inputContainer.addItemStack(processingItemStack);
        if (!transaction.succeeded()) {
            DebugHelper.Print("Failed to add item to input container of Harmonic Pulverizer");
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
