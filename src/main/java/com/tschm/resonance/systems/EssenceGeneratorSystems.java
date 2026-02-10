package com.tschm.resonance.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.EssenceGeneratorComponent;
import com.tschm.resonance.components.EssenceStorageComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EssenceGeneratorSystems {
    public static class GeneratorTicks extends EntityTickingSystem<ChunkStore>{
        @Override
        public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            EssenceGeneratorComponent compGenerator = archetypeChunk.getComponent(index, EssenceGeneratorComponent.getComponentType());
            EssenceStorageComponent compStorage = archetypeChunk.getComponent(index, EssenceStorageComponent.getComponentType());
            assert compGenerator != null && compStorage != null;

            long productionPerTick = compGenerator.getProductionPerTick();
            compStorage.receiveEssence(productionPerTick, false);
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(EssenceGeneratorComponent.getComponentType(), EssenceStorageComponent.getComponentType());
        }
    }
}
