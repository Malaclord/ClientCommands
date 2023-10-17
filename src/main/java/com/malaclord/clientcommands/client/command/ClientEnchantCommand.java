package com.malaclord.clientcommands.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;

import static com.malaclord.clientcommands.client.ClientCommandsClient.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@SuppressWarnings("unchecked")
public class ClientEnchantCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("client")
                .then(literal("enchant")
                        .then(literal("add").then(argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
                                .then(argument("level", IntegerArgumentType.integer(0,32767))
                                        .executes(ClientEnchantCommand::execute))))
                        .then(literal("clear").executes(ClientEnchantCommand::executeClear))));
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (MinecraftClient.getInstance().player == null) return 0;
        if (isGameModeNotCreative()) {
            sendNotInCreativeMessage();
            return 0;
        }

        MinecraftClient.getInstance().player.getInventory().getMainHandStack().addEnchantment(RegistryEntryArgumentType.getEnchantment((CommandContext<ServerCommandSource>) (Object) context,"enchantment").value(),IntegerArgumentType.getInteger(context,"level"));

        syncInventory();

        return 1;
    }

    private static int executeClear(CommandContext<FabricClientCommandSource> context) {
        if (MinecraftClient.getInstance().player == null) return 0;
        if (isGameModeNotCreative()) {
            sendNotInCreativeMessage();
            return 0;
        }

        MinecraftClient.getInstance().player.getInventory().getMainHandStack().getEnchantments().clear();

        syncInventory();

        return 1;
    }

}
