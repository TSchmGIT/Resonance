package com.tschm.resonance.util;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SystemsHelper {
    /**
     * Retrieves the world position of a block based on its index within a given chunk and its associated components.
     *
     * @param archetypeChunk the chunk containing the block to retrieve the position for
     * @param idx the index of the block within the chunk
     * @param componentAccessor the accessor used to retrieve additional chunk-related components
     * @return the world position of the block as a {@code Vector3i}, or {@code null} if the block information is unavailable
     */
    @Nullable
    public static Vector3i getPosForBlock(ArchetypeChunk<ChunkStore> archetypeChunk, int idx, ComponentAccessor<ChunkStore> componentAccessor) {
        BlockModule.BlockStateInfo blockInfo = archetypeChunk.getComponent(idx, BlockModule.BlockStateInfo.getComponentType());
        if (blockInfo == null)
            return null;

        Ref<ChunkStore> chunkRef = blockInfo.getChunkRef();
        if (!chunkRef.isValid())
            return null;

        BlockChunk blockChunk = chunkRef.getStore().getComponent(chunkRef, BlockChunk.getComponentType());
        if (blockChunk == null)
            return null;

        int worldX = blockChunk.getX() << 5 | ChunkUtil.xFromBlockInColumn(blockInfo.getIndex());
        int worldY = ChunkUtil.yFromBlockInColumn(blockInfo.getIndex());
        int worldZ = blockChunk.getZ() << 5 | ChunkUtil.zFromBlockInColumn(blockInfo.getIndex());
        return new Vector3i(worldX, worldY, worldZ);
    }

    /**
     * Retrieves a list of items located within a specified range of a block position.
     *
     * @param entityStore the store containing the entity data
     * @param blockPos the position of the block around which the search is performed
     * @param range the search radius around the specified block position
     * @return a list of references to entity stores representing the items found within the range
     */
    @Nonnull
    public static List<Ref<EntityStore>> getItemsInRange(Store<EntityStore> entityStore, Vector3i blockPos, double range) {
        return getItemsInRange(entityStore, blockPos.toVector3d(), range);
    }

    /**
     * Retrieves a list of items located within a specified range of a given world position.
     *
     * @param entityStore the store containing the entity data
     * @param worldPos the world position around which the search is performed
     * @param range the search radius around the specified world position
     * @return a list of references to entity stores representing the items found within the range
     */
    @Nonnull
    public static List<Ref<EntityStore>> getItemsInRange(@Nonnull Store<EntityStore> entityStore, Vector3d worldPos, double range) {
        ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        SpatialResource<Ref<EntityStore>, EntityStore> itemSpatialResource = entityStore.getResource(EntityModule.get().getItemSpatialResourceType());
        itemSpatialResource.getSpatialStructure().collect(worldPos, range, results);
        return results;
    }
}
