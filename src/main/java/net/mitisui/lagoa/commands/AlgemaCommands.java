package net.mitisui.lagoa.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.mitisui.lagoa.events.AlgemaEvents;

public class AlgemaCommands {
    public static void registrar(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("algema")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("jogador", EntityArgument.player())
                        .then(Commands.literal("prender")
                                .executes(context -> {
                                    ServerPlayer officer = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "jogador");
                                    AlgemaEvents.handleArrest(officer, target);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("soltar")
                                .executes(context -> {
                                    ServerPlayer officer = context.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(context, "jogador");
                                    AlgemaEvents.handleRelease(officer, target);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("status")
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "jogador");
                                    if (AlgemaEvents.isArrested(target.getUUID())) {
                                        context.getSource().sendSuccess(() ->
                                                Component.literal(target.getName().getString() + " está preso!")
                                                        .withStyle(ChatFormatting.YELLOW), false);
                                    } else {
                                        context.getSource().sendSuccess(() ->
                                                Component.literal(target.getName().getString() + " não está preso.")
                                                        .withStyle(ChatFormatting.GREEN), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );

        root.then(Commands.literal("get")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("algemas").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack algema = new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK);

                    CompoundTag nbt = algema.getOrCreateTag();
                    nbt.putInt("CustomModelData", 1);

                    nbt.putInt("Damage", 60);

                    CompoundTag display = new CompoundTag();
                    display.putString("Name", Component.Serializer.toJson(
                            Component.literal("Algemas").withStyle(ChatFormatting.GOLD)
                    ));

                    ListTag lore = new ListTag();
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(
                            Component.literal("Item especial para prender infratores")
                                    .withStyle(ChatFormatting.GRAY)
                    )));
                    display.put("Lore", lore);
                    nbt.put("display", display);
                    nbt.putBoolean("IsAlgema", true);

                    player.addItem(algema);
                    context.getSource().sendSuccess(() ->
                            Component.literal("Você recebeu as algemas!")
                                    .withStyle(ChatFormatting.GREEN), false);
                    return 1;
                }))
        );
    }
}