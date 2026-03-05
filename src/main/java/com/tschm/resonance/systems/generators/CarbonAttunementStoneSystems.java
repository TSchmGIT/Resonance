package com.tschm.resonance.systems.generators;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.essence.generators.CarbonAttunementStoneComponent;
import com.tschm.resonance.systems.EssenceGeneratorSystems;
import com.tschm.resonance.util.SystemsHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class CarbonAttunementStoneSystems {

    public static class GeneratorTicks extends EssenceGeneratorSystems.GeneratorTicks {

        @Override
        public void tick(float dt, int idx, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            super.tick(dt, idx, archetypeChunk, store, commandBuffer);

            CarbonAttunementStoneComponent compCAS = archetypeChunk.getComponent(idx, CarbonAttunementStoneComponent.getComponentType());
            final EssenceStorageComponent compStorage = archetypeChunk.getComponent(idx, EssenceStorageComponent.getComponentType());
            assert compCAS != null;

            final World world = commandBuffer.getExternalData().getWorld();
            final Vector3i blockPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
            assert blockPos != null;

            // Check if we need to find a new item
            if (compCAS.remainingBurnEssence <= 0) {
                world.execute(() -> {
                    Store<EntityStore> entityStore = world.getEntityStore().getStore();

                    for (Ref<EntityStore> entity : SystemsHelper.getItemsInRange(entityStore, blockPos, 2.0)) {
                        ItemComponent compItem = entityStore.getComponent(entity, ItemComponent.getComponentType());
                        if (compItem == null || !compItem.canPickUp())
                            continue;

                        ItemStack itemStack = compItem.getItemStack();
                        Item item = itemStack != null ? itemStack.getItem() : null;
                        if (item == null)
                            continue;

                        var resourceTypes = item.getResourceTypes();
                        boolean isFuel = resourceTypes != null && Arrays.stream(resourceTypes).anyMatch(irt -> irt.id.equals("Fuel"));
                        if (!isFuel)
                            continue;

                        double fuelQuality = item.getFuelQuality();
                        long totalEssence = (long) (compCAS.essencePerFuelQuality * fuelQuality);
                        int totalTicks = (int) (compCAS.burnTicksPerFuelQuality * fuelQuality);

                        // Update component data
                        compCAS.remainingBurnEssence = totalEssence;
                        compCAS.currentEssencePerTick = totalEssence / totalTicks;
                        if (compStorage != null)
                            compStorage.setMaxExtract(compCAS.currentEssencePerTick * 2);

                        // Reduce the ItemStack by 1
                        if (itemStack.getQuantity() == 1)
                            entityStore.removeEntity(entity, RemoveReason.REMOVE);
                        else {
                            ItemStack newItemStack = itemStack.withQuantity(itemStack.getQuantity() - 1);
                            compItem.setItemStack(newItemStack);
                        }
                        break;
                    }
                });
            }

            final boolean isActive = compCAS.remainingBurnEssence > 0;
            world.execute(() -> {
                updateGeneratorBlockState(isActive, world, blockPos, compCAS);
            });

            // If there is something burning up, generate new RE in bound storage
            if (isActive) {
                long essenceProducedThisTick = Math.min(compCAS.currentEssencePerTick, compCAS.remainingBurnEssence);
                super.supplyEssenceToStorage(world, archetypeChunk, idx, compCAS, essenceProducedThisTick);

                // Regardless of whether we produced any RE, we burned up some fuel
                // Else we end up with an endless burn process
                compCAS.remainingBurnEssence -= essenceProducedThisTick;
            }
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(super.getQuery(),  CarbonAttunementStoneComponent.getComponentType());
        }
    }
}
