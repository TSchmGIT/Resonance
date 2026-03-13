package com.tschm.resonance.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.components.indicator.LinkIndicatorComponent;
import com.tschm.resonance.util.DebugHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class LinkIndicatorSystem extends EntityTickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int idx,
                     @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                     @NonNullDecl Store<EntityStore> store,
                     @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        LinkIndicatorComponent comp = archetypeChunk.getComponent(idx, LinkIndicatorComponent.getComponentType());
        assert comp != null;

        comp.progress += comp.speed;
        if (comp.progress >= 1.0f) {
            comp.progress = 0.0f;
        }

        double x = comp.startPos.getX() + 0.5 + (comp.endPos.getX() - comp.startPos.getX()) * comp.progress;
        double y = comp.startPos.getY() + 0.5 + (comp.endPos.getY() - comp.startPos.getY()) * comp.progress;
        double z = comp.startPos.getZ() + 0.5 + (comp.endPos.getZ() - comp.startPos.getZ()) * comp.progress;

        TransformComponent transform = archetypeChunk.getComponent(idx, TransformComponent.getComponentType());
        if (transform != null) {
            transform.setPosition(new Vector3d(x, y, z));
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return LinkIndicatorComponent.getComponentType();
    }
}
