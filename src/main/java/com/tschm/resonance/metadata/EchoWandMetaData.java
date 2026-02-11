package com.tschm.resonance.metadata;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3i;

public class EchoWandMetaData {
    public static final BuilderCodec<EchoWandMetaData> CODEC;
    public static final KeyedCodec<EchoWandMetaData> KEYED_CODEC;

    public enum EchoWandState {
        None,
        Bound;

        public static final EnumCodec<EchoWandState> CODEC = new EnumCodec<>(EchoWandState.class);
    }

    public EchoWandState wandState = EchoWandState.None;
    public Vector3i boundPosition = null;

    static {
        CODEC = BuilderCodec.builder(EchoWandMetaData.class, EchoWandMetaData::new)
                .append(new KeyedCodec<>("WandState", EchoWandState.CODEC), (c, v) -> c.wandState = v, c -> c.wandState).add()
                .append(new KeyedCodec<>("BoundPosition", Vector3i.CODEC), (c, v) -> c.boundPosition = v, c -> c.boundPosition).add()
                .build();
        KEYED_CODEC = new KeyedCodec<>("EchoWandMetaData", CODEC);
    }
}
