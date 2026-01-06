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
    // [C1C] - Comandos

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

        String pos = String.format("%.0f, %.0f, %.0f", victim.getX(), victim.getY(), victim.getZ());
        String dim = victim.level().dimension().location().toString();

        // [A1A / A2A]
        if (victim instanceof Player playerVictim) {
            if (attacker instanceof Player playerAttacker && playerAttacker != playerVictim) {
                LogWriter.write("[A1A] " + playerAttacker.getName().getString() + " matou " + playerVictim.getName().getString() + " {" + pos + "} {" + dim + "}");
            } else {
                LogWriter.write("[A2A] " + deathCause + " {" + pos + "} {" + dim + "}");
            }
            return;
        }

        // [A3A / A4A]
        if (victim instanceof OwnableEntity pet && pet.getOwnerUUID() != null) {
            UUID ownerUUID = pet.getOwnerUUID();
            Player ownerPlayer = victim.level().getServer() != null ?
                    victim.level().getServer().getPlayerList().getPlayer(ownerUUID) : null;

            String ownerName = (ownerPlayer != null) ? ownerPlayer.getName().getString() : "Offline";
            String petName = victim.hasCustomName() ? victim.getCustomName().getString() : victim.getType().getDescriptionId();

            if (attacker instanceof Player p) {
                // [A4A]
                LogWriter.write("[A4A] " + p.getName().getString() + " matou o pet [" + petName + "] de " +
                        ownerName + " (" + ownerUUID + ") em {" + pos + "}");
            } else {
                // [A3A]
                LogWriter.write("[A3A] Pet [" + petName + "] de " + ownerName + " (" + ownerUUID + ") morreu: " + deathCause + " {" + pos + "}");
            }
            return;
        }

        // [A6A]
        if (victim.hasCustomName()) {
            LogWriter.write("[A6A] Mob Nomeado [" + victim.getCustomName().getString() + "] morreu: " + deathCause + " em {" + pos + "} {" + dim + "}");
        }
    }

    @SubscribeEvent
    public static void onBossSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();

        boolean isBoss = entity instanceof WitherBoss ||
                entity instanceof EnderDragon ||
                entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("c", "bosses"))); // no java 21 marca como erro -> é só ignorar que seria o mesmo que ResourveLocation.parse ou fromnameandpath

        if (isBoss) {
            String posicao = String.format("%.0f, %.0f, %.0f", entity.getX(), entity.getY(), entity.getZ());
            String dimensao = entity.level().dimension().location().toString();

            List<String> playersProximos = event.getLevel().players().stream()
                    .filter(p -> p.distanceToSqr(entity) <= (getMaxWitherRadius() * getMaxWitherRadius())) // agora é configurável!
                    .map(p -> p.getName().getString())
                    .toList();

            String ListaPlayersProximos = playersProximos.isEmpty() ? "Nenhum detectado" : String.join(", ", playersProximos);

            LogWriter.write("[A5A] Boss Invocado: " + entity.getName().getString() +
                    " em {" + posicao + "} {" + dimensao + "} | Players na área de " + getMaxWitherRadius()/getMaxWitherRadius() + " blocos: [" + ListaPlayersProximos + "]");
        }
    }

    // [B1B / B2B]
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Entity player = event.getEntity();
        String dimensao = player.level().dimension().location().toString();

        String posicao = String.format("%.0f, %.0f, %.0f", player.getX(), player.getY(), player.getZ());
        LogWriter.write("[B1B] LOGIN: " + player.getName().getString() + " em {" + posicao + "}" + " em {" + dimensao + "}" );
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent evento) {
        Entity p = evento.getEntity();
        String posicao = String.format("%.0f, %.0f, %.0f", p.getX(), p.getY(), p.getZ());
        LogWriter.write("[B1B] LOGOUT: " + p.getName().getString() + " em {" + posicao + "}");
    }

    // C1C
    @SubscribeEvent
    public static void onCommand(CommandEvent evento) {
        String comando = evento.getParseResults().getReader().getString();
        String player = evento.getParseResults().getContext().getSource().getTextName();

        if (!comando.startsWith("/msg") && !comando.startsWith("/tell")) {
            LogWriter.write("[C1C] COMANDO: " + player + " executou [" + comando + "]");
        }
    }

    // gets

    public static double getMaxWitherRadius() {
        return Config.MAX_WITHER_RDIUS.get() * Config.MAX_WITHER_RDIUS.get();
    }
}
