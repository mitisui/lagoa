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
    public static final ForgeConfigSpec.BooleanValue SERVER_COMMANDS_ENABLED;
    public static final ForgeConfigSpec.BooleanValue PLAYER_COMMANDS_ENABLED;

//    pistola
    public static final ForgeConfigSpec.BooleanValue ENABLE_PISTOLA;
    public static final ForgeConfigSpec.DoubleValue PISTOLA_RANGE;
    public static final ForgeConfigSpec.DoubleValue SHOT_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<String> BULLET_EFFECT;


//    algema
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
    public static final ForgeConfigSpec.BooleanValue TELEPORT_ON_BLOCK_BREAK;
    public static final ForgeConfigSpec.DoubleValue MAX_DISTANCE_BEFORE_PULL;

//     itens globais
    public static final ForgeConfigSpec.BooleanValue ENABLE_TELEFONE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CORNETAS;
    public static final ForgeConfigSpec.IntValue CORNETA_CUSTO;

//      ESPADAS
//    E1 -> Entidade
    public static final ForgeConfigSpec.DoubleValue E1_DANO_BASE;

    // AOE Attack
    public static final ForgeConfigSpec.DoubleValue E1_ATAQUE_AOE_DANO;
    public static final ForgeConfigSpec.DoubleValue E1_ATAQUE_AOE_AREA;
    public static final ForgeConfigSpec.IntValue E1_AOE_BEACON_HEIGHT;
    public static final ForgeConfigSpec.IntValue E1_AOE_COOLDOWN;

    // Homing Swords
    public static final ForgeConfigSpec.IntValue E1_HOMING_SWORD_COUNT;
    public static final ForgeConfigSpec.DoubleValue E1_HOMING_SPAWN_RADIUS;
    public static final ForgeConfigSpec.DoubleValue E1_HOMING_SPEED;
    public static final ForgeConfigSpec.DoubleValue E1_HOMING_DAMAGE;
    public static final ForgeConfigSpec.IntValue E1_HOMING_LIFETIME;
    public static final ForgeConfigSpec.IntValue E1_HOMING_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue E1_RAYCAST_RANGE;

    // Projectile Shield
    public static final ForgeConfigSpec.BooleanValue E1_ENABLE_PROJECTILE_SHIELD;
    public static final ForgeConfigSpec.IntValue E1_SHIELD_DISTANCE;

//    E2
    public static final ForgeConfigSpec.DoubleValue E2_DANO_BASE;


    // Iron's Spellbooks Compatibility
    public static final ForgeConfigSpec.BooleanValue E1_ENABLE_IRONSPELL_PROTECTION;
    public static final ForgeConfigSpec.IntValue E1_IRONSPELL_BLINDNESS_DURATION;
    public static final ForgeConfigSpec.IntValue E1_IRONSPELL_MANA_PENALTY_DURATION;



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

        SERVER_COMMANDS_ENABLED = BUILDER
                .comment("Ativa ou Desativa a notificação nos logs de comandos do servidor.")
                .define("serverCommands", false);

        PLAYER_COMMANDS_ENABLED = BUILDER
                .comment("Ativa ou Desativa a notificação nos logs de comandos dos Players (com op).")
                .define("serverCommands", true);

        BUILDER.pop();

//        pistola

        BUILDER.push("Configurações da Pistola");

        ENABLE_PISTOLA = BUILDER
                .comment("Habilita ou Desabilita a pistola")
                .define("enablePistola", true);

        PISTOLA_RANGE = BUILDER
                .comment("Alcance do tiro da pistola")
                .defineInRange("pistolaRange", 50.0,0.0,300.0);

        SHOT_DAMAGE = BUILDER
                .comment("Define o dano da pistola")
                .defineInRange("shotDamage",50.0,0.0, 100000.0);

        BULLET_EFFECT = BUILDER
                .comment("Efeito que a arma da ao player se não morrer")
                .comment("deixar em branco caso não queira")
                .comment("Link com os efeitos do jogo https://minecraft.fandom.com/pt/wiki/Efeito")
                .define("bulletEffect", "");

        BUILDER.pop();


//        algema

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

        TELEPORT_ON_BLOCK_BREAK = BUILDER
                .comment("Teleporta o preso para o oficial ao tentar quebrar blocos")
                .define("teleportOnBlockBreak", true);

        MAX_DISTANCE_BEFORE_PULL = BUILDER
                .comment("Distância máxima antes de puxar automaticamente")
                .defineInRange("maxDistanceBeforePull", 20.0, 5.0, 100.0);

        BUILDER.pop();

