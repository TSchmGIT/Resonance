package com.tschm.resonance.util;

import com.google.common.flogger.StackSize;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.tschm.resonance.Resonance;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class DebugHelper {
    private static final Set<String> printedIds = new HashSet<>();
    private static final Map<String, Long> timedIds = new HashMap<>();
    private static final long DEFAULT_INTERVAL_MS = 5000;

    public static void Print(String message) {
        Print(message, false);
    }

    public static void Print(String message, boolean withStackTrace) {
        Universe.get().getDefaultWorld().sendMessage(Message.raw(message));
        if (withStackTrace)
            Resonance.LOGGER.atInfo().withStackTrace(StackSize.FULL).log(message);
        else
            Resonance.LOGGER.atInfo().log(message);
    }

    public static void PrintOnce(String id, String message) {
        PrintOnce(id, message, false);
    }

    public static void PrintOnce(String id, String message, boolean withStackTrace) {
        if (printedIds.add(id)) {
            Print(message, withStackTrace);
        }
    }

    public static void PrintTimed(String id, String message) {
        PrintTimed(id, message, DEFAULT_INTERVAL_MS, false);
    }

    public static void PrintTimed(String id, String message, long intervalMs) {
        PrintTimed(id, message, intervalMs, false);
    }

    public static void PrintTimed(String id, String message, boolean withStackTrace) {
        PrintTimed(id, message, DEFAULT_INTERVAL_MS, withStackTrace);
    }

    public static void PrintTimed(String id, String message, long intervalMs, boolean withStackTrace) {
        long now = System.currentTimeMillis();
        Long last = timedIds.get(id);
        if (last == null || now - last >= intervalMs) {
            timedIds.put(id, now);
            Print(message, withStackTrace);
        }
    }
}
