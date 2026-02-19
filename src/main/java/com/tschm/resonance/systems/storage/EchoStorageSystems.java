package com.tschm.resonance.systems.storage;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.storage.EchoStorageComponent;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EchoStorageSystems {
    public static class TickingSystem extends EntityTickingSystem<ChunkStore> {
        @Override
        public void tick(float dt, int idx, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            EchoStorageComponent compStorage = archetypeChunk.getComponent(idx, EchoStorageComponent.getComponentType());
            assert compStorage != null;

            compStorage.remainingTicksUntilOperation = Math.max(0, compStorage.remainingTicksUntilOperation - 1);
            if (compStorage.remainingTicksUntilOperation > 0) {
//                DebugHelper.Print("Remaining ticks until operation: " + compStorage.remainingTicksUntilOperation);
                return;
            }

            if (!compStorage.canSend()) {
                DebugHelper.Print("Cannot send items");
                return;
            }

            ItemContainer itemContainerSend = compStorage.inventory;
            if (itemContainerSend == null || itemContainerSend.isEmpty()) {
//                DebugHelper.Print("Inventory null or empty");
                return;
            }

            Vector3i posReceiver = compStorage.getBoundPositionSend();
            if (posReceiver == null) {
                DebugHelper.Print("Bound position is null");
                return;
            }

            World world = commandBuffer.getExternalData().getWorld();
            ItemContainer itemContainerReceive = findItemContainerAt(world, posReceiver);
            if (itemContainerReceive == null) {
                DebugHelper.Print("ItemContainer at bound position is null");
                return;
            }

            int remainingItemsToTransfer = compStorage.itemsPerOperation;
            int totalItemsTransferred = 0;
            short slot = (short) 0;
            while (remainingItemsToTransfer > 0) {
                ItemStack itemStack = itemContainerSend.getItemStack(slot);
                if (itemStack == null)
                    break;

                int itemsTransferred = Math.min(remainingItemsToTransfer, itemStack.getQuantity());
                if (itemsTransferred < itemStack.getQuantity())
                    itemStack = itemStack.withQuantity(itemsTransferred);

                ItemStackTransaction transaction = itemContainerReceive.addItemStack(itemStack);
                ItemStack remainder = transaction.getRemainder();
                if (ItemStack.isEmpty(remainder)) {
                    remainingItemsToTransfer -= itemsTransferred;
                    itemContainerSend.removeItemStackFromSlot(slot, itemsTransferred);
                } else {
                    itemContainerSend.replaceItemStackInSlot(slot, itemStack, remainder);
                }

                totalItemsTransferred += itemsTransferred - (remainder != null ? remainder.getQuantity() : 0);
            }

            compStorage.remainingTicksUntilOperation = compStorage.ticksPerOperation;
            DebugHelper.Print("Transferred " + totalItemsTransferred + " items");
        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return EchoStorageComponent.getComponentType();
        }

        @Nullable
        private static ItemContainer findItemContainerAt(World world, Vector3i pos) {
            EchoStorageComponent echoStorageComponent = ComponentHelper.findComponentAt(world, pos, EchoStorageComponent.getComponentType());
            if (echoStorageComponent != null)
                return echoStorageComponent.inventory;

            ItemContainerState itemContainerState = ComponentHelper.findBlockStateAt(world, pos, ItemContainerState.class);
            if (itemContainerState != null)
                return itemContainerState.getItemContainer();

            return null;
        }
    }
}
