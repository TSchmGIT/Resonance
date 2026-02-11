package com.tschm.resonance.components.essence.generators;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;

import javax.annotation.Nullable;

public class SolarAttunementStoneComponent extends EssenceGeneratorComponent {
    public static final BuilderCodec<SolarAttunementStoneComponent> CODEC;
    private static ComponentType<ChunkStore, SolarAttunementStoneComponent> type;

    public static ComponentType<ChunkStore, SolarAttunementStoneComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, SolarAttunementStoneComponent> type) {
        SolarAttunementStoneComponent.type = type;
    }

    public int ticksPerCycle = 1;
    public long productionPerCycle = 0L;

    public int remainingTicksUntilGeneration = ticksPerCycle;

    public SolarAttunementStoneComponent() {
    }

    public SolarAttunementStoneComponent(Vector3i boundStoragePos, int ticksPerCycle, long productionPerCycle, int lastProductionTimestamp) {
        super(boundStoragePos);
        this.ticksPerCycle = ticksPerCycle;
        this.productionPerCycle = productionPerCycle;
        this.remainingTicksUntilGeneration = lastProductionTimestamp;
    }

    @Nullable
    @Override
    protected EssenceGeneratorComponent cloneImpl() {
        return new SolarAttunementStoneComponent(this.boundStoragePos, this.ticksPerCycle, this.productionPerCycle, this.remainingTicksUntilGeneration);
    }

    static {
        CODEC = BuilderCodec.builder(SolarAttunementStoneComponent.class, () -> new SolarAttunementStoneComponent(), EssenceGeneratorComponent.CODEC)
                .append(new KeyedCodec<>("TicksPerCycle", BuilderCodec.INTEGER), (c, v) -> c.ticksPerCycle = v, c -> c.ticksPerCycle)
                .addValidator(Validators.greaterThanOrEqual(0)).add()
                .append(new KeyedCodec<>("ProductionPerCycle", BuilderCodec.LONG), (c, v) -> c.productionPerCycle = v, c -> c.productionPerCycle)
                .addValidator(Validators.greaterThanOrEqual(0L)).add()
                .append(new KeyedCodec<>("RemainingTicksUntilGeneration", Codec.INTEGER), (c, v) -> c.remainingTicksUntilGeneration = v, c -> c.remainingTicksUntilGeneration).add()
                .build();
    }
}
