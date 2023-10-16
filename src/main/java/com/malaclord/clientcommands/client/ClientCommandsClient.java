package com.malaclord.clientcommands.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameMode;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.*;

public class ClientCommandsClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        addCommands();
    }

    private void addCommands() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder argumentBuilder =
            literal("client")
                .then(literal("give")
                        .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .then(argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    return giveCommand(context);
                                })

                        )).then(argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(context -> {
                            return giveCommand(context);
                        })));

        dispatcher.register(argumentBuilder);

        }));
    }

    private int giveCommand(CommandContext context) {
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

        return 1;
    }
}
