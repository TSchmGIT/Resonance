package com.tschm.resonance.components.essence;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nonnull;

public class EssenceStorageVisualizerComponent implements Component<ChunkStore> {
    public static final BuilderCodec CODEC;
    private static ComponentType<ChunkStore, EssenceStorageVisualizerComponent> type;

    public static ComponentType<ChunkStore, EssenceStorageVisualizerComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, EssenceStorageVisualizerComponent> type) {
        EssenceStorageVisualizerComponent.type = type;
    }

    private Vector3i position;
    private int currentLevel = -1;

    public EssenceStorageVisualizerComponent() {
    }

    public EssenceStorageVisualizerComponent(Vector3i position, int currentLevel) {
        this.position = position;
        this.currentLevel = currentLevel;
    }

    public Vector3i getPosition() {
        return position;
    }

    public void setPosition(@Nonnull Vector3i position) {
        this.position = position;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    @Override
    public Component<ChunkStore> clone() {
        return new EssenceStorageVisualizerComponent(this.position, this.currentLevel);
    }

    static {
        CODEC = BuilderCodec.builder(EssenceStorageVisualizerComponent.class, EssenceStorageVisualizerComponent::new)
                .append(new KeyedCodec<>("Position", Vector3i.CODEC), (c, v) -> c.position = v, c -> c.position).add()
                .build();
    }
}
