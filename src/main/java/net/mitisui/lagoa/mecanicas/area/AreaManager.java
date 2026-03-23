package net.mitisui.lagoa.mecanicas.area;

import com.google.gson.*;
import net.minecraft.core.BlockPos;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AreaManager {
    private static final Path ARQUIVO = Path.of("config/lagoa/areas.json");
    private static final Map<String, AreaRegion> areas = new HashMap<>();

    public static void carregar() {
        if (!Files.exists(ARQUIVO)) { salvarTudo(); return; }
        try (Reader r = Files.newBufferedReader(ARQUIVO)) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            areas.clear();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                AreaRegion area = new AreaRegion(obj.get("nome").getAsString());
                area.legenda = obj.has("legenda") ? obj.get("legenda").getAsString() : "";
                area.isCircular = obj.get("isCircular").getAsBoolean();
                area.bloqueiaMagia = obj.get("bloqueiaMagia").getAsBoolean();
                area.podeQuebrar = obj.get("podeQuebrar").getAsBoolean();
                area.podeColocar = obj.get("podeColocar").getAsBoolean();
                area.spawnaTodosMobs = obj.get("spawnaTodosMobs").getAsBoolean();
                area.spawnaHostis = obj.get("spawnaHostis").getAsBoolean();

                if (area.isCircular) {
                    JsonObject c = obj.getAsJsonObject("centro");
                    area.centro = new BlockPos(c.get("x").getAsInt(), c.get("y").getAsInt(), c.get("z").getAsInt());
                    area.raio = obj.get("raio").getAsInt();
                } else {
                    JsonObject p1 = obj.getAsJsonObject("pos1");
                    JsonObject p2 = obj.getAsJsonObject("pos2");
                    area.pos1 = new BlockPos(p1.get("x").getAsInt(), p1.get("y").getAsInt(), p1.get("z").getAsInt());
                    area.pos2 = new BlockPos(p2.get("x").getAsInt(), p2.get("y").getAsInt(), p2.get("z").getAsInt());
                }

                if (obj.has("excecoes")) {
                    for (JsonElement uuid : obj.getAsJsonArray("excecoes")) {
                        area.excecoes.add(UUID.fromString(uuid.getAsString()));
                    }
                }
                areas.put(area.nome, area);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void salvarTudo() {
        try {
            Files.createDirectories(ARQUIVO.getParent());
            JsonArray arr = new JsonArray();
            for (AreaRegion area : areas.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("nome", area.nome);
                obj.addProperty("legenda", area.legenda);
                obj.addProperty("isCircular", area.isCircular);
                obj.addProperty("bloqueiaMagia", area.bloqueiaMagia);
                obj.addProperty("podeQuebrar", area.podeQuebrar);
                obj.addProperty("podeColocar", area.podeColocar);
                obj.addProperty("spawnaTodosMobs", area.spawnaTodosMobs);
                obj.addProperty("spawnaHostis", area.spawnaHostis);

                if (area.isCircular) {
                    JsonObject c = new JsonObject();
                    c.addProperty("x", area.centro.getX());
                    c.addProperty("y", area.centro.getY());
                    c.addProperty("z", area.centro.getZ());
                    obj.add("centro", c);
                    obj.addProperty("raio", area.raio);
                } else {
                    JsonObject p1 = new JsonObject(); p1.addProperty("x", area.pos1.getX()); p1.addProperty("y", area.pos1.getY()); p1.addProperty("z", area.pos1.getZ());
                    JsonObject p2 = new JsonObject(); p2.addProperty("x", area.pos2.getX()); p2.addProperty("y", area.pos2.getY()); p2.addProperty("z", area.pos2.getZ());
                    obj.add("pos1", p1); obj.add("pos2", p2);
                }

                JsonArray excArr = new JsonArray();
                area.excecoes.forEach(uuid -> excArr.add(uuid.toString()));
                obj.add("excecoes", excArr);
                arr.add(obj);
            }
            Files.writeString(ARQUIVO, new GsonBuilder().setPrettyPrinting().create().toJson(arr));
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void salvarArea(AreaRegion area) {
        areas.put(area.nome, area);
        salvarTudo();
    }

    public static AreaRegion getArea(String nome) { return areas.get(nome); }

    public static AreaRegion getAreaNaPosicao(BlockPos pos) {
        for (AreaRegion area : areas.values()) {
            if (area.contemPosicao(pos)) return area;
        }
        return null;
    }

    public static Collection<AreaRegion> getTodasAreas() { return areas.values(); }
}