package com.tschm.resonance.components.functional;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nullable;

public class ResonantDisrupterComponent implements Component<ChunkStore> {
    public static final BuilderCodec<ResonantDisrupterComponent> CODEC;
    private static ComponentType<ChunkStore, ResonantDisrupterComponent> type;

    public static ComponentType<ChunkStore, ResonantDisrupterComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, ResonantDisrupterComponent> newType) {
        if (type != null) {
            throw new IllegalStateException("ResonantDisrupterComponent ComponentType already set");
        }
        type = newType;
    }

    public int essencePerOperation = 100;
    public int ticksPerOperation = 30;
    public float damagePerOperation = 0.2f;
    public int damageRange = 3;
    public int remainingTicksUntilOperation = 0;
    private int currentBlockIndex = 0;

    public ResonantDisrupterComponent() {
    }

    public ResonantDisrupterComponent(int essencePerOperation, int ticksPerOperation, float damagePerOperation, int damageRange, int remainingTicksUntilOperation, int currentBlockIndex) {
        this.essencePerOperation = essencePerOperation;
        this.ticksPerOperation = ticksPerOperation;
        this.damagePerOperation = damagePerOperation;
        this.damageRange = damageRange;
        this.remainingTicksUntilOperation = remainingTicksUntilOperation;
        this.currentBlockIndex = currentBlockIndex;
    }

    public int getCurrentBlockIndex() {
        return currentBlockIndex;
    }
    public void setCurrentBlockIndex(int currentBlockIndex) {
        this.currentBlockIndex = currentBlockIndex;
    }
    public void advanceCurrentBlockIndex() {
        final int fullRange = (damageRange * 2) + 1;
        currentBlockIndex = (currentBlockIndex + 1) % (fullRange * fullRange);

        // Don't damage the block itself
        final boolean isBlockIndex = (currentBlockIndex % fullRange) == damageRange
                && (currentBlockIndex / fullRange) == damageRange;
        if (isBlockIndex) {
            currentBlockIndex = (currentBlockIndex + 1) % (fullRange * fullRange);
        }
    }

    @Override
    public Component<ChunkStore> clone() {
        return new ResonantDisrupterComponent(
                essencePerOperation,
                ticksPerOperation,
                damagePerOperation,
                damageRange,
                remainingTicksUntilOperation,
                currentBlockIndex);
    }

    static {
        CODEC = BuilderCodec.builder(ResonantDisrupterComponent.class, ResonantDisrupterComponent::new)
                .append(new KeyedCodec<>("EssencePerOperation", BuilderCodec.INTEGER), (c, v) -> c.essencePerOperation = v, c -> c.essencePerOperation)
                .addValidator(Validators.greaterThan(0))
                .documentation("Essence cost per damage operation").add()
                .append(new KeyedCodec<>("TicksPerOperation", BuilderCodec.INTEGER), (c, v) -> c.ticksPerOperation = v, c -> c.ticksPerOperation)
                .addValidator(Validators.greaterThan(0))
                .documentation("How many ticks per damage operation").add()
                .append(new KeyedCodec<>("DamagePerOperation", BuilderCodec.FLOAT), (c, v) -> c.damagePerOperation = v, c -> c.damagePerOperation)
                .addValidator(Validators.range(0.0f, 1.0f))
                .documentation("Damage percentage per operation [0..1]").add()
                .append(new KeyedCodec<>("DamageRange", BuilderCodec.INTEGER), (c, v) -> c.damageRange = v, c -> c.damageRange)
                .addValidator(Validators.greaterThan(0))
                .documentation("Range in blocks that will be damaged").add()
                .append(new KeyedCodec<>("RemainingTicksUntilOperation", BuilderCodec.INTEGER), (c, v) -> c.remainingTicksUntilOperation = v, c -> c.remainingTicksUntilOperation)
                .addValidator(Validators.greaterThanOrEqual(0))
                .documentation("Ticks until next damage operation").add()
                .append(new KeyedCodec<>("CurrentBlockIndex", BuilderCodec.INTEGER), (c, v) -> c.currentBlockIndex = v, c -> c.currentBlockIndex)
                .addValidator(Validators.greaterThanOrEqual(0))
                .documentation("Current index of the block being damaged").add()
                .build();
    }
}
