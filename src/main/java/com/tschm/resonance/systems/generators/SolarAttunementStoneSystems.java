package com.tschm.resonance.systems.generators;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.generators.SolarAttunementStoneComponent;
import com.tschm.resonance.systems.EssenceGeneratorSystems;
import com.tschm.resonance.util.SystemsHelper;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;

public class SolarAttunementStoneSystems {
    public static class GeneratorTicks extends EssenceGeneratorSystems.GeneratorTicks {

        @Override
        public void tick(
                float dt,
                int idx,
                @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
                @Nonnull Store<ChunkStore> store,
                @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            super.tick(dt, idx, archetypeChunk, store, commandBuffer);

            SolarAttunementStoneComponent compGenerator = archetypeChunk.getComponent(idx, SolarAttunementStoneComponent.getComponentType());
            assert compGenerator != null;

            Vector3i blockPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            World world = commandBuffer.getExternalData().getWorld();
            Store<EntityStore> entityStore = world.getEntityStore().getStore();
            WorldTimeResource worldTimeResource = entityStore.getResource(WorldTimeResource.getResourceType());

            final boolean isActive = worldTimeResource.getSunlightFactor() > 0.01f;
            super.updateGeneratorBlockState(isActive, world, blockPos, compGenerator);

            // When not active reset timings and return
            if (!isActive){
                compGenerator.remainingTicksUntilGeneration = 0;
                return;
            }

            // Check if we reached another cycle
            if (--compGenerator.remainingTicksUntilGeneration > 0) {
                return;
            }
            compGenerator.remainingTicksUntilGeneration = compGenerator.ticksPerCycle;

            long productionPerTick = compGenerator.productionPerCycle;
            super.supplyEssenceToStorage(world, archetypeChunk, idx, compGenerator, productionPerTick);
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(super.getQuery(),  SolarAttunementStoneComponent.getComponentType());
        }
    }
}
