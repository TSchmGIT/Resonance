package com.tschm.resonance.components.essence.generators;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;

public class VerdantAttunementStoneComponent extends EssenceGeneratorComponent {
    public static final BuilderCodec<VerdantAttunementStoneComponent> CODEC;
    private static ComponentType<ChunkStore, VerdantAttunementStoneComponent> type;

    public static ComponentType<ChunkStore, VerdantAttunementStoneComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, VerdantAttunementStoneComponent> type) {
        VerdantAttunementStoneComponent.type = type;
    }

    @Override
    protected EssenceGeneratorComponent cloneImpl() {
        return new VerdantAttunementStoneComponent();
    }

    static{
        CODEC = BuilderCodec.builder(VerdantAttunementStoneComponent.class, VerdantAttunementStoneComponent::new, EssenceGeneratorComponent.CODEC).
                build();
    }
}
