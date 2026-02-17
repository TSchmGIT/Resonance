package com.tschm.resonance.components.essence.generators;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;

public class CarbonAttunementStoneComponent extends EssenceGeneratorComponent {
    public static final BuilderCodec<CarbonAttunementStoneComponent> CODEC;
    private static ComponentType<ChunkStore, CarbonAttunementStoneComponent> type;

    public static ComponentType<ChunkStore, CarbonAttunementStoneComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, CarbonAttunementStoneComponent> type) {
        CarbonAttunementStoneComponent.type = type;
    }

    public int burnTicksPerFuelQuality = 5 * 30;
    public long essencePerFuelQuality = 10L;

    public long remainingBurnEssence = 0;
    public long currentEssencePerTick = 0;

    public CarbonAttunementStoneComponent() {
    }

    public CarbonAttunementStoneComponent(Vector3i boundStoragePos, long essencePerFuelQuality, int burnTicksPerFuelQuality, long remainingBurnTicks, long currentEssencePerTick) {
        super(boundStoragePos);
        this.essencePerFuelQuality = essencePerFuelQuality;
        this.burnTicksPerFuelQuality = burnTicksPerFuelQuality;
        this.remainingBurnEssence = remainingBurnTicks;
        this.currentEssencePerTick = currentEssencePerTick;
    }

    @Override
    protected EssenceGeneratorComponent cloneImpl() {
        return new CarbonAttunementStoneComponent(this.boundStoragePos, this.essencePerFuelQuality, this.burnTicksPerFuelQuality, this.remainingBurnEssence, this.currentEssencePerTick);
    }

    static {
        CODEC = BuilderCodec.builder(CarbonAttunementStoneComponent.class, CarbonAttunementStoneComponent::new, EssenceGeneratorComponent.CODEC)
                .append(new KeyedCodec<>("EssencePerFuelQuality", BuilderCodec.LONG), (c, v) -> c.essencePerFuelQuality = v, c -> c.essencePerFuelQuality)
                .addValidator(Validators.greaterThanOrEqual(0L))
                .documentation("Essence per fuel quality of the consumed item").add()
                .append(new KeyedCodec<>("BurnTicksPerFuelQuality", BuilderCodec.INTEGER), (c, v) -> c.burnTicksPerFuelQuality = v, c -> c.burnTicksPerFuelQuality)
                .addValidator(Validators.greaterThan(0))
                .documentation("How long does one unit of fuel quality take to burn up").add()
                .build();
    }
}
