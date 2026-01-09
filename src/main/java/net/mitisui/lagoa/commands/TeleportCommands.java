package net.mitisui.lagoa.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.mitisui.lagoa.mechanics.TeleportSystem;

public class TeleportCommands {
    public static void registrar(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("trazer")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> trazerPlayer(context, "branco"))
                                .then(Commands.argument("cor", StringArgumentType.word())
                                .executes(context -> trazerPlayer(
                                        context,
                                        StringArgumentType.getString(context, "cor")
                                ))
                        )
                )
        );
    }

    private static int trazerPlayer(CommandContext<CommandSourceStack> context, String cor) {
        try {
            ServerPlayer executor = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");

            if (executor.getUUID().equals(target.getUUID())) {
                context.getSource().sendFailure(
                        Component.literal("Você não pode trazer a si mesmo!")
                                .withStyle(ChatFormatting.RED)
                );
                return 0;
            }

            // Verificar se o jogador já está teleportando
            if (TeleportSystem.isPlayerTeleporting(target.getUUID())) {
                context.getSource().sendFailure(
                        Component.literal(target.getName().getString() + " já está sendo teleportado!")
                                .withStyle(ChatFormatting.RED)
                );
                return 0;
            }
            Vec3 destination = executor.position();

            // Iniciar teleporte
            TeleportSystem.startTeleporte(target, destination, cor);

            // Mensagens
            context.getSource().sendSuccess(() ->
                    Component.literal("Trazendo " + target.getName().getString() + "...")
                            .withStyle(ChatFormatting.GREEN), true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Erro ao trazer jogador: " + e.getMessage())
                            .withStyle(ChatFormatting.RED)
            );
            return 0;
        }
    }

}
