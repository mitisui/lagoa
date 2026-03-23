package net.mitisui.lagoa.mecanicas.clima;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class LCMDclima {

    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("clima")
                .requires(s -> s.hasPermission(perm))

                // /lagoa clima ensolarado
                .then(Commands.literal("ensolarado")
                        .executes(ctx -> climaEnsolarado(ctx.getSource()))
                )

                // /lagoa clima chuva <horas>
                .then(Commands.literal("chuva")
                        .then(Commands.argument("horas", FloatArgumentType.floatArg(0.1f))
                                .executes(ctx -> climaChuva(ctx.getSource(), FloatArgumentType.getFloat(ctx, "horas")))
                        )
                )

                // /lagoa clima tempestade <% trovão>
                .then(Commands.literal("tempestade")
                        .then(Commands.argument("chance_trovao", IntegerArgumentType.integer(0, 100))
                                .executes(ctx -> climaTempestade(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "chance_trovao")))
                        )
                )

                // /lagoa clima ciclo <horas_chuva> <% tempestade> [% trovão]
                .then(Commands.literal("ciclo")
                        .then(Commands.argument("horas_chuva", FloatArgumentType.floatArg(0.1f))
                                .then(Commands.argument("chance_tempestade", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> climaCiclo(ctx.getSource(),
                                                FloatArgumentType.getFloat(ctx, "horas_chuva"),
                                                IntegerArgumentType.getInteger(ctx, "chance_tempestade"),
                                                30))
                                        .then(Commands.argument("chance_trovao", IntegerArgumentType.integer(0, 100))
                                                .executes(ctx -> climaCiclo(ctx.getSource(),
                                                        FloatArgumentType.getFloat(ctx, "horas_chuva"),
                                                        IntegerArgumentType.getInteger(ctx, "chance_tempestade"),
                                                        IntegerArgumentType.getInteger(ctx, "chance_trovao")))
                                        )
                                )
                        )
                );
    }

    private static int climaEnsolarado(CommandSourceStack source) {
        source.getLevel().setWeatherParameters(72000, 0, false, false);
        ClimaManager.resetarCiclo();
        source.sendSuccess(() -> Component.literal("§eTempo ensolarado definido."), true);
        return 1;
    }

    private static int climaChuva(CommandSourceStack source, float horas) {
        int ticks = horasParaTicks(horas);
        source.getLevel().setWeatherParameters(0, ticks, true, false);
        ClimaManager.resetarCiclo();
        source.sendSuccess(() -> Component.literal("§9Chuva definida por §f" + horas + "§9 hora(s)."), true);
        return 1;
    }

    private static int climaTempestade(CommandSourceStack source, int chanceTrovao) {
        source.getLevel().setWeatherParameters(0, 72000, true, true);
        ClimaManager.definirTempestade(chanceTrovao);
        source.sendSuccess(() -> Component.literal("§8Tempestade iniciada! Trovões: §f" + chanceTrovao + "%"), true);
        return 1;
    }

    private static int climaCiclo(CommandSourceStack source, float horasChuva, int chanceTempestade, int chanceTrovao) {
        ClimaManager.definirCiclo(horasChuva, chanceTempestade, chanceTrovao);
        source.sendSuccess(() -> Component.literal(
                "§aCiclo climático definido! Chuva: §f" + horasChuva +
                        "§ah | Tempestade: §f" + chanceTempestade +
                        "§a% | Trovões: §f" + chanceTrovao + "§a%"), true);
        return 1;
    }

    private static int horasParaTicks(float horas) {
        return (int)(horas * 60 * 60 * 20);
    }
}