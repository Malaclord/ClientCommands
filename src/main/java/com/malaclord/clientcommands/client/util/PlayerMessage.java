package com.malaclord.clientcommands.client.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.*;

@SuppressWarnings("unused")
public class PlayerMessage {

    public static final int WARN_COLOR = 0xFFC300;
    public static final int ERROR_COLOR = 0xFF5733;
    public static final int SUCCESS_COLOR = 0xDAF7A6;

    private static void send(ClientPlayerEntity player, Text message) {
        player.sendMessage(message);
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

    public static void warn(ClientPlayerEntity player, MutableText message) {
        send(player,getPrefix("!", true).append(message).withColor(WARN_COLOR));
    }

    public static void error(ClientPlayerEntity player, String message) {
        error(player, Text.literal(message));
    }

    public static void error(ClientPlayerEntity player, MutableText message) {
        send(player,getPrefix("❌", false).append(message).withColor(ERROR_COLOR));
    }

    public static void success(ClientPlayerEntity player, String message, String command) {
        success(player,Text.literal(message),command);
    }

    public static void success(ClientPlayerEntity player, MutableText message, String command) {
        send(player,getPrefix("✔", false).append(message)
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Text.literal("Click to copy command")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/"+command))
                ).withColor(SUCCESS_COLOR));
    }

    public static void success(ClientPlayerEntity player, String message) {
        success(player, Text.literal(message));
    }

    public static void success(ClientPlayerEntity player, MutableText message) {
        send(player,getPrefix("✔", false).append(message).withColor(SUCCESS_COLOR));
    }


    public static void sendNotInCreativeMessage(ClientPlayerEntity player) {
        PlayerMessage.error(player,"You need to be in creative mode to use this command!");
    }
}
