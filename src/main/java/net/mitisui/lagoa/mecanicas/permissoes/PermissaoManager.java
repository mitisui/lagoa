package net.mitisui.lagoa.mecanicas.permissoes;

import com.google.gson.*;
import net.minecraft.server.level.ServerPlayer;
import net.mitisui.lagoa.mecanicas.cargos.Cargo;
import net.mitisui.lagoa.mecanicas.cargos.CargoManager;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PermissaoManager {

    private static final Path ARQUIVO = Path.of("config/lagoa/permissoes.json");

    private static Map<String, DadosPermissao> permissoes = new HashMap<>();

    public static void carregar() {
        if (!Files.exists(ARQUIVO)) {
            salvar();
            return;
        }
        try (Reader r = Files.newBufferedReader(ARQUIVO)) {
            JsonObject json = JsonParser.parseReader(r).getAsJsonObject();
            permissoes.clear();
            for (var entry : json.entrySet()) {
                JsonObject dados = entry.getValue().getAsJsonObject();
                DadosPermissao dp = new DadosPermissao();
                dp.nivelPermissao = dados.get("nivelPermissao").getAsInt();
                for (JsonElement cmd : dados.getAsJsonArray("comandosExtras")) {
                    dp.comandosExtras.add(cmd.getAsString());
                }
                permissoes.put(entry.getKey(), dp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void salvar() {
        try {
            Files.createDirectories(ARQUIVO.getParent());
            JsonObject json = new JsonObject();
            for (var entry : permissoes.entrySet()) {
                JsonObject dados = new JsonObject();
                dados.addProperty("nivelPermissao", entry.getValue().nivelPermissao);
                JsonArray cmds = new JsonArray();
                entry.getValue().comandosExtras.forEach(cmds::add);
                dados.add("comandosExtras", cmds);
                json.add(entry.getKey(), dados);
            }
            Files.writeString(ARQUIVO, new GsonBuilder().setPrettyPrinting().create().toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean temPermissao(ServerPlayer player, String comando, int nivelNecessario) {
        UUID playerUUID = player.getUUID();
        Cargo cargo = CargoManager.getCargoDoPlayer(playerUUID);
        if (cargo == null) return false;

        DadosPermissao dp = permissoes.get(cargo.id);
        if (dp == null) return false;

        if (dp.nivelPermissao >= nivelNecessario) return true;

        return dp.comandosExtras.contains(comando);
    }

    public static void setNivel(String cargoId, int nivel) {
        permissoes.computeIfAbsent(cargoId, k -> new DadosPermissao()).nivelPermissao = nivel;
        salvar();
    }

    public static void addComando(String cargoId, String comando) {
        permissoes.computeIfAbsent(cargoId, k -> new DadosPermissao()).comandosExtras.add(comando);
        salvar();
    }

    public static void removeComando(String cargoId, String comando) {
        DadosPermissao dp = permissoes.get(cargoId);
        if (dp != null) {
            dp.comandosExtras.remove(comando);
            salvar();
        }
    }

    public static class DadosPermissao {
        public int nivelPermissao = 0;
        public List<String> comandosExtras = new ArrayList<>();
    }
}