package com.tschm.resonance.components;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EssenceGeneratorComponent implements Component<ChunkStore> {
    public static final BuilderCodec CODEC;
    private static ComponentType<ChunkStore, EssenceGeneratorComponent> type;
    public static ComponentType<ChunkStore, EssenceGeneratorComponent> getComponentType() {
        return type;
    }
    public static void setComponentType(ComponentType<ChunkStore, EssenceGeneratorComponent> type) {
        EssenceGeneratorComponent.type = type;
    }

    private long productionPerTick;

    public EssenceGeneratorComponent(){
    }

    public EssenceGeneratorComponent(long productionPerTick){
        this.productionPerTick = productionPerTick;
    }

    @NullableDecl
    @Override
    public Component<ChunkStore> clone() {
        return new EssenceGeneratorComponent(this.productionPerTick);
    }

    public long getProductionPerTick() {
        return productionPerTick;
    }
    public void setProductionPerTick(long value){
        productionPerTick = value;
    }

    static  {
        CODEC = BuilderCodec.builder(EssenceGeneratorComponent.class, EssenceGeneratorComponent::new)
                .append(new KeyedCodec<>("ProductionPerTick", BuilderCodec.LONG), (c, v) -> c.productionPerTick = v, c -> c.productionPerTick)
                .addValidator(Validators.greaterThanOrEqual(0L)).add()
                .build();
    }
}
