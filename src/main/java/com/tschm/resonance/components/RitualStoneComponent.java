package com.tschm.resonance.components;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.ItemResourceType;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import com.hypixel.hytale.server.core.inventory.container.InternalContainerUtilResource;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.util.DebugHelper;

import javax.annotation.Nullable;
import java.util.*;

public class RitualStoneComponent implements Component<ChunkStore> {
    public enum Slot {
        MAIN(1),
        CATALYST_1(2),
        CATALYST_2(3);

        private final int slotInt;

        Slot(int slotInt) {
            this.slotInt = slotInt;
        }

        public int getSlotInt() {
            return slotInt;
        }

        public static Slot fromInt(int value) {
            for (Slot s : values()) {
                if (s.slotInt == value) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown slot: " + value);
        }
    }

    public static final BuilderCodec CODEC;
    private static ComponentType<ChunkStore, RitualStoneComponent> type;
    public static ComponentType<ChunkStore, RitualStoneComponent> getComponentType() {
        return type;
    }
    public static void setComponentType(ComponentType<ChunkStore, RitualStoneComponent> type) {
        RitualStoneComponent.type = type;
    }

    @Nullable
    private ItemStack mainInput;
    @Nullable
    private UUID mainInputUUID;
    @Nullable
    private ItemStack catalystInput1;
    @Nullable
    private UUID catalystInput1UUID;
    @Nullable
    private ItemStack catalystInput2;
    @Nullable
    private UUID catalystInput2UUID;

    public RitualStoneComponent() {
    }

    public RitualStoneComponent(@Nullable ItemStack mainInput, @Nullable UUID mainInputUUID, @Nullable ItemStack catalystInput1, @Nullable UUID catalystInput1UUID, @Nullable ItemStack catalystInput2, @Nullable UUID catalystInput2UUID) {
        this.mainInput = mainInput;
        this.mainInputUUID = mainInputUUID;
        this.catalystInput1 = catalystInput1;
        this.catalystInput1UUID = catalystInput1UUID;
        this.catalystInput2 = catalystInput2;
        this.catalystInput2UUID = catalystInput2UUID;
    }

    @Override
    public Component<ChunkStore> clone() {
        return new RitualStoneComponent(this.mainInput, this.mainInputUUID, this.catalystInput1, this.catalystInput1UUID, this.catalystInput2, this.catalystInput2UUID);
    }

    @Nullable
    public ItemStack getMainInput() {
        return mainInput;
    }

    public void setMainInput(@Nullable ItemStack mainInput) {
        this.mainInput = mainInput;
    }

    @Nullable
    public UUID getMainInputUUID() {
        return mainInputUUID;
    }

    public void setMainInputUUID(@Nullable UUID mainInputUUID) {
        this.mainInputUUID = mainInputUUID;
    }

    @Nullable
    public ItemStack getCatalystInput1() {
        return catalystInput1;
    }

    public void setCatalystInput1(@Nullable ItemStack catalystInput1) {
        this.catalystInput1 = catalystInput1;
    }

    @Nullable
    public UUID getCatalystInput1UUID() {
        return catalystInput1UUID;
    }

    public void setCatalystInput1UUID(@Nullable UUID catalystInput1UUID) {
        this.catalystInput1UUID = catalystInput1UUID;
    }


    @Nullable
    public ItemStack getCatalystInput2() {
        return catalystInput2;
    }

    public void setCatalystInput2(@Nullable ItemStack catalystInput2) {
        this.catalystInput2 = catalystInput2;
    }

    @Nullable
    public UUID getCatalystInput2UUID() {
        return catalystInput2UUID;
    }

    public void setCatalystInput2UUID(@Nullable UUID catalystInput2UUID) {
        this.catalystInput2UUID = catalystInput2UUID;
    }

    @Nullable
    public ItemStack getItem(Slot slot) {
        return switch (slot) {
            case MAIN -> mainInput;
            case CATALYST_1 -> catalystInput1;
            case CATALYST_2 -> catalystInput2;
        };
    }

    public void setItem(Slot slot, @Nullable ItemStack item) {
        switch (slot) {
            case MAIN -> mainInput = item;
            case CATALYST_1 -> catalystInput1 = item;
            case CATALYST_2 -> catalystInput2 = item;
        }
    }

