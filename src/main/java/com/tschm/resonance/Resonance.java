package com.tschm.resonance;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.tschm.resonance.components.RitualStoneComponent;
import com.tschm.resonance.events.OreGenChunkEvent;
import com.tschm.resonance.interactions.RitualStoneInteraction;
import com.tschm.resonance.systems.RitualStoneSystems;

import javax.annotation.Nonnull;

public class Resonance extends JavaPlugin {

    public Resonance(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        this.getCodecRegistry(Interaction.CODEC).register("ritual_stone_interaction", RitualStoneInteraction.class, RitualStoneInteraction.CODEC);

        this.getEventRegistry().registerGlobal(EventPriority.NORMAL, ChunkPreLoadProcessEvent.class, OreGenChunkEvent::onChunkPreLoadProcess);

        var ritualStoneComponentType = this.getChunkStoreRegistry().registerComponent(RitualStoneComponent.class, "Resonance_RitualStone", RitualStoneComponent.CODEC);
        RitualStoneComponent.setComponentType(ritualStoneComponentType);

        this.getEntityStoreRegistry().registerSystem(new RitualStoneSystems.BreakSystem());
    }
}