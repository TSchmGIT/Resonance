package com.tschm.resonance.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.EssenceGeneratorComponent;
import com.tschm.resonance.components.essence.EssenceStorageComponent;

public class EssenceGeneratorSystems {
    public abstract static class GeneratorTicks extends EntityTickingSystem<ChunkStore> {
        protected long supplyEssenceToBoundStorage(World world, ArchetypeChunk<ChunkStore> archetypeChunk, int idx, EssenceGeneratorComponent compGenerator, long amount){
            EssenceStorageComponent compStorage = compGenerator.findTargetStorage(world, archetypeChunk, idx);
            if (compStorage == null)
                return 0L;

            return compStorage.receiveEssence(amount, false);
        }

    }
}
