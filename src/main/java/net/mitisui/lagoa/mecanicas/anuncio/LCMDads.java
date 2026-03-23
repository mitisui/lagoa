package net.mitisui.lagoa.mecanicas.anuncio;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.mitisui.lagoa.mecanicas.permissoes.PermissaoManager;

public class LCMDads {
    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("anuncio")
                .requires(s -> s.hasPermission(perm)
                        || (s.getEntity() instanceof ServerPlayer p
                        && PermissaoManager.temPermissao(p, "lagoa.anuncio", 1)))

                // /lagoa anuncio <mensagem>
                .then(Commands.argument("mensagem", StringArgumentType.string())
                        .executes(ctx -> enviarAnuncio(ctx.getSource(),
                                StringArgumentType.getString(ctx, "mensagem"),
                                null, null, null, null))

                        // /lagoa anuncio <mensagem> url <link> <texto_botao>
                        .then(Commands.literal("url")
                                .then(Commands.argument("link", StringArgumentType.string())
                                        .then(Commands.argument("texto_botao", StringArgumentType.string())
                                                .executes(ctx -> enviarAnuncio(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "mensagem"),
                                                        StringArgumentType.getString(ctx, "link"),
                                                        StringArgumentType.getString(ctx, "texto_botao"),
                                                        null, null))

                                                // /lagoa anuncio <msg> url <link> <texto> tp <comando> <texto_tp>
                                                .then(Commands.literal("tp")
                                                        .then(Commands.argument("comando_tp", StringArgumentType.string())
                                                                .then(Commands.argument("texto_tp", StringArgumentType.string())
                                                                        .executes(ctx -> enviarAnuncio(ctx.getSource(),
                                                                                StringArgumentType.getString(ctx, "mensagem"),
                                                                                StringArgumentType.getString(ctx, "link"),
                                                                                StringArgumentType.getString(ctx, "texto_botao"),
                                                                                StringArgumentType.getString(ctx, "comando_tp"),
                                                                                StringArgumentType.getString(ctx, "texto_tp")))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )

                        // /lagoa anuncio <mensagem> tp <comando> <texto_botao>
                        .then(Commands.literal("tp")
                                .then(Commands.argument("comando_tp", StringArgumentType.string())
                                        .then(Commands.argument("texto_tp", StringArgumentType.string())
                                                .executes(ctx -> enviarAnuncio(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "mensagem"),
                                                        null, null,
                                                        StringArgumentType.getString(ctx, "comando_tp"),
                                                        StringArgumentType.getString(ctx, "texto_tp")))
                                        )
                                )
                        )
                );
    }

    private static int enviarAnuncio(CommandSourceStack source, String mensagem,
                                     String url, String urlTexto,
                                     String comando, String comandoTexto) {

        var payload = new AnuncioValidator.AnuncioPayload(mensagem,
                source.getTextName(), url, urlTexto, comando, comandoTexto);

        var resultado = AnuncioValidator.validar(payload);
        if (!resultado.valido()) {
            source.sendFailure(Component.literal(resultado.erro()));
            return 0;
        }

        // Monta o Component final
        MutableComponent anuncio = Component.empty()
                .append(Component.literal("§8[§6Anúncio§8] §e" + source.getTextName() + "§7: "))
                .append(Component.literal("§f" + mensagem));

        // Botão de URL
        if (url != null) {
            anuncio.append(Component.literal(" §7["))
                    .append(Component.literal("§b" + (urlTexto != null ? urlTexto : url))
                            .setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("§7Abrir: §b" + url)))))
                    .append(Component.literal("§7]"));
        }

        // Botão de teleporte
        if (comando != null) {
            anuncio.append(Component.literal(" §7["))
                    .append(Component.literal("§a" + (comandoTexto != null ? comandoTexto : "Ir agora"))
                            .setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("§7Clique para teleportar")))))
                    .append(Component.literal("§7]"));
        }

        // Envia para todos
        source.getServer().getPlayerList().getPlayers()
                .forEach(p -> p.sendSystemMessage(anuncio));

        return 1;
    }
}
