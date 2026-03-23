package net.mitisui.lagoa.mecanicas.area;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LCMDarea {

    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("area")
                .requires(s -> s.hasPermission(perm))

                // /lagoa area <nome> pos1 <p1> pos2 <p2> <legenda> <flags...>
                .then(Commands.argument("nome", StringArgumentType.string())
                        .then(Commands.literal("pos1")
                                .then(Commands.argument("p1", BlockPosArgument.blockPos())
                                        .then(Commands.literal("pos2")
                                                .then(Commands.argument("p2", BlockPosArgument.blockPos())
                                                        .then(Commands.argument("legenda", StringArgumentType.string())
                                                                .then(Commands.argument("bloq_magia", BoolArgumentType.bool())
                                                                        .then(Commands.argument("pode_quebrar", BoolArgumentType.bool())
                                                                                .then(Commands.argument("mobs_todos", BoolArgumentType.bool())
                                                                                        .then(Commands.argument("pode_colocar", BoolArgumentType.bool())
                                                                                                .then(Commands.argument("mobs_hostis", BoolArgumentType.bool())
                                                                                                        .executes(LCMDarea::criarAreaQuadrada)
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )

                        // /lagoa area <nome> raio <dist> <legenda> <flags...>
                        .then(Commands.literal("raio")
                                .then(Commands.argument("distancia", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("legenda", StringArgumentType.string())
                                                .then(Commands.argument("bloq_magia", BoolArgumentType.bool())
                                                        .then(Commands.argument("pode_quebrar", BoolArgumentType.bool())
                                                                .then(Commands.argument("mobs_todos", BoolArgumentType.bool())
                                                                        .then(Commands.argument("pode_colocar", BoolArgumentType.bool())
                                                                                .then(Commands.argument("mobs_hostis", BoolArgumentType.bool())
                                                                                        .executes(LCMDarea::criarAreaCircular)
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )

                // /lagoa area player <alvo> add/remove <area>
                .then(Commands.literal("player")
                        .then(Commands.argument("alvo", EntityArgument.player())
                                .then(Commands.literal("add")
                                        .then(Commands.argument("area_nome", StringArgumentType.string())
                                                .executes(LCMDarea::addExcecao)))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("area_nome", StringArgumentType.string())
                                                .executes(LCMDarea::removeExcecao)))
                        )
                )

                // /lagoa area info <nome>  — nível 1
                .then(Commands.literal("info")
                        .requires(s -> s.hasPermission(1))
                        .then(Commands.argument("area_nome", StringArgumentType.string())
                                .executes(ctx -> {
                                    String nome = StringArgumentType.getString(ctx, "area_nome");
                                    AreaRegion area = AreaManager.getArea(nome);
                                    if (area == null) {
                                        ctx.getSource().sendFailure(Component.literal("§cÁrea não encontrada!"));
                                        return 0;
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "§6[Área] §f" + area.nome + "\n" +
                                                    "§7Legenda: §f" + area.legenda + "\n" +
                                                    "§7Forma: §f" + (area.isCircular ? "Circular (raio " + area.raio + ")" : "Quadrada") + "\n" +
                                                    "§7Bloqueia magia: §f" + area.bloqueiaMagia + "\n" +
                                                    "§7Pode quebrar: §f" + area.podeQuebrar + " §7| Colocar: §f" + area.podeColocar
                                    ), false);
                                    return 1;
                                })
                        )
                )

                // /lagoa area lista — nível 1
                .then(Commands.literal("lista")
                        .requires(s -> s.hasPermission(1))
                        .executes(ctx -> {
                            var todas = AreaManager.getTodasAreas();
                            if (todas.isEmpty()) {
                                ctx.getSource().sendSuccess(() -> Component.literal("§7Nenhuma área cadastrada."), false);
                                return 1;
                            }
                            StringBuilder sb = new StringBuilder("§6Áreas cadastradas:\n");
                            todas.forEach(a -> sb.append("§7- §f").append(a.nome).append("\n"));
                            ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                            return 1;
                        })
                );
    }

    private static int criarAreaQuadrada(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String nome = StringArgumentType.getString(ctx, "nome");
        AreaRegion area = new AreaRegion(nome);
        area.isCircular = false;
        area.pos1 = BlockPosArgument.getLoadedBlockPos(ctx, "p1");
        area.pos2 = BlockPosArgument.getLoadedBlockPos(ctx, "p2");
        preencherFlags(ctx, area);
        AreaManager.salvarArea(area);
        ctx.getSource().sendSuccess(() -> Component.literal("§aÁrea quadrada §f" + nome + " §acriada!"), true);
        return 1;
    }

    private static int criarAreaCircular(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String nome = StringArgumentType.getString(ctx, "nome");
        AreaRegion area = new AreaRegion(nome);
        area.isCircular = true;
        area.centro = BlockPosArgument.getLoadedBlockPos(ctx, "p1");
        area.raio = IntegerArgumentType.getInteger(ctx, "distancia");
        preencherFlags(ctx, area);
        AreaManager.salvarArea(area);
        ctx.getSource().sendSuccess(() -> Component.literal("§aÁrea circular §f" + nome + " §acriada!"), true);
        return 1;
    }

    private static void preencherFlags(CommandContext<CommandSourceStack> ctx, AreaRegion area) {
        area.legenda = StringArgumentType.getString(ctx, "legenda");
        area.bloqueiaMagia = BoolArgumentType.getBool(ctx, "bloq_magia");
        area.podeQuebrar = BoolArgumentType.getBool(ctx, "pode_quebrar");
        area.podeColocar = BoolArgumentType.getBool(ctx, "pode_colocar");
        area.spawnaTodosMobs = BoolArgumentType.getBool(ctx, "mobs_todos");
        area.spawnaHostis = BoolArgumentType.getBool(ctx, "mobs_hostis");
    }

    private static int addExcecao(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "alvo");
            String nomeArea = StringArgumentType.getString(ctx, "area_nome");
            AreaRegion area = AreaManager.getArea(nomeArea);
            if (area == null) { ctx.getSource().sendFailure(Component.literal("§cÁrea não encontrada!")); return 0; }
            area.excecoes.add(player.getUUID());
            AreaManager.salvarTudo();
            ctx.getSource().sendSuccess(() -> Component.literal("§a" + player.getName().getString() + " agora é exceção na área §f" + nomeArea), true);
            return 1;
        } catch (Exception e) { return 0; }
    }

    private static int removeExcecao(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "alvo");
            String nomeArea = StringArgumentType.getString(ctx, "area_nome");
            AreaRegion area = AreaManager.getArea(nomeArea);
            if (area != null) { area.excecoes.remove(player.getUUID()); AreaManager.salvarTudo(); }
            ctx.getSource().sendSuccess(() -> Component.literal("§e" + player.getName().getString() + " removido das exceções de §f" + nomeArea), true);
            return 1;
        } catch (Exception e) { return 0; }
    }
}