//       Itens globais

        BUILDER.push("Configurações itens globais");

        ENABLE_TELEFONE = BUILDER
                .comment("Permite a criação do celular")
                .define("enableTelefone", true);

        ENABLE_CORNETAS = BUILDER
                .comment("Permite a criação das cornetas")
                .define("enableCorneta", true);

        CORNETA_CUSTO = BUILDER
                .comment("Custo de criação das cornetas na bigornas (nível de xp)")
                .defineInRange("cornetaCusto", 20,5,10000);

        BUILDER.pop();


        BUILDER.push("Configurações das espadas");
        BUILDER.comment("E1 - espada Entidade");

        // Espadas - E1 (Espada de ADM)
        BUILDER.push("Configurações da Espada de ADM (E1)");

        E1_DANO_BASE = BUILDER
                .comment("Dano base da espada ao atacar normalmente")
                .defineInRange("e1DanoBase", 50.0, 1.0, 100000.0);

        BUILDER.comment("=== Ataque AOE (Click direito sem alvo) ===");

        E1_ATAQUE_AOE_DANO = BUILDER
                .comment("Dano contínuo do ataque AOE em área")
                .defineInRange("e1AoeDano", 15.0, 0.0, 200000.0);

        E1_ATAQUE_AOE_AREA = BUILDER
                .comment("Raio da área do ataque AOE (similar ao beacon)")
                .defineInRange("e1AoeArea", 10.0, 1.0, 100.0);

        E1_AOE_BEACON_HEIGHT = BUILDER
                .comment("Altura do efeito de beacon do AOE")
                .defineInRange("e1AoeBeaconHeight", 40, 10, 256);

        E1_AOE_COOLDOWN = BUILDER
                .comment("Cooldown do ataque AOE em ticks (20 ticks = 1 segundo)")
                .defineInRange("e1AoeCooldown", 400, 20, 12000);

        BUILDER.comment("=== Espadas Teleguiadas (Click direito com alvo) ===");

        E1_HOMING_SWORD_COUNT = BUILDER
                .comment("Quantidade de espadas que surgem ao redor do alvo")
                .defineInRange("e1HomingSwordCount", 8, 1, 20);

        E1_HOMING_SPAWN_RADIUS = BUILDER
                .comment("Raio de spawn das espadas ao redor do alvo")
                .defineInRange("e1HomingSpawnRadius", 4.0, 1.0, 10.0);

        E1_HOMING_SPEED = BUILDER
                .comment("Velocidade das espadas teleguiadas (maior = mais rápido)")
                .defineInRange("e1HomingSpeed", 1.5, 0.1, 5.0);

        E1_HOMING_DAMAGE = BUILDER
                .comment("Dano de cada espada teleguiada ao acertar")
                .defineInRange("e1HomingDamage", 40.0, 1.0, 1000.0);

        E1_HOMING_LIFETIME = BUILDER
                .comment("Tempo de vida das espadas teleguiadas em ticks")
                .defineInRange("e1HomingLifetime", 200, 20, 1200);

        E1_HOMING_COOLDOWN = BUILDER
                .comment("Cooldown do ataque teleguiado em ticks")
                .defineInRange("e1HomingCooldown", 100, 20, 6000);

        E1_RAYCAST_RANGE = BUILDER
                .comment("Alcance do raycast para detectar alvos")
                .defineInRange("e1RaycastRange", 50.0, 10.0, 200.0);

        BUILDER.comment("=== Escudo Anti-Projétil (Passivo) ===");

        E1_ENABLE_PROJECTILE_SHIELD = BUILDER
                .comment("Ativa o escudo automático contra projéteis")
                .define("e1EnableProjectileShield", true);

        E1_SHIELD_DISTANCE = BUILDER
                .comment("Distância em blocos para spawnar o escudo de concreto")
                .defineInRange("e1ShieldDistance", 4, 2, 10);

        BUILDER.pop();

        BUILDER.push("Configurações da Espada de ADM (E1)");

        E2_DANO_BASE = BUILDER
                .comment("Dano base da espada ao atacar normalmente")
                .defineInRange("e2DanoBase", 20.0, 1.0, 100000.0);

        BUILDER.pop();

        BUILDER.push("=== Compatibilidade: Iron's Spells 'n Spellbooks ===");

        E1_ENABLE_IRONSPELL_PROTECTION = BUILDER
                .comment("Ativa proteção contra magias do Iron's Spellbooks")
                .comment("Cancela magias e aplica penalidades no atacante")
                .define("e1EnableIronspellProtection", true);

        E1_IRONSPELL_BLINDNESS_DURATION = BUILDER
                .comment("Duração do efeito de cegueira em ticks (20 = 1 segundo)")
                .defineInRange("e1IronspellBlindnessDuration", 200, 20, 6000);

        E1_IRONSPELL_MANA_PENALTY_DURATION = BUILDER
                .comment("Duração da penalidade de mana em ticks (600 = 30 segundos)")
                .defineInRange("e1IronspellManaPenaltyDuration", 600, 100, 12000);

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