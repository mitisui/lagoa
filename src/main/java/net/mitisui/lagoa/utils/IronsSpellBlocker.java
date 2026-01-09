package net.mitisui.lagoa.utils;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "lagoa")
public class IronsSpellBlocker {

    @SubscribeEvent
    public static void onSpellPreCast(SpellPreCastEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof ServerPlayer player && !player.level().isClientSide) {

            if (player.hasEffect(MobEffects.UNLUCK)) {
                event.setCanceled(true);

                MagicData magicData = MagicData.getPlayerMagicData(player);
                if (magicData.getMana() > 0) {
                    magicData.setMana(0.0f);
                }

                player.displayClientMessage(Component.literal("§cVocê não consegue usar magias nesse estado"), true);
            }
        }
    }
}
