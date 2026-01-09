package net.mitisui.lagoa.logger;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mitisui.lagoa.Config;

import java.util.List;
import java.util.UUID;

public class ServerLoggerUtils {

    // [A1A] e [A2A] - Player matando Player ou Player morrendo sozinho
    // [A3A] e [A4A] - Pets (Cachorros, Gatos, Cavalos domesticados)
    // [A5A] - Invocação de Bosses (Wither e Ender Dragon)
    // [A6A] - Mob com Nametag
    // [B1B] - Login e Logout
    // [C1C] - Comandos executados por Players
    // [C2C] - Comandos executados por Command Blocks

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        String serverName = event.getServer().getWorldData().getLevelName();
        LogWriter.initialize(serverName);
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        String deathCause = source.getLocalizedDeathMessage(victim).getString();
        String pos = formatPosition(victim);
        String dim = victim.level().dimension().location().toString();

        // [A1A / A2A] - Mortes de Players
        if (victim instanceof Player playerVictim) {
            if (attacker instanceof Player playerAttacker && playerAttacker != playerVictim) {
                LogWriter.write("[A1A] " + playerAttacker.getName().getString() + " matou " +
                        playerVictim.getName().getString() + " {" + pos + "} {" + dim + "}");
            } else {
                LogWriter.write("[A2A] " + deathCause + " {" + pos + "} {" + dim + "}");
            }
            return;
        }

        // [A3A / A4A] - Mortes de Pets
        if (victim instanceof OwnableEntity pet && pet.getOwnerUUID() != null) {
            handlePetDeath(pet, victim, attacker, deathCause, pos);
            return;
        }

        // [A6A] - Mobs com Nametag
        if (victim.hasCustomName()) {
            LogWriter.write("[A6A] Mob Nomeado [" + victim.getCustomName().getString() +
                    "] morreu: " + deathCause + " em {" + pos + "} {" + dim + "}");
        }
    }

    @SubscribeEvent
    public static void onBossSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();

        boolean isBoss = entity instanceof WitherBoss ||
                entity instanceof EnderDragon ||
                entity.getType().is(TagKey.create(Registries.ENTITY_TYPE,
                        new ResourceLocation("c", "bosses")));

        if (isBoss) {
            String posicao = formatPosition(entity);
            String dimensao = entity.level().dimension().location().toString();
            double radius = getMaxWitherRadius();

            List<String> playersProximos = event.getLevel().players().stream()
                    .filter(p -> p.distanceToSqr(entity) <= (radius * radius))
                    .map(p -> p.getName().getString())
                    .toList();

            String listaPlayers = playersProximos.isEmpty() ?
                    "Nenhum detectado" : String.join(", ", playersProximos);

            LogWriter.write("[A5A] Boss Invocado: " + entity.getName().getString() +
                    " em {" + posicao + "} {" + dimensao + "} | Players na área de " +
                    (int)radius + " blocos: [" + listaPlayers + "]");
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Entity player = event.getEntity();
        String pos = formatPosition(player);
        String dim = player.level().dimension().location().toString();

        LogWriter.write("[B1B] LOGIN: " + player.getName().getString() +
                " em {" + pos + "} {" + dim + "}");
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Entity player = event.getEntity();
        String pos = formatPosition(player);
        String dim = player.level().dimension().location().toString();

        LogWriter.write("[B1B] LOGOUT: " + player.getName().getString() +
                " em {" + pos + "} {" + dim + "}");
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        try {
            String comando = event.getParseResults().getReader().getString();
            CommandSourceStack source = event.getParseResults().getContext().getSource();

            // Ignora mensagens privadas
            if (comando.startsWith("/msg") || comando.startsWith("/tell") ||
                    comando.startsWith("/w") || comando.startsWith("/whisper")) {
                return;
            }
                // Verifica se é um player real
            if (Config.PLAYER_COMMANDS_ENABLED.get()) {
                if (source.getEntity() instanceof Player player) {
                    String pos = formatPosition(player);
                    String dim = player.level().dimension().location().toString();

                    LogWriter.write("[C1C] COMANDO PLAYER: " + player.getName().getString() +
                            " executou [" + comando + "] em {" + pos + "} {" + dim + "}");
                }
            }
            // Command Block ou outra fonte
            else {
                if (Config.SERVER_COMMANDS_ENABLED.get()) {
                    String sourceName = source.getTextName();
                    String pos = formatPosition(source.getPosition());
                    String dim = source.getLevel() != null ?
                            source.getLevel().dimension().location().toString() : "unknown";

                    // Detecta tipo de origem
                    String sourceType = "DESCONHECIDO";
                    if (sourceName.contains("Command Block") || sourceName.contains("commandBlock")) {
                        sourceType = "COMMAND BLOCK";
                    } else if (sourceName.contains("Server") || sourceName.equals("Server")) {
                        sourceType = "SERVIDOR";
                    } else if (sourceName.contains("Function")) {
                        sourceType = "FUNCTION";
                    }

                    LogWriter.write("[C2C] COMANDO " + sourceType + ": [" + comando +
                            "] executado por [" + sourceName + "] em {" + pos + "} {" + dim + "}");
                }
            }
        } catch (Exception e) {
            LogWriter.write("[ERRO] Falha ao registrar comando: " + e.getMessage());
        }
    }

    // ===================== MÉTODOS AUXILIARES =====================

    private static void handlePetDeath(OwnableEntity pet, LivingEntity victim,
                                       Entity attacker, String deathCause, String pos) {
        UUID ownerUUID = pet.getOwnerUUID();
        Player ownerPlayer = victim.level().getServer() != null ?
                victim.level().getServer().getPlayerList().getPlayer(ownerUUID) : null;

        String ownerName = (ownerPlayer != null) ?
                ownerPlayer.getName().getString() : "Offline";
        String petName = victim.hasCustomName() ?
                victim.getCustomName().getString() : victim.getType().getDescriptionId();

        if (attacker instanceof Player playerAttacker) {
            LogWriter.write("[A4A] " + playerAttacker.getName().getString() +
                    " matou o pet [" + petName + "] de " + ownerName +
                    " (" + ownerUUID + ") em {" + pos + "}");
        } else {
            LogWriter.write("[A3A] Pet [" + petName + "] de " + ownerName +
                    " (" + ownerUUID + ") morreu: " + deathCause + " {" + pos + "}");
        }
    }

    private static String formatPosition(Entity entity) {
        return String.format("%.0f, %.0f, %.0f", entity.getX(), entity.getY(), entity.getZ());
    }

    private static String formatPosition(net.minecraft.world.phys.Vec3 pos) {
        return String.format("%.0f, %.0f, %.0f", pos.x, pos.y, pos.z);
    }

    private static double getMaxWitherRadius() {
        return Config.MAX_WITHER_RDIUS.get();
    }
}