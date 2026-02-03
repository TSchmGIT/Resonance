package com.tschm.resonance.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.RitualStoneComponent;
import com.tschm.resonance.util.ItemHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nullable;
import java.util.UUID;

public class RitualStoneSystems {
    public static class BreakSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

        public BreakSystem() {
            super(BreakBlockEvent.class);
        }

        @Override
        public void handle(int i,
                           @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                           @NonNullDecl BreakBlockEvent breakBlockEvent) {

            Vector3i pos = breakBlockEvent.getTargetBlock();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            long indexChunk = ChunkUtil.indexChunkFromBlock(x, z);
            World world = commandBuffer.getExternalData().getWorld();
            WorldChunk worldchunk = world.getChunk(indexChunk);
            Ref<ChunkStore> chunkRef = worldchunk.getBlockComponentEntity(x, y, z);
            if (chunkRef == null) {
                return;
            }

            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            RitualStoneComponent ritualStoneComponent = chunkStore.getComponent(chunkRef, RitualStoneComponent.getComponentType());
            if (ritualStoneComponent == null) {
                return;
            }

            @Nullable RitualStoneComponent.Slot filledSlot;
            while ((filledSlot = ritualStoneComponent.getTopmostFilledSlot()) != null) {
                UUID oldUUID = ritualStoneComponent.getEntityUUID(filledSlot);
                ItemStack oldItemStack = ritualStoneComponent.getItem(filledSlot);

                Ref<EntityStore> previewItemEntity = oldUUID != null ? world.getEntityRef(oldUUID) : null;
                if (previewItemEntity != null) {
                    ItemHelper.spawnItem(commandBuffer, previewItemEntity, oldItemStack);
                    commandBuffer.removeEntity(previewItemEntity, RemoveReason.REMOVE);
                }

                ritualStoneComponent.setItemUUID(filledSlot, null);
                ritualStoneComponent.setItem(filledSlot, null);
            }

        }

        @NullableDecl
        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
    }
}
