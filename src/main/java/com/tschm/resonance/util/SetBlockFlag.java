package com.tschm.resonance.util;

/**
 * Bit flags used by ChunkAccessor#setBlock(..., int settings).
 *
 * These meanings are inferred from the current implementation:
 * - If a flag is named SKIP_..., it means "when this bit is set, the code path is skipped".
 * - If a flag is named FORCE_..., it means "when this bit is set, the code path is forced/enabled".
 */
public enum SetBlockFlag {
    /**
     * If set, do NOT notify BlockState changes (the last parameter of setState becomes false).
     * Code: this.setState(..., (settings & 1) == 0)
     */
    NO_STATE_NOTIFY(1),

    /**
     * If set, skip creation/assignment of BlockEntity / BlockState entirely.
     * Code: if ((settings & 2) == 0) { ... create entity/state ... }
     */
    SKIP_STATE_AND_BLOCK_ENTITY(2),

    /**
     * If set, do NOT spawn block particles for build/break.
     * Code: if ((settings & 4) == 0) { ... sendBlockParticle ... }
     */
    SKIP_PARTICLES(4),

    /**
     * Internal/recursive protection flag: "do not place filler blocks for this block".
     * Code: if ((settings & 8) == 0 && filler == 0) { ... set filler blocks ... }
     *
     * The code sets this when recursively placing filler blocks:
     * settingsWithoutSetFiller = settings | 8
     */
    SKIP_SET_FILLER_BLOCKS(8),

    /**
     * Internal/recursive protection flag: "do not break old filler blocks".
     * Code: if ((settings & 16) == 0) { ... break old filler blocks ... }
     *
     * The code sets this when breaking old filler blocks:
     * settingsWithoutFiller = settings | 8 | 16
     */
    SKIP_BREAK_OLD_FILLER_BLOCKS(16),

    /**
     * When breaking a block (oldBlock != 0 && id == 0), choose Physics particles instead of Break particles.
     * Code: BlockParticleEvent particleType = (settings & 32) != 0 ? Physics : Break;
     */
    BREAK_PARTICLES_AS_PHYSICS(32),

    /**
     * Force invalidation of the block in the BlockSection even if nothing changed.
     * Code: if (changed || (settings & 64) != 0) { ... }
     *       if ((settings & 64) != 0) { blockSection.invalidateBlock(...) }
     */
    FORCE_INVALIDATE_BLOCK(64),

    /**
     * If set, perform block updates on all filler blocks after placement.
     * Code: if ((settings & 256) != 0) { ... performBlockUpdate(...) }
     */
    DO_BLOCK_UPDATES(256),

    /**
     * If set, skip heightmap updates (chunk height tracking) even if y is above old height.
     * Code: if ((settings & 512) == 0 && oldHeight <= y) { ... updateHeight/setHeight ... }
     */
    SKIP_HEIGHT_UPDATE(512);

    public final int mask;

    SetBlockFlag(int mask) {
        this.mask = mask;
    }

    /** Convenience: test whether a flag is set in the bitfield. */
    public boolean in(int settings) {
        return (settings & mask) != 0;
    }

    /** Convenience: set this flag in the bitfield. */
    public int set(int settings) {
        return settings | mask;
    }

    /** Convenience: clear this flag in the bitfield. */
    public int clear(int settings) {
        return settings & ~mask;
    }

    /** Convenience: compose multiple flags into one settings int. */
    public static int of(SetBlockFlag... flags) {
        int v = 0;
        for (SetBlockFlag f : flags) v |= f.mask;
        return v;
    }
}
