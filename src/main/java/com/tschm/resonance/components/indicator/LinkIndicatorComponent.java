package com.tschm.resonance.components.indicator;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tschm.resonance.config.LinkIndicatorConfig;

public class LinkIndicatorComponent implements Component<EntityStore> {

    public static final BuilderCodec<LinkIndicatorComponent> CODEC;

    private static ComponentType<EntityStore, LinkIndicatorComponent> type;

    public static ComponentType<EntityStore, LinkIndicatorComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<EntityStore, LinkIndicatorComponent> t) {
        type = t;
    }

    public Vector3i startPos;
    public Vector3i endPos;
    public float progress;
    public float speed;

    public LinkIndicatorComponent() {
    }

    public LinkIndicatorComponent(Vector3i startPos, Vector3i endPos) {
        this.startPos = startPos;
        this.endPos   = endPos;
        this.progress = 0.0f;
        this.speed    = LinkIndicatorConfig.DEFAULT_SPEED;
    }

    public LinkIndicatorComponent(Vector3i startPos, Vector3i endPos, float speed) {
        this.startPos = startPos;
        this.endPos   = endPos;
        this.progress = 0.0f;
        this.speed    = speed;
    }

    @Override
    public Component<EntityStore> clone() {
        LinkIndicatorComponent copy = new LinkIndicatorComponent();
        copy.startPos = this.startPos;
        copy.endPos   = this.endPos;
        copy.progress = this.progress;
        copy.speed    = this.speed;
        return copy;
    }

    static {
        CODEC = BuilderCodec.builder(LinkIndicatorComponent.class, LinkIndicatorComponent::new)
                .append(new KeyedCodec<>("StartPos", Vector3i.CODEC), (c, v) -> c.startPos = v, c -> c.startPos).add()
                .append(new KeyedCodec<>("EndPos",   Vector3i.CODEC), (c, v) -> c.endPos   = v, c -> c.endPos).add()
                .append(new KeyedCodec<>("Progress", BuilderCodec.FLOAT), (c, v) -> c.progress = v, c -> c.progress).add()
                .append(new KeyedCodec<>("Speed",    BuilderCodec.FLOAT), (c, v) -> c.speed    = v, c -> c.speed).add()
                .build();
    }
}
