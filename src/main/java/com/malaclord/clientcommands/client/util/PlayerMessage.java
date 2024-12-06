package com.malaclord.clientcommands.client.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;

@SuppressWarnings("unused")
public class PlayerMessage {
    private static final Text PLAYER_NOT_IN_CREATIVE_MESSAGE = Text.translatable("commands.client.not_in_creative");
    private static final Text CLICK_TO_COPY_TOOLTIP = Text.translatable("commands.client.click_to_copy");
    private static final Text PLAYER_NOT_HOLDING_ITEM_MESSAGE = Text.translatable("commands.client.not_holding_item");
    private static final Text NO_SPACE_IN_INVENTORY_MESSAGE = Text.translatable("commands.client.no_space_in_inventory");

    public static final int WARN_COLOR = 0xFFC300;
    public static final int ERROR_COLOR = 0xFF5733;
    public static final int SUCCESS_COLOR = 0xDAF7A6;

    private static void send(ClientPlayerEntity player, Text message) {
        player.sendMessage(message,false);
    }

    public static void info(ClientPlayerEntity player, String message) {
        send(player,Text.literal(message));
    }

    private static MutableText getPrefix(String prefix, boolean bold) {
        return Text.literal("[").append(Text.literal(prefix).setStyle(Style.EMPTY.withBold(bold))).append(Text.literal("] "));
    }

    public static void warn(ClientPlayerEntity player, String message) {
        warn(player,Text.literal(message));
    }

    public static void warn(ClientPlayerEntity player, Text message) {
        send(player,getPrefix("!", true).append(message).withColor(WARN_COLOR));
    }

    public static void error(ClientPlayerEntity player, String message) {
        error(player, Text.literal(message));
    }

    public static void error(ClientPlayerEntity player, Text message) {
        send(player,getPrefix("❌", false).append(message).withColor(ERROR_COLOR));
    }

    public static void success(ClientPlayerEntity player, String message, String command) {
        success(player,Text.literal(message),command);
    }

    public static void success(ClientPlayerEntity player, Text message, String command) {
        send(player,getPrefix("✔", false).append(message)
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,CLICK_TO_COPY_TOOLTIP))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/"+command))
                ).withColor(SUCCESS_COLOR));
    }

    public static void success(ClientPlayerEntity player, String message) {
        success(player, Text.literal(message));
    }

    public static void success(ClientPlayerEntity player, Text message) {
        send(player,getPrefix("✔", false).append(message).withColor(SUCCESS_COLOR));
    }

    public static void sendNotHoldingItemMessage(ClientPlayerEntity player) {
        error(player,PLAYER_NOT_HOLDING_ITEM_MESSAGE);
    }

    public static void sendNotInCreativeMessage(ClientPlayerEntity player) {
        PlayerMessage.error(player,PLAYER_NOT_IN_CREATIVE_MESSAGE);
    }

    public static void sendNoSpaceOrSuccess(ClientPlayerEntity player, ItemStack itemStack, Text message, String command) {
        if (player.getInventory().getEmptySlot() == -1 && player.getInventory().getOccupiedSlotWithRoomForStack(itemStack) == -1) {
            warn(player,NO_SPACE_IN_INVENTORY_MESSAGE);
        } else {
            success(player,message,command);
        }
    }
}
