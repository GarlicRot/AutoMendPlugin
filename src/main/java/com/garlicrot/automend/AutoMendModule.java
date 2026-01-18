package com.garlicrot.automend;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;

public class AutoMendModule extends ToggleableModule {

    private final BooleanSetting hotbarOnly =
            new BooleanSetting("HotbarOnly", "Only use items from hotbar (0–8)", true);

    private int currentOffhandOriginSlot = -1;

    public AutoMendModule() {
        super("AutoMend", "Auto-Mend automatically swaps mending tools to repair", ModuleCategory.PLAYER);
        this.registerSettings(hotbarOnly);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        currentOffhandOriginSlot = -1;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        currentOffhandOriginSlot = -1;
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onUpdate(EventUpdate event) {
        // touch event so the parameter is considered used
        if (event == null || mc == null || mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;

        ItemStack offhandStack = mc.player.getInventory().offhand.getFirst();

        if (isMendableDamaged(offhandStack)) {
            return;
        }

        if (currentOffhandOriginSlot >= 0 && currentOffhandOriginSlot < menu.slots.size()) {
            if (hasMendingEnchantment(offhandStack) && !offhandStack.isEmpty() && !offhandStack.isDamaged()) {
                mc.gameMode.handleInventoryMouseClick(
                        menu.containerId,
                        currentOffhandOriginSlot,
                        40,
                        ClickType.SWAP,
                        mc.player
                );

                currentOffhandOriginSlot = -1;
                offhandStack = mc.player.getInventory().offhand.getFirst();
            }
        } else {
            currentOffhandOriginSlot = -1;
        }

        if (isMendableDamaged(offhandStack)) {
            return;
        }

        int nextSlot = findNextDamagedMendingItem(menu);
        if (nextSlot == -1) {
            return;
        }

        ItemStack targetStack = menu.slots.get(nextSlot).getItem();
        if (!isMendableDamaged(targetStack)) {
            return;
        }

        mc.gameMode.handleInventoryMouseClick(
                menu.containerId,
                nextSlot,
                40,
                ClickType.SWAP,
                mc.player
        );

        currentOffhandOriginSlot = nextSlot;
    }

    private boolean isMendableDamaged(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.isDamageableItem()) return false;
        if (!hasMendingEnchantment(stack)) return false;
        return stack.isDamaged();
    }

    private boolean hasMendingEnchantment(ItemStack stack) {
        var entries = stack.getEnchantments().entrySet();
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : entries) {
            Holder<Enchantment> holder = entry.getKey();
            String id = holder.getRegisteredName();
            if (id.equals("minecraft:mending") || id.endsWith(":mending")) {
                return true;
            }
        }
        return false;
    }

    private int findNextDamagedMendingItem(AbstractContainerMenu menu) {
        if (mc.player == null) return -1;

        ItemStack targetStack = null;
        ItemStack mainHand = mc.player.getMainHandItem();

        if (hotbarOnly.getValue()) {
            for (int invIndex = 0; invIndex < 9; invIndex++) {
                ItemStack stack = mc.player.getInventory().getItem(invIndex);
                if (stack == mainHand) continue;
                if (isMendableDamaged(stack)) {
                    targetStack = stack;
                    break;
                }
            }
        } else {
            for (int invIndex = 0; invIndex < mc.player.getInventory().items.size(); invIndex++) {
                ItemStack stack = mc.player.getInventory().items.get(invIndex);
                if (stack == mainHand) continue;
                if (isMendableDamaged(stack)) {
                    targetStack = stack;
                    break;
                }
            }
        }

        if (targetStack == null) {
            return -1;
        }

        for (int slotIndex = 0; slotIndex < menu.slots.size(); slotIndex++) {
            if (menu.slots.get(slotIndex).getItem() == targetStack) {
                return slotIndex;
            }
        }

        return -1;
    }
}
