/// MIT LICENSE CREDIT to Mrbysco/ItemFrames https://github.com/Mrbysco/ItemFrames/
package com.tschm.resonance.util;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.AssetIconProperties;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ItemHelper {
    public static Ref<EntityStore> remakeItemEntity(
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> oldRef,
            @Nullable ItemStack stack,
            int yawDegrees
    ) {
        // Remove existing variant-specific components
        removeVariantComponents(store, oldRef);

        // Copy the existing entity (includes transform, network, uuid, flags, etc.)
        Holder<EntityStore> holder = store.copyEntity(oldRef);

        float scale = 0.5F;

        if (stack != null) {
            ItemStack newStack = new ItemStack(stack.getItemId(), 1);
            Item item = newStack.getItem();
            AssetIconProperties properties = item.getIconProperties();
            if (properties != null) {
                scale = properties.getScale();
            }

            newStack.setOverrideDroppedItemAnimation(true);
            holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(newStack));

            Model model = getItemModel(item);
            if (model != null) {
                String modelId = getItemModelId(item);
                if (modelId != null) {
                    holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
                    holder.addComponent(
                            PersistentModel.getComponentType(),
                            new PersistentModel(new Model.ModelReference(modelId, scale, null, true))
                    );
                }
            } else if (item.hasBlockType()) {
                holder.addComponent(BlockEntity.getComponentType(), new BlockEntity(newStack.getItemId()));
                holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(scale * 2.0F));

                HeadRotation headRotation = store.getComponent(oldRef, HeadRotation.getComponentType());
                if (headRotation != null) {
                    Vector3f oldRotation = headRotation.getRotation();
                    Vector3f rotation = new Vector3f();
                    rotation.setPitch(oldRotation.getPitch());
                    rotation.addRotationOnAxis(Axis.Y, yawDegrees + 180);

                    holder.putComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
                }
            } else {
                holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(scale));
            }
        }

        // Remove old entity AFTER copying
        store.removeEntity(oldRef, RemoveReason.REMOVE);

        // Respawn cleanly
        return store.addEntity(holder, AddReason.SPAWN);
    }

    public static void removeVariantComponents(Store<EntityStore> store, Ref<EntityStore> ref) {
        // variant-specific visuals/identity
        store.removeComponentIfExists(ref, ModelComponent.getComponentType());
        store.removeComponentIfExists(ref, PersistentModel.getComponentType());
        store.removeComponentIfExists(ref, BlockEntity.getComponentType());
        // scaling / network identity
        store.removeComponentIfExists(ref, EntityScaleComponent.getComponentType());

        // Remove item component last
        store.removeComponentIfExists(ref, ItemComponent.getComponentType());
    }

    @Nullable
    public static Model getItemModel(@Nonnull Item item) {
        String s = getItemModelId(item);
        if (s == null) {
            return null;
        } else {
            ModelAsset modelasset = ModelAsset.getAssetMap().getAsset(s);
            return modelasset != null ? Model.createStaticScaledModel(modelasset, 0.5F) : null;
        }
    }

    @Nullable
    public static String getItemModelId(@Nonnull Item item) {
        String s = item.getModel();
        if (s == null && item.hasBlockType()) {
            BlockType blocktype = BlockType.getAssetMap().getAsset(item.getId());
            if (blocktype != null && blocktype.getCustomModel() != null) {
                s = blocktype.getCustomModel();
            }
        }

        return s;
    }

    public static UUID spawnStaticItemModel(CommandBuffer<EntityStore> store, Holder<EntityStore> holder, Vector3d position, Vector3f rotation, int yawDegrees, ItemStack stack, float scale) {

        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));

        holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
        holder.addComponent(PreventItemMerging.getComponentType(), PreventItemMerging.INSTANCE);
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
        UUID uuid = UUID.randomUUID();
        holder.putComponent(UUIDComponent.getComponentType(), new UUIDComponent(uuid));
        holder.ensureComponent(PropComponent.getComponentType());
        holder.ensureComponent(PrefabCopyableComponent.getComponentType());

        if (stack != null) {
            ItemStack newStack = new ItemStack(stack.getItemId(), 1);
            Item item = newStack.getItem();

            newStack.setOverrideDroppedItemAnimation(true);
            holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(newStack));

            Model model = getItemModel(item);
            if (model != null) {
                String modelId = getItemModelId(item);
                if (modelId != null) {
                    holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
                    holder.addComponent(
                            PersistentModel.getComponentType(),
                            new PersistentModel(new Model.ModelReference(modelId, scale, null, true))
                    );
                }
            } else if (item.hasBlockType()) {
                holder.addComponent(BlockEntity.getComponentType(), new BlockEntity(newStack.getItemId()));
                holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(scale * 2.0F));

                HeadRotation headRotation = holder.getComponent(HeadRotation.getComponentType());
                if (headRotation != null) {
                    Vector3f oldRotation = headRotation.getRotation();
                    Vector3f newRotation = new Vector3f();
                    newRotation.setPitch(oldRotation.getPitch());
                    newRotation.addRotationOnAxis(Axis.Y, yawDegrees + 180);

                    holder.putComponent(HeadRotation.getComponentType(), new HeadRotation(newRotation));
                }
            } else {
                holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(scale));
            }
        }

        store.addEntity(holder, AddReason.SPAWN);
        return uuid;
    }

    public static void spawnItem(CommandBuffer<EntityStore> store, Ref<EntityStore> ref, ItemStack stack) {
        Vector3d position = null;

        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent != null) {
            position = transformComponent.getPosition();
        } else {
            DebugHelper.Print("No Transform Component found");
            return;
        }
        Holder<EntityStore> holder = ItemComponent.generateItemDrop(
                store,
                stack,
                position,
                Vector3f.ZERO,
                0.0F,
                0.0F,
                0.0F
        );
        if (holder != null) {
            ItemComponent itemcomponent = holder.getComponent(ItemComponent.getComponentType());
            if (itemcomponent != null) {
                itemcomponent.setPickupDelay(1.5F);
            }

            store.addEntity(holder, AddReason.SPAWN);
        }
    }
}