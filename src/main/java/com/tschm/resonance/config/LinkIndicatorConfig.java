package com.tschm.resonance.config;

public class LinkIndicatorConfig {

    // Item IDs — must match resource filenames under Server/Item/Items/
    public static final String ITEM_SEND          = "Resonance_Link_Indicator_Send";
    public static final String ITEM_RECEIVE        = "Resonance_Link_Indicator_Receive";
    public static final String ITEM_BIDIRECTIONAL  = "Resonance_Link_Indicator_Bidirectional";

    /** Ticks until the indicator entity removes itself (~10 s at 30 TPS). */
    public static final int DEFAULT_LIFETIME_TICKS = 30 * 3;

    /** Progress increment per tick. 0.015 → ~67 ticks per src→dst crossing. */
    public static final float DEFAULT_SPEED = 0.05f;

    public enum LinkType {
        SEND,
        RECEIVE,
        BIDIRECTIONAL;

        public String itemId() {
            return switch (this) {
                case SEND          -> ITEM_SEND;
                case RECEIVE       -> ITEM_RECEIVE;
                case BIDIRECTIONAL -> ITEM_BIDIRECTIONAL;
            };
        }
    }
}
