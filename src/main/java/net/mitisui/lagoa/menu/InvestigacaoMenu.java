package net.mitisui.lagoa.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.mitisui.lagoa.Config;


public class InvestigacaoMenu extends ChestMenu {
    private final ServerPlayer targetPlayer;
    private final Player viewer;
    private final SimpleContainer container;
    private final boolean isViewOnly;

    public InvestigacaoMenu(int windowId, Inventory viewerInventory, ServerPlayer target) {
        super(MenuType.GENERIC_9x6, windowId, viewerInventory, new SimpleContainer(54), 6);

        this.targetPlayer = target;
        this.viewer = viewerInventory.player;
        this.container = (SimpleContainer) this.getContainer();
        this.isViewOnly = true;

        atualizarInventario();

        tornarSlotsReadOnly();
    }

    private void atualizarInventario() {
        if (targetPlayer == null) return;

        Inventory targetInv = targetPlayer.getInventory();

        if (isViewOnly) {
            container.setItem(0, targetInv.armor.get(3).copy()); // Helmet
            container.setItem(1, targetInv.armor.get(2).copy()); // Chestplate
            container.setItem(2, targetInv.armor.get(1).copy()); // Leggings
            container.setItem(3, targetInv.armor.get(0).copy()); // Boots
            container.setItem(4, targetInv.offhand.get(0).copy()); // Offhand

            for (int i = 0; i < 9; i++) {
                container.setItem(9 + i, targetInv.items.get(i).copy());
            }

            for (int i = 9; i < 36; i++) {
                container.setItem(9 + i, targetInv.items.get(i).copy());
            }
        } else {
            sincronizarComInventarioAlvo();
        }
    }

    private void sincronizarComInventarioAlvo() {
        if (targetPlayer == null) return;

        Inventory targetInv = targetPlayer.getInventory();

        // Sincroniza armadura e offhand
        container.setItem(0, targetInv.armor.get(3));
        container.setItem(1, targetInv.armor.get(2));
        container.setItem(2, targetInv.armor.get(1));
        container.setItem(3, targetInv.armor.get(0));
        container.setItem(4, targetInv.offhand.get(0));

        // Sincroniza hotbar
        for (int i = 0; i < 9; i++) {
            container.setItem(9 + i, targetInv.items.get(i));
        }

        // Sincroniza inventário principal
        for (int i = 9; i < 36; i++) {
            container.setItem(9 + i, targetInv.items.get(i));
        }
    }

    private void tornarSlotsReadOnly() {
        for (int i = 0; i < 54; i++) {
            Slot originalSlot = this.slots.get(i);
            this.slots.set(i, new ReadOnlySlot(
                    originalSlot.container,
                    originalSlot.getContainerSlot(),
                    originalSlot.x,
                    originalSlot.y
            ));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (targetPlayer == null || !targetPlayer.isAlive()) {
            return false;
        }

        atualizarInventario();

        return player.distanceToSqr(targetPlayer) < Config.LUPA_RAIO_DE_USO.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        if (index < 54) {
            return ItemStack.EMPTY;
        }

        return super.quickMoveStack(player, index);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    private static class ReadOnlySlot extends Slot {
        public ReadOnlySlot(net.minecraft.world.Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setByPlayer(ItemStack stack) {
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);
        }
    }
}