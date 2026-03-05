package com.tschm.resonance.util;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;

import javax.annotation.Nullable;

public class BlockHelper {
    /**
     * Activates a block state at the specified position in the given world, using default settings
     * for block state changes. This method ensures the block is updated to reflect the desired state,
     * if applicable.
     *
     * @param stateName The name of the block state to activate.
     * @param world The world instance where the block state update should occur.
     * @param pos The 3-dimensional coordinates of the block's position in the world.
     * @return A string describing the reason for failure if the operation cannot be completed,
     *         or null if the activation is successful without any errors.
     */
    @Nullable
    public static String activateBlockState(String stateName, World world, Vector3i pos) {
        // Copy the same settings as ChangeStateInteraction
        int defaultSettings = SetBlockFlag.of(
                SetBlockFlag.DO_BLOCK_UPDATES,
                SetBlockFlag.SKIP_STATE_AND_BLOCK_ENTITY,
                SetBlockFlag.SKIP_PARTICLES,
                SetBlockFlag.SKIP_BREAK_OLD_FILLER_BLOCKS);

        return activateBlockState(stateName, world, pos, defaultSettings);
    }

    /**
     * Activates a specific block state at the given position in the world. If the block at the
     * given position is not already in the desired state, it will be updated accordingly.
     *
     * @param stateName The name of the block state to activate.
     * @param world The world in which the block state is to be activated.
     * @param pos The position of the block in the world as a 3-dimensional vector.
     * @param settings The configuration flags for setting the block, which dictate
     *                 various behaviors such as whether block updates are triggered.
     * @return A string describing an error if the operation fails, or null if the operation
     *         succeeds without any issues.
     */
    @Nullable
    public static String activateBlockState(String stateName, World world, Vector3i pos, int settings) {
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

        int rotation = worldChunk.getRotationIndex(pos.x, pos.y, pos.z);
        worldChunk.setBlock(pos.x, pos.y, pos.z, newBlockIdx, newBlockType, rotation, 0, settings);
        return null; // success
    }
}

