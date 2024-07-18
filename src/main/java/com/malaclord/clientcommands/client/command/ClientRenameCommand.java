package com.malaclord.clientcommands.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.malaclord.clientcommands.client.ClientCommandsClient.syncInventory;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@SuppressWarnings(value = "unchecked")
public class ClientRenameCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("client")
                .then(literal("rename").then(literal("json").then(argument("name", TextArgumentType.text(registryAccess)).executes((context -> {
                    if (MinecraftClient.getInstance().player == null) return 0;

                    MinecraftClient.getInstance().player.getMainHandStack().set(DataComponentTypes.ITEM_NAME,
                            TextArgumentType.getTextArgument((CommandContext<ServerCommandSource>) (Object) context, "name"));

                    syncInventory();

                    return 1;
                })))).then(argument("name", StringArgumentType.string()).executes((context -> {
                    if (MinecraftClient.getInstance().player == null) return 0;
                    MinecraftClient.getInstance().player.getMainHandStack().set(DataComponentTypes.ITEM_NAME,
                            Text.of(StringArgumentType.getString(context, "name")));

                    syncInventory();

                    return 1;
                })).then(argument("customName", BoolArgumentType.bool()).executes((context -> {
                    if (MinecraftClient.getInstance().player == null) return 0;
                    MinecraftClient.getInstance().player.getMainHandStack().set(BoolArgumentType.getBool(context, "customName") ? DataComponentTypes.CUSTOM_NAME : DataComponentTypes.ITEM_NAME,
                            Text.literal(StringArgumentType.getString(context, "name")));

                    syncInventory();

                    return 1;
                }))))));
    }
}
