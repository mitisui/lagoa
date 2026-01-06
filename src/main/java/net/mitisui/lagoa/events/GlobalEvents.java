package net.mitisui.lagoa.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mitisui.lagoa.Config;

public class GlobalEvents {
    @SubscribeEvent
    public static void onMemeItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) return;

        ItemStack item = event.getItemStack();
        if (!item.hasCustomHoverName()) return;

        String nome = item.getHoverName().getString().toLowerCase();
        Player player = event.getEntity();

        // vara de pesca com cenoura
        if (Config.ENABLE_TELEFONE.get()) {
            if (nome.contains("telefone")) {
                CompoundTag nbt = item.getOrCreateTag();
                int stage = nbt.getInt("XuxaStage"); // estágio 0, 1, 2 e 3-(final)

                String soundName = "xuxa_toque" + (stage + 1);
                event.getLevel().playSound(null, event.getEntity().blockPosition(),
                        SoundEvent.createVariableRangeEvent(new ResourceLocation("lagoa", soundName)),
                        SoundSource.PLAYERS, 1.0F, 1.0F);

                // Cooldown pelo estágio
                int cooldownTicks;
                if (stage < 3) {
                    // Sons 1, 2 e 3 -> 3 segundos
                    cooldownTicks = 3 * 20;
                } else {
                    // Som 4 (o último) -> 30 segundos
                    cooldownTicks = 30 * 20;
                }
                event.getEntity().getCooldowns().addCooldown(item.getItem(), cooldownTicks);

                // Atualiza o estágio
                nbt.putInt("XuxaStage", (stage + 1) % 4);

                event.setCanceled(true);
            }
        }

        // Cornetas
        if (Config.ENABLE_CORNETAS.get()) {
            if (item.is(Items.GOAT_HORN)) {
                String soundPath = null;
                if (nome.contains("flamengo")) soundPath = "hino_mengo";
                else if (nome.contains("corinthians")) soundPath = "hino_timao";
                else if (nome.contains("remo")) soundPath = "hino_remo";
                else if (nome.contains("paysandu")) soundPath = "hino_papao";
                else if (nome.contains("policia")) soundPath = "vtr_siren";

                if (soundPath != null) {
                    event.getLevel().playSound(null, player.blockPosition(),
                            SoundEvent.createVariableRangeEvent(new ResourceLocation("lagoa", soundPath)),
                            SoundSource.PLAYERS, 1.0F, 1.0F);

                    player.getCooldowns().addCooldown(item.getItem(), 100);
                    event.setCanceled(true);
                }
            }
        }
    }
}
