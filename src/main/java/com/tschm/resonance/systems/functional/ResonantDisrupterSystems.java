package com.tschm.resonance.systems.functional;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockBreakingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.functional.ResonantDisrupterComponent;
import com.tschm.resonance.util.SystemsHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResonantDisrupterSystems {
    public static class TickingSystem extends EntityTickingSystem<ChunkStore> {
        @Override
        public void tick(float v, int idx, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
                         @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            ResonantDisrupterComponent compRD = archetypeChunk.getComponent(idx, ResonantDisrupterComponent.getComponentType());
            EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            final Vector3i blockPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            if (compStorage == null || compRD == null || blockPos == null)
                return;

            // Check if we have enough essence to perform damage
            if (compStorage.getEssenceStored() < compRD.ticksPerOperation)
                return;

            final Vector3i currentSearchPos = calculatePosFromIndex(blockPos, compRD.getCurrentBlockIndex(), compRD.damageRange);

            // Check if we have to find a new damage block
            if (compRD.remainingTicksUntilOperation == 0) {
                commandBuffer.run(cs -> {
                    World world = commandBuffer.getExternalData().getWorld();
                    if (canDamageBlockAt(world, currentSearchPos)) {
                        compRD.remainingTicksUntilOperation = compRD.ticksPerOperation;
                    } else {
                        compRD.advanceCurrentBlockIndex();
                    }

                });

                return;
            }

            // Check if there is currently a damage operation in progress
            if (compRD.remainingTicksUntilOperation == 0)
                return;

            // Update ongoing damage operation ticks
            compRD.remainingTicksUntilOperation = Math.max(0, compRD.remainingTicksUntilOperation - 1);

            // Check if cooldown is over to apply damage tick
            if (compRD.remainingTicksUntilOperation == 0) {
                final World world = commandBuffer.getExternalData().getWorld();

                // Check if target block is still valid
                if (!canDamageBlockAt(world, currentSearchPos)) {
                    compRD.advanceCurrentBlockIndex();
                    return;
                }

                // Reset remaining ticks as operation is executed
                compRD.remainingTicksUntilOperation = compRD.ticksPerOperation;

                // Defer store manipulation to command buffer
                // Until the operation is completed pretend the block was not broken
                commandBuffer.run(cs -> {
                    final boolean brokeBlock = damageBlockWithDrops(world, currentSearchPos, compRD.damagePerOperation);
                    compStorage.extractEssence(compRD.essencePerOperation, false);

                    if (brokeBlock) {
                        compRD.advanceCurrentBlockIndex();
                    } else {
                        compRD.remainingTicksUntilOperation = compRD.ticksPerOperation;
                    }
                });
            }
        }

        private Vector3i calculatePosFromIndex(Vector3i blockPos, int idx, int range) {
            int xStart = blockPos.x - range;
            int zStart = blockPos.z - range;

            int fullRange = (range * 2) + 1;

            Vector3i damagePos = blockPos.clone();
            damagePos.x = xStart + (idx % fullRange);
            damagePos.z = zStart + (idx / fullRange);

            return damagePos;
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(ResonantDisrupterComponent.getComponentType(), EssenceStorageComponent.getComponentType());
        }

        private boolean canDamageBlockAt(World world, Vector3i pos) {
            BlockType blockType = world.getBlockType(pos);
            if (blockType == null)
                return false;

            // Check if this block can be harvested
            ItemToolSpec itemToolSpec = BlockHarvestUtils.getSpecPowerDamageBlock(null, blockType, null);
            if (itemToolSpec == null)
                return false;

            BlockGathering gathering = blockType.getGathering();
            if (gathering == null)
                return false;

            BlockBreakingDropType breaking = gathering.getBreaking();
            if (breaking == null)
                return false;

            return !breaking.getGatherType().isEmpty();
        }

        private boolean damageBlockWithDrops(World world, Vector3i pos, float damage) {
            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
            Ref<ChunkStore> chunkReference = chunkStore.getExternalData().getChunkReference(chunkIndex);
            if (chunkReference == null)
                return false;

            BlockType blockType = world.getBlockType(pos);
            assert blockType != null;

            ItemToolSpec itemToolSpec = BlockHarvestUtils.getSpecPowerDamageBlock(null, blockType, null);
            assert itemToolSpec != null;
            damage /= itemToolSpec.getPower(); // Apply enough damage regardless of required power

            return BlockHarvestUtils.performBlockDamage(
                    null,
                    null,
                    pos,
                    null,
                    null,
                    null,
                    false,
                    damage,
                    0,
                    chunkReference,
                    world.getEntityStore().getStore(),
                    chunkStore);
        }
    }
}