package com.tschm.resonance.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.essence.generators.CarbonAttunementStoneComponent;
import com.tschm.resonance.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EssenceGeneratorSystems {
    public abstract static class GeneratorTicks extends EntityTickingSystem<ChunkStore> {

        @Override
        public void tick(float v, int idx, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            // Transfer from the generator's local storage to bound receivers
            // is handled by EssenceStorageSystems.EssenceStorageTransferSystem.
        }

        protected long supplyEssenceToStorage(World world, ArchetypeChunk<ChunkStore> archetypeChunk, int idx, EssenceGeneratorComponent compGenerator, long amount) {
            EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            if (compStorage == null)
                return 0L;

            return compStorage.addEssence(amount, false);
        }

        protected void updateGeneratorBlockState(boolean isActive, World world, Vector3i pos, EssenceGeneratorComponent compGenerator) {
            compGenerator.active = isActive;
            String errorString = BlockHelper.activateBlockState(isActive ? "On" : "Off", world, pos);
            if (errorString != null) {
                DebugHelper.Print("Error while updating generator block state: " + errorString);
            }
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() { return EssenceStorageComponent.getComponentType(); }
    }
}
