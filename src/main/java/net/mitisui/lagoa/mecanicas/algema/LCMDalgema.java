package net.mitisui.lagoa.mecanicas.algema;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

public class LCMDalgema {
    public static LiteralArgumentBuilder<CommandSourceStack> register(int perm) {
        return Commands.literal("algema")
                .requires(s -> s.hasPermission(perm))

                // /lagoa algema <jogador> prender/soltar/status
                .then(Commands.argument("jogador", EntityArgument.player())
                        .then(Commands.literal("prender")
                                .executes(ctx -> {
                                    ServerPlayer officer = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target  = EntityArgument.getPlayer(ctx, "jogador");
                                    AlgemaEvents.handleArrest(officer, target);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("soltar")
                                .executes(ctx -> {
                                    ServerPlayer officer = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target  = EntityArgument.getPlayer(ctx, "jogador");
                                    AlgemaEvents.handleRelease(officer, target, true);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
                                    if (AlgemaEvents.isArrested(target.getUUID())) {
                                        ctx.getSource().sendSuccess(() ->
                                                Component.literal(target.getName().getString() + " está preso!")
                                                        .withStyle(ChatFormatting.YELLOW), false);
                                    } else {
                                        ctx.getSource().sendSuccess(() ->
                                                Component.literal(target.getName().getString() + " não está preso.")
                                                        .withStyle(ChatFormatting.GREEN), false);
                                    }
                                    return 1;
                                })
                        )
                )

                // /lagoa algema get algemas
                .then(Commands.literal("get")
                        .then(Commands.literal("algemas")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ItemStack algema = new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK);

                                    // MIGRAÇÃO: NBT -> DataComponents
                                    algema.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
                                    algema.set(DataComponents.CUSTOM_NAME,
                                            Component.literal("Algemas").withStyle(ChatFormatting.GOLD));

                                    // Marca o item como algema via CustomData
                                    CompoundTag tag = new CompoundTag();
                                    tag.putBoolean("IsAlgema", true);
                                    algema.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                                    player.addItem(algema);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal("Você recebeu as algemas!").withStyle(ChatFormatting.GREEN), false);
                                    return 1;
                                })
                        )
                );
    }
}