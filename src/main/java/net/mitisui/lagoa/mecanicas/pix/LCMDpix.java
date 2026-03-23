package net.mitisui.lagoa.mecanicas.pix;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.mitisui.lagoa.mecanicas.permissoes.PermissaoManager;
import net.neoforged.fml.ModList;

public class LCMDpix {
    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("pix")
                // Permissão: cargo de morador OU op nível 1
                .requires(s -> s.getEntity() instanceof ServerPlayer p
                        && (s.hasPermission(perm)
                        || PermissaoManager.temPermissao(p, "pix", 0)))

                .then(Commands.argument("destinatario", EntityArgument.player())
                        .then(Commands.argument("valor", IntegerArgumentType.integer(1))
                                .executes(ctx -> executarPix(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "destinatario"),
                                        IntegerArgumentType.getInteger(ctx, "valor")
                                ))
                        )
                );
    }

    private static int executarPix(CommandSourceStack source, ServerPlayer para, int valor) {
        if (!(source.getEntity() instanceof ServerPlayer de)) {
            source.sendFailure(Component.literal("§cApenas players podem usar o /pix!"));
            return 0;
        }

        if (de.getUUID().equals(para.getUUID())) {
            source.sendFailure(Component.literal("§cVocê não pode fazer pix para si mesmo!"));
            return 0;
        }

        if (!ModList.get().isLoaded("numismatics")) {
            source.sendFailure(Component.literal("§cO mod Numismatics não está instalado!"));
            return 0;
        }

        boolean sucesso = NumismaticsIntegration.transferir(de, para, valor);

        if (!sucesso) {
            source.sendFailure(Component.literal("§cSaldo insuficiente ou erro na transferência!"));
            return 0;
        }

        // Notifica remetente
        de.sendSystemMessage(Component.literal(
                "§aPix enviado! §f" + valor + " §aspurs para §f" + para.getName().getString()));

        // Notifica destinatário com som
        para.sendSystemMessage(Component.literal(
                "§a💸 Pix recebido! §f" + de.getName().getString() +
                        " §ate enviou §f" + valor + " §aspurs!"));
        para.playNotifySound(
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 1.0f, 1.2f
        );

        return 1;
    }
}
