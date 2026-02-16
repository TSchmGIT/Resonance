package com.tschm.resonance.systems.generators;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.components.essence.generators.VerdantAttunementStoneComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class VerdantAttunementStoneSystems extends EntityEventSystem<ChunkStore, PlaceBlockEvent> {

    protected VerdantAttunementStoneSystems() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer, @NonNullDecl PlaceBlockEvent placeBlockEvent) {

    }

    @NullableDecl
    @Override
    public Query<ChunkStore> getQuery() {
        return VerdantAttunementStoneComponent.getComponentType();
    }
}
