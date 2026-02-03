package com.tschm.resonance.util;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nullable;

public class ComponentHelper {
    @Nullable
    public static <T extends Component<ChunkStore>> T findChunkComponentAt(World world, Vector3i pos, ComponentType<ChunkStore, T> componentType) {
        int x = pos.x;
        int y = pos.y;
        int z = pos.z;

        // Retrieve world chunk
        long indexChunk = ChunkUtil.indexChunkFromBlock(x, z);
        WorldChunk worldChunk = world.getChunk(indexChunk);
        if (worldChunk == null || worldChunk.getBlockType(pos) == null) {
            return null;
        }

        // Retrieve block component entity at given bock position
        Ref<ChunkStore> chunkRef = worldChunk.getBlockComponentEntity(x, y, z);
        if (chunkRef == null) {
            chunkRef = BlockModule.ensureBlockEntity(worldChunk, x, y, z);
        }

        // Retrieve component from chunk store
        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        return chunkStore.getComponent(chunkRef, componentType);
    }
}
