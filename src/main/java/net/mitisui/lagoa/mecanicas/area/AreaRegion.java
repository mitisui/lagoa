package net.mitisui.lagoa.mecanicas.area;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AreaRegion {
    public String nome;
    public String legenda = "";

    // Forma
    public boolean isCircular = false;

    // Quadrada
    public BlockPos pos1;
    public BlockPos pos2;

    // Circular
    public BlockPos centro;
    public int raio = 0;

    // Flags de comportamento
    public boolean bloqueiaMagia = false;
    public boolean podeQuebrar = true;
    public boolean podeColocar = true;
    public boolean spawnaTodosMobs = true;
    public boolean spawnaHostis = true;

    // Players com exceção às regras da área
    public List<UUID> excecoes = new ArrayList<>();

    public AreaRegion(String nome) {
        this.nome = nome;
    }

    public boolean contemPosicao(BlockPos pos) {
        if (isCircular) {
            if (centro == null) return false;
            double dx = pos.getX() - centro.getX();
            double dz = pos.getZ() - centro.getZ();
            return (dx * dx + dz * dz) <= (raio * raio);
        } else {
            if (pos1 == null || pos2 == null) return false;
            int minX = Math.min(pos1.getX(), pos2.getX());
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());
            return pos.getX() >= minX && pos.getX() <= maxX
                    && pos.getY() >= minY && pos.getY() <= maxY
                    && pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }
    }
}