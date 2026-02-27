package com.tschm.resonance.components.essence;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.util.ComponentHelper;

import javax.annotation.Nullable;

public abstract class EssenceGeneratorComponent implements Component<ChunkStore> {
    public static final BuilderCodec<EssenceGeneratorComponent> CODEC;

    @Nullable
    public Vector3i boundStoragePos = null;

    public EssenceGeneratorComponent() {
    }

    public EssenceGeneratorComponent(@Nullable Vector3i boundStoragePos) {
        this.boundStoragePos = boundStoragePos;
    }

    public EssenceStorageComponent findTargetStorage(World world, ArchetypeChunk<ChunkStore> archetypeChunk, int idx){
        EssenceStorageComponent compStorage = null;
        if (boundStoragePos != null)
            compStorage = ComponentHelper.findComponentAt(world, boundStoragePos, EssenceStorageComponent.getComponentType());
        else
            compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());

        return compStorage;
    }

    @Nullable
    public Component<ChunkStore> clone() {
        return cloneImpl();
    }
    protected abstract EssenceGeneratorComponent cloneImpl();

    static {
        CODEC = BuilderCodec.abstractBuilder(EssenceGeneratorComponent.class, null)
                .append(new KeyedCodec<>("BoundStoragePos", Vector3i.CODEC), (c, v) -> c.boundStoragePos = v, c -> c.boundStoragePos).add()
                .build();
    }
}
