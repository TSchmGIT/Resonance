package com.tschm.resonance.components.functional;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.tschm.resonance.codecs.codec.ListCodec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EchoConjurerComponent implements Component<ChunkStore> {
    public static final BuilderCodec<EchoConjurerComponent> CODEC;
    private static ComponentType<ChunkStore, EchoConjurerComponent> type;

    public static ComponentType<ChunkStore, EchoConjurerComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<ChunkStore, EchoConjurerComponent> newType) {
        if (type != null) {
            throw new IllegalStateException("EchoConjurerComponent ComponentType already set");
        }
        type = newType;
    }

    public List<String> npcIds = new ArrayList<>();
    public int selectedIndex = 0;

    public SimpleItemContainer lifeEssenceContainer = new SimpleItemContainer((short) 1);
    public String lifeEssenceItemId = "Ingredient_Life_Essence";
    public int lifeEssencePerSpawn = 1;

    public long essencePerSpawn = 50L;
    public double spawnRange = 8.0;
    public int maxNpcsInRange = 5;
    public long ticksPerSpawn = 200L;
    public long remainingTicks = 0L;

    @Nullable
    @Override
    public Component<ChunkStore> clone() {
        EchoConjurerComponent clone = new EchoConjurerComponent();
        clone.npcIds = new ArrayList<>(this.npcIds);
        clone.selectedIndex = this.selectedIndex;
        clone.lifeEssenceContainer = new SimpleItemContainer(this.lifeEssenceContainer);
        clone.lifeEssenceItemId = this.lifeEssenceItemId;
        clone.lifeEssencePerSpawn = this.lifeEssencePerSpawn;
        clone.essencePerSpawn = this.essencePerSpawn;
        clone.spawnRange = this.spawnRange;
        clone.maxNpcsInRange = this.maxNpcsInRange;
        clone.ticksPerSpawn = this.ticksPerSpawn;
        clone.remainingTicks = this.remainingTicks;
        return clone;
    }

    static {
        CODEC = BuilderCodec.builder(EchoConjurerComponent.class, EchoConjurerComponent::new)
                .append(new KeyedCodec<>("NpcIds", new ListCodec<>(BuilderCodec.STRING, ArrayList::new, false)),
                        (c, v) -> c.npcIds = v, c -> c.npcIds)
                .documentation("List of NPC IDs that this Conjurer can spawn; cycled via Echo Wand").add()
                .append(new KeyedCodec<>("SelectedIndex", BuilderCodec.INTEGER),
                        (c, v) -> c.selectedIndex = v, c -> c.selectedIndex)
                .documentation("Currently active index into NpcIds").addValidator(Validators.greaterThanOrEqual(0)).add()
                .append(new KeyedCodec<>("LifeEssenceContainer", SimpleItemContainer.CODEC),
                        (c, v) -> c.lifeEssenceContainer = v, c -> c.lifeEssenceContainer)
                .documentation("Input container holding Life Essence items consumed per spawn").add()
                .append(new KeyedCodec<>("LifeEssenceItemId", BuilderCodec.STRING),
                        (c, v) -> c.lifeEssenceItemId = v, c -> c.lifeEssenceItemId)
                .documentation("Item ID required as spawning fuel (default: Ingredient_Life_Essence)").add()
                .append(new KeyedCodec<>("LifeEssencePerSpawn", BuilderCodec.INTEGER),
                        (c, v) -> c.lifeEssencePerSpawn = v, c -> c.lifeEssencePerSpawn)
                .documentation("Amount of Life Essence items consumed per successful spawn")
                .addValidator(Validators.greaterThanOrEqual(0)).add()
                .append(new KeyedCodec<>("EssencePerSpawn", BuilderCodec.LONG),
                        (c, v) -> c.essencePerSpawn = v, c -> c.essencePerSpawn)
                .documentation("RE consumed per successful spawn")
                .addValidator(Validators.greaterThanOrEqual(0L)).add()
                .append(new KeyedCodec<>("SpawnRange", BuilderCodec.DOUBLE),
                        (c, v) -> c.spawnRange = v, c -> c.spawnRange)
                .documentation("Radius (blocks) for both NPC placement and cap counting")
                .addValidator(Validators.greaterThan(0d)).add()
                .append(new KeyedCodec<>("MaxNpcsInRange", BuilderCodec.INTEGER),
                        (c, v) -> c.maxNpcsInRange = v, c -> c.maxNpcsInRange)
                .documentation("Maximum NPCs of the active ID allowed within SpawnRange before spawning pauses")
                .addValidator(Validators.greaterThanOrEqual(0)).add()
                .append(new KeyedCodec<>("TicksPerSpawn", BuilderCodec.LONG),
                        (c, v) -> c.ticksPerSpawn = v, c -> c.ticksPerSpawn)
                .documentation("Ticks between spawn attempts")
                .addValidator(Validators.greaterThan(0L)).add()
                .append(new KeyedCodec<>("RemainingTicks", BuilderCodec.LONG),
                        (c, v) -> c.remainingTicks = v, c -> c.remainingTicks)
                .documentation("Ticks remaining until next spawn attempt").add()
                .build();
    }
}
