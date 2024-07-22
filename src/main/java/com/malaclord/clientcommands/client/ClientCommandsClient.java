package com.malaclord.clientcommands.client;

import com.malaclord.clientcommands.client.command.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.Objects;

import static com.malaclord.clientcommands.client.util.PlayerMessage.sendNotHoldingItemMessage;
import static com.malaclord.clientcommands.client.util.PlayerMessage.sendNotInCreativeMessage;

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
            ClientGiveCommand.register(dispatcher,registryAccess);
            ClientRenameCommand.register(dispatcher,registryAccess);
            ClientEnchantCommand.register(dispatcher,registryAccess);
            ClientPlayerHeadCommand.register(dispatcher);
            ClientPotionCommand.register(dispatcher,registryAccess);
            ClientAttributeModifierCommand.register(dispatcher,registryAccess);
            ClientComponentCommand.register(dispatcher,registryAccess);
        }));
    }

    // Bit hacky but works.
    public static void syncInventory() {
        // Just in case.
        if (MinecraftClient.getInstance().player == null) return;

        Screen currentScreen = MinecraftClient.getInstance().currentScreen;

        // This runs whatever magic code is required for syncing the inventory with the server.
        MinecraftClient.getInstance().setScreen(new InventoryScreen(MinecraftClient.getInstance().player));

        // Set screen back to the screen we were on.
        MinecraftClient.getInstance().setScreen(currentScreen);
    }

    public static boolean isGameModeNotCreative(ClientPlayerEntity player) {
        return Objects.requireNonNull(Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerListEntry(player.getUuid())).getGameMode() != GameMode.CREATIVE;
    }

    public static boolean checkNotCreative(ClientPlayerEntity player) {
        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return true;
        }

        return false;
    }

    public static boolean checkNotHoldingItem(ClientPlayerEntity player) {
        if (player.getMainHandStack().isEmpty()) {
            sendNotHoldingItemMessage(player);
            return true;
        }

        return false;
    }
}
