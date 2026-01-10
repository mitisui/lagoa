package net.mitisui.lagoa.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import net.mitisui.lagoa.events.InvestigacaoEvents;

public class InvestigacaoCommands {
    public static void registrar(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("get")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("lupa").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack lupa = new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK);

                    CompoundTag nbt = lupa.getOrCreateTag();
                    nbt.putInt("CustomModelData", 3);

                    CompoundTag display = new CompoundTag();
                    display.putString("Name", Component.Serializer.toJson(
                            Component.literal("Lupa de Investigação")
                                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                    ));

                    ListTag lore = new ListTag();
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(
                            Component.literal("Clique em um jogador para investigá-lo")
                                    .withStyle(ChatFormatting.GRAY)
                    )));
                    display.put("Lore", lore);
                    nbt.put("display", display);

                    nbt.putBoolean("IsLupa", true);
                    nbt.putInt("Unbreakable", 1);

                    player.addItem(lupa);
                    context.getSource().sendSuccess(() ->
                            Component.literal("§aVocê recebeu uma lupa de investigação!"), false);
                    return 1;
                }))
        );

        root.then(Commands.literal("inspecionar")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("alvo", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer investigador = context.getSource().getPlayerOrException();
                            ServerPlayer alvo = EntityArgument.getPlayer(context, "alvo");

                            InvestigacaoEvents.abrirMenuInvestigacao(investigador, alvo);
                            return 1;
                        })
                )
        );
    }
}