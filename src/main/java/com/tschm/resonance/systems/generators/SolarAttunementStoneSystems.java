package com.tschm.resonance.systems.generators;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.essence.generators.SolarAttunementStoneComponent;
import com.tschm.resonance.systems.EssenceGeneratorSystems;
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
            SolarAttunementStoneComponent compGenerator = archetypeChunk.getComponent(idx, SolarAttunementStoneComponent.getComponentType());
            assert compGenerator != null;

            // Check if we reached another cycle
            if (--compGenerator.remainingTicksUntilGeneration > 0)
                return;
            compGenerator.remainingTicksUntilGeneration = compGenerator.ticksPerCycle;

            World world = commandBuffer.getExternalData().getWorld();
            long productionPerTick = compGenerator.productionPerCycle;
            supplyEssenceToBoundStorage(world, archetypeChunk, idx, compGenerator, productionPerTick);
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return SolarAttunementStoneComponent.getComponentType();
        }
    }
}
