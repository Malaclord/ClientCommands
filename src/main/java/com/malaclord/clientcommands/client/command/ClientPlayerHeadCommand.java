package com.malaclord.clientcommands.client.command;

import com.malaclord.clientcommands.client.command.argument.PlayerNameArgumentType;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

import static com.malaclord.clientcommands.client.ClientCommandsClient.*;
import static com.malaclord.clientcommands.client.util.PlayerMessage.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientPlayerHeadCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("client").then(literal("player-head")
                .then(argument("name", PlayerNameArgumentType.name()).executes(
                ClientPlayerHeadCommand::execute
        ))));
    }

    private static int execute(CommandContext<FabricClientCommandSource> ctx) {
        var player = ctx.getSource().getPlayer();

        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        NbtCompound compound = new NbtCompound();
        compound.putString("SkullOwner",PlayerNameArgumentType.getName(ctx, "name"));

        var name = Optional.of(PlayerNameArgumentType.getName(ctx, "name"));

        head.set(DataComponentTypes.PROFILE, new ProfileComponent(name, Optional.empty(), new PropertyMap()));

        if (player.getInventory().getEmptySlot() == -1 && player.getInventory().getOccupiedSlotWithRoomForStack(head) == -1) {
            warn(player,"You don't have space in your inventory for this item!");
        } else {
            success(player, "Gave you the head of " + name.get() + "!");
        }

        player.giveItemStack(head);

        syncInventory();

        return 1;
    }


}
