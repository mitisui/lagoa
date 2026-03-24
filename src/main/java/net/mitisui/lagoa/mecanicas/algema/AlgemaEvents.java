package net.mitisui.lagoa.mecanicas.algema;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.mitisui.lagoa.Config;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class AlgemaEvents {

    private static final Map<UUID, ArrestData> ARRESTED_PLAYERS = new HashMap<>();

    public static class ArrestData {
        public UUID officerUUID;
        public long arrestTime;

        public ArrestData(UUID officerUUID) {
            this.officerUUID = officerUUID;
            this.arrestTime = System.currentTimeMillis();
        }
    }

    // -------------------------------------------------------------------------
    // Utilitários NBT — 1.21.1 usa DataComponents.CUSTOM_DATA
    // -------------------------------------------------------------------------

    /** Lê o CustomData do item como CompoundTag. Retorna tag vazia se não existir. */
    private static CompoundTag readNbt(ItemStack item) {
        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    /** Edita o CustomData do item via consumer, salvando de volta no componente. */
    private static void editNbt(ItemStack item, Consumer<CompoundTag> editor) {
        CompoundTag tag = readNbt(item);
        editor.accept(tag);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Remove uma chave do CustomData do item. */
    private static void removeNbtKey(ItemStack item, String key) {
        editNbt(item, tag -> tag.remove(key));
    }

    private static boolean isAlgema(ItemStack item) {
        return readNbt(item).getBoolean("IsAlgema");
    }

    // -------------------------------------------------------------------------
    // Prender / Soltar ao clicar em player com algema na mão
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!isAlgema(event.getItemStack())) return;
        if (!(event.getTarget() instanceof ServerPlayer target)) return;

        ServerPlayer officer = (ServerPlayer) event.getEntity();

        if (isArrested(target.getUUID())) {
            handleRelease(officer, target, false);
        } else {
            handleArrest(officer, target);
        }
        event.setCanceled(true);
    }

    // -------------------------------------------------------------------------
    // Teleportar preso ao clicar num bloco com algema
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onRightClickGround(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer officer)) return;
        if (!isAlgema(event.getItemStack())) return;

        CompoundTag nbt = readNbt(event.getItemStack());
        if (!nbt.contains("PrisonerUUID")) return;

        UUID targetUUID = nbt.getUUID("PrisonerUUID");
        ServerPlayer target = officer.getServer().getPlayerList().getPlayer(targetUUID);

        BlockPos posAlvo = event.getPos().above();
        String dimensaoAtual = officer.level().dimension().location().toString();

        handleTeleport(officer, target, posAlvo, dimensaoAtual);
        event.setCanceled(true);
    }

    // -------------------------------------------------------------------------
    // Lógica de prisão
    // -------------------------------------------------------------------------
    public static void handleArrest(ServerPlayer officer, ServerPlayer target) {
        if (target == null) {
            officer.displayClientMessage(Component.literal("§cJogador não encontrado!"), true);
            return;
        }
        if (ARRESTED_PLAYERS.containsKey(target.getUUID())) {
            officer.displayClientMessage(Component.literal("§eEste jogador já está preso!"), true);
            return;
        }

        ARRESTED_PLAYERS.put(target.getUUID(), new ArrestData(officer.getUUID()));

        if (Config.ENABLE_GLOWING.get()) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING,
                    Integer.MAX_VALUE, 0, false, false));
        }
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                Integer.MAX_VALUE, Config.SLOWNESS_LEVEL.get(), false, false));
        target.addEffect(new MobEffectInstance(MobEffects.UNLUCK,
                Integer.MAX_VALUE, 1, false, false));

        // Grava UUID do preso no CustomData da algema
        editNbt(officer.getMainHandItem(), nbt -> {
            nbt.putUUID("PrisonerUUID", target.getUUID());
            nbt.putString("PrisonerName", target.getName().getString());
        });

        officer.displayClientMessage(
                Component.literal("§aVocê prendeu §f" + target.getName().getString() + "§a!"), true);
        target.displayClientMessage(
                Component.literal("§cVocê foi preso por §f" + officer.getName().getString() + "§c!"), true);
    }

    public static void handleRelease(ServerPlayer officer, ServerPlayer target) {
        handleRelease(officer, target, false);
    }

    public static void handleRelease(ServerPlayer officer, ServerPlayer target, boolean bypassOfficerCheck) {
        if (target == null) {
            officer.displayClientMessage(Component.literal("§cJogador não encontrado!"), true);
            return;
        }

        ArrestData data = ARRESTED_PLAYERS.get(target.getUUID());
        if (data == null) {
            officer.displayClientMessage(Component.literal("§eEste jogador não está preso!"), true);
            return;
        }

        if (!bypassOfficerCheck && !data.officerUUID.equals(officer.getUUID())) {
            officer.displayClientMessage(
                    Component.literal("§cApenas o oficial que prendeu pode soltar!"), true);
            return;
        }

        ARRESTED_PLAYERS.remove(target.getUUID());

        target.removeEffect(MobEffects.GLOWING);
        target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        target.removeEffect(MobEffects.UNLUCK);

        // Limpa as chaves da algema
        removeNbtKey(officer.getMainHandItem(), "PrisonerUUID");
        removeNbtKey(officer.getMainHandItem(), "PrisonerName");

        officer.displayClientMessage(
                Component.literal("§aVocê soltou §f" + target.getName().getString() + "§a!"), true);
        target.displayClientMessage(Component.literal("§aVocê foi solto!"), true);
    }

    public static void handleTeleport(ServerPlayer officer, ServerPlayer target,
                                      BlockPos pos, String dimensionStr) {
        if (target == null) {
            officer.displayClientMessage(Component.literal("§cJogador não encontrado!"), true);
            return;
        }
        ArrestData data = ARRESTED_PLAYERS.get(target.getUUID());
        if (data == null) {
            officer.displayClientMessage(Component.literal("§eEste jogador não está preso!"), true);
            return;
        }
        if (!data.officerUUID.equals(officer.getUUID())) {
            officer.displayClientMessage(
                    Component.literal("§cApenas o oficial que prendeu pode teleportar!"), true);
            return;
        }

        ResourceKey<Level> dimensionKey = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                ResourceLocation.tryParse(dimensionStr)
        );

        ServerLevel targetLevel = officer.getServer().getLevel(dimensionKey);
        if (targetLevel != null) {
            target.teleportTo(targetLevel,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    target.getYRot(), target.getXRot());
            officer.displayClientMessage(
                    Component.literal("§aVocê teleportou §f" + target.getName().getString()
                            + " §apara a sua posição!"), true);
        }
    }

    // -------------------------------------------------------------------------
    // Restrições para presos
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!Config.PREVENT_BLOCK_BREAK.get()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!isArrested(player.getUUID())) return;

        event.setCanceled(true);
        player.displayClientMessage(
                Component.literal("§cVocê não pode quebrar blocos enquanto está preso!"), true);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!Config.PREVENT_BLOCK_PLACE.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isArrested(player.getUUID())) return;

        event.setCanceled(true);
        player.displayClientMessage(
                Component.literal("§cVocê não pode colocar blocos enquanto está preso!"), true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (isAlgema(event.getItemStack())) return;
        if (!Config.PREVENT_INTERACTIONS.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isArrested(player.getUUID())) return;

        event.setCanceled(true);
        player.displayClientMessage(Component.literal("§cAção bloqueada enquanto preso!"), true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!Config.PREVENT_ITEM_USE.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isArrested(player.getUUID())) return;

        event.setCanceled(true);
        player.displayClientMessage(
                Component.literal("§cVocê não pode usar itens enquanto está preso!"), true);
    }

    @SubscribeEvent
    public static void onItemDrop(ItemTossEvent event) {
        if (!Config.PREVENT_ITEM_DROP.get()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!isArrested(player.getUUID())) return;

        event.setCanceled(true);
        player.displayClientMessage(
                Component.literal("§cVocê não pode dropar itens enquanto está preso!"), true);
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (!Config.PREVENT_ITEM_PICKUP.get()) return;

        Player player = event.getPlayer();

        if (isArrested(player.getUUID())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onTickBlockMovement(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isArrested(player.getUUID())) return;

        player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!Config.PREVENT_ATTACK.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isArrested(player.getUUID())) return;

        event.setCanceled(true);
        player.displayClientMessage(
                Component.literal("§cVocê não pode atacar enquanto está preso!"), true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isAlgema(event.getItemStack())) return;
        if (!Config.PREVENT_INTERACTIONS.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isArrested(player.getUUID())) return;

        event.setCanceled(true);
        player.displayClientMessage(
                Component.literal("§cVocê não pode interagir enquanto está preso!"), true);
    }

    @SubscribeEvent
    public static void onDamageWhileArrested(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer target)) return;
        if (!isArrested(target.getUUID())) return;

        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            ArrestData data = ARRESTED_PLAYERS.get(target.getUUID());
            if (data != null && attacker.getUUID().equals(data.officerUUID)) return;
            event.setCanceled(true);
        }
    }

    // -------------------------------------------------------------------------
    // Ao deslogar: remove da lista de presos
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof Player player) {
            ARRESTED_PLAYERS.remove(player.getUUID());
        }
    }

    // -------------------------------------------------------------------------
    // Utilitários públicos
    // -------------------------------------------------------------------------
    public static boolean isArrested(UUID playerUUID) {
        return ARRESTED_PLAYERS.containsKey(playerUUID);
    }

    public static ArrestData getArrestData(UUID playerUUID) {
        return ARRESTED_PLAYERS.get(playerUUID);
    }
}