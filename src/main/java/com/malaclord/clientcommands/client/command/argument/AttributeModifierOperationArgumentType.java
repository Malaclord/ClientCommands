package com.malaclord.clientcommands.client.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.malaclord.clientcommands.client.util.StringUtils.listValues;

public class AttributeModifierOperationArgumentType implements ArgumentType<EntityAttributeModifier.Operation> {

    private static final DynamicCommandExceptionType OPERATION_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((o -> {
        var operations = EntityAttributeModifier.Operation.values();

        return Text.literal("Operation '"+ o.toString() + "' not found (expected "+ listValues(Arrays.stream(operations).map(operation -> "'"+operation.name().toLowerCase()+"'").toArray(),"or") +").");
    }));

    @Override
    public EntityAttributeModifier.Operation parse(StringReader reader) throws CommandSyntaxException {
        var name = reader.readString();

        var operation = Arrays.stream(EntityAttributeModifier.Operation.values()).filter(o -> o.name().toLowerCase().equals(name)).findFirst();

        if (operation.isEmpty()) throw OPERATION_NOT_FOUND_EXCEPTION.create(name);

        return operation.get();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        return CommandSource.suggestMatching(Arrays.stream(EntityAttributeModifier.Operation.values()).map(o -> o.name().toLowerCase()),builder);
    }

    public static EntityAttributeModifier.Operation getOperation(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, EntityAttributeModifier.Operation.class);
    }

    public static AttributeModifierOperationArgumentType operation() {
        return new AttributeModifierOperationArgumentType();
    }
}
