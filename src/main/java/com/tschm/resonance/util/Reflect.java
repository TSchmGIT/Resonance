package com.tschm.resonance.util;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.BiConsumer;

public class Reflect {

    public static class HudManager_ {
        private static final MethodHandles.Lookup LOOKUP = lookup(HudManager.class);
        private static final VarHandle _CUSTOM_HUD = handle(LOOKUP, HudManager.class, "customHud", CustomUIHud.class);
        public static final BiConsumer<HudManager, CustomUIHud> CUSTOM_HUD = _CUSTOM_HUD::set;
    }

    private static VarHandle handle(MethodHandles.Lookup lookup, Class<?> clazz, String name, Class<?> type) {
        try {
            return lookup.findVarHandle(clazz, name, type);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            DebugHelper.Print(String.format("Unable to access field '%s' for class '%s'!", name, clazz.getCanonicalName()));
            throw new RuntimeException(e);
        }
    }

    private static MethodHandles.Lookup lookup(Class<?> clazz) {
        try {
            return MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
        }
        catch (IllegalAccessException e) {
            DebugHelper.Print(String.format("Unable to create lookup for class '%s'!", clazz.getCanonicalName()));
            throw new RuntimeException(e);
        }
    }
}