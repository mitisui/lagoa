package net.mitisui.lagoa.mecanicas.espadas;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.Unbreakable;
import net.mitisui.lagoa.Config;

import java.util.List;

public class LCMDespadas {

    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("get")
            .requires(s -> s.hasPermission(perm))
            .then(Commands.literal("espadas")
                .then(Commands.literal("divisorDeAlmas")
                    .executes(LCMDespadas::darEspadaAdm))
                .then(Commands.literal("katana")
                    .executes(LCMDespadas::darKatana))
            );
    }

    private static int darKatana(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ItemStack katana = new ItemStack(Items.NETHERITE_SWORD);

            katana.set(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(false));

            katana.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(102));

            katana.set(DataComponents.CUSTOM_NAME,
                Component.literal("Katana")
                    .withStyle(ChatFormatting.RED)
                    .withStyle(ChatFormatting.BOLD));

            katana.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("⚔ Espada rápida e letal").withStyle(ChatFormatting.GRAY),
                Component.literal("")
            )));

            // MIGRAÇÃO: AttributeModifiers via NBT -> DataComponents.ATTRIBUTE_MODIFIERS
            katana.set(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                    .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath("lagoa", "katana_damage"),
                            // 19.0 total - 1.0 base = 18.0 de modifier
                            18.0,
                            AttributeModifier.Operation.ADD_VALUE
                        ),
                        net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND
                    )
                    .build()
            );

            player.addItem(katana);
            ctx.getSource().sendSuccess(() ->
                Component.literal("Você recebeu a Katana!").withStyle(ChatFormatting.DARK_RED), false);
            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(
                Component.literal("Erro ao dar a Katana: " + e.getMessage()).withStyle(ChatFormatting.RED));
            e.printStackTrace();
            return 0;
        }
    }

    private static int darEspadaAdm(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ItemStack espada = new ItemStack(Items.NETHERITE_SWORD);

            // Indestrutível
            espada.set(DataComponents.UNBREAKABLE, new Unbreakable(false));

            // Custom Model Data
            espada.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(101));

            // Nome
            espada.set(DataComponents.CUSTOM_NAME,
                Component.literal("Divisor de Almas")
                    .withStyle(ChatFormatting.BLACK)
                    .withStyle(ChatFormatting.OBFUSCATED));

            // Lore
            espada.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("⚔ Click Direito (com alvo):").withStyle(ChatFormatting.YELLOW),
                Component.literal("  • Invoca espadas teleguiadas").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal(""),
                Component.literal("⚔ Click Direito (sem alvo):").withStyle(ChatFormatting.YELLOW),
                Component.literal("  • Cria AOE devastador").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal(""),
                Component.literal("🛡 Passivo:").withStyle(ChatFormatting.AQUA),
                Component.literal("  • Bloqueia projéteis").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal("  • Imune a magias").withStyle(ChatFormatting.DARK_GRAY)
            )));


            // Atributo de dano
            double danoConfig       = Config.E1_ATAQUE_AOE_DANO.get() * 3; // dano de aoe é 1/3 do dano normal
            double danoBaseSword    = 8.0;
            double modifier         = danoConfig - danoBaseSword;

            espada.set(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                    .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath("lagoa", "divisor_damage"),
                            modifier,
                            AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                    )
                    .build()
            );

            player.addItem(espada);
            ctx.getSource().sendSuccess(() ->
                Component.literal("Você recebeu o Divisor de Almas!")
                    .withStyle(ChatFormatting.BLACK)
                    .withStyle(ChatFormatting.BOLD), false);
            return 1;

        } catch (Exception e) {
            ctx.getSource().sendFailure(
                Component.literal("Erro ao dar o Divisor de Almas: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            e.printStackTrace();
            return 0;
        }
    }
}
