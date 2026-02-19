package com.tschm.resonance.systems.functional;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.functional.ResonantAttractorComponent;
import com.tschm.resonance.util.SystemsHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class ResonantAttractorSystems {
    public static class TickingSystem extends EntityTickingSystem<ChunkStore> {

        @Override
        public void tick(float dt, int idx, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            ResonantAttractorComponent compAttractor = archetypeChunk.getComponent(idx, ResonantAttractorComponent.getComponentType());
            EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            Vector3i blockPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            if (compAttractor == null || compStorage == null || blockPos == null)
                return;

            World world = commandBuffer.getExternalData().getWorld();
            Store<EntityStore> entityStore = world.getEntityStore().getStore();

            for (Ref<EntityStore> itemEntity : SystemsHelper.getItemsInRange(entityStore, blockPos, compAttractor.getAttractionRange())) {
                TransformComponent entityTransform = entityStore.getComponent(itemEntity, TransformComponent.getComponentType());
                ItemComponent pickup = entityStore.getComponent(itemEntity, ItemComponent.getComponentType());
                if (entityTransform == null || pickup == null || !pickup.canPickUp())
                    continue;

                Vector3d targetPos = blockPos.toVector3d().add(0.5, 0.5, 0.5);
                Vector3d currentPos = entityTransform.getPosition();
                Vector3d newPos = Vector3d.lerp(currentPos, targetPos, compAttractor.attractionSpeed);
                entityTransform.setPosition(newPos);
            }
        }

        @NullableDecl
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(ResonantAttractorComponent.getComponentType(), EssenceStorageComponent.getComponentType());
        }
    }
}
