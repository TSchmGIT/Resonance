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
import com.tschm.resonance.components.EssenceStorageBlockComponent;
import com.tschm.resonance.components.EssenceStorageVisualizerComponent;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.SetBlockFlag;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EssenceStorageSystems {
    // Event system to initialize the properties of the visualizer component
    public static class VisualizerPlacedChunk extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public VisualizerPlacedChunk() {
            super(PlaceBlockEvent.class);
        }

        @Override
        public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl PlaceBlockEvent placeBlockEvent) {
            ItemStack itemStack = placeBlockEvent.getItemInHand();
            String itemId = itemStack != null ? itemStack.getItemId() : "";
            if (!itemId.equals("Resonant_Vessel")){
                return;
            }

            World world = commandBuffer.getExternalData().getWorld();
            Vector3i targetPos = placeBlockEvent.getTargetBlock();
            commandBuffer.run(entityStore -> {
                var compVisualizer = ComponentHelper.findComponentAt(world, targetPos, EssenceStorageVisualizerComponent.getComponentType());
                if (compVisualizer == null){
                    DebugHelper.Print("Position NOT set!");
                    return;
                }

                compVisualizer.setPosition(targetPos);
            });
        }

        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
    }

    // Updates the block state of the visualizer
    public static class EssenceStorageVisualizerSystem extends EntityTickingSystem<ChunkStore> {

        // The amount of levels possible in the visual representation
        private static final int LEVEL_COUNT = 4;

        @Override
        public void tick(float v, int idx, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            EssenceStorageVisualizerComponent compVisualizer = archetypeChunk.getComponent(idx, EssenceStorageVisualizerComponent.getComponentType());
            EssenceStorageBlockComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageBlockComponent.getComponentType());
            if (compStorage == null || compVisualizer == null) {
                DebugHelper.Print("compStorage or compVisualizer null");
                return;
            }

            int newLevel = (int) Math.floor(compStorage.getFillRatio() * LEVEL_COUNT);
            // Skip if no change is required
            if (compVisualizer.getCurrentLevel() == newLevel)
                return;

            compVisualizer.setCurrentLevel(newLevel);

            Vector3i targetBlock = compVisualizer.getPosition();
            if (targetBlock == null){
                DebugHelper.Print("targetBlock null");
                return;
            }

            Ref<ChunkStore> ref = archetypeChunk.getReferenceTo(idx);
            BlockModule.BlockStateInfo info = (BlockModule.BlockStateInfo) commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
            if (info == null){
                DebugHelper.Print("info null");
                return;
            }
            WorldChunk worldChunk = (WorldChunk) commandBuffer.getComponent(info.getChunkRef(), WorldChunk.getComponentType());
            if (worldChunk == null)
            {
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

                World world = commandBuffer.getExternalData().getWorld();
                EssenceStorageVisualizerComponent compStorageNew = ComponentHelper.findComponentAt(world, targetBlock, EssenceStorageVisualizerComponent.getComponentType());
                if (compStorageNew != null)
                    compStorageNew.setPosition(targetBlock);
            });
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(EssenceStorageBlockComponent.getComponentType(), EssenceStorageVisualizerComponent.getComponentType());
        }
    }

}
