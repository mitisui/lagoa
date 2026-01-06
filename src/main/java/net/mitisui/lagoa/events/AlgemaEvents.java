package net.mitisui.lagoa.events;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mitisui.lagoa.Config;
import net.mitisui.lagoa.Lagoa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Lagoa.MODID)
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

    private static Integer getSlownessLevel() {
        return Config.SLOWNESS_LEVEL.get();
    }

    private static Boolean getEnableGlowing() {
        return Config.ENABLE_GLOWING.get();
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack item = event.getItemStack();

        // Verifica se é a algema via NBT
        if (item.hasTag() && item.getTag().getBoolean("IsAlgema")) {
            if (event.getTarget() instanceof Player target) {
                Player officer = event.getEntity();

                if (!event.getLevel().isClientSide) {
                    // Lógica de prender/soltar
                    if (isArrested(target.getUUID())) {
                        handleRelease((ServerPlayer) officer, (ServerPlayer) target);
                    } else {
                        handleArrest((ServerPlayer) officer, (ServerPlayer) target);
                    }
                }
                event.setCanceled(true);
            }
        }
    }

    public static void handleArrest(ServerPlayer officer, ServerPlayer target) {
        if (target == null) {
            officer.sendSystemMessage(Component.literal("§cJogador não encontrado"));
            return;
        }

        if (ARRESTED_PLAYERS.containsKey(target.getUUID())) {
            officer.sendSystemMessage(Component.literal("§eEste jogador já está preso"));
            return;
        }

        // Registrar prisão
        ARRESTED_PLAYERS.put(target.getUUID(), new ArrestData(officer.getUUID()));

        // Aplicar efeitos
        if (getEnableGlowing()) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        }
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Integer.MAX_VALUE, getSlownessLevel(), false, false));

        // Salvar UUID e nome do prisioneiro no NBT do item
        ItemStack algema = officer.getMainHandItem();
        CompoundTag nbt = algema.getOrCreateTag();
        nbt.putUUID("PrisonerUUID", target.getUUID());
        nbt.putString("PrisonerName", target.getName().getString());

        // Mensagens
        officer.sendSystemMessage(Component.literal("§aVocê prendeu " + target.getName().getString() + "!"));
        target.sendSystemMessage(Component.literal("§cVocê foi preso por " + officer.getName().getString() + "!"));
    }

    public static void handleRelease(ServerPlayer officer, ServerPlayer target) {
        if (target == null) {
            officer.sendSystemMessage(Component.literal("§cJogador não encontrado!"));
            return;
        }

        ArrestData data = ARRESTED_PLAYERS.get(target.getUUID());
        if (data == null) {
            officer.sendSystemMessage(Component.literal("§eEste jogador não está preso!"));
            return;
        }

        // Verificar se é o oficial que prendeu
        if (!data.officerUUID.equals(officer.getUUID())) {
            officer.sendSystemMessage(Component.literal("§cApenas o oficial que prendeu pode soltar!"));
            return;
        }

        // Soltar jogador
        ARRESTED_PLAYERS.remove(target.getUUID());

        // Remover efeitos
        target.removeEffect(MobEffects.GLOWING);
        target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

        // Remover dados do NBT
        ItemStack algema = officer.getMainHandItem();
        if (algema.hasTag()) {
            CompoundTag nbt = algema.getTag();
            nbt.remove("PrisonerUUID");
            nbt.remove("PrisonerName");
        }

        // Mensagens
        officer.sendSystemMessage(Component.literal("§aVocê soltou " + target.getName().getString() + "!"));
        target.sendSystemMessage(Component.literal("§aVocê foi solto por " + officer.getName().getString() + "!"));
    }

    public static void handleTeleport(ServerPlayer officer, ServerPlayer target, BlockPos pos, String dimensionStr) {
        if (target == null) {
            officer.sendSystemMessage(Component.literal("§cJogador não encontrado!"));
            return;
        }

        ArrestData data = ARRESTED_PLAYERS.get(target.getUUID());
        if (data == null) {
            officer.sendSystemMessage(Component.literal("§eEste jogador não está preso!"));
            return;
        }

        if (!data.officerUUID.equals(officer.getUUID())) {
            officer.sendSystemMessage(Component.literal("§cApenas o oficial que prendeu pode teleportar!"));
            return;
        }

        // Teleportar para a dimensão e posição corretas
        ResourceKey<Level> dimensionKey = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                new ResourceLocation(dimensionStr)
        );

        ServerLevel targetLevel = officer.getServer().getLevel(dimensionKey);
        if (targetLevel != null) {
            target.teleportTo(targetLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    target.getYRot(), target.getXRot());

            officer.sendSystemMessage(Component.literal("§aJogador teleportado!"));
            target.sendSystemMessage(Component.literal("§eVocê foi teleportado!"));
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (Config.PREVENT_BLOCK_BREAK.get() && event.getPlayer() instanceof ServerPlayer player) {
            if (ARRESTED_PLAYERS.containsKey(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§cVocê não pode quebrar blocos enquanto está preso!"));
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (Config.PREVENT_BLOCK_PLACE.get() && event.getEntity() instanceof ServerPlayer player) {
            if (ARRESTED_PLAYERS.containsKey(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§cVocê não pode colocar blocos enquanto está preso!"));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (Config.PREVENT_INTERACTIONS.get() && event.getEntity() instanceof ServerPlayer player) {
            if (ARRESTED_PLAYERS.containsKey(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§cAção bloqueada enquanto preso!"));
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (Config.PREVENT_ITEM_USE.get() && event.getEntity() instanceof ServerPlayer player) {
            if (ARRESTED_PLAYERS.containsKey(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§cVocê não pode usar itens enquanto está preso!"));
            }
        }
    }

    @SubscribeEvent
    public static void onItemDrop(ItemTossEvent event) {
        if (Config.PREVENT_ITEM_DROP.get() && event.getPlayer() instanceof ServerPlayer player) {
            if (ARRESTED_PLAYERS.containsKey(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§cVocê não pode dropar itens enquanto está preso!"));
            }
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (Config.PREVENT_ITEM_PICKUP.get() && event.getEntity() instanceof ServerPlayer player) {
            if (ARRESTED_PLAYERS.containsKey(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (Config.PREVENT_ATTACK.get() && event.getEntity() instanceof ServerPlayer player) {
            if (ARRESTED_PLAYERS.containsKey(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§cVocê não pode atacar enquanto está preso!"));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Primeiro verifica se NÃO é uma algema
        ItemStack item = event.getItemStack();
        if (item.hasTag() && item.getTag().getBoolean("IsAlgema")) {
            return; // Deixa o outro handler cuidar disso
        }

        // Agora verifica se deve bloquear a interação
        if (Config.PREVENT_INTERACTIONS.get() && event.getEntity() instanceof ServerPlayer player) {
            if (ARRESTED_PLAYERS.containsKey(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§cVocê não pode interagir enquanto está preso!"));
            }
        }
    }

    public static void onPlayerLogout(Player player) {
        ARRESTED_PLAYERS.remove(player.getUUID());
    }

    public static boolean isArrested(UUID playerUUID) {
        return ARRESTED_PLAYERS.containsKey(playerUUID);
    }

    public static ArrestData getArrestData(UUID playerUUID) {
        return ARRESTED_PLAYERS.get(playerUUID);
    }
}