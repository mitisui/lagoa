package net.mitisui.lagoa.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mitisui.lagoa.Config;
import org.jetbrains.annotations.NotNull;

public class GlobalEvents {

    // renomeando por bigorna
    @SubscribeEvent
    public static void onAnvilRepair(AnvilUpdateEvent event) {
        if (!Config.ENABLE_CORNETAS.get()) return;

        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        String newName = event.getName();

        if (left.is(Items.CARROT_ON_A_STICK) && right.isEmpty() && newName != null) {
            ItemStack result = left.copy();
            CompoundTag nbt = result.getOrCreateTag();
            nbt.putInt("CustomModelData", getCarrotStickModelData(newName));
            result.setHoverName(Component.literal(newName));

            event.setOutput(result);
            event.setCost(5); // Custo em níveis
        }

        if (left.is(Items.GOAT_HORN) && right.isEmpty() && newName != null) {
            ItemStack result = left.copy();
            CompoundTag nbt = result.getOrCreateTag();
            String instrumentId = getCornetaInstrument(newName.toLowerCase());

            if (instrumentId != null) {
                nbt.putString("instrument", instrumentId);

                int modelId = getCornetaModelData(instrumentId);
                nbt.putInt("CustomModelData", modelId);

                result.setHoverName(Component.literal(newName));
                event.setOutput(result);
                event.setCost(Config.CORNETA_CUSTO.get());
            }
        }
    }
    private static int getCarrotStickModelData(String name) {
        if (name.contains("telefone")) return 5;
        return 0;
    }

    private static int getCornetaModelData(String instrumentId) {
        return switch (instrumentId) {
            case "minecraft:policia_goat_horn" -> 101;
            case "minecraft:flamengo_goat_horn" -> 102;
            case "minecraft:remo_goat_horn" -> 103;
            case "minecraft:paysandu_goat_horn" -> 104;
            case "minecraft:corinthians_goat_horn" -> 105;
            default -> 0;
        };
    }

    private static String getCornetaInstrument(String name) {
        String[] validNames = {"policia", "flamengo", "remo", "paysandu", "corinthians"};

        for (String validName : validNames) {
            if (name.contains(validName)) {
                return "minecraft:" + validName + "_goat_horn";
            }
        }
        return null; // corneta normal
    }

    @SubscribeEvent
    public static void onMemeItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) return;

        ItemStack item = event.getItemStack();
        Player player = event.getEntity();

        String nome = item.hasCustomHoverName() ? item.getHoverName().getString().toLowerCase() : "";

        // vara de pesca com cenoura
        if (Config.ENABLE_TELEFONE.get()) {
            if (nome.contains("telefone") && item.is(Items.CARROT_ON_A_STICK)) {
                CompoundTag nbt = item.getOrCreateTag();
                int stage = nbt.getInt("TelStage");

                String soundName = "tel_toque" + (stage + 1);
                event.getLevel().playSound(null, event.getEntity().blockPosition(),
                        SoundEvent.createVariableRangeEvent(new ResourceLocation("minecraft", soundName)),
                        SoundSource.PLAYERS, 1.0F, 1.0F);

                int cooldownTicks;
                if (stage < 3) {
                    cooldownTicks = 3 * 20;
                } else {
                    cooldownTicks = 30 * 20;
                }
                event.getEntity().getCooldowns().addCooldown(item.getItem(), cooldownTicks);

                nbt.putInt("TelStage", (stage + 1) % 4);

                event.setCanceled(true);
            }
        }
        if (Config.ENABLE_CORNETAS.get()) {
            if (item.is(Items.GOAT_HORN)) {
                CompoundTag nbt = item.getTag();

                // Verifica se tem o NBT de instrument customizado
                if (nbt != null && nbt.contains("instrument")) {
                    String instrument = nbt.getString("instrument");
                    String soundName = getCornetaSoundFromInstrument(instrument);

                    if (soundName != null) {
                        // Toca o som customizado
                        event.getLevel().playSound(null, player.blockPosition(),
                                SoundEvent.createVariableRangeEvent(new ResourceLocation("minecraft", soundName)),
                                SoundSource.PLAYERS, 1.5F, 1.0F);

                        // Adiciona cooldown (140 ticks = 7 segundos, igual goat horn normal)
                        player.getCooldowns().addCooldown(Items.GOAT_HORN, 140);

                        // Cancela o comportamento padrão
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    private static String getCornetaSoundFromInstrument(String instrument) {
        return switch (instrument) {
            case "minecraft:policia_goat_horn" -> "sirene";
            case "minecraft:flamengo_goat_horn" -> "flamengo";
            case "minecraft:remo_goat_horn" -> "remo";
            case "minecraft:paysandu_goat_horn" -> "paysandu";
            case "minecraft:corinthians_goat_horn" -> "corinthians";
            default -> null;
        };
    }

}
