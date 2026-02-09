package com.tschm.resonance.ui;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.tschm.resonance.util.HUDProvider;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class EmptyHUD extends HUDProvider<EmptyHUD> {

    public static EmptyHUD get(@Nonnull PlayerRef playerRef) {
        return get(playerRef, EmptyHUD::new);
    }

    public EmptyHUD(@NonNullDecl PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@NonNullDecl UICommandBuilder uiCommandBuilder) {
    }
}
