package com.tschm.resonance.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.essence.EssenceStorageVisualizerComponent;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.SetBlockFlag;
import com.tschm.resonance.util.SystemsHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EssenceStorageSystems {
    // Updates the block state of the visualizer
    public static class EssenceStorageVisualizerSystem extends EntityTickingSystem<ChunkStore> {

        // The amount of levels possible in the visual representation
        private static final int LEVEL_COUNT = 4;

        @Override
        public void tick(float v, int idx, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            EssenceStorageVisualizerComponent compVisualizer = archetypeChunk.getComponent(idx, EssenceStorageVisualizerComponent.getComponentType());
            EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            final Vector3i targetBlock = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            if (compStorage == null || compVisualizer == null || targetBlock == null) {
                DebugHelper.Print("compStorage or compVisualizer or targetPos null");
                return;
            }

            int newLevel = (int) Math.floor(compStorage.getFillRatio() * LEVEL_COUNT);
            // Skip if no change is required
            if (compVisualizer.getCurrentLevel() == newLevel)
                return;

            compVisualizer.setCurrentLevel(newLevel);

            Ref<ChunkStore> ref = archetypeChunk.getReferenceTo(idx);
            BlockModule.BlockStateInfo info = (BlockModule.BlockStateInfo) commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
            if (info == null) {
                DebugHelper.Print("info null");
                return;
            }
            WorldChunk worldChunk = (WorldChunk) commandBuffer.getComponent(info.getChunkRef(), WorldChunk.getComponentType());
            if (worldChunk == null) {
                DebugHelper.Print("worldChunk null");
                return;
            }

            BlockType current = worldChunk.getBlockType(targetBlock);
            if (current == null) {
                DebugHelper.Print("OldBlockType null");
                return;
            }

            String newState = "Lvl" + newLevel;
            String newBlockKey = current.getBlockKeyForState(newState);
            if (newBlockKey == null) {
                DebugHelper.Print("BlockKey null");
                return;
            }

            int newBlockId = BlockType.getAssetMap().getIndex(newBlockKey);
            if (newBlockId == Integer.MIN_VALUE) {
                DebugHelper.Print("BlockId null");
                return;
            }

            BlockType newBlockType = (BlockType) BlockType.getAssetMap().getAsset(newBlockId);
            if (newBlockType == null) {
                DebugHelper.Print("NewBlockType null");
                return;
            }

            commandBuffer.run(cs -> {
                int rotation = worldChunk.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);

                // Copy the same settings as ChangeStateInteraction
                int settings = SetBlockFlag.of(
                        SetBlockFlag.DO_BLOCK_UPDATES,
                        SetBlockFlag.SKIP_STATE_AND_BLOCK_ENTITY,
                        SetBlockFlag.SKIP_PARTICLES,
                        SetBlockFlag.SKIP_BREAK_OLD_FILLER_BLOCKS);

                worldChunk.setBlock(targetBlock.x, targetBlock.y, targetBlock.z, newBlockId, newBlockType, rotation, 0, settings);
            });
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(EssenceStorageComponent.getComponentType(), EssenceStorageVisualizerComponent.getComponentType());
        }
    }

}
