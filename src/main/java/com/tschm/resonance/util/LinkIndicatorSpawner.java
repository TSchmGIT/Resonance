package com.tschm.resonance.util;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.IComponentRegistry;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector;
import com.tschm.resonance.components.indicator.LinkIndicatorComponent;
import com.tschm.resonance.config.LinkIndicatorConfig;
import com.tschm.resonance.systems.LinkIndicatorSystem;

import java.util.UUID;

public class LinkIndicatorSpawner {

    /**
     * Spawns a self-destructing item entity that moves in a loop from {@code from} to {@code to},
     * carrying the particle spawner associated with the given {@link LinkIndicatorConfig.LinkType}.
     */
    public static void spawnIndicator(
            CommandBuffer<EntityStore> commandBuffer,
            Vector3i from,
            Vector3i to,
            LinkIndicatorConfig.LinkType type) {
        spawnIndicator(commandBuffer, from, to, type, LinkIndicatorConfig.DEFAULT_LIFETIME_TICKS);
    }

    /**
     * Spawns a self-destructing item entity that moves in a loop from {@code from} to {@code to},
     * carrying the particle spawner associated with the given {@link LinkIndicatorConfig.LinkType}.
     */
    public static void spawnIndicator(
            CommandBuffer<EntityStore> commandBuffer,
            Vector3i from,
            Vector3i to,
            LinkIndicatorConfig.LinkType type,
            long lifetime) {

        Vector3d startPos = new Vector3d(from.getX() + 0.5, from.getY() + 0.5, from.getZ() + 0.5);
        Vector3d endPos = new Vector3d(to.getX() + 0.5, to.getY() + 0.5, to.getZ() + 0.5);

        ItemStack stack = new ItemStack(type.itemId(), 1);
        stack.setOverrideDroppedItemAnimation(true);

        Holder<EntityStore> holder = ItemComponent.generateItemDrop(
                commandBuffer,
                stack,
                startPos,
                Vector3f.ZERO,
                0.0f, 0.0f, 0.0f);

        if (holder == null)
            return;

        holder.removeComponent(PhysicsValues.getComponentType());
        holder.removeComponent(Velocity.getComponentType());

        holder.putComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
        holder.putComponent(PreventItemMerging.getComponentType(), PreventItemMerging.INSTANCE);
        holder.addComponent(LinkIndicatorComponent.getComponentType(), new LinkIndicatorComponent(from, to));

        final World world = commandBuffer.getExternalData().getWorld();;
        final TimeResource timeResource = commandBuffer.getResource(TimeResource.getResourceType());
        final float lifetimeSeconds = (float) lifetime / world.getTps();
        holder.replaceComponent(DespawnComponent.getComponentType(), DespawnComponent.despawnInSeconds(timeResource, lifetimeSeconds));

        commandBuffer.addEntity(holder, AddReason.SPAWN);

        // Detailed debug message
        DebugHelper.Print("Spawned link indicator from " + from + " to " + to + " with item id " + type.itemId());
    }
}
