package com.malaclord.clientcommands.client.command;

import com.malaclord.clientcommands.client.command.argument.PlayerNameArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import static com.malaclord.clientcommands.client.ClientCommandsClient.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientPlayerHeadCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("client").then(literal("player-head")
                .then(argument("name", PlayerNameArgumentType.name()).executes(
                ClientPlayerHeadCommand::execute
        ))));
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        if (MinecraftClient.getInstance().player == null) return 0;
        if (isGameModeNotCreative()) {
            sendNotInCreativeMessage();
            return 0;
        }

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        NbtCompound compound = new NbtCompound();
        compound.putString("SkullOwner",PlayerNameArgumentType.getName(context, "name"));
        head.setNbt(compound);

        MinecraftClient.getInstance().player.giveItemStack(head);

        syncInventory();

        return 1;
    }


}
