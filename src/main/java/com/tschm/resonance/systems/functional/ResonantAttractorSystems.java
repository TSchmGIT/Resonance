package com.tschm.resonance.systems.functional;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.functional.ResonantAttractorComponent;
import com.tschm.resonance.components.storage.EchoStorageComponent;
import com.tschm.resonance.util.SystemsHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResonantAttractorSystems {
    public static class TickingSystem extends EntityTickingSystem<ChunkStore> {

        @Override
        public void tick(float dt, int idx, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            ResonantAttractorComponent compAttractor = archetypeChunk.getComponent(idx, ResonantAttractorComponent.getComponentType());
            EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            EchoStorageComponent compEchoStorage = archetypeChunk.getComponent(idx, EchoStorageComponent.getComponentType());
            Vector3i blockPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            if (compAttractor == null || compStorage == null || compEchoStorage == null || blockPos == null)
                return;

            World world = commandBuffer.getExternalData().getWorld();
            Store<EntityStore> entityStore = world.getEntityStore().getStore();

            for (Ref<EntityStore> itemEntity : SystemsHelper.getItemsInRange(entityStore, blockPos, compAttractor.getAttractionRange())) {
                TransformComponent entityTransform = entityStore.getComponent(itemEntity, TransformComponent.getComponentType());
                ItemComponent compItem = entityStore.getComponent(itemEntity, ItemComponent.getComponentType());
                if (entityTransform == null || compItem == null || !compItem.canPickUp())
                    continue;

                Vector3d targetPos = blockPos.toVector3d().add(0.5, 0.5, 0.5);
                Vector3d currentPos = entityTransform.getPosition();
                Vector3d newPos = Vector3d.lerp(currentPos, targetPos, compAttractor.attractionSpeed);
                entityTransform.setPosition(newPos);

                ItemContainer itemContainer = compEchoStorage.inventory;
                final float pickupRadius = compItem.getPickupRadius(entityStore);
                final boolean isInPickUpRange = targetPos.distanceSquaredTo(newPos) < (pickupRadius * pickupRadius);
                final ItemStack itemStack = compItem.getItemStack();
                if (isInPickUpRange && itemStack != null) {
                    world.execute(() -> {
                        if (!itemContainer.canAddItemStack(itemStack))
                            return;

                        compEchoStorage.inventory.addItemStack(itemStack);
                        entityStore.removeEntity(itemEntity, RemoveReason.REMOVE);
                    });
                }
            }
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(ResonantAttractorComponent.getComponentType(), EchoStorageComponent.getComponentType(), EssenceStorageComponent.getComponentType());
        }
    }
}
