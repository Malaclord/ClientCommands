package com.malaclord.clientcommands.client.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.ComponentChanges;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ItemComponentsArgumentType implements ArgumentType<ComponentChanges> {
    private static final Collection<String> EXAMPLES = Arrays.asList("[]","[unbreakable={}]");
    private final ItemComponentStringReader reader;

    public ItemComponentsArgumentType(CommandRegistryAccess commandRegistryAccess) {
        this.reader = new ItemComponentStringReader(commandRegistryAccess);
    }

    public static ItemComponentsArgumentType itemComponents(CommandRegistryAccess registryAccess) {
        return new ItemComponentsArgumentType(registryAccess);
    }

    public static <S> ComponentChanges getComponents(CommandContext<S> context, String name) {
        return context.getArgument(name, ComponentChanges.class);
    }

    @Override
    public ComponentChanges parse(StringReader reader) throws CommandSyntaxException {
        ItemComponentStringReader.ItemComponentsResult result = this.reader.consume(reader);
        return result.components();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return this.reader.getSuggestions(builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
