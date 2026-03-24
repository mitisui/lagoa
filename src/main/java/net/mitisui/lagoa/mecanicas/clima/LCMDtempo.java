package net.mitisui.lagoa.mecanicas.clima;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class LCMDtempo {

    // 1.0 = velocidade normal (1 tick de dia por tick de jogo)
    // 2.0 = dobro da velocidade, 0.5 = metade, 0.0 = dia congelado
    private static float velocidadeDia = 1.0f;
    private static boolean velocidadeAtiva = false;

    // Acumula a fração de tick para velocidades menores que 1.0
    private static float acumulador = 0f;

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Pre event) {
        if (!velocidadeAtiva) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;

        if (velocidadeDia == 1.0f) return;
    }

    @SubscribeEvent
    public static void onTickPost(LevelTickEvent.Post event) {
        if (!velocidadeAtiva) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;
        if (velocidadeDia == 1.0f) return;

        float diferenca = velocidadeDia - 1.0f;

        if (diferenca > 0) {
            acumulador += diferenca;
            int ticksExtras = (int) acumulador;
            acumulador -= ticksExtras;
            if (ticksExtras > 0) {
                level.setDayTime(level.getDayTime() + ticksExtras);
            }
        } else if (diferenca < 0) {
            acumulador += Math.abs(diferenca);
            int ticksVoltar = (int) acumulador;
            acumulador -= ticksVoltar;
            if (ticksVoltar > 0) {
                level.setDayTime(level.getDayTime() - ticksVoltar);
            }
        } else if (velocidadeDia == 0f) {
            level.setDayTime(level.getDayTime() - 1);
        }
    }

    // ---- Comandos ----

    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("tempo")
                .requires(s -> s.hasPermission(perm))

                // /lagoa tempo velocidade <multiplicador>
                .then(Commands.literal("velocidade")
                        .then(Commands.argument("multiplicador", FloatArgumentType.floatArg(0f, 500f))
                                .executes(ctx -> definirVelocidade(
                                        ctx.getSource(),
                                        FloatArgumentType.getFloat(ctx, "multiplicador")
                                ))
                        )
                        // /lagoa tempo velocidade reset
                        .then(Commands.literal("reset")
                                .executes(ctx -> resetarVelocidade(ctx.getSource()))
                        )
                );
    }

    // ---- Implementações ----

    private static int definirTempoLimpo(CommandSourceStack source, int minutos) {
        int ticks = minutos * 60 * 20;
        source.getLevel().setWeatherParameters(ticks, 0, false, false);
        source.sendSuccess(() -> Component.literal(
                "§aO tempo ficará limpo por §f" + minutos + " §aminutos."), true);
        return 1;
    }

    private static int definirVelocidade(CommandSourceStack source, float multiplicador) {
        velocidadeDia = multiplicador;
        velocidadeAtiva = true;
        acumulador = 0f;

        String descricao;
        if (multiplicador == 0f)       descricao = "§7dia congelado";
        else if (multiplicador < 1f)   descricao = "§9dia mais lento (§f" + multiplicador + "x§9)";
        else if (multiplicador == 1f)  descricao = "§avelocidade normal";
        else                           descricao = "§6dia mais rápido (§f" + multiplicador + "x§6)";

        source.sendSuccess(() -> Component.literal(
                "§aVelocidade do dia definida: " + descricao), true);
        return 1;
    }

    private static int resetarVelocidade(CommandSourceStack source) {
        velocidadeDia = 1.0f;
        velocidadeAtiva = false;
        acumulador = 0f;
        source.sendSuccess(() -> Component.literal(
                "§aVelocidade do dia resetada para o padrão."), true);
        return 1;
    }

    public static float getVelocidadeDia() { return velocidadeDia; }
    public static boolean isVelocidadeAtiva() { return velocidadeAtiva; }
}