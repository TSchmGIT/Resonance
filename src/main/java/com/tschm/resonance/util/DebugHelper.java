package com.tschm.resonance.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;

public class DebugHelper {
    public static void Print(String message){
        Universe.get().sendMessage(Message.raw(message));
    }
}
