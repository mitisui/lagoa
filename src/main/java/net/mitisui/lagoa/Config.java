package net.mitisui.lagoa;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

@EventBusSubscriber(modid = Lagoa.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // =========================================================================
    // ANÚNCIO
    // =========================================================================

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ANUNCIO_COMANDOS_BLOQUEADOS =
            BUILDER
                    .comment("Comandos que não podem ser usados em botões de anúncio")
                    .defineListAllowEmpty("anuncio.comandos_bloqueados",
                            List.of(
                                    "/give", "/gamemode", "/gm", "/summon", "/op", "/deop",
                                    "/ban", "/kick", "/kill", "/clear", "/effect", "/enchant",
                                    "/tp @a", "/lagoa perm", "/lagoa cargo admin"
                            ),
                            entry -> entry instanceof String
                    );

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ANUNCIO_DOMINIOS_PERMITIDOS =
            BUILDER
                    .comment("Domínios de URL permitidos nos anúncios")
                    .defineListAllowEmpty("anuncio.dominios_permitidos",
                            List.of(
                                    "twitch.tv", "youtube.com", "youtu.be",
                                    "discord.gg", "discord.com",
                                    "twitter.com", "x.com", "instagram.com"
                            ),
                            entry -> entry instanceof String
                    );

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ANUNCIO_COMANDOS_PERMITIDOS =
            BUILDER
                    .comment("Prefixos de comandos permitidos nos botões de teleporte dos anúncios")
                    .defineListAllowEmpty("anuncio.comandos_permitidos",
                            List.of(
                                    "/tp ", "/teleport ", "/warp ",
                                    "/lagoa teletransporte", "/lagoa area info"
                            ),
                            entry -> entry instanceof String
                    );

    // =========================================================================
    // CLIMA
    // =========================================================================

    public static final ModConfigSpec.DoubleValue CLIMA_CHANCE_CANCELAR_CHUVA =
            BUILDER
                    .comment("Chance padrão de cancelar a chuva quando ela começa (0.0 a 1.0)")
                    .defineInRange("clima.chance_cancelar_chuva", 0.8, 0.0, 1.0);

    public static final ModConfigSpec.IntValue CLIMA_SOL_MINIMO_TICKS =
            BUILDER
                    .comment("Tempo mínimo de sol após cancelar chuva (em ticks, 20 ticks = 1 segundo)")
                    .defineInRange("clima.sol_minimo_ticks", 24000, 1200, 144000);

    public static final ModConfigSpec.IntValue CLIMA_SOL_EXTRA_MAXIMO_TICKS =
            BUILDER
                    .comment("Ticks extras aleatórios adicionados ao sol mínimo")
                    .defineInRange("clima.sol_extra_maximo_ticks", 12000, 0, 72000);

    // =========================================================================
    // ALGEMA
    // =========================================================================

    public static final ModConfigSpec.IntValue SLOWNESS_LEVEL =
            BUILDER
                    .comment("Nível do efeito de lentidão aplicado ao preso (0 = lentidão I, 1 = lentidão II...)")
                    .defineInRange("algema.slowness_level", 2, 0, 10);

    public static final ModConfigSpec.BooleanValue ENABLE_GLOWING =
            BUILDER
                    .comment("Ativa o efeito de brilho (glowing) no jogador preso")
                    .define("algema.enable_glowing", true);

    public static final ModConfigSpec.BooleanValue PREVENT_BLOCK_BREAK =
            BUILDER
                    .comment("Impede que jogadores presos quebrem blocos")
                    .define("algema.prevent_block_break", true);

    public static final ModConfigSpec.BooleanValue PREVENT_BLOCK_PLACE =
            BUILDER
                    .comment("Impede que jogadores presos coloquem blocos")
                    .define("algema.prevent_block_place", true);

    public static final ModConfigSpec.BooleanValue PREVENT_INTERACTIONS =
            BUILDER
                    .comment("Impede que jogadores presos interajam com blocos e entidades")
                    .define("algema.prevent_interactions", true);

    public static final ModConfigSpec.BooleanValue PREVENT_ITEM_USE =
            BUILDER
                    .comment("Impede que jogadores presos usem itens")
                    .define("algema.prevent_item_use", true);

    public static final ModConfigSpec.BooleanValue PREVENT_ITEM_DROP =
            BUILDER
                    .comment("Impede que jogadores presos dropem itens")
                    .define("algema.prevent_item_drop", true);

    public static final ModConfigSpec.BooleanValue PREVENT_ITEM_PICKUP =
            BUILDER
                    .comment("Impede que jogadores presos peguem itens do chão")
                    .define("algema.prevent_item_pickup", true);

    public static final ModConfigSpec.BooleanValue PREVENT_ATTACK =
            BUILDER
                    .comment("Impede que jogadores presos ataquem")
                    .define("algema.prevent_attack", true);

    // =========================================================================
    // SPEC — deve ser o último campo estático
    // =========================================================================

    public static final ModConfigSpec SPEC = BUILDER.build();

    // -------------------------------------------------------------------------
    // Cache dos valores lidos
    // -------------------------------------------------------------------------
    public static List<String> comandosBloqueados;
    public static List<String> dominiosPermitidos;
    public static List<String> comandosPermitidos;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        comandosBloqueados = ANUNCIO_COMANDOS_BLOQUEADOS.get()
                .stream().map(Object::toString).toList();
        dominiosPermitidos = ANUNCIO_DOMINIOS_PERMITIDOS.get()
                .stream().map(Object::toString).toList();
        comandosPermitidos = ANUNCIO_COMANDOS_PERMITIDOS.get()
                .stream().map(Object::toString).toList();
    }
}