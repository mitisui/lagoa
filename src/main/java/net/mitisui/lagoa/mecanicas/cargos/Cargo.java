package net.mitisui.lagoa.mecanicas.cargos;

import java.util.HashSet;
import java.util.UUID;

public class Cargo {
    public String id;
    public String nomeExibicao;
    public String cor = "§f";

    // Atributos
    public double vidaExtra = 0;
    public double danoExtra = 0;
    public double velocidadeExtra = 0;
    public double resistenciaExtra = 0;

    // Permissões
    public boolean podeVoar = false;
    public boolean levaDano = true;

    public HashSet<UUID> jogadores = new HashSet<>();
    public Cargo() {} // sem referência a id nulo

    public Cargo(String id) {
        this.id = id;
        this.nomeExibicao = id;
    }
}