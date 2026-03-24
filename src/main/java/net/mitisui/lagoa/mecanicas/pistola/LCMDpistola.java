package net.mitisui.lagoa.mecanicas.pistola;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;

import java.util.List;

public class LCMDpistola {
    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("get")
                .requires(s -> s.hasPermission(perm))
                .then(Commands.literal("pistola")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ItemStack pistola = new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK);

                            // CustomModelData
                            pistola.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(2));

                            // Nome
                            pistola.set(DataComponents.CUSTOM_NAME,
                                    Component.literal("Pistola").withStyle(ChatFormatting.DARK_RED));

                            pistola.set(DataComponents.LORE, new ItemLore(List.of(
                                    Component.literal("Arma letal - Use com cuidado").withStyle(ChatFormatting.GRAY)
                            )));

                            pistola.set(DataComponents.UNBREAKABLE, new Unbreakable(false));

                            CompoundTag tag = new CompoundTag();
                            tag.putBoolean("IsPistola", true);
                            pistola.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                            player.addItem(pistola);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("Você recebeu uma pistola!").withStyle(ChatFormatting.RED), false);
                            return 1;
                        })
                );
    }
}
