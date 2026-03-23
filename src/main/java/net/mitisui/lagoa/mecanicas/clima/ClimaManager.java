package net.mitisui.lagoa.mecanicas.clima;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.mitisui.lagoa.Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class ClimaManager {

    private static boolean cicloAtivo = false;
    private static float horasChuva = 0.5f;
    private static int chanceTempestade = 20;
    private static int chanceTrovao = 30;

    private static boolean chuvaAnterior = false;

    // ---- Tick ----

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;

        boolean chuvaAgora = level.isRaining();

        if (chuvaAgora && !chuvaAnterior) {
            aoIniciarChuva(level);
        }

        chuvaAnterior = chuvaAgora;
    }

    // Chamado quando a chuva começa
    public static void aoIniciarChuva(ServerLevel level) {
        if (!cicloAtivo) {
            // Sem ciclo ativo: usa a chance padrão da config
            Double chance = Config.CLIMA_CHANCE_CANCELAR_CHUVA.get();
            if (level.getRandom().nextFloat() < chance) {
                int solMin   = Config.CLIMA_SOL_MINIMO_TICKS.get();
                int solExtra = Config.CLIMA_SOL_EXTRA_MAXIMO_TICKS.get();
                level.setWeatherParameters(
                        solMin + level.getRandom().nextInt(Math.max(1, solExtra)),
                        0, false, false
                );
            }
            return;
        }

        boolean virarTempestade = level.getRandom().nextInt(100) < chanceTempestade;
        int ticks = (int)(horasChuva * 60 * 60 * 20);
        level.setWeatherParameters(0, ticks, true, virarTempestade);
    }

    // ---- API ----

    public static void resetarCiclo()  { cicloAtivo = false; }

    public static void definirTempestade(int trovao) {
        cicloAtivo = false;
        chanceTrovao = trovao;
    }

    public static void definirCiclo(float horas, int tempestade, int trovao) {
        cicloAtivo = true;
        horasChuva = horas;
        chanceTempestade = tempestade;
        chanceTrovao = trovao;
    }

    public static boolean isCicloAtivo()   { return cicloAtivo; }
    public static int getChanceTrovao()    { return chanceTrovao; }
}