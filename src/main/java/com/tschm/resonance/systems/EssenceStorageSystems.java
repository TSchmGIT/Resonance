package com.tschm.resonance.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.AbstractEssenceStorage;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.essence.EssenceStorageVisualizerComponent;
import com.tschm.resonance.util.*;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class EssenceStorageSystems {

    /** Pushes RE from every EssenceStorage with canSend=true to its boundReceiverList each tick. */
    public static class EssenceStorageTransferSystem extends EntityTickingSystem<ChunkStore> {

        @Override
        public void tick(float v, int idx, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            if (compStorage == null || !compStorage.canSend || compStorage.isEmpty())
                return;

            World world = commandBuffer.getExternalData().getWorld();
            for (Vector3i boundPos : compStorage.getBoundReceiverList()) {
                EssenceStorageComponent compReceiver = ComponentHelper.findComponentAt(world, boundPos, EssenceStorageComponent.getComponentType());
                if (compReceiver == null)
                    continue;

                AbstractEssenceStorage.transferEssence(compStorage, compReceiver, compStorage.getMaxExtract(), false);
            }
        }

        @Override
        public Query<ChunkStore> getQuery() {
            return EssenceStorageComponent.getComponentType();
        }
    }

    /** Updates the fill-level block state of storage blocks that have a visualizer component. */
    public static class EssenceStorageVisualizerSystem extends EntityTickingSystem<ChunkStore> {

        private static final int LEVEL_COUNT = 4;

        @Override
        public void tick(float v, int idx, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            EssenceStorageVisualizerComponent compVisualizer = archetypeChunk.getComponent(idx, EssenceStorageVisualizerComponent.getComponentType());
            EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            final Vector3i targetBlock = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            if (compStorage == null || compVisualizer == null || targetBlock == null) {
                DebugHelper.Print("compStorage or compVisualizer or targetPos null");
                return;
            }

            int newLevel = (int) Math.floor(compStorage.getFillRatio() * LEVEL_COUNT);
            if (compVisualizer.getCurrentLevel() == newLevel)
                return;

            compVisualizer.setCurrentLevel(newLevel);

            World world = commandBuffer.getExternalData().getWorld();
            world.execute(() -> {
                String newState = "Lvl" + newLevel;
                BlockHelper.activateBlockState(newState, world, targetBlock);
            });
        }

        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(EssenceStorageComponent.getComponentType(), EssenceStorageVisualizerComponent.getComponentType());
        }
    }
}
