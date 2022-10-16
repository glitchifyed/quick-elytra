package net.glitchifyed.quick_elytra.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.glitchifyed.quick_elytra.config.ConfigHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

import static net.glitchifyed.quick_elytra.QuickElytra.CLIENT;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_EQUIP = "key.category.glitchifyed.quick_elytra";
    public static final String KEY_TOGGLE_EQUIP = "key.glitchifyed.quick_elytra.toggle_equip";
    public static final String KEY_EQUIP_TOTEM = "key.glitchifyed.quick_elytra.equip_totem";
    public static final String KEY_OPEN_CONFIG = "key.glitchifyed.quick_elytra.open_config";

    public static KeyBinding toggleEquip;
    public static KeyBinding equipTotem;
    public static KeyBinding openConfig;

    final static int ARMOUR_SLOT = 6;
    final static int OFFHAND_SLOT = 45;
    static boolean chestPressed;
    static boolean totemPressed;

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            swapChestplateSlot();

            quickTotemEquip();

            configPressed();
        });
    }

    static void configPressed() {
        if (openConfig.wasPressed()) {
            CLIENT.setScreen(ConfigHandler.createConfigMenu());
        }
    }

    static void swapChestplateSlot() {
        boolean togglePress = toggleEquip.isPressed();

        if (togglePress && !chestPressed) {
            chestPressed = true;

            int swapSlot = -1;

            DefaultedList<ItemStack> inventory = CLIENT.player.getInventory().main;

            ItemStack chestplate = CLIENT.player.getInventory().armor.get(2);
            boolean notWearing = chestplate.getItem() == Items.AIR;
            boolean wearingChestplate = isItemChestplate(chestplate);
            boolean wearingElytra = isItemElytra(chestplate);

            int highEnchantLevel = -1;
            int highestDurability = -1;

            for (ItemStack itemStack : inventory) {
                Item item = itemStack.getItem();

                if (item == Items.AIR) {
                    continue;
                }

                if (notWearing || wearingChestplate && isItemElytra(itemStack) || wearingElytra && isItemChestplate(itemStack)) {
                    int durability = itemStack.getMaxDamage();

                    if (durability > highestDurability) {
                        highestDurability = durability;
                    }
                }
            }

            for (ItemStack itemStack : inventory) {
                if (!isItemChestplateSlot(itemStack)) {
                    continue;
                }

                int i = inventory.indexOf(itemStack);

                if (notWearing || wearingChestplate && isItemElytra(itemStack) || wearingElytra && isItemChestplate(itemStack)) {
                    int level = getEnchantLevel(itemStack);
                    int durability = itemStack.getMaxDamage();

                    if (durability == highestDurability && level > highEnchantLevel) {
                        swapSlot = i;
                        highEnchantLevel = level;
                    }
                }
            }

            if (swapSlot != -1) {
                CLIENT.interactionManager.clickSlot(
                        CLIENT.player.playerScreenHandler.syncId,
                        ARMOUR_SLOT,
                        swapSlot,
                        SlotActionType.SWAP,
                        CLIENT.player
                );
            }
        }
        else if (!togglePress && chestPressed) {
            chestPressed = false;
        }
    }

    static void quickTotemEquip() {
        boolean togglePress = equipTotem.isPressed();

        if (togglePress && !totemPressed) {
            totemPressed = true;

            int swapSlot = -1;

            DefaultedList<ItemStack> inventory = CLIENT.player.getInventory().main;

            if (isItemTotem(CLIENT.player.getOffHandStack())) {
                return;
            }

            for (ItemStack itemStack : inventory) {
                if (!isItemTotem(itemStack)) {
                    continue;
                }

                swapSlot = inventory.indexOf(itemStack);

                break;
            }

            if (swapSlot != -1) {
                CLIENT.interactionManager.clickSlot(
                        CLIENT.player.playerScreenHandler.syncId,
                        OFFHAND_SLOT,
                        swapSlot,
                        SlotActionType.SWAP,
                        CLIENT.player
                );

                CLIENT.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f));
            }
        }
        else if (!togglePress && totemPressed) {
            totemPressed = false;
        }
    }

    public static void register() {
        toggleEquip = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_TOGGLE_EQUIP,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KEY_CATEGORY_EQUIP
        ));

        equipTotem = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_EQUIP_TOTEM,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                KEY_CATEGORY_EQUIP
        ));

        openConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_OPEN_CONFIG,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KEY_CATEGORY_EQUIP
        ));

        registerKeyInputs();
    }

    static int getEnchantLevel(ItemStack itemStack) {
        int level = 0;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(itemStack);

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            level += entry.getValue();
        }

        return level;
    }

    static boolean itemContainsString(Item item, String contains) {
        return item.getName().getString().toLowerCase().contains(contains);
    }

    static boolean itemStackContainsString(ItemStack itemStack, String contains) {
        return itemContainsString(itemStack.getItem(), contains);
    }

    static boolean isItemChestplate(ItemStack itemStack) {
        return MobEntity.getPreferredEquipmentSlot(itemStack) == EquipmentSlot.CHEST && !itemStackContainsString(itemStack, "elytra");
    }

    static boolean isItemElytra(ItemStack itemStack) {
        return MobEntity.getPreferredEquipmentSlot(itemStack) == EquipmentSlot.CHEST && itemStackContainsString(itemStack, "elytra");
    }

    static boolean isItemTotem(ItemStack itemStack) {
        return itemStackContainsString(itemStack, "totem");
    }

    static boolean isItemChestplateSlot(ItemStack itemStack) {
        return MobEntity.getPreferredEquipmentSlot(itemStack) == EquipmentSlot.CHEST;
    }
}
