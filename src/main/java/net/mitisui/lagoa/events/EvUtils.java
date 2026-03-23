package net.mitisui.lagoa.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.mitisui.lagoa.mecanicas.area.AreaManager;
import net.mitisui.lagoa.mecanicas.area.AreaRegion;
import net.mitisui.lagoa.mecanicas.cargos.Cargo;
import net.mitisui.lagoa.mecanicas.cargos.CargoManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class EvUtils {
    // MECÂNICA DE INVULNERABILIDADE
    @SubscribeEvent
    public static void aoReceberDano(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Cargo cargo = CargoManager.getCargoDoPlayer(player.getUUID());
            if (cargo != null && !cargo.levaDano) {
                event.getEntity().setInvulnerable(true);
            }
        }
    }

    // PERSISTÊNCIA DE ATRIBUTOS
    @SubscribeEvent
    public static void aoTrocarDeDimensaoOuRenacer(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Cargo cargo = CargoManager.getCargoDoPlayer(player.getUUID());
            if (cargo != null) {
                CargoManager.aplicarBuffs(player, cargo);
            }
        }
    }

    //ATUALIZAÇÃO AO LOGAR
    @SubscribeEvent
    public static void aoEntrarNoServidor(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Cargo cargo = CargoManager.getCargoDoPlayer(player.getUUID());
            if (cargo != null) {
                CargoManager.aplicarBuffs(player, cargo);
            }
        }
    }

    @SubscribeEvent
    public static void checkSpawn(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity() instanceof Monster) {
            BlockPos pos = new BlockPos((int)event.getX(), (int)event.getY(), (int)event.getZ());

            AreaRegion area = AreaManager.getAreaNaPosicao(pos);
            if (area != null && !area.spawnaHostis) {
                event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL); // Bloqueia o spawn
            }
        }
    }

    @SubscribeEvent
    public static void aoQuebrarBloco(BlockEvent.BreakEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        AreaRegion area = AreaManager.getAreaNaPosicao(event.getPos());

        if (area != null && !area.podeQuebrar) {
            if (!area.excecoes.contains(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void aoColocarBloco(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AreaRegion area = AreaManager.getAreaNaPosicao(event.getPos());

            // Se a área existe e NÃO permite colocar blocos
            if (area != null && !area.podeColocar) {
                // Se o player NÃO for uma exceção, cancela a ação
                if (!area.excecoes.contains(player.getUUID())) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
