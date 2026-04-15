package com.tschm.resonance.interactions.echo_wand.handler;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.functional.EchoConjurerComponent;
import com.tschm.resonance.interactions.echo_wand.EchoWandInteraction;
import com.tschm.resonance.interactions.echo_wand.WandInteractionHandler;
import com.tschm.resonance.metadata.EchoWandMetaData;
import com.tschm.resonance.util.DebugHelper;

import java.util.Optional;

/**
 * Cycles the active NPC selection on an Echo Conjurer block.
 * Only runs when the wand is unbound (state = None) to avoid conflict with RE linking.
 */
public class EchoConjurerHandler implements WandInteractionHandler {

    @Override
    public boolean isApplicable(EchoWandInteraction.WandTarget target) {
        return target.echoConjurer() != null;
    }

    @Override
    public Optional<String> handle(World world, Vector3i targetPos, EchoWandMetaData metaData,
                                   EchoWandInteraction.WandTarget target, CommandBuffer<EntityStore> commandBuffer,
                                   InteractionContext context) {
        if (metaData.getWandState() != EchoWandMetaData.EchoWandState.None)
            return Optional.of("Wand is bound — cannot cycle Echo Conjurer selection");

        EchoConjurerComponent comp = target.echoConjurer();
        if (comp == null)
            return Optional.of("No Echo Conjurer component found");

        if (comp.npcIds == null || comp.npcIds.isEmpty()) {
            DebugHelper.Print("Echo Conjurer: No NPCs configured");
            return Optional.empty();
        }

        comp.selectedIndex = Math.floorMod(comp.selectedIndex + 1, comp.npcIds.size());
        DebugHelper.Print("Echo Conjurer: " + comp.npcIds.get(comp.selectedIndex));
        return Optional.empty();
    }
}
