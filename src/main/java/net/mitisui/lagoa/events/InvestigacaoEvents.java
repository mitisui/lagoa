package net.mitisui.lagoa.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mitisui.lagoa.Config;
import net.mitisui.lagoa.Lagoa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Lagoa.MODID)
public class InvestigacaoEvents {

    private static final Map<UUID, InvestigationData> ACTIVE_INVESTIGATIONS = new HashMap<>();

    public static class InvestigationData {
        public UUID targetUUID;
        public long startTime;

        public InvestigationData(UUID targetUUID) {
            this.targetUUID = targetUUID;
            this.startTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        if (!item.hasTag() || !item.getTag().getBoolean("IsLupa")) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        event.setCanceled(true);
        investigar((ServerPlayer) player);
    }

    private static void investigar(ServerPlayer investigator) {
        ServerLevel level = investigator.serverLevel();

        Vec3 eyePos = investigator.getEyePosition();
        Vec3 lookVec = investigator.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(10.0));

        AABB searchBox = investigator.getBoundingBox()
                .expandTowards(lookVec.scale(10.0))
                .inflate(2.0);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, investigator, eyePos, endPos,
                searchBox, entity -> entity instanceof ServerPlayer &&
                        !entity.isSpectator() &&
                        entity != investigator
        );

        if (entityHit != null && entityHit.getEntity() instanceof ServerPlayer target) {
            abrirMenuInvestigacao(investigator, target);
        }

        investigator.getCooldowns().addCooldown(investigator.getMainHandItem().getItem(), 20);
    }

    public static void abrirMenuInvestigacao(ServerPlayer investigator, ServerPlayer target) {
        if (investigator == target) {
            investigator.sendSystemMessage(
                    Component.literal("§cVocê não pode investigar a si mesmo!")
            );
            return;
        }

        ACTIVE_INVESTIGATIONS.put(investigator.getUUID(), new InvestigationData(target.getUUID()));

        SimpleContainer container = new SimpleContainer(54);
        preencherInventarioReadOnly(container, target);

        investigator.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("§b§lInvestigando: §f" + target.getName().getString());
            }

            @Override
            public AbstractContainerMenu createMenu(
                    int windowId, Inventory inventory, Player player) {
                return new ReadOnlyChestMenu(windowId, inventory, container, target);
            }
        });

        target.displayClientMessage(
                Component.literal("§eVocê está sendo investigado por " + investigator.getName().getString())
                        .withStyle(ChatFormatting.YELLOW),
                true
        );
    }

    private static void preencherInventarioReadOnly(SimpleContainer container, ServerPlayer target) {
        Inventory targetInv = target.getInventory();

        // Linha 1 Armadura e offhand
        container.setItem(0, targetInv.armor.get(3).copy()); // Capacete
        container.setItem(1, targetInv.armor.get(2).copy()); // Peitoral
        container.setItem(2, targetInv.armor.get(1).copy()); // Calça
        container.setItem(3, targetInv.armor.get(0).copy()); // Botas
        container.setItem(4, targetInv.offhand.get(0).copy()); // Offhand

        // Linha 2 Hotbar
        for (int i = 0; i < 9; i++) {
            container.setItem(9 + i, targetInv.items.get(i).copy());
        }

        // Linhas 3-4 Inventário principal
        for (int i = 9; i < 27; i++) {
            container.setItem(9 + i, targetInv.items.get(i).copy());
        }
    }

    // Menu Read-Only simples
    private static class ReadOnlyChestMenu extends ChestMenu {
        private final ServerPlayer target;

        public ReadOnlyChestMenu(int windowId, Inventory viewerInventory,
                                 SimpleContainer container, ServerPlayer target) {
            super(MenuType.GENERIC_9x6, windowId, viewerInventory, container, 6);
            this.target = target;

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
            if (target == null || !target.isAlive()) {
                return false;
            }

            double maxDistance = Config.LUPA_RAIO_DE_USO.get();
            return player.distanceToSqr(target) < (maxDistance );
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
        }
    }

    private static class ReadOnlySlot extends Slot {
        public ReadOnlySlot(Container container, int slot, int x, int y) {
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
    }
}