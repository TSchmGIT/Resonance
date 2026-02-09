package com.tschm.resonance;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.tschm.resonance.components.*;
import com.tschm.resonance.events.OreGenChunkEvent;
import com.tschm.resonance.interactions.*;
import com.tschm.resonance.systems.EchoWandSystems;
import com.tschm.resonance.systems.EssenceGeneratorSystems;
import com.tschm.resonance.systems.EssenceStorageSystems;
import com.tschm.resonance.systems.RitualStoneSystems;

import javax.annotation.Nonnull;

public class Resonance extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public Resonance(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        // Interactions
        this.getCodecRegistry(Interaction.CODEC).register("ritual_stone_interaction", RitualStoneInteraction.class, RitualStoneInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("essence_storage_clear", ClearEssenceInteraction.class, ClearEssenceInteraction.CODEC);

        this.getEventRegistry().registerGlobal(EventPriority.NORMAL, ChunkPreLoadProcessEvent.class, OreGenChunkEvent::onChunkPreLoadProcess);

        var regChunk = this.getChunkStoreRegistry();
        var regEntity = this.getEntityStoreRegistry();

        // Components
        RitualStoneComponent.setComponentType(regChunk.registerComponent(RitualStoneComponent.class, "Resonance_RitualStone", RitualStoneComponent.CODEC));
        EssenceStorageBlockComponent.setComponentType(regChunk.registerComponent(EssenceStorageBlockComponent.class, "Resonance_EssenceStorage", EssenceStorageBlockComponent.CODEC));
        EssenceStorageVisualizerComponent.setComponentType(regChunk.registerComponent(EssenceStorageVisualizerComponent.class, "Resonance_EssenceStorageVisualizer", EssenceStorageVisualizerComponent.CODEC));
        EssenceGeneratorComponent.setComponentType(regChunk.registerComponent(EssenceGeneratorComponent.class, "Resonance_EssenceGenerator", EssenceGeneratorComponent.CODEC));

        EssenceStorageEntityComponent.setComponentType(regEntity.registerComponent(EssenceStorageEntityComponent.class, "Resonance_EssenceStorage", EssenceStorageEntityComponent.CODEC));

        // Systems
        regEntity.registerSystem(new RitualStoneSystems.BreakSystem());
        regEntity.registerSystem(new EssenceStorageSystems.VisualizerPlacedChunk());
        regEntity.registerSystem(new EchoWandSystems.HUDManager());

        regChunk.registerSystem(new EssenceGeneratorSystems.GeneratorTicks());
        regChunk.registerSystem(new EssenceStorageSystems.EssenceStorageVisualizerSystem());
    }
}