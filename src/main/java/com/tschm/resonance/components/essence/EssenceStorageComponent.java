package com.tschm.resonance.components.essence;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import java.util.ArrayList;
import java.util.List;

public final class EssenceStorageComponent extends AbstractEssenceStorage implements Component<ChunkStore> {

    public static final BuilderCodec<EssenceStorageComponent> CODEC;
    private static ComponentType<ChunkStore, EssenceStorageComponent> type;

    public static ComponentType<ChunkStore, EssenceStorageComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, EssenceStorageComponent> type) {
        EssenceStorageComponent.type = type;
    }

    public boolean canReceive = true;
    public boolean canSend = false;

    // List of block position that send RE to this storage
    private List<Vector3i> boundSenderList = new ArrayList<Vector3i>();
    // List of block position that receive RE from this storage
    private List<Vector3i> boundReceiverList = new ArrayList<Vector3i>();

    public EssenceStorageComponent() {
    }

    public List<Vector3i> getBoundSenderList() {
        return boundSenderList;
    }
    public List<Vector3i> getBoundReceiverList() { return boundReceiverList; }

    @Override
    public Component<ChunkStore> clone() {
        EssenceStorageComponent copy = new EssenceStorageComponent();
        copy.copyFrom(this);
        return copy;
    }

    public void copyFrom(EssenceStorageComponent other) {
        super.copyFrom(other);
        this.boundSenderList = new ArrayList<>(other.boundSenderList);
        this.boundReceiverList = new ArrayList<>(other.boundReceiverList);
        this.canReceive = other.canReceive;
        this.canSend = other.canSend;
    }

    static {
        CODEC = BuilderCodec.builder(EssenceStorageComponent.class, EssenceStorageComponent::new, AbstractEssenceStorage.CODEC)
                .append(new KeyedCodec<>("CanReceive", BuilderCodec.BOOLEAN), (c, v) -> c.canReceive = v, c -> c.canReceive)
                .documentation("Can this storage receive essence from another storage").add()
                .append(new KeyedCodec<>("CanSend", BuilderCodec.BOOLEAN), (c, v) -> c.canSend = v, c -> c.canSend)
                .documentation("Can this storage send essence to another storage").add()

                .build();
    }
}
