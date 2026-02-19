package com.tschm.resonance.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTranslationProperties;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
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

    public static Message getDisplayName(@Nonnull BlockType type) {
        final Item item = type.getItem();
        final ItemTranslationProperties translations = item != null ? item.getTranslationProperties() : null;
        final String nameKey = translations != null ? translations.getName() : null;

        return nameKey != null ? Message.translation(nameKey) : Message.raw(type.getId());
    }
}