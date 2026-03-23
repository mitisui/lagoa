package net.mitisui.lagoa.mecanicas.pix;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * Integração com Create Numismatics via comandos nativos do mod.
 * Comandos usados:
 *   /numismatics pay <player> <valor>    — transfere para outro player
 *   /numismatics deduct <player> <valor> — debita (requer OP)
 *   /numismatics view <player>           — consulta saldo
 */
public class NumismaticsIntegration {

    /**
     * Transfere valor da conta do remetente para o destinatário
     * usando /numismatics pay — executado como o próprio remetente.
     */
    public static boolean transferir(ServerPlayer de, ServerPlayer para, int valor) {
        try {
            String cmd = "numismatics pay " + para.getGameProfile().getName() + " " + valor;
            return executarComando(cmd, de.createCommandSourceStack());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Debita valor da conta de um player (requer permissão de admin).
     * Útil para cobranças de taxas, multas, etc.
     */
    public static boolean debitar(ServerPlayer player, int valor, CommandSourceStack adminSource) {
        try {
            String cmd = "numismatics deduct " + player.getGameProfile().getName() + " " + valor;
            return executarComando(cmd, adminSource);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se o Numismatics está respondendo para o player.
     * Usa /numismatics view sem output visível.
     */
    public static boolean disponivel(ServerPlayer player) {
        try {
            String cmd = "numismatics view " + player.getGameProfile().getName();
            // Executa com output suprimido só para checar se o comando existe
            return executarComando(cmd,
                    player.createCommandSourceStack().withSuppressedOutput());
        } catch (Exception e) {
            return false;
        }
    }

    // ---- Interno ----

    private static boolean executarComando(String cmd, CommandSourceStack source) {
        try {
            var dispatcher = source.getServer().getCommands().getDispatcher();
            ParseResults<CommandSourceStack> parsed = dispatcher.parse(cmd, source);
            return dispatcher.execute(parsed) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}