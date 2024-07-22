package com.malaclord.clientcommands.client.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.malaclord.clientcommands.client.util.StringUtils.listValues;

public class AttributeModifierSlotArgumentType implements ArgumentType<AttributeModifierSlot> {

    private static final DynamicCommandExceptionType SLOT_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((o -> {
        var slots = AttributeModifierSlot.values();

        return Text.literal("Slot '"+ o.toString() + "' not found (expected "+ listValues(Arrays.stream(slots).map(slot -> "'"+slot.name().toLowerCase()+"'").toArray(),"or") +").");
    }));

    @Override
    public AttributeModifierSlot parse(StringReader reader) throws CommandSyntaxException {
        var name = reader.readString();

        var slot = Arrays.stream(AttributeModifierSlot.values()).filter(o -> o.name().toLowerCase().equals(name)).findFirst();

        if (slot.isEmpty()) throw SLOT_NOT_FOUND_EXCEPTION.create(name);

        return slot.get();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        return CommandSource.suggestMatching(Arrays.stream(AttributeModifierSlot.values()).map(o -> o.name().toLowerCase()),builder);
    }

    public static AttributeModifierSlot getSlot(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, AttributeModifierSlot.class);
    }

    public static AttributeModifierSlotArgumentType slot() {
        return new AttributeModifierSlotArgumentType();
    }
}
