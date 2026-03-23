package com.tschm.resonance.util;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;

import javax.annotation.Nullable;

public class CraftingHelper {
    @Nullable
    public static CraftingRecipe findMatchingRecipe(BenchType benchType, String benchName, ItemStack inputStack) {
        if (inputStack.isEmpty())
            return null;

        // Check if any recipe on the given bench has the item as one of its inputs
        String itemId = inputStack.getItemId();
        for (CraftingRecipe recipe : CraftingPlugin.getBenchRecipes(benchType, benchName)) {
            for (MaterialQuantity input : recipe.getInput()) {
                if (itemId.equals(input.getItemId())) {
                    return recipe;
                }
            }
        }

        return null;
    }
}
