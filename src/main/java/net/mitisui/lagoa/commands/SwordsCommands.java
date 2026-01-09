package net.mitisui.lagoa.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.mitisui.lagoa.Config;
import net.mitisui.lagoa.events.AlgemaEvents;

public class SwordsCommands {

    public static void registrar(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("get")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("espadas")
                        .then(Commands.literal("divisorDeAlmas")
                                .executes(SwordsCommands::darEspadaAdm)
                        )
                        .then(Commands.literal("katana")
                                .executes(SwordsCommands::darKatana)
                        )
                )
        );
    }

    private static int darKatana(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ItemStack katana = new ItemStack(Items.NETHERITE_SWORD);
            CompoundTag nbt = katana.getOrCreateTag();

            nbt.putBoolean("Unbreakable", true);
            nbt.putInt("CustomModelData", 102);

            CompoundTag display = new CompoundTag();
            display.putString("Name", Component.Serializer.toJson(
                    Component.literal("Katana")
                            .withStyle(ChatFormatting.RED)
                            .withStyle(ChatFormatting.BOLD)
            ));

            ListTag attributeModifiers = new ListTag();
            CompoundTag attackDamage = new CompoundTag();

            attackDamage.putString("AttributeName", "generic.attack_damage");
            attackDamage.putString("Name", "generic.attack_damage");

            attackDamage.putDouble("Amount", 19.0);
            attackDamage.putInt("Operation", 0);
            attackDamage.putString("Slot", "mainhand");

            int[] uuid = new int[]{10, 20, 30, 40};
            attackDamage.putIntArray("UUID", uuid);

            attributeModifiers.add(attackDamage);
            nbt.put("AttributeModifiers", attributeModifiers);
            // ------------------------------

            ListTag lore = new ListTag();
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(""))));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("⚔ Espada rápida e letal")
                            .withStyle(ChatFormatting.GRAY)
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(""))));

            display.put("Lore", lore);
            nbt.put("display", display);

            player.addItem(katana);

            context.getSource().sendSuccess(() ->
                    Component.literal("Você recebeu a Katana!")
                            .withStyle(ChatFormatting.DARK_RED)
                            .withStyle(ChatFormatting.BOLD), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao dar a Katana: " + e.getMessage())
                            .withStyle(ChatFormatting.RED)
            );
            e.printStackTrace();
            return 0;
        }
    }

    private static int darEspadaAdm(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            ItemStack espada = new ItemStack(Items.NETHERITE_SWORD);

            CompoundTag nbt = espada.getOrCreateTag();

            // Indestrutível (não gasta durabilidade)
            nbt.putBoolean("Unbreakable", true);

            // Custom Model Data
            nbt.putInt("CustomModelData", 101);

            // Display (Nome e Lore)
            CompoundTag display = new CompoundTag();
            display.putString("Name", Component.Serializer.toJson(
                    Component.literal("Divisor de Almas")
                            .withStyle(ChatFormatting.BLACK)
                            .withStyle(ChatFormatting.BOLD)
            ));

            ListTag lore = new ListTag();
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("")
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("⚔ Click Direito (com alvo):")
                            .withStyle(ChatFormatting.YELLOW)
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("  • Invoca espadas teleguiadas")
                            .withStyle(ChatFormatting.DARK_GRAY)
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("")
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("⚔ Click Direito (sem alvo):")
                            .withStyle(ChatFormatting.YELLOW)
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("  • Cria AOE devastador")
                            .withStyle(ChatFormatting.DARK_GRAY)
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("")
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("🛡 Passivo:")
                            .withStyle(ChatFormatting.AQUA)
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("  • Bloqueia projéteis")
                            .withStyle(ChatFormatting.DARK_GRAY)
            )));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.literal("  • Imune a magias")
                            .withStyle(ChatFormatting.DARK_GRAY)
            )));

            display.put("Lore", lore);
            nbt.put("display", display);

            ListTag enchantments = new ListTag();

            CompoundTag sharpness = new CompoundTag();
            sharpness.putString("id", "minecraft:sharpness");
            sharpness.putInt("lvl", 10);
            enchantments.add(sharpness);

            nbt.put("Enchantments", enchantments);

            ListTag attributeModifiers = new ListTag();

            CompoundTag attackDamage = new CompoundTag();
            attackDamage.putString("AttributeName", "generic.attack_damage");
            attackDamage.putString("Name", "generic.attack_damage");

            double danoConfig = Config.E1_DANO_BASE.get();
            double danoBaseDiamondSword = 7.0;
            double danoParaAdicionar = danoConfig - danoBaseDiamondSword;

            attackDamage.putDouble("Amount", danoParaAdicionar);
            attackDamage.putInt("Operation", 0);
            attackDamage.putString("Slot", "mainhand");

            int[] uuid = new int[]{1, 2, 3, 4};
            attackDamage.putIntArray("UUID", uuid);

            attributeModifiers.add(attackDamage);
            nbt.put("AttributeModifiers", attributeModifiers);

            nbt.putInt("HideFlags", 0);

            player.addItem(espada);

            context.getSource().sendSuccess(() ->
                    Component.literal("Você recebeu o Divisor de Almas!")
                            .withStyle(ChatFormatting.BLACK)
                            .withStyle(ChatFormatting.BOLD), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao dar o Divisor de Almas: " + e.getMessage())
                            .withStyle(ChatFormatting.BLACK)
            );
            e.printStackTrace();
            return 0;
        }
    }
}