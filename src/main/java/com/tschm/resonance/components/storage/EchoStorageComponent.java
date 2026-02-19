package com.tschm.resonance.components.storage;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nullable;

public class EchoStorageComponent implements Component<ChunkStore> {
    public static final BuilderCodec<EchoStorageComponent> CODEC;
    private static ComponentType<ChunkStore, EchoStorageComponent> type;

    public static ComponentType<ChunkStore, EchoStorageComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, EchoStorageComponent> newType) {
        if (type != null) {
            throw new IllegalStateException("EchoStorageComponent ComponentType already set");
        }
        type = newType;
    }

    public SimpleItemContainer inventory = new SimpleItemContainer((short) 1);

    public int ticksPerOperation = 30;
    public int itemsPerOperation = 1;

    private boolean canSend = true;
    private boolean canReceive = true;

    public int remainingTicksUntilOperation = 0;
    @Nullable
    private Vector3i boundPositionSend;
    @Nullable
    private Vector3i boundPositionReceive;

    public EchoStorageComponent() {
    }

    public EchoStorageComponent(
            SimpleItemContainer inventory,
            int ticksPerOperation,
            int itemsPerOperation,
            boolean canRetrieve,
            boolean canStore,
            int remainingTicksUntilOperation,
            @Nullable Vector3i boundPositionSend,
            @Nullable Vector3i boundPositionReceive) {
        this.inventory = inventory;
        this.ticksPerOperation = ticksPerOperation;
        this.itemsPerOperation = itemsPerOperation;
        this.canSend = canRetrieve;
        this.canReceive = canStore;
        this.remainingTicksUntilOperation = remainingTicksUntilOperation;
        this.boundPositionSend = boundPositionSend;
        this.boundPositionReceive = boundPositionReceive;
    }

    @Override
    public Component<ChunkStore> clone() {
        return new EchoStorageComponent(inventory, ticksPerOperation, itemsPerOperation, canSend, canReceive, remainingTicksUntilOperation, boundPositionSend, boundPositionReceive);
    }

    public boolean canSend() {
        return canSend;
    }

    public boolean canReceive() {
        return canReceive;
    }

    @Nullable
    public Vector3i getBoundPositionSend() {
        return boundPositionSend;
    }

    @Nullable
    public Vector3i getBoundPositionReceive() {
        return boundPositionReceive;
    }

    public void setBoundPositionSend(@Nullable Vector3i boundPositionSend) {
        if (boundPositionSend != null && !canSend)
            throw new IllegalStateException("Cannot set bound position send without canRetrieve being true");

        this.boundPositionSend = boundPositionSend;
    }

    public void setBoundPositionReceive(@Nullable Vector3i boundPositionReceive) {
        if (boundPositionReceive != null && !canReceive)
            throw new IllegalStateException("Cannot set bound position receive without canStore being true");

        this.boundPositionReceive = boundPositionReceive;
    }

    static {
        CODEC = BuilderCodec.builder(EchoStorageComponent.class, EchoStorageComponent::new)
                .append(new KeyedCodec<>("Inventory", SimpleItemContainer.CODEC), (c, v) -> c.inventory = v, c -> c.inventory)
                .documentation("Inventory container for EchoStorageComponent").add()

                .append(new KeyedCodec<>("TicksPerOperation", BuilderCodec.INTEGER), (c, v) -> c.ticksPerOperation = v, c -> c.ticksPerOperation)
                .addValidator(Validators.greaterThan(0))
                .documentation("Ticks required for each operation").add()
                .append(new KeyedCodec<>("ItemsPerOperation", BuilderCodec.INTEGER), (c, v) -> c.itemsPerOperation = v, c -> c.itemsPerOperation)
                .addValidator(Validators.greaterThanOrEqual(0))
                .documentation("Items transferred per operation").add()
                .append(new KeyedCodec<>("CanSend", BuilderCodec.BOOLEAN), (c, v) -> c.canSend = v, c -> c.canSend)
                .documentation("Can this storage retrieve items from another storage").add()
                .append(new KeyedCodec<>("CanReceive", BuilderCodec.BOOLEAN), (c, v) -> c.canReceive = v, c -> c.canReceive)
                .documentation("Can this storage store items in another storage").add()

                .append(new KeyedCodec<>("RemainingTicksUntilOperation", BuilderCodec.INTEGER), (c, v) -> c.remainingTicksUntilOperation = v, c -> c.remainingTicksUntilOperation)
                .addValidator(Validators.greaterThanOrEqual(0))
                .documentation("Ticks until next operation").add()
                .append(new KeyedCodec<>("BoundPositionSend", Vector3i.CODEC), (c, v) -> c.boundPositionSend = v, c -> c.boundPositionSend)
                .documentation("Position of bound EchoStorageComponent to send to").add()
                .append(new KeyedCodec<>("BoundPositionReceive", Vector3i.CODEC), (c, v) -> c.boundPositionReceive = v, c -> c.boundPositionReceive)
                .documentation("Position of bound EchoStorageComponent to receive from").add()
                .build();
    }
}
