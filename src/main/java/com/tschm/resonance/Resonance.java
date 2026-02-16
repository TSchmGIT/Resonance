package com.tschm.resonance;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.tschm.resonance.components.*;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.components.essence.EssenceStorageVisualizerComponent;
import com.tschm.resonance.components.essence.generators.SolarAttunementStoneComponent;
import com.tschm.resonance.components.essence.generators.VerdantAttunementStoneComponent;
import com.tschm.resonance.events.OreGenChunkEvent;
import com.tschm.resonance.interactions.*;
import com.tschm.resonance.systems.EchoWandSystems;
import com.tschm.resonance.systems.EssenceStorageSystems;
import com.tschm.resonance.systems.RitualStoneSystems;
import com.tschm.resonance.systems.generators.SolarAttunementStoneSystems;

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
        var interactionRegistry = this.getCodecRegistry(Interaction.CODEC);
        interactionRegistry.register("ritual_stone_interaction", RitualStoneInteraction.class, RitualStoneInteraction.CODEC);
        interactionRegistry.register("essence_storage_clear", ClearEssenceInteraction.class, ClearEssenceInteraction.CODEC);
        interactionRegistry.register("echo_wand_interaction", EchoWandInteraction.class, EchoWandInteraction.CODEC);

        this.getEventRegistry().registerGlobal(EventPriority.NORMAL, ChunkPreLoadProcessEvent.class, OreGenChunkEvent::onChunkPreLoadProcess);

        var regChunk = this.getChunkStoreRegistry();
        var regEntity = this.getEntityStoreRegistry();

        // Components
        RitualStoneComponent.setComponentType(regChunk.registerComponent(RitualStoneComponent.class, "Resonance_RitualStone", RitualStoneComponent.CODEC));
        EssenceStorageComponent.setComponentType(regChunk.registerComponent(EssenceStorageComponent.class, "Resonance_EssenceStorage", EssenceStorageComponent.CODEC));
        EssenceStorageVisualizerComponent.setComponentType(regChunk.registerComponent(EssenceStorageVisualizerComponent.class, "Resonance_EssenceStorageVisualizer", EssenceStorageVisualizerComponent.CODEC));

        SolarAttunementStoneComponent.setComponentType(regChunk.registerComponent(SolarAttunementStoneComponent.class, "Resonance_SolarAttunementStoneComponent", SolarAttunementStoneComponent.CODEC));
        VerdantAttunementStoneComponent.setComponentType(regChunk.registerComponent(VerdantAttunementStoneComponent.class, "Resonance_VerdantAttunementStoneComponent", VerdantAttunementStoneComponent.CODEC));

        // Systems
        regEntity.registerSystem(new RitualStoneSystems.BreakSystem());
        regEntity.registerSystem(new EssenceStorageSystems.VisualizerPlacedChunk());
        regEntity.registerSystem(new EchoWandSystems.HUDManager());

        regChunk.registerSystem(new EssenceStorageSystems.EssenceStorageVisualizerSystem());
        regChunk.registerSystem(new SolarAttunementStoneSystems.GeneratorTicks());
    }
}