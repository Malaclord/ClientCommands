package com.malaclord.clientcommands.client;

import com.malaclord.clientcommands.client.command.ClientEnchantCommand;
import com.malaclord.clientcommands.client.command.ClientGiveCommand;
import com.malaclord.clientcommands.client.command.ClientRenameCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

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
            ClientRenameCommand.register(dispatcher);
            ClientEnchantCommand.register(dispatcher,registryAccess);

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
}
