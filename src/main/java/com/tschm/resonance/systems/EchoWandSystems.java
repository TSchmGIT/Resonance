package com.tschm.resonance.systems;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.tschm.resonance.Resonance;
import com.tschm.resonance.components.essence.EssenceStorageComponent;
import com.tschm.resonance.ui.EchoWandUI;
import com.tschm.resonance.ui.EmptyHUD;
import com.tschm.resonance.util.ComponentHelper;
import com.tschm.resonance.util.DebugHelper;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EchoWandSystems {

    public static class HUDManager extends EntityTickingSystem<EntityStore> {

        @Override
        public void tick(float dt, int idx, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
            final Player player = archetypeChunk.getComponent(idx, Player.getComponentType());
            final PlayerRef playerRef = archetypeChunk.getComponent(idx, PlayerRef.getComponentType());
            final Ref<EntityStore> ref = archetypeChunk.getReferenceTo(idx);
            if (player == null || playerRef == null || !playerRef.isValid()) {
                DebugHelper.Print("Player or playerRef null");
                return;
            }
            LivingEntity entity = (LivingEntity) EntityUtils.getEntity(ref, commandBuffer);
            Inventory inventory = entity != null ? entity.getInventory() : null;
            ItemStack heldItem = inventory != null ? inventory.getActiveHotbarItem() : null;

            boolean hasWand = heldItem != null && "Echo_Wand".equals(heldItem.getItemId());
            Vector3i targetPos = hasWand ? TargetUtil.getTargetBlock(ref, 5, commandBuffer) : null;
            boolean hasTarget = targetPos != null;

            final boolean shouldCheckHud = hasWand && hasTarget;
            final Vector3i targetPosFinal = targetPos;

            commandBuffer.getExternalData().getWorld().execute(() -> {
                boolean showHud = shouldCheckHud;
                EssenceStorageComponent compStorage = null;
                if (showHud) {
                    compStorage = ComponentHelper.findComponentAt(commandBuffer.getExternalData().getWorld(), targetPosFinal, EssenceStorageComponent.getComponentType());
                    showHud = compStorage != null;
                }

                if (showHud) {
                    EchoWandUI ui = EchoWandUI.get(playerRef, EchoWandUI::new);
                    ui.updateHUDContent(compStorage);

                    if (Resonance.isMultipleHUDLoaded())
                        MultipleHUD.getInstance().setCustomHud(player, playerRef, "tschm:Resonance_EchoWand", ui);
                    else
                        player.getHudManager().setCustomHud(playerRef, ui);
                } else {
                    if (Resonance.isMultipleHUDLoaded())
                        MultipleHUD.getInstance().hideCustomHud(player, "tschm:Resonance_EchoWand");
                    else
                        player.getHudManager().setCustomHud(playerRef, new EmptyHUD(playerRef));
                }
            });
        }

        @NullableDecl
        @Override
        public Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }
}
