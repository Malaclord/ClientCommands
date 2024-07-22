package com.malaclord.clientcommands.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.function.Function;

import static com.malaclord.clientcommands.client.ClientCommandsClient.*;
import static com.malaclord.clientcommands.client.util.PlayerMessage.success;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@SuppressWarnings(value = "unchecked")
public class ClientRenameCommand {
    private static final Function<ItemStack,Text> SUCCESS_MESSAGE = itemStack -> Text.translatable("commands.client.rename.success", itemStack.getName());

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("client")
                .then(literal("rename").then(literal("json").then(argument("name", TextArgumentType.text(registryAccess)).executes((context -> {
                    var player = context.getSource().getPlayer();
                    if (checkNotCreative(player)) return 0;
                    if (checkNotHoldingItem(player)) return 0;

                    player.getMainHandStack().set(DataComponentTypes.ITEM_NAME,
                            TextArgumentType.getTextArgument((CommandContext<ServerCommandSource>) (Object) context, "name"));

                    success(player,SUCCESS_MESSAGE.apply(player.getMainHandStack()));

                    syncInventory();

                    return 1;
                })))).then(argument("name", StringArgumentType.string()).executes((context -> {
                    var player = context.getSource().getPlayer();
                    if (checkNotCreative(player)) return 0;
                    if (checkNotHoldingItem(player)) return 0;

                    player.getMainHandStack().set(DataComponentTypes.ITEM_NAME,
                            Text.of(StringArgumentType.getString(context, "name")));

                    success(player,SUCCESS_MESSAGE.apply(player.getMainHandStack()));

                    syncInventory();

                    return 1;
                })).then(argument("customName", BoolArgumentType.bool()).executes((context -> {
                    var player = context.getSource().getPlayer();
                    if (checkNotCreative(player)) return 0;
                    if (checkNotHoldingItem(player)) return 0;

                    player.getMainHandStack().set(BoolArgumentType.getBool(context, "customName") ? DataComponentTypes.CUSTOM_NAME : DataComponentTypes.ITEM_NAME,
                            Text.literal(StringArgumentType.getString(context, "name")));

                    success(player,SUCCESS_MESSAGE.apply(player.getMainHandStack()));

                    syncInventory();

                    return 1;
                })))))
        );
    }
}
