package com.tschm.resonance.util;

import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum TransferMode {
    Disabled,
    Pull,
    Push,
    Both;

    public static final EnumCodec<TransferMode> CODEC = new EnumCodec<>(TransferMode.class);

    public boolean isPull() {
        return this == Pull || this == Both;
    }

    public boolean isPush() {
        return this == Push || this == Both;
    }

    public TransferMode next() {
        TransferMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
