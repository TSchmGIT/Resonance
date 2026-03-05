package com.tschm.resonance.systems.functional;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.MathUtil;
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
import com.tschm.resonance.util.BlockHelper;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.SystemsHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class ResonantAttractorSystems {
    public static class TickingSystem extends EntityTickingSystem<ChunkStore> {

        @Override
        public void tick(float dt, int idx, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            ResonantAttractorComponent compAttractor = archetypeChunk.getComponent(idx, ResonantAttractorComponent.getComponentType());
            EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            EchoStorageComponent compEchoStorage = archetypeChunk.getComponent(idx, EchoStorageComponent.getComponentType());
            Vector3i blockPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            if (compAttractor == null || compStorage == null || compEchoStorage == null || blockPos == null) {
                DebugHelper.Print("compAttractor or compStorage or compEchoStorage or blockPos null");
                return;
            }

            World world = commandBuffer.getExternalData().getWorld();
            Store<EntityStore> entityStore = world.getEntityStore().getStore();

            final long requiredEssence = compAttractor.essenceUsedPerTick;
            long simulateInsert = compStorage.removeEssence(requiredEssence, true);

            // Gather items to attract
            ArrayList<Ref<EntityStore>> itemsToAttract = new ArrayList<>();
            if (simulateInsert >= requiredEssence) {
                for (Ref<EntityStore> itemEntity : SystemsHelper.getItemsInRange(entityStore, blockPos, compAttractor.getAttractionRange())) {
                    TransformComponent entityTransform = entityStore.getComponent(itemEntity, TransformComponent.getComponentType());
                    ItemComponent compItem = entityStore.getComponent(itemEntity, ItemComponent.getComponentType());
                    if (entityTransform == null || compItem == null || !compItem.canPickUp())
                        continue;

                    itemsToAttract.add(itemEntity);
                }
            }

            // Update component and block state
            final boolean isActive = !itemsToAttract.isEmpty();
            if (isActive && compAttractor.activationTick == 0L) {
                compAttractor.activationTick = world.getTick();
                BlockHelper.activateBlockState("On", world, blockPos);
            } else if (!isActive) {
                compAttractor.activationTick = 0L;
                BlockHelper.activateBlockState("Off", world, blockPos);
            }

            // Apply attraction and essence removal
            if (isActive) {
                final boolean isAttractingTimeframe = isAttractingTimeframe(world, compAttractor);
                for (Ref<EntityStore> itemEntity : itemsToAttract) {
                    attractItemEntity(itemEntity, entityStore, blockPos, compAttractor, compEchoStorage, world, isAttractingTimeframe);
                }

                compStorage.removeEssence(requiredEssence, false);
            }
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(ResonantAttractorComponent.getComponentType(), EchoStorageComponent.getComponentType(), EssenceStorageComponent.getComponentType());
        }

        private static void attractItemEntity(Ref<EntityStore> itemEntity, Store<EntityStore> entityStore, Vector3i blockPos, ResonantAttractorComponent compAttractor, EchoStorageComponent compEchoStorage, World world, boolean isAttractingTimeframe) {
            TransformComponent entityTransform = entityStore.getComponent(itemEntity, TransformComponent.getComponentType());
            ItemComponent compItem = entityStore.getComponent(itemEntity, ItemComponent.getComponentType());
            assert entityTransform != null && compItem != null;

            Vector3d targetPos = blockPos.toVector3d().add(0.5, 0.5, 0.5);
            Vector3d currentPos = entityTransform.getPosition();

            // Calculate attraction speed
            double attractionSpeed = compAttractor.attractionSpeed;
            if (!isAttractingTimeframe) {
                attractionSpeed *= 0.2;
            }

            // Speed decreases the heavier/more the item stack is
            // min speed of 25% reached at 10 items
            final ItemStack itemStack = compItem.getItemStack();
            if (itemStack != null)
                attractionSpeed *= MathUtil.mapToRange(itemStack.getQuantity(), 1.0f, 10.0f, 1.0f, 0.25f);

            Vector3d newPos = Vector3d.lerp(currentPos, targetPos, attractionSpeed);
            entityTransform.setPosition(newPos);

            ItemContainer itemContainer = compEchoStorage.inventory;
            final float pickupRadius = compItem.getPickupRadius(entityStore);
            final boolean isInPickUpRange = targetPos.distanceSquaredTo(newPos) < (pickupRadius * pickupRadius);
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

    private static boolean isAttractingTimeframe(World world, ResonantAttractorComponent compAttractor) {
        final long ticks = world.getTick();
        final long delta = ticks - compAttractor.activationTick;
        final long tps = world.getTps();
        final long ticksDuringSecond = delta % tps;

        return ticksDuringSecond >= compAttractor.tickAttractionStart && ticksDuringSecond <= compAttractor.tickAttractionEnd;
    }
}
