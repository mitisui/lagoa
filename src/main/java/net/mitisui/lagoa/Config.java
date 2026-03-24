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
    // PISTOLA
    // =========================================================================

    public static final ModConfigSpec.BooleanValue ENABLE_PISTOLA =
            BUILDER
                    .comment("Ativa ou desativa a pistola no servidor")
                    .define("pistola.enable_pistola", true);

    public static final ModConfigSpec.DoubleValue SHOT_DAMAGE =
            BUILDER
                    .comment("Dano do tiro da pistola (em pontos de vida, 2 = 1 coração)")
                    .defineInRange("pistola.shot_damage", 6.0, 0.5, 40.0);

    public static final ModConfigSpec.ConfigValue<String> BULLET_EFFECT =
            BUILDER
                    .comment("Efeito aplicado ao alvo ao ser atingido (ex: minecraft:slowness). Deixe vazio para nenhum.")
                    .define("pistola.bullet_effect", "");

    public static final ModConfigSpec.BooleanValue PISTOLA_RICOCHET_ENABLED =
            BUILDER
                    .comment("Ativa o ricochete na pistola")
                    .define("pistola.ricochet_enabled", false);

    public static final ModConfigSpec.IntValue PISTOLA_RICOCHET_COUNT =
            BUILDER
                    .comment("Quantas vezes o tiro pode ricochetear (requer ricochet_enabled = true)")
                    .defineInRange("pistola.ricochet_count", 2, 0, 10);

    public static final ModConfigSpec.DoubleValue PISTOLA_RANGE =
            BUILDER
                    .comment("Alcance do tiro principal em blocos")
                    .defineInRange("pistola.range", 50.0, 1.0, 200.0);

    // =========================================================================
    // CORNETAS / TELEFONE
    // =========================================================================

    public static final ModConfigSpec.BooleanValue ENABLE_CORNETAS =
            BUILDER
                    .comment("Ativa a personalização de cornetas via bigorna")
                    .define("itens.enable_cornetas", true);

    public static final ModConfigSpec.IntValue CORNETA_CUSTO =
            BUILDER
                    .comment("Custo em níveis de experiência para criar uma corneta na bigorna")
                    .defineInRange("itens.corneta_custo", 5, 0, 30);

    public static final ModConfigSpec.BooleanValue ENABLE_TELEFONE =
            BUILDER
                    .comment("Ativa o item de telefone (vara de cenoura renomeada)")
                    .define("itens.enable_telefone", true);

    // =========================================================================
    // DIVISOR DE ALMAS (ESPADA)
    // =========================================================================

    public static final ModConfigSpec.DoubleValue E1_RAYCAST_RANGE =
            BUILDER.comment("Alcance do raio para detectar o alvo (Soul Division)")
                    .defineInRange("espada.divisor_almas.range_raio", 15.0, 1.0, 64.0);

    public static final ModConfigSpec.IntValue E1_HOMING_COOLDOWN =
            BUILDER.comment("Cooldown da habilidade Soul Division (em ticks)")
                    .defineInRange("espada.divisor_almas.cooldown_divisao", 200, 0, 72000);

    public static final ModConfigSpec.DoubleValue E1_ATAQUE_AOE_AREA =
            BUILDER.comment("Raio da explosão de almas (Ataque Massivo)")
                    .defineInRange("espada.divisor_almas.aoe_raio", 10.0, 1.0, 32.0);

    public static final ModConfigSpec.DoubleValue E1_ATAQUE_AOE_DANO =
            BUILDER.comment("Dano causado pelo Ataque Massivo")
                    .defineInRange("espada.divisor_almas.aoe_dano", 20.0, 0.0, 100.0);

    public static final ModConfigSpec.IntValue E1_AOE_COOLDOWN =
            BUILDER.comment("Cooldown do Ataque Massivo (em ticks)")
                    .defineInRange("espada.divisor_almas.cooldown_aoe", 600, 0, 72000);

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