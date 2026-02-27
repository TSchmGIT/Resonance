package com.tschm.resonance.systems.generators;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.essence.generators.CarbonAttunementStoneComponent;
import com.tschm.resonance.systems.EssenceGeneratorSystems;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.SystemsHelper;
import it.unimi.dsi.fastutil.objects.ObjectList;

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

            // Check if we need to find a new item
            if (compCAS.remainingBurnEssence <= 0) {
                Vector3i blockPos = SystemsHelper.getPosForBlock(archetypeChunk, idx, commandBuffer);
                assert blockPos != null;

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

                        compCAS.remainingBurnEssence = totalEssence;
                        compCAS.currentEssencePerTick = totalEssence / totalTicks;
                        if (compStorage != null)
                            compStorage.setMaxExtract(compCAS.currentEssencePerTick);

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

            // If there is something burning up add
            if (compCAS.remainingBurnEssence > 0) {
                long essenceProducedThisTick = Math.min(compCAS.currentEssencePerTick, compCAS.remainingBurnEssence);
                compCAS.remainingBurnEssence -= essenceProducedThisTick;
                supplyEssenceToBoundStorage(world, archetypeChunk, idx, compCAS, essenceProducedThisTick);
            }
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return CarbonAttunementStoneComponent.getComponentType();
        }
    }
}
