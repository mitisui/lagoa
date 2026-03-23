package net.mitisui.lagoa.utils;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.mitisui.lagoa.mecanicas.area.AreaManager;
import net.mitisui.lagoa.mecanicas.area.AreaRegion;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Integração opcional com Iron's Spells 'n Spellbooks.
 * Esta classe só é carregada se o mod estiver presente.
 * Registrada condicionalmente em Lagoa.java via ModList.get().isLoaded("irons_spellbooks")
 */
public class IronsSpellsIntegration {

    @SubscribeEvent
    public static void onSpellPreCast(SpellPreCastEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        AreaRegion area = AreaManager.getAreaNaPosicao(player.blockPosition());

        // Bloqueio por ÁREA
        if (area != null && area.bloqueiaMagia) {
            if (!area.excecoes.contains(player.getUUID())) {
                cancelarMagia(event, player, "§cUma força mística nesta área impede sua magia!");
                return;
            }
        }

        // Bloqueio por EFEITO
        if (player.hasEffect(MobEffects.UNLUCK) || player.hasEffect(MobEffects.WEAKNESS)) {
            cancelarMagia(event, player, "§cVocê está muito fraco para conjurar magias!");
        }
    }

    private static void cancelarMagia(SpellPreCastEvent event, ServerPlayer player, String msg) {
        event.setCanceled(true);
        player.displayClientMessage(Component.literal(msg), true);
    }
}