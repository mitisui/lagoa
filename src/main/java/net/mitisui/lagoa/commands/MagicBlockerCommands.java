package net.mitisui.lagoa.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.mitisui.lagoa.mechanics.MagicBlockerZones;

import java.util.Map;

public class MagicBlockerCommands {

    public static void registrar(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("magicblock")
                .requires(source -> source.hasPermission(2))

                // Adicionar zona
                .then(Commands.literal("add")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("nome", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            // Usa a posição do player como referência
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String id = StringArgumentType.getString(context, "id");
                                            String nome = StringArgumentType.getString(context, "nome");

                                            BlockPos pos = player.blockPosition();
                                            String dimension = player.level().dimension().location().toString();

                                            // Cria uma zona de 10x10x10 ao redor do player
                                            BlockPos pos1 = pos.offset(-5, -5, -5);
                                            BlockPos pos2 = pos.offset(5, 5, 5);

                                            MagicBlockerZones.addZone(id, nome, pos1, pos2, dimension);

                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("Zona de bloqueio '" + nome + "' criada!")
                                                            .withStyle(ChatFormatting.GREEN), false);
                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("ID: " + id + " | Centro: " + pos.toShortString())
                                                            .withStyle(ChatFormatting.GRAY), false);
                                            return 1;
                                        })
                                )
                        )
                )

                // Adicionar zona com coordenadas customizadas
                .then(Commands.literal("addcustom")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                        .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                                .then(Commands.argument("nome", StringArgumentType.greedyString())
                                                        .executes(context -> {
                                                            String id = StringArgumentType.getString(context, "id");
                                                            BlockPos pos1 = BlockPosArgument.getBlockPos(context, "pos1");
                                                            BlockPos pos2 = BlockPosArgument.getBlockPos(context, "pos2");
                                                            String nome = StringArgumentType.getString(context, "nome");
                                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                                            String dimension = player.level().dimension().location().toString();

                                                            MagicBlockerZones.addZone(id, nome, pos1, pos2, dimension);

                                                            context.getSource().sendSuccess(() ->
                                                                    Component.literal("Zona de bloqueio '" + nome + "' criada!")
                                                                            .withStyle(ChatFormatting.GREEN), false);
                                                            context.getSource().sendSuccess(() ->
                                                                    Component.literal("ID: " + id)
                                                                            .withStyle(ChatFormatting.GRAY), false);
                                                            context.getSource().sendSuccess(() ->
                                                                    Component.literal("Pos1: " + pos1.toShortString() +
                                                                                    " | Pos2: " + pos2.toShortString())
                                                                            .withStyle(ChatFormatting.GRAY), false);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                )

                // Remover zona
                .then(Commands.literal("remove")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> {
                                    String id = StringArgumentType.getString(context, "id");

                                    if (MagicBlockerZones.removeZone(id)) {
                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Zona '" + id + "' removida com sucesso!")
                                                        .withStyle(ChatFormatting.GREEN), false);
                                    } else {
                                        context.getSource().sendFailure(
                                                Component.literal("Zona '" + id + "' não encontrada!")
                                                        .withStyle(ChatFormatting.RED));
                                    }
                                    return 1;
                                })
                        )
                )

                // Listar zonas
                .then(Commands.literal("list")
                        .executes(context -> {
                            Map<String, MagicBlockerZones.ZoneData> zones = MagicBlockerZones.getAllZones();

                            if (zones.isEmpty()) {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("Nenhuma zona de bloqueio configurada.")
                                                .withStyle(ChatFormatting.YELLOW), false);
                            } else {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("=== Zonas de Bloqueio de Magia ===")
                                                .withStyle(ChatFormatting.GOLD), false);

                                zones.forEach((id, zone) -> {
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("• " + id + ": " + zone.zoneName)
                                                    .withStyle(ChatFormatting.AQUA), false);
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("  Pos1: " + zone.pos1.toShortString() +
                                                            " | Pos2: " + zone.pos2.toShortString())
                                                    .withStyle(ChatFormatting.GRAY), false);
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("  Dimensão: " + zone.dimension)
                                                    .withStyle(ChatFormatting.GRAY), false);
                                });
                            }
                            return 1;
                        })
                )

                // Limpar todas as zonas
                .then(Commands.literal("clear")
                        .executes(context -> {
                            MagicBlockerZones.clearAllZones();
                            context.getSource().sendSuccess(() ->
                                    Component.literal("Todas as zonas de bloqueio foram removidas!")
                                            .withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                )

                // Informações sobre zona específica
                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> {
                                    String id = StringArgumentType.getString(context, "id");
                                    MagicBlockerZones.ZoneData zone = MagicBlockerZones.getZone(id);

                                    if (zone == null) {
                                        context.getSource().sendFailure(
                                                Component.literal("Zona '" + id + "' não encontrada!")
                                                        .withStyle(ChatFormatting.RED));
                                    } else {
                                        context.getSource().sendSuccess(() ->
                                                Component.literal("=== Informações da Zona ===")
                                                        .withStyle(ChatFormatting.GOLD), false);
                                        context.getSource().sendSuccess(() ->
                                                Component.literal("ID: " + id)
                                                        .withStyle(ChatFormatting.AQUA), false);
                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Nome: " + zone.zoneName)
                                                        .withStyle(ChatFormatting.WHITE), false);
                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Posição 1: " + zone.pos1.toShortString())
                                                        .withStyle(ChatFormatting.GRAY), false);
                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Posição 2: " + zone.pos2.toShortString())
                                                        .withStyle(ChatFormatting.GRAY), false);
                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Dimensão: " + zone.dimension)
                                                        .withStyle(ChatFormatting.GRAY), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}