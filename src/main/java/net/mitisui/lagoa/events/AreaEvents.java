package net.mitisui.lagoa.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.mitisui.lagoa.mecanicas.area.AreaManager;
import net.mitisui.lagoa.mecanicas.area.AreaRegion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AreaEvents {

    private static final Map<UUID, String> ultimaAreaDoPlayer = new HashMap<>();

    @SubscribeEvent
    public static void aoMover(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Checa a cada 10 ticks (meio segundo)
        if (player.tickCount % 10 != 0) return;

        AreaRegion areaAtual = AreaManager.getAreaNaPosicao(player.blockPosition());
        String nomeAreaAtual = (areaAtual != null) ? areaAtual.nome : "nenhuma";
        String ultimaArea = ultimaAreaDoPlayer.getOrDefault(player.getUUID(), "nenhuma");

        if (!nomeAreaAtual.equals(ultimaArea)) {
            if (areaAtual != null && !areaAtual.legenda.isEmpty()) {
                // Mostra a legenda na ActionBar ao entrar na área
                player.displayClientMessage(
                        Component.literal(areaAtual.legenda.replace("&", "§")), true
                );
            } else if (areaAtual == null) {
                // Limpa a ActionBar silenciosamente ao sair
                player.displayClientMessage(Component.literal(""), true);
            }
            ultimaAreaDoPlayer.put(player.getUUID(), nomeAreaAtual);
        }
    }


}