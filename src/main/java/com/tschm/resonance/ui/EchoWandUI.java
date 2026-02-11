package com.tschm.resonance.ui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.util.HUDProvider;

import javax.annotation.Nonnull;

public class EchoWandUI extends HUDProvider<EchoWandUI> {

    private long currentRE = 0L;
    private long maxRE = 0L;

    public EchoWandUI(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("HUD/Echo_Wand_HUD.ui");

        uiCommandBuilder.set("#ValueText.TextSpans", Message.raw(String.format("%,d / %,d RE", currentRE, maxRE)));
        Anchor widthAnchor = new Anchor();
        widthAnchor.setWidth(Value.of((int)(((float)currentRE / maxRE) * 396)));
        widthAnchor.setLeft(Value.of(0));
        widthAnchor.setTop(Value.of(0));
        widthAnchor.setBottom(Value.of(0));
        uiCommandBuilder.setObject("#Fill.Anchor", widthAnchor);
    }

    public void updateHUDContent(EssenceStorageComponent compStorage) {
        currentRE = compStorage.getEssenceStored();
        maxRE = compStorage.getMaxEssenceStored();
    }
}
