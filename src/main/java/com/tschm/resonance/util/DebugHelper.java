package com.tschm.resonance.util;

import com.google.common.flogger.StackSize;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.tschm.resonance.Resonance;

public class DebugHelper {
    public static void Print(String message) {
        Print(message, false);
    }

    public static void Print(String message, boolean withStackTrace) {
        Universe.get().sendMessage(Message.raw(message));
        if (withStackTrace)
            Resonance.LOGGER.atInfo().withStackTrace(StackSize.FULL).log(message);
        else
            Resonance.LOGGER.atInfo().log(message);
    }
}
