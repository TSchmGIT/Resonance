package com.tschm.resonance.interactions.echo_wand.handler;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.functional.HarmonicProcessorComponent;
import com.tschm.resonance.interactions.echo_wand.EchoWandInteraction;
import com.tschm.resonance.interactions.echo_wand.WandInteractionHandler;
import com.tschm.resonance.metadata.EchoWandMetaData;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.TransferMode;

import java.util.Optional;

/**
 * Configures per-side item transfer mode on Harmonic Pulverizer/Reconstructor blocks.
 * Single-click: cycles the transfer mode (Disabled → Pull → Push → Both) for the clicked face.
 * Only activates when wand is unbound (state = None) to avoid conflict with RE linking.
 */
public class HarmonicProcessorHandler implements WandInteractionHandler {

    @Override
    public boolean isApplicable(EchoWandInteraction.WandTarget target) {
        return target.harmonicProcessor() != null;
    }

    @Override
    public Optional<String> handle(World world, Vector3i targetPos, EchoWandMetaData metaData,
                                   EchoWandInteraction.WandTarget target, CommandBuffer<EntityStore> commandBuffer,
                                   InteractionContext context) {
        // Only intercept when wand is unbound — let EssenceStorageHandler handle Bound state for RE linking
        if (metaData.getWandState() != EchoWandMetaData.EchoWandState.None)
            return Optional.of("Wand is bound — cannot configure sides");

        InteractionSyncData clientState = context.getClientState();
        BlockFace face = clientState != null ? clientState.blockFace : null;
        if (face == null)
            return Optional.of("No block face detected");

        if (target.harmonicProcessor() != null) {
            TransferMode newMode = cycleSide(target.harmonicProcessor(), face);
            DebugHelper.Print(face + ": " + newMode.name());
            return Optional.empty();
        }

        return Optional.of("No Harmonic component found");
    }

    private static TransferMode cycleSide(HarmonicProcessorComponent comp, BlockFace face) {
        TransferMode current = getSide(comp, face);
        TransferMode next = current.next();
        setSide(comp, face, next);
        return next;
    }

    private static TransferMode getSide(HarmonicProcessorComponent comp, BlockFace face) {
        return switch (face) {
            case BlockFace.Up -> comp.sideUp;
            case BlockFace.Down -> comp.sideDown;
            case BlockFace.North -> comp.sideNorth;
            case BlockFace.South -> comp.sideSouth;
            case BlockFace.West -> comp.sideWest;
            case BlockFace.East -> comp.sideEast;
            default -> TransferMode.Disabled;
        };
    }

    private static void setSide(HarmonicProcessorComponent comp, BlockFace face, TransferMode mode) {
        switch (face) {
            case BlockFace.Up -> comp.sideUp = mode;
            case BlockFace.Down -> comp.sideDown = mode;
            case BlockFace.North -> comp.sideNorth = mode;
            case BlockFace.South -> comp.sideSouth = mode;
            case BlockFace.West -> comp.sideWest = mode;
            case BlockFace.East -> comp.sideEast = mode;
        }
    }
}
