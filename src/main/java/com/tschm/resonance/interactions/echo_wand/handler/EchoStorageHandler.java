package com.tschm.resonance.interactions.echo_wand.handler;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.storage.EchoStorageComponent;
import com.tschm.resonance.config.LinkIndicatorConfig;
import com.tschm.resonance.interactions.echo_wand.EchoWandInteraction;
import com.tschm.resonance.interactions.echo_wand.WandInteractionHandler;
import com.tschm.resonance.metadata.EchoWandMetaData;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.LinkIndicatorSpawner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * First click: caches the source EchoStorage. Second click after EchoStorage: creates the
 * directional or bidirectional Echo Storage link.
 */
public class EchoStorageHandler implements WandInteractionHandler {
    @Override
    public boolean isApplicable(EchoWandInteraction.WandTarget target) {
        return target.echoStorage() != null;
    }

    @Override
    public Optional<String> handle(World world, Vector3i targetPos, EchoWandMetaData metaData, EchoWandInteraction.WandTarget target, CommandBuffer<EntityStore> commandBuffer) {
        EchoStorageComponent comp = target.echoStorage();
        assert comp != null;

        return switch (metaData.getWandState()) {
            case None -> handleState_None(targetPos, metaData, comp);
            case Bound -> handleState_Bound(world, targetPos, metaData, comp, commandBuffer);
        };
    }

    private static Optional<String> handleState_None(Vector3i targetPos, EchoWandMetaData metaData, @Nonnull EchoStorageComponent comp) {
        if (!comp.canSend())
            return Optional.of("This echo storage cannot retrieve essence!");
        if (!metaData.bindTo(targetPos, EchoWandMetaData.EchoWandBindingType.EchoStorage))
            return Optional.of("Cannot bind to echo storage!");
        DebugHelper.Print("Cached Echo Storage at " + targetPos);
        return Optional.empty();
    }

    private static Optional<String> handleState_Bound(World world, Vector3i targetPos, EchoWandMetaData metaData, @Nonnull EchoStorageComponent comp, CommandBuffer<EntityStore> commandBuffer) {
        if (metaData.getBindingType() != EchoWandMetaData.EchoWandBindingType.EchoStorage)
            return Optional.of("Can only bind this Echo Storage to another Echo Storage!");

        Vector3i boundPos = metaData.getBoundPosition();
        assert boundPos != null;

        EchoStorageComponent boundEchoStorage = ComponentHelper.findComponentAt(world, boundPos, EchoStorageComponent.getComponentType());
        if (boundEchoStorage == null)
            return Optional.of("No echo storage at bound position " + boundPos);
        if (!boundEchoStorage.canSend())
            return Optional.of("Bound Echo storage cannot retrieve essence!");
        if (comp != null && !comp.canReceive())
            return Optional.of("Selected Echo storage cannot store essence!");

        createEchoStorageBinding(world, boundEchoStorage, boundPos, comp, targetPos);
        LinkIndicatorSpawner.spawnIndicator(commandBuffer, boundPos, targetPos, LinkIndicatorConfig.LinkType.BIDIRECTIONAL);
        // Note: metaData is intentionally NOT reset here (pre-existing behaviour)
        return Optional.empty();
    }

    /**
     * Creates a directional Echo Storage binding from {@code sender} → {@code receiverPos}, cleaning up any
     * pre-existing bindings on both sides first. When {@code receiver} is non-null the link is bidirectional;
     * when null the sender links to an ItemContainerState target instead.
     */
    private static void createEchoStorageBinding(
            World world,
            EchoStorageComponent sender, Vector3i senderPos,
            @Nullable EchoStorageComponent receiver, Vector3i receiverPos) {
        // Detach sender's current outbound link
        if (sender.getBoundPositionSend() != null) {
            EchoStorageComponent oldReceiver = ComponentHelper.findComponentAt(
                    world, sender.getBoundPositionSend(), EchoStorageComponent.getComponentType());
            if (oldReceiver != null)
                oldReceiver.setBoundPositionReceive(null);
        }
        sender.setBoundPositionSend(receiverPos);

        if (receiver != null) {
            // Detach receiver's current inbound link
            if (receiver.getBoundPositionReceive() != null) {
                EchoStorageComponent oldSender = ComponentHelper.findComponentAt(
                        world, receiver.getBoundPositionReceive(), EchoStorageComponent.getComponentType());
                if (oldSender != null)
                    oldSender.setBoundPositionSend(null);
            }
            receiver.setBoundPositionReceive(senderPos);
            DebugHelper.Print("Bound two Echo Storages!");
        } else {
            DebugHelper.Print("Bound Echo Storage to ItemContainerState at " + receiverPos);
        }
    }
}
