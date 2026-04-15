package com.tschm.resonance.util;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ItemTransferHelper {

    public static void processTransfers(
            World world, Vector3i pos,
            ItemContainer inputContainer, ItemContainer outputContainer,
            TransferMode[] sideConfigs, List<CraftingRecipe> allowedRecipes) {
        for (int i = 0; i < Vector3i.BLOCK_SIDES.length; i++) {
            TransferMode mode = sideConfigs[i];
            if (mode == TransferMode.Disabled)
                continue;

            Vector3i adjacentPos = pos.add(Vector3i.BLOCK_SIDES[i]);
            ItemContainer adjacent = findItemContainerAt(world, adjacentPos);
            if (adjacent == null)
                continue;

            if (mode.isPull())
                transferItems(adjacent, inputContainer, 1, allowedRecipes);

            if (mode.isPush())
                transferItems(outputContainer, adjacent, Integer.MAX_VALUE, null);
        }
    }

    /**
     * Returns the side configs as an array matching Vector3i.BLOCK_SIDES order:
     * Up(0), Down(1), North(2), South(3), West(4), East(5).
     */
    public static TransferMode[] toSideArray(
            TransferMode up, TransferMode down,
            TransferMode north, TransferMode south,
            TransferMode west, TransferMode east) {
        return new TransferMode[]{up, down, north, south, west, east};
    }

    private static void transferItems(ItemContainer from, ItemContainer to, int maxQuantity, @Nullable List<CraftingRecipe> allowedRecipes) {
        for (short slot = 0; slot < from.getCapacity(); slot++) {
            ItemStack itemStack = from.getItemStack(slot);
            if (itemStack == null || itemStack.isEmpty()) {
                continue;
            }

            boolean isRecipe = allowedRecipes == null || allowedRecipes.stream().anyMatch(r -> Arrays.stream(r.getInput()).anyMatch(i -> i.getItemId().equals(itemStack.getItemId())));
            if (!isRecipe)
                continue;

            int quantity = Math.min(itemStack.getQuantity(), maxQuantity);
            MoveTransaction<ItemStackTransaction> transaction = from.moveItemStackFromSlot(slot, quantity, to, false, false);
            if (transaction.succeeded()) {
                break;
            } else {
                DebugHelper.PrintTimed("transfer_move_fail", "ItemTransfer: Failed to move " + itemStack.getItemId() + " (qty " + quantity + ") - target container may be full");
            }
        }
    }

    @Nullable
    private static ItemContainer findItemContainerAt(World world, Vector3i pos) {
        ItemContainerBlock containerBlock = ComponentHelper.findComponentAt(world, pos, ItemContainerBlock.getComponentType());
        if (containerBlock != null)
            return containerBlock.getItemContainer();

        return null;
    }
}
