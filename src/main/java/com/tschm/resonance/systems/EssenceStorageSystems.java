package com.tschm.resonance.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.essence.EssenceStorageVisualizerComponent;
import com.tschm.resonance.util.*;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EssenceStorageSystems {
    // Updates the block state of the visualizer
    public static class EssenceStorageVisualizerSystem extends EntityTickingSystem<ChunkStore> {

        // The number of levels possible in the visual representation
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
            // Skip if no change is required
            if (compVisualizer.getCurrentLevel() == newLevel)
                return;

            compVisualizer.setCurrentLevel(newLevel);

            World world = commandBuffer.getExternalData().getWorld();
            world.execute(() -> {
                String newState = "Lvl" + newLevel;
                BlockHelper.activateBlockState(newState, world, targetBlock);
            });
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(EssenceStorageComponent.getComponentType(), EssenceStorageVisualizerComponent.getComponentType());
        }
    }

}
