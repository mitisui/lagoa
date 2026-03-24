package net.mitisui.lagoa.events;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.mitisui.lagoa.Config;

import java.util.function.Consumer;

public class GlobalEvents {

    // -------------------------------------------------------------------------
    // Utilitários NBT — 1.21.1
    // -------------------------------------------------------------------------

    private static CompoundTag readNbt(ItemStack item) {
        CustomData data = item.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static void editNbt(ItemStack item, Consumer<CompoundTag> editor) {
        CompoundTag tag = readNbt(item);
        editor.accept(tag);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // -------------------------------------------------------------------------
    // Renomeando itens pela bigorna
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onAnvilRepair(AnvilUpdateEvent event) {
        if (!Config.ENABLE_CORNETAS.get()) return;

        ItemStack left  = event.getLeft();
        ItemStack right = event.getRight();
        String newName  = event.getName();

        // Vara de cenoura -> telefone
        if (left.is(Items.CARROT_ON_A_STICK) && right.isEmpty() && newName != null) {
            ItemStack result = left.copy();
            int modelData = getCarrotStickModelData(newName);

            result.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(modelData));
            result.set(DataComponents.CUSTOM_NAME, Component.literal(newName));

            event.setOutput(result);
            event.setCost(5);
        }

        // Chifre de cabra -> corneta customizada
        if (left.is(Items.GOAT_HORN) && right.isEmpty() && newName != null) {
            String instrumentId = getCornetaInstrument(newName.toLowerCase());

            if (instrumentId != null) {
                ItemStack result = left.copy();
                int modelData = getCornetaModelData(instrumentId);

                result.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(modelData));
                result.set(DataComponents.CUSTOM_NAME, Component.literal(newName));

                // Salva o instrument no CustomData
                editNbt(result, nbt -> nbt.putString("instrument", instrumentId));

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
            case "minecraft:policia_goat_horn"    -> 101;
            case "minecraft:flamengo_goat_horn"   -> 102;
            case "minecraft:remo_goat_horn"        -> 103;
            case "minecraft:paysandu_goat_horn"    -> 104;
            case "minecraft:corinthians_goat_horn" -> 105;
            default -> 0;
        };
    }

    private static String getCornetaInstrument(String name) {
        String[] validNames = {"policia", "flamengo", "remo", "paysandu", "corinthians"};
        for (String validName : validNames) {
            if (name.contains(validName)) return "minecraft:" + validName + "_goat_horn";
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Uso dos itens meme
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onMemeItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;

        ItemStack item   = event.getItemStack();
        Player    player = event.getEntity();
        String nome = item.has(DataComponents.CUSTOM_NAME)
                ? item.getHoverName().getString().toLowerCase()
                : "";
        // Telefone (vara de cenoura)
        if (Config.ENABLE_TELEFONE.get()) {
            if (nome.contains("telefone") && item.is(Items.CARROT_ON_A_STICK)) {
                CompoundTag nbt   = readNbt(item);
                int         stage = nbt.getInt("TelStage");

                String soundName = "tel_toque" + (stage + 1);
                event.getLevel().playSound(null, player.blockPosition(),
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath("minecraft", soundName)),
                        SoundSource.PLAYERS, 1.0F, 1.0F);

                int cooldownTicks = (stage < 3) ? 3 * 20 : 30 * 20;
                player.getCooldowns().addCooldown(item.getItem(), cooldownTicks);

                // Atualiza o stage no CustomData
                editNbt(item, tag -> tag.putInt("TelStage", (stage + 1) % 4));

                event.setCanceled(true);
            }
        }

        // Cornetas (chifre de cabra)
        if (Config.ENABLE_CORNETAS.get()) {
            if (item.is(Items.GOAT_HORN)) {
                CompoundTag nbt = readNbt(item);
                if (nbt.contains("instrument")) {
                    String soundName = getCornetaSoundFromInstrument(nbt.getString("instrument"));
                    if (soundName != null) {
                        event.getLevel().playSound(null, player.blockPosition(),
                                SoundEvent.createVariableRangeEvent(
                                        ResourceLocation.fromNamespaceAndPath("minecraft", soundName)),
                                SoundSource.PLAYERS, 1.5F, 1.0F);

                        player.getCooldowns().addCooldown(Items.GOAT_HORN, 140);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    private static String getCornetaSoundFromInstrument(String instrument) {
        return switch (instrument) {
            case "minecraft:policia_goat_horn"    -> "sirene";
            case "minecraft:flamengo_goat_horn"   -> "flamengo";
            case "minecraft:remo_goat_horn"        -> "remo";
            case "minecraft:paysandu_goat_horn"    -> "paysandu";
            case "minecraft:corinthians_goat_horn" -> "corinthians";
            default -> null;
        };
    }
}