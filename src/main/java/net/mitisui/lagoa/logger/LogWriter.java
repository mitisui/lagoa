package net.mitisui.lagoa.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogWriter {
    private static File ArquivoLog;
    private static final Object LOCK = new Object();

    public static void initialize(String serverName) {
        // Caminho: /logs/lagoa/nome_do_servidor/dd-MM-yyyy-HH-mm.log
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm"));

        File dir = new File("logs/lagoa/" + serverName);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("[LogWriter] Falha ao criar diretório: " + dir.getPath());
                return;
            }
        }

        ArquivoLog = new File(dir, data + "-inicio-" + hora + ".log");

        try {
            if (!ArquivoLog.exists()) {
                if (ArquivoLog.createNewFile()) {
                    writeHeader();
                } else {
                    System.err.println("[LogWriter] Falha ao criar arquivo de log");
                }
            }
        } catch (IOException e) {
            System.err.println("[LogWriter] Erro ao inicializar log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeHeader() {
        write("_______________________ Legenda:");
        write("[A1A] - Player matou Player {pos} {dim}");
        write("[A2A] - Player morreu (causa) {pos} {dim}");
        write("[A3A] - Pet morreu (causa) {nome} {pos}");
        write("[A4A] - Player matou Pet {nome} {pos}");
        write("[A5A] - Boss invocado {pos} {dim}");
        write("[A6A] - Mob com Nametag morreu {nome} {pos}");
        write("[B1B] - Player Entrou/Saiu {pos} {dim}");
        write("[C1C] - Comando executado por PLAYER {player} {comando} {pos} {dim}");
        write("[C2C] - Comando executado por COMMAND BLOCK/SERVIDOR {comando} {origem} {pos} {dim}");
        write("_______________________ Início do Log:");
    }

    public static void write(String mensagem) {
        if (ArquivoLog == null) {
            System.err.println("[LogWriter] Arquivo de log não foi inicializado!");
            return;
        }

        synchronized (LOCK) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            try (PrintWriter out = new PrintWriter(new FileWriter(ArquivoLog, true))) {
                out.println("[" + timestamp + "] " + mensagem);
            } catch (IOException e) {
                System.err.println("[LogWriter] Erro ao escrever no log: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static File getArquivoLog() {
        return ArquivoLog;
    }

    public static boolean isInitialized() {
        return ArquivoLog != null && ArquivoLog.exists();
    }
}