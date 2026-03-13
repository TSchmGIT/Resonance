package com.tschm.resonance.components.essence.generators;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;

import javax.annotation.Nullable;

public class VerdantAttunementStoneComponent extends EssenceGeneratorComponent {
    public static final BuilderCodec<VerdantAttunementStoneComponent> CODEC;
    private static ComponentType<ChunkStore, VerdantAttunementStoneComponent> type;

    public static ComponentType<ChunkStore, VerdantAttunementStoneComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, VerdantAttunementStoneComponent> type) {
        VerdantAttunementStoneComponent.type = type;
    }

    public int ticksPerCycle = 1;
    public long productionPerCycle = 10L;
    public int remainingTicksUntilGeneration = ticksPerCycle;

    public double maxMultiplier = 20.0;
    public int maxVariety = 13;
    public int scanRadius = 5;
    public int scanInterval = 30;
    public int remainingTicksUntilScan = 0;

    // Runtime state (not serialized)
    public int currentVarietyCount = 0;
    public double currentMultiplier = 0.0;

    public VerdantAttunementStoneComponent() {
    }

    public VerdantAttunementStoneComponent(int ticksPerCycle, long productionPerCycle, int remainingTicksUntilGeneration,
                                           double maxMultiplier, int maxVariety, int scanRadius,
                                           int scanInterval, int remainingTicksUntilScan) {
        this.ticksPerCycle = ticksPerCycle;
        this.productionPerCycle = productionPerCycle;
        this.remainingTicksUntilGeneration = remainingTicksUntilGeneration;
        this.maxMultiplier = maxMultiplier;
        this.maxVariety = maxVariety;
        this.scanRadius = scanRadius;
        this.scanInterval = scanInterval;
        this.remainingTicksUntilScan = remainingTicksUntilScan;
    }

    /**
     * Calculates the production multiplier based on the number of unique plant types nearby.
     * Formula: f(i) = 1 + (α-1) * ((i-1)/(n-1))^3
     *
     * @param varietyCount number of unique plant types found
     * @return the multiplier (0.0 if no plants, 1.0 to maxMultiplier otherwise)
     */
    public double calculateMultiplier(int varietyCount) {
        if (varietyCount <= 0) return 0.0;
        if (maxVariety <= 1) return 1.0;
        int i = Math.min(varietyCount, maxVariety);
        double ratio = (double) (i - 1) / (maxVariety - 1);
        return 1.0 + (maxMultiplier - 1.0) * ratio * ratio * ratio;
    }

    @Nullable
    @Override
    protected EssenceGeneratorComponent cloneImpl() {
        return new VerdantAttunementStoneComponent(
                this.ticksPerCycle, this.productionPerCycle, this.remainingTicksUntilGeneration,
                this.maxMultiplier, this.maxVariety, this.scanRadius,
                this.scanInterval, this.remainingTicksUntilScan);
    }

    static {
        CODEC = BuilderCodec.builder(VerdantAttunementStoneComponent.class, () -> new VerdantAttunementStoneComponent(), EssenceGeneratorComponent.CODEC)
                .append(new KeyedCodec<>("TicksPerCycle", BuilderCodec.INTEGER), (c, v) -> c.ticksPerCycle = v, c -> c.ticksPerCycle)
                .addValidator(Validators.greaterThanOrEqual(0)).add()
                .append(new KeyedCodec<>("ProductionPerCycle", BuilderCodec.LONG), (c, v) -> c.productionPerCycle = v, c -> c.productionPerCycle)
                .addValidator(Validators.greaterThanOrEqual(0L)).add()
                .append(new KeyedCodec<>("RemainingTicksUntilGeneration", Codec.INTEGER), (c, v) -> c.remainingTicksUntilGeneration = v, c -> c.remainingTicksUntilGeneration).add()
                .append(new KeyedCodec<>("MaxMultiplier", BuilderCodec.DOUBLE), (c, v) -> c.maxMultiplier = v, c -> c.maxMultiplier)
                .addValidator(Validators.greaterThanOrEqual(1.0)).add()
                .append(new KeyedCodec<>("MaxVariety", BuilderCodec.INTEGER), (c, v) -> c.maxVariety = v, c -> c.maxVariety)
                .addValidator(Validators.greaterThan(1)).add()
                .append(new KeyedCodec<>("ScanRadius", BuilderCodec.INTEGER), (c, v) -> c.scanRadius = v, c -> c.scanRadius)
                .addValidator(Validators.greaterThan(0)).add()
                .append(new KeyedCodec<>("ScanInterval", BuilderCodec.INTEGER), (c, v) -> c.scanInterval = v, c -> c.scanInterval)
                .addValidator(Validators.greaterThan(0)).add()
                .append(new KeyedCodec<>("RemainingTicksUntilScan", Codec.INTEGER), (c, v) -> c.remainingTicksUntilScan = v, c -> c.remainingTicksUntilScan).add()
                .build();
    }
}
