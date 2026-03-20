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

        // Check if any recipe for Harmonic Pulverizer has the item as one of its input
        // Recipes might contain more inputs like Resonant Essence (RE) or catalysts
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
