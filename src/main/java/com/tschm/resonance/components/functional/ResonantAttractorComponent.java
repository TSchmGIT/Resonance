package com.tschm.resonance.components.functional;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ResonantAttractorComponent implements Component<ChunkStore> {
    public static final BuilderCodec<ResonantAttractorComponent> CODEC;
    private static ComponentType<ChunkStore, ResonantAttractorComponent> type;

    public static ComponentType<ChunkStore, ResonantAttractorComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, ResonantAttractorComponent> newType) {
        if (type != null) {
            throw new IllegalStateException("ResonantAttractorComponent ComponentType already set");
        }
        type = newType;
    }

    private double attractionRangeUnattuned = 1;
    private double attractionRangeAttuned = 4;
    public boolean isAttuned = false;
    public double attractionSpeed = 0.05;

    public ResonantAttractorComponent() {}
    public ResonantAttractorComponent(double attractionRangeUnattuned, double attractionRangeAttuned, boolean isAttuned, double attractionSpeed) {
        this.attractionRangeUnattuned = attractionRangeUnattuned;
        this.attractionRangeAttuned = attractionRangeAttuned;
        this.isAttuned = isAttuned;
        this.attractionSpeed = attractionSpeed;
    }

    public double getAttractionRange() {
        return isAttuned ? attractionRangeAttuned : attractionRangeUnattuned;
    }

    @Override
    public Component<ChunkStore> clone() {
        return new ResonantAttractorComponent(attractionRangeUnattuned, attractionRangeAttuned, isAttuned, attractionSpeed);
    }

    static {
        CODEC = BuilderCodec.builder(ResonantAttractorComponent.class, ResonantAttractorComponent::new)
                .append(new KeyedCodec<>("AttractionRangeUnattuned", BuilderCodec.DOUBLE), (c, v) -> c.attractionRangeUnattuned = v, c -> c.attractionRangeUnattuned)
                .addValidator(Validators.greaterThan(0d))
                .documentation("Range of blocks that are attracted while not provided with RE").add()
                .append(new KeyedCodec<>("AttractionRangeAttuned", BuilderCodec.DOUBLE), (c, v) -> c.attractionRangeAttuned = v, c -> c.attractionRangeAttuned)
                .addValidator(Validators.greaterThan(0d))
                .documentation("Range of blocks that are attracted while provided with RE").add()
                .append(new KeyedCodec<>("IsAttuned", BuilderCodec.BOOLEAN), (c, v) -> c.isAttuned = v, c -> c.isAttuned)
                .documentation("Whether the attractor is currently attuned to RE").add()
                .append(new KeyedCodec<>("AttractionSpeed", BuilderCodec.DOUBLE), (c, v) -> c.attractionSpeed = v, c -> c.attractionSpeed)
                .addValidator(Validators.greaterThan(0d))
                .documentation("Speed at which items are attracted to the attractor").add()
                .build();
    }
}
