package net.mitisui.lagoa.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.mitisui.lagoa.Config;
import net.mitisui.lagoa.logger.WebInterface;

public class WebLoggerCommands {

    public static void registrar(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("logger")
                .then(Commands.literal("on").executes(context -> {
                    try {
                        if(Config.ALLOW_WEB_LOGGER.get()) {
                            Integer porta = Config.WEB_LOGGER_PORT.get();
                            WebInterface.start(porta);
                            String url = "http://localhost:" + porta;

                            Component linkComponent = Component.literal(url)
                                    .withStyle(style -> style
                                            .withColor(ChatFormatting.BLUE)
                                            .withUnderlined(true)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Abrir o Log no navegador")))
                                    );

                            Component mensagemFinal = Component.literal("§a Log Web iniciado em: ").append(linkComponent);

                            context.getSource().sendSuccess(() -> mensagemFinal, false);
                        }
                    } catch (Exception e) {
                        context.getSource().sendFailure(Component.literal("Erro ao iniciar: " + e.getMessage()));
                    }
                    return 1;
                }))
                .then(Commands.literal("off").executes(context -> {
                    WebInterface.stop();
                    context.getSource().sendSuccess(() -> Component.literal("§cWeb Log desligado."), true);
                    return 1;
                }))
        );
    }
}
