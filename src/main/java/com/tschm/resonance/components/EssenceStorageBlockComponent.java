package com.tschm.resonance.components;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.essence.AbstractEssenceStorage;
import com.tschm.resonance.util.DebugHelper;

public final class EssenceStorageBlockComponent extends AbstractEssenceStorage implements Component<ChunkStore> {

    public static final BuilderCodec CODEC;
    private static ComponentType<ChunkStore, EssenceStorageBlockComponent> type;
    public static ComponentType<ChunkStore, EssenceStorageBlockComponent> getComponentType() {
        return type;
    }
    public static void setComponentType(ComponentType<ChunkStore, EssenceStorageBlockComponent> type) {
        EssenceStorageBlockComponent.type = type;
    }

    public EssenceStorageBlockComponent() {
        DebugHelper.Print("EssenceStorageBlockComponent created");
    }

    public EssenceStorageBlockComponent(long essenceStored, long maxEssence, long maxReceive, long maxExtract) {
        super(essenceStored, maxEssence, maxReceive, maxExtract);
    }

    @Override
    public Component<ChunkStore> clone() {
        EssenceStorageBlockComponent copy = new EssenceStorageBlockComponent();
        copy.copyFrom(this);
        return copy;
    }

    static {
        CODEC = BuilderCodec.builder(EssenceStorageBlockComponent.class, EssenceStorageBlockComponent::new)
                .append(new KeyedCodec<>("EssenceStored", BuilderCodec.LONG), (c, v) -> c.essenceStored = v, c -> c.essenceStored)
                .addValidator(Validators.greaterThanOrEqual(0L))
                .documentation("Current Stored RE").add()
                .append(new KeyedCodec<>("MaxEssence", BuilderCodec.LONG), (c, v) -> c.maxEssence = v, c -> c.maxEssence)
                .addValidator(Validators.greaterThanOrEqual(0L))
                .documentation("Maximum RE capacity").add()
                .append(new KeyedCodec<>("MaxReceive", BuilderCodec.LONG), (c, v) -> c.maxReceive = v, c -> c.maxReceive)
                .addValidator(Validators.greaterThanOrEqual(0L))
                .documentation("Maximum RE accepted per receive call").add()
                .append(new KeyedCodec<>("MaxExtract", BuilderCodec.LONG), (c, v) -> c.maxExtract = v, c -> c.maxExtract)
                .addValidator(Validators.greaterThanOrEqual(0L))
                .documentation("Maximum RE extracted per extract call").add()
                .build();
    }
}
