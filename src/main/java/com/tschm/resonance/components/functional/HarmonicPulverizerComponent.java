package com.tschm.resonance.components.functional;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nullable;

public class HarmonicPulverizerComponent implements Component<ChunkStore> {
    public static final BuilderCodec<HarmonicPulverizerComponent> CODEC;
    private static ComponentType<ChunkStore, HarmonicPulverizerComponent> type;

    public static ComponentType<ChunkStore, HarmonicPulverizerComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, HarmonicPulverizerComponent> newType) {
        if (type != null) {
            throw new IllegalStateException("HarmonicPulverizerComponent ComponentType already set");
        }
        type = newType;
    }

    public SimpleItemContainer inputContainer = new SimpleItemContainer((short) 4);
    public SimpleItemContainer outputContainer = new SimpleItemContainer((short) 4);

    public CraftingRecipe processingRecipe = null;

    public long ticksPerOperation = 30L * 10L;
    public long remainingProcessingTicks = 0L;

    @Nullable
    @Override
    public Component<ChunkStore> clone() {
        HarmonicPulverizerComponent clone = new HarmonicPulverizerComponent();
        clone.inputContainer = new SimpleItemContainer(this.inputContainer);
        clone.outputContainer = new SimpleItemContainer(this.outputContainer);
        clone.processingRecipe = this.processingRecipe != null ? new CraftingRecipe(this.processingRecipe) : null;
        clone.ticksPerOperation = this.ticksPerOperation;
        clone.remainingProcessingTicks = this.remainingProcessingTicks;

        return clone;
    }

    static {
        CODEC = BuilderCodec.builder(HarmonicPulverizerComponent.class, HarmonicPulverizerComponent::new)
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
                .build();
    }
}
