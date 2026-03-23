package net.mitisui.lagoa.mecanicas.cargos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CargoManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/lagoa/cargos.json");
    private static Map<String, Cargo> CARGOS = new HashMap<>();
    private static final Map<UUID, String> PLAYER_CARGO_MAP = new HashMap<>();

    private static MinecraftServer currentServer;
    public static void setServer(MinecraftServer server) {
        currentServer = server;
    }

    public static void carregar() {
        if (!FILE.exists()) {
            CARGOS = criarCargosDefault();
            // Reconstrói o mapa de players dos cargos default
            CARGOS.values().forEach(cargo ->
                    cargo.jogadores.forEach(uuid -> PLAYER_CARGO_MAP.put(uuid, cargo.id))
            );
            salvarTudo();
            return;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(FILE), StandardCharsets.UTF_8)) {
            CARGOS = GSON.fromJson(reader, new TypeToken<Map<String, Cargo>>(){}.getType());
            CARGOS.values().forEach(cargo ->
                    cargo.jogadores.forEach(uuid -> PLAYER_CARGO_MAP.put(uuid, cargo.id))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void salvarTudo() {
        try {
            if (!FILE.getParentFile().exists()) FILE.getParentFile().mkdirs();
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8)) {
                GSON.toJson(CARGOS, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Cargo getCargo(String id) {
        return CARGOS.get(id);
    }

    public static Cargo getCargoDoPlayer(UUID uuid) {
        String id = PLAYER_CARGO_MAP.get(uuid);
        return id != null ? CARGOS.get(id) : null;
    }

    public static void salvarCargo(Cargo cargo) {
        CARGOS.put(cargo.id, cargo);
        salvarTudo();
    }

    public static void deletarCargo(String id) {
        CARGOS.remove(id);
        PLAYER_CARGO_MAP.entrySet().removeIf(entry -> entry.getValue().equals(id));
        salvarTudo();
    }

    public static boolean existe(String id) {
        return CARGOS.containsKey(id);
    }

    // Vincula o player ao cargo e aplica os buffs na hora
    public static void vincularPlayer(ServerPlayer player, Cargo cargo) {
        desvincularPlayer(player); // Remove cargo antigo se houver
        cargo.jogadores.add(player.getUUID());
        PLAYER_CARGO_MAP.put(player.getUUID(), cargo.id);
        aplicarBuffs(player, cargo);
        salvarTudo();
    }

    public static void desvincularPlayer(ServerPlayer player) {
        Cargo antigo = getCargoDoPlayer(player.getUUID());
        if (antigo != null) {
            antigo.jogadores.remove(player.getUUID());
            removerBuffs(player);
        }
        PLAYER_CARGO_MAP.remove(player.getUUID());
        salvarTudo();
    }

    // A MÁGICA: Aplica os atributos reais no Minecraft
    public static void aplicarBuffs(ServerPlayer player, Cargo cargo) {
        // Vida Extra
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(20.0 + cargo.vidaExtra);

        // Dano Extra
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(1.0 + cargo.danoExtra);

        // Velocidade
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.1 + (cargo.velocidadeExtra / 10));

        // Voo (Server-side)
        player.getAbilities().mayfly = cargo.podeVoar;
        player.onUpdateAbilities();

        // Se a vida atual for maior que a nova vida máxima, ajusta
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    public static void removerBuffs(ServerPlayer player) {
        // Reseta para os valores padrão do Minecraft
        Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20.0);
        Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(1.0);
        Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.1);

        if (!player.isCreative()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    public static void atualizarPlayersDoCargo(Cargo cargo) {
        if (currentServer == null) return;
        cargo.jogadores.forEach(uuid -> {
            ServerPlayer player = currentServer.getPlayerList().getPlayer(uuid);
            if (player != null) aplicarBuffs(player, cargo);
        });
    }

    // Auxiliar para o auto-complete dos comandos
    public static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> sugerirCargos(SuggestionsBuilder builder) {
        CARGOS.keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }
    private static Map<String, Cargo> criarCargosDefault() {
        Map<String, Cargo> defaults = new LinkedHashMap<>();

        // -------------------------------------------------------
        // Morador — cargo base, sem privilégios especiais
        // -------------------------------------------------------
        Cargo morador = new Cargo();
        morador.id           = "morador";
        morador.vidaExtra    = 0;
        morador.danoExtra    = 0;
        morador.velocidadeExtra = 0;
        morador.podeVoar     = false;
        morador.jogadores    = new HashSet<>();
        defaults.put(morador.id, morador);

        // -------------------------------------------------------
        // Delegado — equivalente a moderador
        // -------------------------------------------------------
        Cargo delegado = new Cargo();
        delegado.id           = "delegado";
        delegado.vidaExtra    = 10;   // 15 corações no total
        delegado.danoExtra    = 1;    // 1 coração de dano extra
        delegado.velocidadeExtra = 0;
        delegado.podeVoar     = true;
        delegado.jogadores    = new HashSet<>();
        defaults.put(delegado.id, delegado);

        // -------------------------------------------------------
        // Vice-Prefeito — player de confiança
        // -------------------------------------------------------
        Cargo vice = new Cargo();
        vice.id           = "vice_prefeito";
        vice.vidaExtra    = 20;  // 20 corações no total
        vice.danoExtra    = 2;
        vice.velocidadeExtra = 1;
        vice.podeVoar     = true;
        vice.jogadores    = new HashSet<>();
        defaults.put(vice.id, vice);

        // -------------------------------------------------------
        // Prefeita — dona do server / streamer
        // -------------------------------------------------------
        Cargo prefeita = new Cargo();
        prefeita.id           = "prefeita";
        prefeita.vidaExtra    = 40;  // 30 corações no total
        prefeita.danoExtra    = 4;
        prefeita.velocidadeExtra = 2;
        prefeita.podeVoar     = true;
        prefeita.jogadores    = new HashSet<>();
        defaults.put(prefeita.id, prefeita);

        return defaults;
    }
}