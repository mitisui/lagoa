package net.mitisui.lagoa;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Lagoa.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ALLOW_WEB_LOGGER;
    public static final ForgeConfigSpec.ConfigValue<Integer>  WEB_LOGGER_PORT;
    public static final ForgeConfigSpec.BooleanValue ALLOW_LOGGER;
    public static final ForgeConfigSpec.DoubleValue MAX_WITHER_RDIUS;

    public static final ForgeConfigSpec.IntValue SLOWNESS_LEVEL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GLOWING;
    public static final ForgeConfigSpec.BooleanValue PREVENT_ITEM_USE;
    public static final ForgeConfigSpec.BooleanValue PREVENT_ATTACK;
    public static final ForgeConfigSpec.BooleanValue PREVENT_ITEM_PICKUP;
    public static final ForgeConfigSpec.BooleanValue PREVENT_ITEM_DROP;
    public static final ForgeConfigSpec.BooleanValue PREVENT_BLOCK_BREAK;
    public static final ForgeConfigSpec.BooleanValue PREVENT_BLOCK_PLACE;
    public static final ForgeConfigSpec.BooleanValue PREVENT_INTERACTIONS;
    public static final ForgeConfigSpec.IntValue MAX_ARREST_TIME; // Em minutos, 0 = sem limite


    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Configurações da Lagoa");

        ALLOW_WEB_LOGGER = BUILDER
                .comment("Habilita ou desabilita a visualiza;'ao web dos logs.")
                .define("allowWebLogger", true);

        WEB_LOGGER_PORT = BUILDER
                .comment("Porta a ser utilizada pelo web viwer de logs.")
                .define("webLoggerPort", 5646);

        ALLOW_LOGGER = BUILDER
                .comment("Habilita ou desabilita a criação de logs de eventos (Kills, Bosses, Pets) no servidor.")
                .define("allowLogger", true);

        MAX_WITHER_RDIUS = BUILDER
                .comment("Tamanho do raio de deteção de players")
                        .defineInRange("witherRadius", 64.0, 1.0,512.0);

        BUILDER.pop();

//        algemas

        BUILDER.push("Configurações da Algema");
        SLOWNESS_LEVEL = BUILDER
                .comment("Nível do efeito de lentidão aplicado ao jogador preso (0-255)")
                .defineInRange("slowness_level", 150, 0, 255);

        ENABLE_GLOWING = BUILDER
                .comment("Ativar efeito de glowing no jogador preso")
                .define("enable_glowing", true);

        PREVENT_ITEM_USE = BUILDER
                .comment("Impedir jogador preso de usar itens")
                .define("prevent_item_use", true);

        PREVENT_ITEM_PICKUP = BUILDER
                .comment("Impedir jogador preso de pegar itens do chão")
                .define("prevent_item_pickup", true);

        PREVENT_ITEM_DROP = BUILDER
                .comment("Impedir jogador preso de jogar itens no chão")
                .define("prevent_item_drop", true);

        PREVENT_ATTACK = BUILDER
                .comment("Impedir jogador preso de atacar")
                .define("prevent_attack", true);

        PREVENT_BLOCK_BREAK = BUILDER
                .comment("Impedir jogador preso de quebrar blocos")
                .define("prevent_block_break", true);

        PREVENT_BLOCK_PLACE = BUILDER
                .comment("Impedir jogador preso de colocar blocos")
                .define("prevent_block_place", true);

        PREVENT_INTERACTIONS = BUILDER
                .comment("Impedir jogador preso de interagir com blocos (baús, portas, etc)")
                .define("prevent_interactions", true);

        MAX_ARREST_TIME = BUILDER
                .comment("Tempo máximo de prisão em minutos (0 = sem limite)")
                .defineInRange("max_arrest_time", 0, 0, 100000);

        BUILDER.pop();



        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}