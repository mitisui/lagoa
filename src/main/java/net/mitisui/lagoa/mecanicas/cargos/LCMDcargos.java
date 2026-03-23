package net.mitisui.lagoa.mecanicas.cargos;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LCMDcargos {

    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("cargo")
                .requires(s -> s.hasPermission(perm))

                // /lagoa cargo <player> add <cargo>
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("add")
                                .then(Commands.argument("nome_cargo", StringArgumentType.string())
                                        .suggests((ctx, b) -> CargoManager.sugerirCargos(b))
                                        .executes(ctx -> adicionarCargoAoPlayer(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "nome_cargo")
                                        ))
                                )
                        )
                        // /lagoa cargo <player> remove
                        .then(Commands.literal("remove")
                                .executes(ctx -> removerCargoDoPlayer(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player")
                                ))
                        )
                )

                // /lagoa cargo admin create/delete/edit
                .then(Commands.literal("admin")
                        .then(Commands.literal("create")
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .executes(ctx -> criarCargo(ctx.getSource(), StringArgumentType.getString(ctx, "id")))
                                )
                        )
                        .then(Commands.literal("delete")
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .suggests((ctx, b) -> CargoManager.sugerirCargos(b))
                                        .executes(ctx -> deletarCargo(ctx.getSource(), StringArgumentType.getString(ctx, "id")))
                                )
                        )
                        .then(Commands.literal("edit")
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .suggests((ctx, b) -> CargoManager.sugerirCargos(b))
                                        .then(Commands.literal("nome_exibicao").then(Commands.argument("valor", StringArgumentType.string()).executes(c -> editarCargo(c, "nome",         StringArgumentType.getString(c, "valor")))))
                                        .then(Commands.literal("cor")          .then(Commands.argument("valor", StringArgumentType.string()).executes(c -> editarCargo(c, "cor",          StringArgumentType.getString(c, "valor")))))
                                        .then(Commands.literal("vida")         .then(Commands.argument("valor", DoubleArgumentType.doubleArg()).executes(c -> editarCargo(c, "vida",       DoubleArgumentType.getDouble(c, "valor")))))
                                        .then(Commands.literal("voar")         .then(Commands.argument("valor", BoolArgumentType.bool()).executes(c -> editarCargo(c, "voar",             BoolArgumentType.getBool(c, "valor")))))
                                        .then(Commands.literal("dano")         .then(Commands.argument("valor", DoubleArgumentType.doubleArg()).executes(c -> editarCargo(c, "dano",       DoubleArgumentType.getDouble(c, "valor")))))
                                        .then(Commands.literal("resistencia")  .then(Commands.argument("valor", DoubleArgumentType.doubleArg()).executes(c -> editarCargo(c, "resistencia",DoubleArgumentType.getDouble(c, "valor")))))
                                        .then(Commands.literal("invulneravel") .then(Commands.argument("valor", BoolArgumentType.bool()).executes(c -> editarCargo(c, "levaDano",         !BoolArgumentType.getBool(c, "valor")))))
                                )
                        )
                );
    }

    private static int criarCargo(CommandSourceStack source, String id) {
        if (CargoManager.existe(id)) {
            source.sendFailure(Component.literal("§cEste ID de cargo já existe!"));
            return 0;
        }
        CargoManager.salvarCargo(new Cargo(id));
        source.sendSuccess(() -> Component.literal("§aCargo §f" + id + " §acriado!"), true);
        return 1;
    }

    private static int editarCargo(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String campo, Object valor) {
        String id = StringArgumentType.getString(ctx, "id");
        Cargo cargo = CargoManager.getCargo(id);
        if (cargo == null) { ctx.getSource().sendFailure(Component.literal("§cCargo não encontrado!")); return 0; }

        switch (campo) {
            case "cor"         -> cargo.cor             = (String)  valor;
            case "vida"        -> cargo.vidaExtra       = (Double)  valor;
            case "voar"        -> cargo.podeVoar        = (Boolean) valor;
            case "dano"        -> cargo.danoExtra       = (Double)  valor;
            case "resistencia" -> cargo.resistenciaExtra= (Double)  valor;
            case "levaDano"    -> cargo.levaDano        = (Boolean) valor;
        }

        CargoManager.salvarCargo(cargo);
        CargoManager.atualizarPlayersDoCargo(cargo);
        ctx.getSource().sendSuccess(() -> Component.literal("§aCargo §f" + id + " §aatualizado: " + campo + " = " + valor), true);
        return 1;
    }

    private static int adicionarCargoAoPlayer(CommandSourceStack source, ServerPlayer player, String cargoId) {
        Cargo cargo = CargoManager.getCargo(cargoId);
        if (cargo == null) { source.sendFailure(Component.literal("§cCargo não encontrado!")); return 0; }
        CargoManager.vincularPlayer(player, cargo);
        source.sendSuccess(() -> Component.literal("§aPlayer §f" + player.getName().getString() + " §aagora tem o cargo §f" + cargo.id), true);
        return 1;
    }

    private static int removerCargoDoPlayer(CommandSourceStack source, ServerPlayer player) {
        CargoManager.desvincularPlayer(player);
        source.sendSuccess(() -> Component.literal("§eCargo removido de §f" + player.getName().getString()), true);
        return 1;
    }

    private static int deletarCargo(CommandSourceStack source, String id) {
        CargoManager.deletarCargo(id);
        source.sendSuccess(() -> Component.literal("§cCargo §f" + id + " §cdeletado."), true);
        return 1;
    }
}