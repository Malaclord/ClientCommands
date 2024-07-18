package com.malaclord.clientcommands.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;

import static com.malaclord.clientcommands.client.ClientCommandsClient.*;
import static com.malaclord.clientcommands.client.util.PlayerMessage.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@SuppressWarnings("unchecked")
public class ClientEnchantCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("client")
                .then(literal("enchant")
                        .then(literal("add").then(argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
                                .then(argument("level", IntegerArgumentType.integer(0,255))
                                        .executes(ClientEnchantCommand::execute))))
                        .then(literal("remove").then(argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
                                .executes(ClientEnchantCommand::executeRemove)))
                        .then(literal("clear").executes(ClientEnchantCommand::executeClear))));
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        player.getInventory().getMainHandStack().addEnchantment(RegistryEntryReferenceArgumentType.getEnchantment((CommandContext<ServerCommandSource>) (Object) context,"enchantment"),IntegerArgumentType.getInteger(context,"level"));

        success(player,"Added enchantment!",context.getInput());

        syncInventory();

        return 1;
    }

    private static int executeClear(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        player.getInventory().getMainHandStack().set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        success(player,"Cleared all enchantments from item!",context.getInput());

        syncInventory();

        return 1;
    }

    private static int executeRemove(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        RegistryEntry.Reference<Enchantment> enchantment = RegistryEntryReferenceArgumentType.getEnchantment((CommandContext<ServerCommandSource>) (Object) context,"enchantment");

        ItemEnchantmentsComponent ec = player.getInventory().getMainHandStack().get(DataComponentTypes.ENCHANTMENTS);

        assert ec != null;

        if (!ec.getEnchantments().contains(enchantment)) {
            error(player,"Item does not have enchantment!");
            return 0;
        }

        var enchantments = ec.getEnchantments();

        var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

        for (RegistryEntry<Enchantment> e : enchantments) {
            if (e == enchantment) continue;

            builder.add(e,ec.getLevel(e));
        }

        player.getInventory().getMainHandStack().set(DataComponentTypes.ENCHANTMENTS, builder.build());

        success(player,"Removed enchantment!",context.getInput());

        syncInventory();

        return 1;
    }

}
