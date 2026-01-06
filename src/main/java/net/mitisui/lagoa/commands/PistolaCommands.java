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

public class PistolaCommands {
    public static void registrar(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("get")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("pistola").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack pistola = new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK);

                    CompoundTag nbt = pistola.getOrCreateTag();
                    nbt.putInt("CustomModelData", 2);

                    CompoundTag display = new CompoundTag();
                    display.putString("Name", Component.Serializer.toJson(
                            Component.literal("Pistola").withStyle(ChatFormatting.DARK_RED)
                    ));

                    ListTag lore = new ListTag();
                    lore.add(StringTag.valueOf(Component.Serializer.toJson(
                            Component.literal("Arma letal - Use com cuidado")
                                    .withStyle(ChatFormatting.GRAY)
                    )));
                    display.put("Lore", lore);
                    nbt.put("display", display);

                    nbt.putBoolean("IsPistola", true);
                    nbt.putInt("Unbreakable", 1);

                    player.addItem(pistola);
                    context.getSource().sendSuccess(() ->
                            Component.literal("Você recebeu uma pistola!")
                                    .withStyle(ChatFormatting.RED), false);
                    return 1;
                }))
        );
    }
}
