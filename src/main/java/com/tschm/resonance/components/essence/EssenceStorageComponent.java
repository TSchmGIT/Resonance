package com.tschm.resonance.components.essence;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import java.util.*;

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

    // Set of block position that send RE to this storage
    private Set<Vector3i> boundSenderList = new HashSet<Vector3i>();
    // Set of block position that receive RE from this storage
    private Set<Vector3i> boundReceiverList = new HashSet<Vector3i>();

    public EssenceStorageComponent() {
    }

    /**
     * Retrieves the list of block positions that send RE (Resource Essence) to this storage.
     *
     * @return A list of {@code Vector3i} objects representing the positions of blocks that send RE to this storage.
     */
    public Set<Vector3i> getBoundSenderList() {
        return boundSenderList;
    }

    /**
     * Retrieves the list of block positions that receive RE (Resource Essence) from this storage.
     *
     * @return A list of {@code Vector3i} objects representing the positions of blocks
     * that receive RE from this storage.
     */
    public Set<Vector3i> getBoundReceiverList() {
        return boundReceiverList;
    }

    @Override
    public Component<ChunkStore> clone() {
        EssenceStorageComponent copy = new EssenceStorageComponent();
        copy.copyFrom(this);
        return copy;
    }

    public void copyFrom(EssenceStorageComponent other) {
        super.copyFrom(other);
        this.boundSenderList = new HashSet<>(other.boundSenderList);
        this.boundReceiverList = new HashSet<>(other.boundReceiverList);
        this.canReceive = other.canReceive;
        this.canSend = other.canSend;
    }

    static {
        CODEC = BuilderCodec.builder(EssenceStorageComponent.class, EssenceStorageComponent::new, AbstractEssenceStorage.CODEC)
                .append(new KeyedCodec<>("CanReceive", BuilderCodec.BOOLEAN), (c, v) -> c.canReceive = v, c -> c.canReceive)
                .documentation("Can this storage receive essence from another storage").add()
                .append(new KeyedCodec<>("CanSend", BuilderCodec.BOOLEAN), (c, v) -> c.canSend = v, c -> c.canSend)
                .documentation("Can this storage send essence to another storage").add()

                .append(new KeyedCodec<>("BoundSenderList", new SetCodec<>(Vector3i.CODEC, HashSet::new, false)), (c, v) -> c.boundSenderList = v, c -> c.boundSenderList)
                .documentation("List of storage positions that send essence to this storage").add()
                .append(new KeyedCodec<>("BoundReceiverList", new SetCodec<>(Vector3i.CODEC, HashSet::new, false)), (c, v) -> c.boundReceiverList = v, c -> c.boundReceiverList)
                .documentation("List of storage positions that receive essence from this storage").add()

                .build();
    }
}
