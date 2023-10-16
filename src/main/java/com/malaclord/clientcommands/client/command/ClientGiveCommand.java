package com.malaclord.clientcommands.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameMode;

import java.util.Objects;

import static com.malaclord.clientcommands.client.ClientCommandsClient.syncInventory;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class ClientGiveCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {

        dispatcher
                .register(literal("client").then(literal("give")
                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                        .then(argument("amount", IntegerArgumentType.integer(0))
                                .executes(ClientGiveCommand::execute)
                        )).then(argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(ClientGiveCommand::execute)))
        );
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        ItemStack itemStack;

        try {
            int amount = 1;

            try {
                amount = IntegerArgumentType.getInteger(context,"amount");
            } catch (Exception ignored) {}

            itemStack = ItemStackArgumentType.getItemStackArgument(context,"item").createStack(amount,false);
        } catch (Exception ignored) {
            return 0;
        }

        // Just making sure.
        if (MinecraftClient.getInstance().player == null) return 0;

        // Only try giving item if in creative.
        if (Objects.requireNonNull(Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerListEntry(MinecraftClient.getInstance().player.getUuid())).getGameMode() != GameMode.CREATIVE) return 0;

        // Give item to player.
        MinecraftClient.getInstance().player.giveItemStack(itemStack);

        syncInventory();

        return 1;
    }
}
