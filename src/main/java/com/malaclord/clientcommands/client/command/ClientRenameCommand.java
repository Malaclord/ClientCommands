package com.malaclord.clientcommands.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import static com.malaclord.clientcommands.client.ClientCommandsClient.syncInventory;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@SuppressWarnings(value = "unchecked")
public class ClientRenameCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("client")
                .then(literal("rename").then(literal("json").then(argument("name", TextArgumentType.text()).executes((context -> {
                    if (MinecraftClient.getInstance().player == null) return 0;
                    MinecraftClient.getInstance().player.getMainHandStack().setCustomName(
                            TextArgumentType.getTextArgument((CommandContext<ServerCommandSource>) (Object) context, "name"));

                    syncInventory();

                    return 1;
                })))).then(argument("name", StringArgumentType.string()).executes((context -> {
                    if (MinecraftClient.getInstance().player == null) return 0;
                    MinecraftClient.getInstance().player.getMainHandStack().setCustomName(
                            Text.of(StringArgumentType.getString(context, "name")));

                    syncInventory();

                    return 1;
                })).then(argument("italic", BoolArgumentType.bool()).executes((context -> {
                    if (MinecraftClient.getInstance().player == null) return 0;
                    MinecraftClient.getInstance().player.getMainHandStack().setCustomName(
                            Text.literal(StringArgumentType.getString(context, "name"))
                                    .setStyle(Style.EMPTY.withItalic(BoolArgumentType.getBool(context, "italic"))));

                    syncInventory();

                    return 1;
                }))))));
    }
}
