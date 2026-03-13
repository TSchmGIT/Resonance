package com.tschm.resonance.systems.generators;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.generators.VerdantAttunementStoneComponent;
import com.tschm.resonance.systems.EssenceGeneratorSystems;
import com.tschm.resonance.util.SystemsHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class VerdantAttunementStoneSystems {

    public static class GeneratorTicks extends EssenceGeneratorSystems.GeneratorTicks {

        @Override
        public void tick(
                float dt,
                int idx,
                @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
                @Nonnull Store<ChunkStore> store,
                @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            super.tick(dt, idx, archetypeChunk, store, commandBuffer);

            VerdantAttunementStoneComponent comp = archetypeChunk.getComponent(idx, VerdantAttunementStoneComponent.getComponentType());
            assert comp != null;

            Vector3i blockPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            World world = commandBuffer.getExternalData().getWorld();

            // Periodic crop variety scan
            if (--comp.remainingTicksUntilScan <= 0) {
                comp.remainingTicksUntilScan = comp.scanInterval;
                world.execute(() -> {
                    int varietyCount = scanForPlantVariety(world, blockPos, comp.scanRadius);
                    comp.currentVarietyCount = varietyCount;
                    comp.currentMultiplier = comp.calculateMultiplier(varietyCount);
                });
            }

            // Update active state
            final boolean isActive = comp.currentVarietyCount > 0;
            world.execute(() -> {
                super.updateGeneratorBlockState(isActive, world, blockPos, comp);
            });

            // When not active, reset cycle and return
            if (!isActive) {
                comp.remainingTicksUntilGeneration = 0;
                return;
            }

            // Check if we reached another cycle
            if (--comp.remainingTicksUntilGeneration > 0) {
                return;
            }
            comp.remainingTicksUntilGeneration = comp.ticksPerCycle;

            long production = (long) (comp.productionPerCycle * comp.currentMultiplier);
            super.supplyEssenceToStorage(world, archetypeChunk, idx, comp, production);
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(super.getQuery(), VerdantAttunementStoneComponent.getComponentType());
        }

        private static int scanForPlantVariety(World world, Vector3i center, int radius) {
            Set<String> uniquePlantTypes = new HashSet<>();

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        Vector3i pos = new Vector3i(center.x + dx, center.y + dy, center.z + dz);
                        BlockType blockType = world.getBlockType(pos);
                        if (blockType == null) continue;

                        if (isPlantBlock(blockType)) {
                            uniquePlantTypes.add(blockType.getId());
                        }
                    }
                }
            }

            return uniquePlantTypes.size();
        }
    }

    public static class PlaceBlockHandler extends EntityEventSystem<ChunkStore, PlaceBlockEvent> {

        public PlaceBlockHandler() {
            super(PlaceBlockEvent.class);
        }

        @Override
        public void handle(int idx, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer, @Nonnull PlaceBlockEvent placeBlockEvent) {
            Vector3i placedBlockPos = placeBlockEvent.getTargetBlock();
            World world = commandBuffer.getExternalData().getWorld();
            BlockType blockType = world.getBlockType(placedBlockPos);
            assert blockType != null;
            if (!isPlantBlock(blockType))
                return;

            VerdantAttunementStoneComponent comp = archetypeChunk.getComponent(idx, VerdantAttunementStoneComponent.getComponentType());
            Vector3i generatorPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            assert comp != null && generatorPos != null;

            if (Math.abs(placedBlockPos.x - generatorPos.x) > comp.scanRadius
                    || Math.abs(placedBlockPos.y - generatorPos.y) > comp.scanRadius
                    || Math.abs(placedBlockPos.z - generatorPos.z) > comp.scanRadius)
                return;

            // Force immediate scan on next tick
            comp.remainingTicksUntilScan = 0;
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return VerdantAttunementStoneComponent.getComponentType();
        }
    }

    private static boolean isPlantBlock(BlockType blockType) {
        var farming = blockType.getFarming();
        if (farming == null) return false;
        return farming.getStages() != null;
    }
}