    @Nullable
    public UUID getEntityUUID(Slot slot) {
        return switch (slot) {
            case MAIN -> mainInputUUID;
            case CATALYST_1 -> catalystInput1UUID;
            case CATALYST_2 -> catalystInput2UUID;
        };
    }

    public void setItemUUID(Slot slot, @Nullable UUID uuid) {
        switch (slot) {
            case MAIN -> mainInputUUID = uuid;
            case CATALYST_1 -> catalystInput1UUID = uuid;
            case CATALYST_2 -> catalystInput2UUID = uuid;
        }
    }

    /**
     * Slot order reflects how items are extracted: catalyst2 -> catalyst1 -> main.
     */
    @Nullable
    public Slot getTopmostFilledSlot() {
        if (catalystInput2 != null) {
            return Slot.CATALYST_2;
        }
        if (catalystInput1 != null) {
            return Slot.CATALYST_1;
        }
        if (mainInput != null) {
            return Slot.MAIN;
        }
        return null;
    }

    @Nullable
    public Slot getFirstEmptySlot() {
        if (mainInput == null) {
            return Slot.MAIN;
        }
        if (catalystInput1 == null) {
            return Slot.CATALYST_1;
        }
        if (catalystInput2 == null) {
            return Slot.CATALYST_2;
        }
        return null;
    }

    @Nullable
    public CraftingRecipe findMatchingCraftingRecipe() {
        // Find matching recipe for current inputs
        List<CraftingRecipe> benchRecipes = CraftingPlugin.getBenchRecipes(BenchType.Crafting, "Ritual_Stone");
        Set<Slot> availableSlots = new HashSet<>(Arrays.asList(Slot.values()));

        for (CraftingRecipe recipe : benchRecipes) {
            boolean recipeRequirementsMet = true;

            // Iterate inputs to check availability
            for (MaterialQuantity input : recipe.getInput()) {
                // Check if any available slot contains required inpt
                Slot matchingSlot = null;
                for (Slot availableSlot : availableSlots) {

                    // If item -> check item requirements
                    if (input.getItemId() != null) {
                        ItemStack inputStack = input.toItemStack();
                        ItemStack slotStack = getItem(availableSlot);
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
                        ItemStack slotItemStack = getItem(availableSlot);
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
//                    DebugHelper.Print("No slot matched for material: " + input.toString());
                    recipeRequirementsMet = false;
                    break;
                }

                // A slot cannot be used twice
                // This accounts for recipes with multiples items of the same id
                availableSlots.remove(matchingSlot);
//                DebugHelper.Print("Slot matched: " + matchingSlot.toString());
            }

            // If still true, we found a recipe
            if (recipeRequirementsMet) {
                return recipe;
            }
        }
        return null;
    }

    static {
        CODEC = BuilderCodec.builder(RitualStoneComponent.class, RitualStoneComponent::new)
                .append(new KeyedCodec<>("MainInput", ItemStack.CODEC),
                        (component, stack) -> component.mainInput = stack,
                        (component) -> component.mainInput).add()
                .append(new KeyedCodec<>("MainInputUUID", BuilderCodec.UUID_BINARY),
                        (component, uuid) -> component.mainInputUUID = uuid,
                        (component) -> component.mainInputUUID).add()
                .append(new KeyedCodec<>("CatalystInput1", ItemStack.CODEC),
                        (component, stack) -> component.catalystInput1 = stack,
                        (component) -> component.catalystInput1).add()
                .append(new KeyedCodec<>("CatalystInput1UUID", BuilderCodec.UUID_BINARY),
                        (component, uuid) -> component.catalystInput1UUID = uuid,
                        (component) -> component.catalystInput1UUID).add()
                .append(new KeyedCodec<>("CatalystInput2", ItemStack.CODEC),
                        (component, stack) -> component.catalystInput2 = stack,
                        (component) -> component.catalystInput2).add()
                .append(new KeyedCodec<>("CatalystInput2UUID", BuilderCodec.UUID_BINARY),
                        (component, uuid) -> component.catalystInput2UUID = uuid,
                        (component) -> component.catalystInput2UUID).add()
                .build();
    }
}
