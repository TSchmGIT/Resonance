package com.tschm.resonance.components.functional;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.util.TransferMode;

import javax.annotation.Nullable;

public class HarmonicProcessorComponent implements Component<ChunkStore> {
    public static final BuilderCodec<HarmonicProcessorComponent> CODEC;
    private static ComponentType<ChunkStore, HarmonicProcessorComponent> type;

    public static ComponentType<ChunkStore, HarmonicProcessorComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, HarmonicProcessorComponent> newType) {
        if (type != null) {
            throw new IllegalStateException("HarmonicProcessorComponent ComponentType already set");
        }
        type = newType;
    }

    public SimpleItemContainer inputContainer = new SimpleItemContainer((short) 1);
    public SimpleItemContainer outputContainer = new SimpleItemContainer((short) 4);

    public CraftingRecipe processingRecipe = null;

    public long ticksPerOperation = 30L * 10L;
    public long remainingProcessingTicks = 0L;
    public long essencePerTick = 5L;

    // Side configuration for item transfer
    public TransferMode sideUp = TransferMode.Disabled;
    public TransferMode sideDown = TransferMode.Disabled;
    public TransferMode sideNorth = TransferMode.Disabled;
    public TransferMode sideSouth = TransferMode.Disabled;
    public TransferMode sideWest = TransferMode.Disabled;
    public TransferMode sideEast = TransferMode.Disabled;
    public int ticksPerTransfer = 15;
    public int remainingTransferTicks = 0;

    @Nullable
    @Override
    public Component<ChunkStore> clone() {
        HarmonicProcessorComponent clone = new HarmonicProcessorComponent();
        clone.inputContainer = new SimpleItemContainer(this.inputContainer);
        clone.outputContainer = new SimpleItemContainer(this.outputContainer);
        clone.processingRecipe = this.processingRecipe != null ? new CraftingRecipe(this.processingRecipe) : null;
        clone.ticksPerOperation = this.ticksPerOperation;
        clone.remainingProcessingTicks = this.remainingProcessingTicks;
        clone.essencePerTick = this.essencePerTick;
        clone.sideUp = this.sideUp;
        clone.sideDown = this.sideDown;
        clone.sideEast = this.sideEast;
        clone.sideWest = this.sideWest;
        clone.sideNorth = this.sideNorth;
        clone.sideSouth = this.sideSouth;
        clone.ticksPerTransfer = this.ticksPerTransfer;
        clone.remainingTransferTicks = this.remainingTransferTicks;

        return clone;
    }

    static {
        CODEC = BuilderCodec.builder(HarmonicProcessorComponent.class, HarmonicProcessorComponent::new)
                .append(new KeyedCodec<>("InputContainer", SimpleItemContainer.CODEC), (c, v) -> c.inputContainer = v, c -> c.inputContainer)
                .documentation("Input Container that holds the items that should be processed").add()
                .append(new KeyedCodec<>("OutputContainer", SimpleItemContainer.CODEC), (c, v) -> c.outputContainer = v, c -> c.outputContainer)
                .documentation("Output Container that holds the processed items").add()
                .append(new KeyedCodec<>("ProcessingRecipe", CraftingRecipe.CODEC), (c, v) -> c.processingRecipe = v, c -> c.processingRecipe)
                .documentation("Recipe that is currently being processed").add()
                .append(new KeyedCodec<>("TicksPerOperation", BuilderCodec.LONG), (c, v) -> c.ticksPerOperation = v, c -> c.ticksPerOperation)
                .addValidator(Validators.greaterThan(0L)).add()
                .append(new KeyedCodec<>("RemainingProcessingTicks", BuilderCodec.LONG), (c, v) -> c.remainingProcessingTicks = v, c -> c.remainingProcessingTicks)
                .documentation("Ticks remaining for the current processing operation").add()
                .append(new KeyedCodec<>("EssencePerTick", BuilderCodec.LONG), (c, v) -> c.essencePerTick = v, c -> c.essencePerTick)
                .documentation("RE consumed per tick during processing").addValidator(Validators.greaterThanOrEqual(0L)).add()
                .append(new KeyedCodec<>("SideUp", TransferMode.CODEC), (c, v) -> c.sideUp = v, c -> c.sideUp)
                .documentation("Transfer mode for the top face").add()
                .append(new KeyedCodec<>("SideDown", TransferMode.CODEC), (c, v) -> c.sideDown = v, c -> c.sideDown)
                .documentation("Transfer mode for the bottom face").add()
                .append(new KeyedCodec<>("SideNorth", TransferMode.CODEC), (c, v) -> c.sideNorth = v, c -> c.sideNorth)
                .documentation("Transfer mode for the north face").add()
                .append(new KeyedCodec<>("SideSouth", TransferMode.CODEC), (c, v) -> c.sideSouth = v, c -> c.sideSouth)
                .documentation("Transfer mode for the south face").add()
                .append(new KeyedCodec<>("SideWest", TransferMode.CODEC), (c, v) -> c.sideWest = v, c -> c.sideWest)
                .documentation("Transfer mode for the west face").add()
                .append(new KeyedCodec<>("SideEast", TransferMode.CODEC), (c, v) -> c.sideEast = v, c -> c.sideEast)
                .documentation("Transfer mode for the east face").add()
                .append(new KeyedCodec<>("TicksPerTransfer", BuilderCodec.INTEGER), (c, v) -> c.ticksPerTransfer = v, c -> c.ticksPerTransfer)
                .documentation("Ticks between each transfer operation").addValidator(Validators.greaterThan(0)).add()
                .append(new KeyedCodec<>("RemainingTransferTicks", BuilderCodec.INTEGER), (c, v) -> c.remainingTransferTicks = v, c -> c.remainingTransferTicks)
                .documentation("Ticks remaining until next transfer").add()
                .build();
    }
}
