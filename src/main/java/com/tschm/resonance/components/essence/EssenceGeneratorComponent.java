package com.tschm.resonance.components.essence;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public abstract class EssenceGeneratorComponent implements Component<ChunkStore> {
    public static final BuilderCodec<EssenceGeneratorComponent> CODEC;

    public boolean active = false;

    public EssenceGeneratorComponent() {
    }

    @Override
    public Component<ChunkStore> clone() {
        return cloneImpl();
    }
    protected abstract EssenceGeneratorComponent cloneImpl();

    static {
        CODEC = BuilderCodec.abstractBuilder(EssenceGeneratorComponent.class, null)
                .append(new KeyedCodec<>("Active", BuilderCodec.BOOLEAN), (c, v) -> c.active = v, c -> c.active).add()
                .build();
    }
}
