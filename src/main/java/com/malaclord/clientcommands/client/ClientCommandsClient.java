package com.malaclord.clientcommands.client;

import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.datafixer.fix.OminousBannerBlockEntityRenameFix;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
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
                .then(literal("rename").then(literal("json").then(argument("name", TextArgumentType.text()).executes((context -> {
                    ItemStack itemStack = MinecraftClient.getInstance().player.getMainHandStack().setCustomName(TextArgumentType.getTextArgument(context,"name"));

                    syncInventory();

                    return 0;
                })))).then(argument("name",StringArgumentType.string()).executes((context -> {
                    ItemStack itemStack = MinecraftClient.getInstance().player.getMainHandStack().setCustomName(
                            Text.of(StringArgumentType.getString(context,"name")));

                    syncInventory();

                    return 0;
                })).then(argument("italic", BoolArgumentType.bool()).executes((context -> {
                    ItemStack itemStack = MinecraftClient.getInstance().player.getMainHandStack().setCustomName(
                            Text.literal(StringArgumentType.getString(context,"name"))
                                    .setStyle(Style.EMPTY.withItalic(BoolArgumentType.getBool(context,"italic"))));

                    syncInventory();

                    return 0;
                })))))
                .then(literal("give")
                        .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .then(argument("amount", IntegerArgumentType.integer(0))
                                        .executes(this::giveCommand)
                                )).then(argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(this::giveCommand)));

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

        syncInventory();

        return 1;
    }

    // Bit hacky but works.
    private void syncInventory() {
        if (MinecraftClient.getInstance().player == null) return;
        MinecraftClient.getInstance().setScreen(new InventoryScreen(MinecraftClient.getInstance().player));
        MinecraftClient.getInstance().setScreen(null);
    }
}
