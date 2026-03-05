package com.tschm.resonance.metadata;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EchoWandMetaData {
    public static final BuilderCodec<EchoWandMetaData> CODEC;
    public static final KeyedCodec<EchoWandMetaData> KEYED_CODEC;

    public enum EchoWandState {
        None,
        Bound;

        public static final EnumCodec<EchoWandState> CODEC = new EnumCodec<>(EchoWandState.class);
    }

    public enum EchoWandBindingType {
        None,
        Generator,
        EssenceStorage,
        EchoStorage;

        public static final EnumCodec<EchoWandBindingType> CODEC = new EnumCodec<>(EchoWandBindingType.class);
    }

    @Nonnull
    private EchoWandState wandState = EchoWandState.None;
    @Nonnull
    private EchoWandBindingType bindingType = EchoWandBindingType.None;
    @Nullable
    private Vector3i boundPosition = null;

    /**
     * Binds the EchoWand to a specified position and binding type if it is currently unbound.
     *
     * @param boundPosition the position to bind the wand to, represented as a {@code Vector3i}.
     *                      This value will be stored as the bound position.
     * @param bindingType   the type of object to bind the wand to, represented as an {@code EchoWandBindingType}.
     *                      Determines the nature of the binding.
     * @return {@code true} if the binding was successful; {@code false} if the wand is already bound.
     */
    public boolean bindTo(Vector3i boundPosition, EchoWandBindingType bindingType) {
        if (this.wandState == EchoWandState.Bound)
            return false;

        this.wandState = EchoWandState.Bound;
        this.boundPosition = boundPosition;
        this.bindingType = bindingType;
        return true;
    }

    public void resetBinding() {
        this.wandState = EchoWandState.None;
        this.bindingType = EchoWandBindingType.None;
        this.boundPosition = null;
    }

    @Nullable
    public Vector3i getBoundPosition() {
        return boundPosition;
    }

    @Nonnull
    public EchoWandBindingType getBindingType() {
        return bindingType;
    }

    @Nonnull
    public EchoWandState getWandState() {
        return wandState;
    }

    /**
     * Returns an error message if the current binding type does not match {@code expected}, or null if it matches.
     */
    @Nullable
    public String requireBoundType(EchoWandBindingType expected) {
        if (bindingType != expected)
            return "Cannot link " + bindingType + " here!";
        return null;
    }

    static {
        CODEC = BuilderCodec.builder(EchoWandMetaData.class, EchoWandMetaData::new)
                .append(new KeyedCodec<>("WandState", EchoWandState.CODEC), (c, v) -> c.wandState = v, c -> c.wandState).add()
                .append(new KeyedCodec<>("BoundPosition", Vector3i.CODEC), (c, v) -> c.boundPosition = v, c -> c.boundPosition).add()
                .append(new KeyedCodec<>("BindingType", EchoWandBindingType.CODEC), (c, v) -> c.bindingType = v, c -> c.bindingType).add()
                .build();
        KEYED_CODEC = new KeyedCodec<>("EchoWandMetaData", CODEC);
    }
}
