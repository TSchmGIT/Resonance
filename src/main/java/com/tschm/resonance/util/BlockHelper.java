package com.tschm.resonance.util;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;

import javax.annotation.Nullable;

public class BlockHelper {
    @Nullable
    public static String activateBlockState(String stateName, World world, Vector3i pos) {
        long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
        WorldChunk worldChunk = world.getChunk(chunkIndex);
        if (worldChunk == null)
            return "No world chunk";

        BlockType current = worldChunk.getBlockType(pos);
        if (current == null)
            return "No current block type at pos " + pos;

        String newBlockKey = current.getBlockKeyForState(stateName);
        if (newBlockKey == null) {
            return "No new block key for state " + stateName;
        }

        // No change if already in correct state
        if (current.getId().equals(newBlockKey))
            return null;

        int newBlockIdx = BlockType.getAssetMap().getIndex(newBlockKey);
        if (newBlockIdx == Integer.MIN_VALUE)
            return "Invalid block id for block key " + newBlockKey;

        BlockType newBlockType = BlockType.getAssetMap().getAsset(newBlockIdx);
        if (newBlockType == null)
            return "No new block type for id " + newBlockIdx + " and key " + newBlockKey;

        // Copy the same settings as ChangeStateInteraction
        int settings = SetBlockFlag.of(
                SetBlockFlag.DO_BLOCK_UPDATES,
                SetBlockFlag.SKIP_STATE_AND_BLOCK_ENTITY,
                SetBlockFlag.SKIP_PARTICLES,
                SetBlockFlag.SKIP_BREAK_OLD_FILLER_BLOCKS);
        int rotation = worldChunk.getRotationIndex(pos.x, pos.y, pos.z);
        worldChunk.setBlock(pos.x, pos.y, pos.z, newBlockIdx, newBlockType, rotation, 0, settings);
        return null; // success
    }
}

