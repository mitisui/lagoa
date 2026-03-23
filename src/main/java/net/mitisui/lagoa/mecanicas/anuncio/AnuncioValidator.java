package net.mitisui.lagoa.mecanicas.anuncio;

import net.mitisui.lagoa.Config;

public class AnuncioValidator {

    public static ValidacaoResult validar(AnuncioPayload payload) {
        if (payload.url() != null && !payload.url().isBlank()) {
            if (!isDominioPermitido(payload.url())) {
                return ValidacaoResult.erro("§cDomínio não permitido: " + payload.url());
            }
        }
        if (payload.comando() != null && !payload.comando().isBlank()) {
            if (isComandoBloqueado(payload.comando())) {
                return ValidacaoResult.erro("§cEste comando não é permitido em anúncios.");
            }
            if (!isComandoPermitido(payload.comando())) {
                return ValidacaoResult.erro("§cApenas comandos de teleporte e interação são permitidos.");
            }
        }
        return ValidacaoResult.ok();
    }

    private static boolean isDominioPermitido(String url) {
        return Config.dominiosPermitidos.stream().anyMatch(url.toLowerCase()::contains);
    }

    private static boolean isComandoPermitido(String cmd) {
        String lower = cmd.toLowerCase().trim();
        return Config.comandosPermitidos.stream().anyMatch(lower::startsWith);
    }

    private static boolean isComandoBloqueado(String cmd) {
        String lower = cmd.toLowerCase().trim();
        return Config.comandosBloqueados.stream().anyMatch(lower::startsWith);
    }

    public record AnuncioPayload(
            String mensagem,
            String autor,
            String url,
            String urlTexto,
            String comando,
            String comandoTexto
    ) {}

    public record ValidacaoResult(boolean valido, String erro) {
        public static ValidacaoResult ok()           { return new ValidacaoResult(true, null); }
        public static ValidacaoResult erro(String m) { return new ValidacaoResult(false, m); }
    }
}