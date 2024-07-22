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
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.function.Function;

import static com.malaclord.clientcommands.client.ClientCommandsClient.*;
import static com.malaclord.clientcommands.client.util.PlayerMessage.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@SuppressWarnings("unchecked")
public class ClientEnchantCommand {
    private static final Function<EnchantmentMessageData, Text> ADD_SUCCESS_MESSAGE = data -> Text.translatable("commands.client.enchant.add.success", data.enchantmentRegistryEntry.value().description(), data.itemStack.getName());
    private static final Function<EnchantmentMessageData, Text> REMOVE_SUCCESS_MESSAGE = data -> Text.translatable("commands.client.enchant.remove.success", data.enchantmentRegistryEntry.value().description(), data.itemStack.getName());
    private static final Function<ItemStack, Text> CLEAR_SUCCESS_MESSAGE = itemStack -> Text.translatable("commands.client.enchant.clear.success",itemStack.getName());
    private static final Function<EnchantmentMessageData, Text> REMOVE_NO_ENCHANTMENT_ERROR_MESSAGE = data -> Text.translatable("commands.client.enchant.remove.no_enchantment", data.itemStack.getName(), data.enchantmentRegistryEntry.value().description());

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

        if (checkNotCreative(player)) return 0;
        if (checkNotHoldingItem(player)) return 0;

        var item = player.getMainHandStack();
        var enchantment = RegistryEntryReferenceArgumentType.getEnchantment((CommandContext<ServerCommandSource>) (Object) context,"enchantment");

        if (item.get(DataComponentTypes.ENCHANTMENTS) == null) {
            item.set(DataComponentTypes.ENCHANTMENTS,ItemEnchantmentsComponent.DEFAULT);
        }
        item.addEnchantment(enchantment,IntegerArgumentType.getInteger(context,"level"));

        success(player,ADD_SUCCESS_MESSAGE.apply(new EnchantmentMessageData(enchantment,player.getMainHandStack())),context.getInput());

        syncInventory();

        return 1;
    }

    private static int executeClear(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (checkNotCreative(player)) return 0;
        if (checkNotHoldingItem(player)) return 0;

        player.getInventory().getMainHandStack().set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        success(player,CLEAR_SUCCESS_MESSAGE.apply(player.getMainHandStack()),context.getInput());

        syncInventory();

        return 1;
    }

    private static int executeRemove(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (checkNotCreative(player)) return 0;
        if (checkNotHoldingItem(player)) return 0;

        RegistryEntry.Reference<Enchantment> enchantment = RegistryEntryReferenceArgumentType.getEnchantment((CommandContext<ServerCommandSource>) (Object) context,"enchantment");

        ItemEnchantmentsComponent ec = player.getInventory().getMainHandStack().get(DataComponentTypes.ENCHANTMENTS);

        if (ec == null || !ec.getEnchantments().contains(enchantment)) {
            error(player,REMOVE_NO_ENCHANTMENT_ERROR_MESSAGE.apply(new EnchantmentMessageData(enchantment,player.getMainHandStack())));
            return 0;
        }

        var enchantments = ec.getEnchantments();

        var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

        for (RegistryEntry<Enchantment> e : enchantments) {
            if (e == enchantment) continue;

            builder.add(e,ec.getLevel(e));
        }

        player.getInventory().getMainHandStack().set(DataComponentTypes.ENCHANTMENTS, builder.build());

        success(player,REMOVE_SUCCESS_MESSAGE.apply(new EnchantmentMessageData(enchantment,player.getMainHandStack())),context.getInput());

        syncInventory();

        return 1;
    }

    private record EnchantmentMessageData(RegistryEntry<Enchantment> enchantmentRegistryEntry, ItemStack itemStack) {}
}
