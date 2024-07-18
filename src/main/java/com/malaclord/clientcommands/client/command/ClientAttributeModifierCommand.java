package com.malaclord.clientcommands.client.command;

import com.malaclord.clientcommands.client.command.argument.AttributeModifierOperationArgumentType;
import com.malaclord.clientcommands.client.command.argument.AttributeModifierSlotArgumentType;
import com.malaclord.clientcommands.client.util.PlayerMessage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.malaclord.clientcommands.client.ClientCommandsClient.isGameModeNotCreative;
import static com.malaclord.clientcommands.client.ClientCommandsClient.syncInventory;
import static com.malaclord.clientcommands.client.util.PlayerMessage.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@SuppressWarnings("unchecked")
public class ClientAttributeModifierCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("client").then(literal("modifier")
                .then(literal("add").then(
                        argument("attribute", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE)).then(
                                argument("operation", AttributeModifierOperationArgumentType.operation()).then(
                                        argument("value", DoubleArgumentType.doubleArg()).then(
                                                argument("slot", AttributeModifierSlotArgumentType.slot())
                                                        .then(argument("id", IdentifierArgumentType.identifier())
                                                                .executes(ClientAttributeModifierCommand::executeAdd)
                                                        ).executes(ClientAttributeModifierCommand::executeAdd))
                                ))
                )).then(literal("remove").then(
                        argument("id", IdentifierArgumentType.identifier())
                                .executes(ClientAttributeModifierCommand::executeRemove)
                )).then(literal("modify").then(
                        argument("id", IdentifierArgumentType.identifier())
                                .then(literal("attribute").then(argument("attribute", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
                                        .executes(ctx -> executeModify(ctx,ModifyField.ATTRIBUTE))
                                )).then(literal("operation").then(argument("operation", AttributeModifierOperationArgumentType.operation())
                                        .executes(ctx -> executeModify(ctx,ModifyField.OPERATION))
                                )).then(literal("value").then(argument("value", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> executeModify(ctx,ModifyField.VALUE))
                                )).then(literal("slot").then(argument("slot",AttributeModifierSlotArgumentType.slot())
                                        .executes(ctx -> executeModify(ctx,ModifyField.SLOT))
                                )).then(literal("id").then(argument("newId",IdentifierArgumentType.identifier())
                                        .executes(ctx -> executeModify(ctx,ModifyField.ID))
                                ))
                )).then(literal("list")
                        .executes(ClientAttributeModifierCommand::executeList)
                ).then(literal("clear")
                        .executes(ClientAttributeModifierCommand::executeClear)
                )
        ));
    }

    public static String generateUniqueString(List<String> existingStrings, String input) {
        Set<String> stringSet = new HashSet<>(existingStrings);
        if (!stringSet.contains(input)) {
            return input;
        }

        int counter = 1;
        String newString;
        while (true) {
            newString = input + counter;
            if (!stringSet.contains(newString)) {
                break;
            }
            counter++;
        }

        return newString;
    }

    private static int executeClear(CommandContext<FabricClientCommandSource> ctx) {
        var player = ctx.getSource().getPlayer();
        var item = player.getInventory().getMainHandStack();
        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        if (item.isEmpty()) {
            error(player,"You need hold an item to use this command!");
            return 0;
        }

        item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS,AttributeModifiersComponent.DEFAULT);

        success(player, "Cleared attribute modifiers of item!", ctx.getInput());

        syncInventory();

        return 1;
    }

    private static int executeModify(CommandContext<FabricClientCommandSource> ctx, ModifyField field) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayer();
        var item = player.getInventory().getMainHandStack();
        var id = IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>) (Object) ctx,"id");

        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        var modifiersComponent = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        if (item.isEmpty()) {
            error(player,"You need hold an item to use this command!");
            return 0;
        }

        if (modifiersComponent == null) return 0;

        var modifiers = modifiersComponent.modifiers();

        var builder = AttributeModifiersComponent.builder();

        for (var modifier : modifiers) {
            if (modifier.modifier().id().equals(id)) {
                var attribute = modifier.attribute();
                var operation = modifier.modifier().operation();
                var value = modifier.modifier().value();
                var slot = modifier.slot();

                switch (field) {
                    case OPERATION -> operation = AttributeModifierOperationArgumentType.getOperation(ctx,"operation");
                    case VALUE -> value = DoubleArgumentType.getDouble(ctx,"value");
                    case ATTRIBUTE -> attribute = RegistryEntryReferenceArgumentType.getRegistryEntry((CommandContext<ServerCommandSource>) (Object) ctx,"attribute",RegistryKeys.ATTRIBUTE);
                    case SLOT -> slot = AttributeModifierSlotArgumentType.getSlot(ctx,"slot");
                    case ID -> id = IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>) (Object) ctx, "newId");
                }

                builder.add(attribute,new EntityAttributeModifier(id,value,operation),slot);
                continue;
            }
            builder.add(modifier.attribute(),modifier.modifier(),modifier.slot());
        }

        item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS,builder.build());

        success(player,"Modified attribute modifier!", ctx.getInput());

        syncInventory();

        return 1;
    }

    private static int executeAdd(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayer();
        var args = getArguments(ctx);
        var item = player.getInventory().getMainHandStack();

        String id = null;

        try {
            id = IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>) (Object) ctx, "id").toString();
        } catch (IllegalArgumentException ignored) {

        }

        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        if (item.isEmpty()) {
            error(player,"You need hold an item to use this command!");
            return 0;
        }

        var modifiersComponent = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        if (modifiersComponent == null) return 0;


        var allIds = modifiersComponent.modifiers().stream().map(m -> m.modifier().id().toString()).toList();
        var newId = generateUniqueString(allIds,id == null ? Identifier.of(player.getNameForScoreboard().toLowerCase(),Identifier.of(args.attribute.getIdAsString()).getPath()).toString() : id);

        var modifier = new EntityAttributeModifier(Identifier.of(newId),args.value,args.operation);

        item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiersComponent.with(args.attribute,modifier,args.slot));

        success(player, "Added new attribute modifier!", ctx.getInput());

        syncInventory();
        return 1;
    }

    @SuppressWarnings("unckeched")
    private static int executeRemove(CommandContext<FabricClientCommandSource> ctx) {
        var player = ctx.getSource().getPlayer();
        var item = player.getInventory().getMainHandStack();
        var id = IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>) (Object) ctx,"id");

        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        if (item.isEmpty()) {
            error(player,"You need hold an item to use this command!");
            return 0;
        }

        var modifiersComponent = item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        if (modifiersComponent == null) return 0;

        var modifiers = modifiersComponent.modifiers();

        var builder = AttributeModifiersComponent.builder();

        for (var modifier : modifiers) {
            if (modifier.modifier().id().equals(id)) continue;
            builder.add(modifier.attribute(),modifier.modifier(),modifier.slot());
        }

        item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());

        success(player,"Removed attribute modifier!", ctx.getInput());
        /*if (!modifiersComponent.modifiers().isEmpty())
            player.sendMessage(
                    Text.literal("Remaining modifiers:\n").append(
                    getList(item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS)))
            );*/


        syncInventory();

        return 1;
    }



    private static int executeList(CommandContext<FabricClientCommandSource> ctx) {
        ClientPlayerEntity player = ctx.getSource().getPlayer();
        ItemStack item = player.getInventory().getMainHandStack();

        if (item.isEmpty()) {
            error(player,"You need to hold an item for this command to work!");
            return 0;
        }

        var modifiersComponent = item.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        if (item.isEmpty()) {
            error(player,"You need hold an item to use this command!");
            return 0;
        }

        if (modifiersComponent == null) return 0;

        if (modifiersComponent.modifiers().isEmpty()) {
            player.sendMessage(item.getName().copy().append(" does not have any attribute modifiers."));
            return 1;
        }

        MutableText text = Text.literal("Attribute modifiers of ").append(item.getName()).append(":\n");

        text.append(getList(modifiersComponent));

        player.sendMessage(text);

        return 1;
    }

    private static MutableText getList(AttributeModifiersComponent modifiersComponent) {
        MutableText text = Text.empty();

        int i = 0;

        for (var modifier : modifiersComponent.modifiers()) {
            String id = modifier.modifier().id().toString();

            text.append((i++ != 0 ? "\n" : "") + "> ");

            text.append(
                    Text.literal("[-]")
                            .setStyle(Style.EMPTY
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Remove").withColor(PlayerMessage.ERROR_COLOR)))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/client modifier remove "+id))
                            ).withColor(PlayerMessage.ERROR_COLOR)
            ).append(" ");

            text.append(
                    Text.literal("[\uD83D\uDD27]")
                            .setStyle(Style.EMPTY
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Modify").withColor(PlayerMessage.SUCCESS_COLOR)))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/client modifier modify "+id))
                            ).withColor(PlayerMessage.SUCCESS_COLOR)
            ).append(" ");

            text.append(
                    Text.literal((modifier.modifier().id().toString()) + " ")
                            .setStyle(Style.EMPTY
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Text.literal("Click to copy")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id))
                            )
            );


            text.append(Text.literal(modifier.attribute().getIdAsString() + " ").formatted(Formatting.AQUA));
            text.append(Text.literal(modifier.modifier().operation().name().toLowerCase() + " ").formatted(Formatting.YELLOW));
            text.append(Text.literal(modifier.modifier().value() + " ").formatted(Formatting.GREEN));
            text.append(Text.literal(modifier.slot().name().toLowerCase() + " ").formatted(Formatting.LIGHT_PURPLE));
        }

        return text;
    }

    public static String truncateWithEllipsis(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }

        if (maxLength <= 3) {
            return "...".substring(0, maxLength); // If maxLength is very small, return a truncated ellipsis
        }

        return input.substring(0, maxLength - 3) + "...";
    }

    private static CommandArguments getArguments(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        var attribute = RegistryEntryReferenceArgumentType.getRegistryEntry((CommandContext<ServerCommandSource>) (Object) ctx,"attribute",RegistryKeys.ATTRIBUTE);
        var operation = AttributeModifierOperationArgumentType.getOperation(ctx,"operation");
        var value = DoubleArgumentType.getDouble(ctx, "value");
        var slot = AttributeModifierSlotArgumentType.getSlot(ctx,"slot");

        return new CommandArguments(attribute,operation,value,slot);
    }

    private record CommandArguments(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation, double value, AttributeModifierSlot slot) { }

    private enum ModifyField {
        OPERATION,
        VALUE,
        ATTRIBUTE,
        SLOT,
        ID
    }
}
