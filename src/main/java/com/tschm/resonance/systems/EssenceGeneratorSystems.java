package com.tschm.resonance.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.AbstractEssenceStorage;
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.SystemsHelper;

import javax.annotation.Nonnull;

public class EssenceGeneratorSystems {
    public abstract static class GeneratorTicks extends EntityTickingSystem<ChunkStore> {

        @Override
        public void tick(float v, int idx, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            Vector3i blockPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            if (blockPos == null)
                return;

            final World world = commandBuffer.getExternalData().getWorld();
            EssenceGeneratorComponent compGenerator = ComponentHelper.findGeneratorComponentAt(world, blockPos);
            EssenceStorageComponent compGeneratorStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            if (compGenerator == null || compGeneratorStorage == null || !compGeneratorStorage.canSend || compGeneratorStorage.isEmpty())
                return;

            final Vector3i boundPos = compGenerator.boundStoragePos;
            if (boundPos == null)
                return;

            EssenceStorageComponent compBoundStorage = ComponentHelper.findComponentAt(world, boundPos, EssenceStorageComponent.getComponentType());
            if (compBoundStorage == null)
                return;

            AbstractEssenceStorage.transferEssence(compGeneratorStorage, compBoundStorage, compGeneratorStorage.getMaxExtract(), false);
        }

        protected long supplyEssenceToBoundStorage(World world, ArchetypeChunk<ChunkStore> archetypeChunk, int idx, EssenceGeneratorComponent compGenerator, long amount) {
            EssenceStorageComponent compStorage = compGenerator.findTargetStorage(world, archetypeChunk, idx);
            if (compStorage == null)
                return 0L;

            return compStorage.receiveEssence(amount, false);
        }

    }
}
