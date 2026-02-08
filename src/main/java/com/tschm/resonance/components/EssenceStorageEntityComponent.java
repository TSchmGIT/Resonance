package com.tschm.resonance.components;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.essence.AbstractEssenceStorage;

public final class EssenceStorageEntityComponent extends AbstractEssenceStorage implements Component<EntityStore> {

    public static final BuilderCodec CODEC;
    private static ComponentType<EntityStore, EssenceStorageEntityComponent> type;
    public static ComponentType<EntityStore, EssenceStorageEntityComponent> getComponentType() {
        return type;
    }
    public static void setComponentType(ComponentType<EntityStore, EssenceStorageEntityComponent> type) {
        EssenceStorageEntityComponent.type = type;
    }

    public EssenceStorageEntityComponent() {
    }
    public EssenceStorageEntityComponent(long essenceStored, long maxEssence, long maxReceive, long maxExtract) {
        super(essenceStored, maxEssence, maxReceive, maxExtract);
    }

    @Override
    public Component<EntityStore> clone() {
        EssenceStorageEntityComponent copy = new EssenceStorageEntityComponent();
        copy.copyFrom(this);
        return copy;
    }
    
    static{
        CODEC = BuilderCodec.builder(EssenceStorageEntityComponent.class, EssenceStorageEntityComponent::new)
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
