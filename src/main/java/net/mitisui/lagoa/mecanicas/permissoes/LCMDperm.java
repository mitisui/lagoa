package net.mitisui.lagoa.mecanicas.permissoes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.mitisui.lagoa.mecanicas.cargos.CargoManager;

public class LCMDperm {
    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("perm")
                .requires(s -> s.hasPermission(perm))

                // /lagoa perm <cargo> nivel <0-4>
                .then(Commands.argument("cargo", StringArgumentType.string())
                        .suggests((ctx, b) -> CargoManager.sugerirCargos(b))
                        .then(Commands.literal("nivel")
                                .then(Commands.argument("nivel", IntegerArgumentType.integer(0, 4))
                                        .executes(ctx -> {
                                            String cargoId = StringArgumentType.getString(ctx, "cargo");
                                            int nivel = IntegerArgumentType.getInteger(ctx, "nivel");
                                            PermissaoManager.setNivel(cargoId, nivel);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aNível §f" + nivel + " §adefinido para o cargo §f" + cargoId), true);
                                            return 1;
                                        })
                                )
                        )
                        // /lagoa perm <cargo> add <comando>
                        .then(Commands.literal("add")
                                .then(Commands.argument("comando", StringArgumentType.string())
                                        .executes(ctx -> {
                                            String cargoId = StringArgumentType.getString(ctx, "cargo");
                                            String cmd = StringArgumentType.getString(ctx, "comando");
                                            PermissaoManager.addComando(cargoId, cmd);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§aComando §f" + cmd + " §aliberado para §f" + cargoId), true);
                                            return 1;
                                        })
                                )
                        )
                        // /lagoa perm <cargo> remove <comando>
                        .then(Commands.literal("remove")
                                .then(Commands.argument("comando", StringArgumentType.string())
                                        .executes(ctx -> {
                                            String cargoId = StringArgumentType.getString(ctx, "cargo");
                                            String cmd = StringArgumentType.getString(ctx, "comando");
                                            PermissaoManager.removeComando(cargoId, cmd);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§eComando §f" + cmd + " §eremovido de §f" + cargoId), true);
                                            return 1;
                                        })
                                )
                        )
                );
    }
}