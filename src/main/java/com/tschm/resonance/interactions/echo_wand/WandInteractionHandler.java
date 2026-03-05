package com.tschm.resonance.interactions.echo_wand;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.tschm.resonance.metadata.EchoWandMetaData;

import java.util.Optional;

/**
 * A self-contained handler for one category of wand interaction.
 * Handlers are tried in registration order; the first to return {@link Optional#empty()} wins.
 */
public interface WandInteractionHandler {
    boolean isApplicable(EchoWandInteraction.WandTarget target);

    /**
     * Returns {@link Optional#empty()} on success, or a user-facing error string on failure.
     */
    Optional<String> handle(World world, Vector3i targetPos, EchoWandMetaData metaData, EchoWandInteraction.WandTarget target);
}