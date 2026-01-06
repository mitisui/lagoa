package net.mitisui.lagoa.logger;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WebInterface {
    private static HttpServer server;

    public static void start(int port) throws IOException {
        if (server != null) return;

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            String logContent = "";
            if (LogWriter.getArquivoLog() != null && LogWriter.getArquivoLog().exists()) {
                try {
                    // Lemos os bytes diretamente para evitar problemas de encoding
                    byte[] encoded = Files.readAllBytes(LogWriter.getArquivoLog().toPath());
                    logContent = new String(encoded, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    logContent = "Erro ao ler arquivo de log: " + e.getMessage();
                }
            }

            // Cores (Ajuste para garantir que as tags HTML não quebrem o layout)
            String coloredLog = logContent
                    .replace("[A1A]", "<span style='color:#ff5555;font-weight:bold;'>[A1A]</span>")
                    .replace("[A2A]", "<span style='color:#ff5555;font-weight:bold;'>[A2A]</span>")
                    .replace("[B1B]", "<span style='color:#55ff55;font-weight:bold;'>[B1B]</span>")
                    .replace("[C1C]", "<span style='color:#ffff55;font-weight:bold;'>[C1C]</span>");

            // HTML com META REFRESH (comando do navegador) + JAVASCRIPT (backup)
            String html = "<!DOCTYPE html><html><head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta http-equiv='refresh' content='3'>" + // ATUALIZAÇÃO FORÇADA PELO NAVEGADOR
                    "<title>Lagoa Live Logger</title>" +
                    "<style>" +
                    "body { background: #0c0c0c; color: #dcdcdc; font-family: 'Segoe UI', Consolas, monospace; padding: 20px; }" +
                    "pre { background: #161616; padding: 20px; border-radius: 10px; border: 1px solid #333; " +
                    "white-space: pre-wrap; word-wrap: break-word; font-size: 14px; line-height: 1.6; }" +
                    "h1 { color: #5dade2; margin-bottom: 5px; }" +
                    ".status { color: #888; font-size: 0.9em; margin-bottom: 20px; font-style: italic; }" +
                    "</style>" +
                    "</head><body>" +
                    "<h1>🌊 Lagoa Live Logger</h1>" +
                    "<div class='status'>Atualizando automaticamente via Meta-Refresh (3s)...</div>" +
                    "<pre>" + coloredLog + "</pre>" +
                    "<script>" +
                    "// Backup caso o Meta-Refresh falhe e auto-scroll" +
                    "window.onload = function() { window.scrollTo(0, document.body.scrollHeight); };" +
                    "</script>" +
                    "</body></html>";

            byte[] response = html.getBytes(StandardCharsets.UTF_8);

            // Forçamos o navegador a não guardar cache desta página
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-store, no-cache, must-revalidate");

            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}