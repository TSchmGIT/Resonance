package com.tschm.resonance.util;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nullable;

public class SystemsHelper {
    @Nullable
    public static Vector3i getPosForBlock(ArchetypeChunk<ChunkStore> archetypeChunk, int idx, ComponentAccessor<ChunkStore> componentAccessor) {
        BlockModule.BlockStateInfo info = archetypeChunk.getComponent(idx, BlockModule.BlockStateInfo.getComponentType());
        if (info == null)
            return null;

        int x = ChunkUtil.xFromBlockInColumn(info.getIndex());
        int y = ChunkUtil.yFromBlockInColumn(info.getIndex());
        int z = ChunkUtil.zFromBlockInColumn(info.getIndex());
        ChunkColumn column = componentAccessor.getComponent(info.getChunkRef(), ChunkColumn.getComponentType());
        Ref<ChunkStore> sectionRef = column != null ? column.getSection(ChunkUtil.chunkCoordinate(y)) : null;
        assert sectionRef != null;

        ChunkSection chunkSection = componentAccessor.getComponent(sectionRef, ChunkSection.getComponentType());

        assert chunkSection != null;

        int worldX = ChunkUtil.worldCoordFromLocalCoord(chunkSection.getX(), x);
        int worldY = ChunkUtil.worldCoordFromLocalCoord(chunkSection.getY(), y);
        int worldZ = ChunkUtil.worldCoordFromLocalCoord(chunkSection.getZ(), z);

        return new Vector3i(worldX, worldY, worldZ);
    }
}
