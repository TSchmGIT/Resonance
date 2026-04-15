package com.tschm.resonance.interactions.echo_wand.handler;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.config.LinkIndicatorConfig;
import com.tschm.resonance.interactions.echo_wand.EchoWandInteraction;
import com.tschm.resonance.interactions.echo_wand.WandInteractionHandler;
import com.tschm.resonance.metadata.EchoWandMetaData;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import com.tschm.resonance.util.LinkIndicatorSpawner;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * First click: caches the EssenceStorage position (unused flow — no second-click handler consumes
 * an EssenceStorage binding type). Second click after Generator: toggles the generator↔storage link.
 */
public class EssenceStorageHandler implements WandInteractionHandler {
    @Override
    public boolean isApplicable(EchoWandInteraction.WandTarget target) {
        return target.essenceStorage() != null;
    }

    @Override
    public Optional<String> handle(World world, Vector3i targetPos, EchoWandMetaData metaData, EchoWandInteraction.WandTarget target, CommandBuffer<EntityStore> commandBuffer, InteractionContext context) {
        EssenceStorageComponent compStorage = target.essenceStorage();
        assert compStorage != null;

        return switch (metaData.getWandState()) {
            case None -> handleState_None(targetPos, metaData, compStorage);
            case Bound -> handleState_Bound(world, targetPos, metaData, compStorage, commandBuffer);
        };
    }

    @Nonnull
    private static Optional<String> handleState_Bound(World world, Vector3i targetPos, EchoWandMetaData metaData, EssenceStorageComponent compStorage, CommandBuffer<EntityStore> commandBuffer) {
        if (!compStorage.canReceive)
            return Optional.of("Target storage cannot receive essence!");

        String typeError = metaData.requireBoundType(EchoWandMetaData.EchoWandBindingType.EssenceStorage);
        if (typeError != null)
            return Optional.of(typeError);

        Vector3i boundPos = metaData.getBoundPosition();
        assert boundPos != null;

        var sourceStorage = ComponentHelper.findComponentAt(world, boundPos, EssenceStorageComponent.getComponentType());
        if (sourceStorage == null) {
            DebugHelper.Print("Cannot find essence storage at source position " + boundPos);
            metaData.resetBinding();
            return Optional.of("No essence storage at source position " + boundPos);
        }

        var receivers = sourceStorage.getBoundReceiverList();
        if (receivers.contains(targetPos)) {
            receivers.remove(targetPos);
            compStorage.getBoundSenderList().remove(boundPos);
            DebugHelper.Print("Unlinked storages");
        } else {
            receivers.add(targetPos);
            compStorage.getBoundSenderList().add(boundPos);
            DebugHelper.Print("Linked storage at " + boundPos + " to " + targetPos);
            LinkIndicatorSpawner.spawnIndicator(commandBuffer, boundPos, targetPos, LinkIndicatorConfig.LinkType.SEND);
        }

        metaData.resetBinding();
        return Optional.empty();
    }

    @Nonnull
    private static Optional<String> handleState_None(Vector3i targetPos, EchoWandMetaData metaData, EssenceStorageComponent compStorage) {
        if (!compStorage.canSend)
            return Optional.of("This storage cannot send essence!");

        if (metaData.bindTo(targetPos, EchoWandMetaData.EchoWandBindingType.EssenceStorage)) {
            DebugHelper.Print("Bound wand to storage " + targetPos);
            return Optional.empty();
        }
        metaData.resetBinding();
        return Optional.of("Cannot bind to storage!");
    }
}
