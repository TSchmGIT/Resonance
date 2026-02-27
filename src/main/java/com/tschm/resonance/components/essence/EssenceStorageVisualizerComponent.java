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
    private int currentLevel = -1;

    public EssenceStorageVisualizerComponent() {
    }

    public EssenceStorageVisualizerComponent(int currentLevel) {
        this.currentLevel = currentLevel;
    }
    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    @Override
    public Component<ChunkStore> clone() {
        return new EssenceStorageVisualizerComponent(this.currentLevel);
    }

    static {
        CODEC = BuilderCodec.builder(EssenceStorageVisualizerComponent.class, EssenceStorageVisualizerComponent::new)
                .append(new KeyedCodec<>("CurrentLevel", BuilderCodec.INTEGER), (c, v) -> c.currentLevel = v, c -> c.currentLevel).add()
                .build();
    }
}
