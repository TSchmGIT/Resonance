package com.tschm.resonance.util;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class HUDProvider<T extends HUDProvider<T>> extends CustomUIHud {
    private static final Map<PlayerRef, HUDProvider<?>> huds = new HashMap<>();

    protected HUDProvider(PlayerRef playerRef) {
        super(playerRef);
    }

    public static <T extends HUDProvider<T>> T get(PlayerRef playerRef, Function<PlayerRef, T> factory) {
        return (T) huds.computeIfAbsent(playerRef, factory);
    }
}