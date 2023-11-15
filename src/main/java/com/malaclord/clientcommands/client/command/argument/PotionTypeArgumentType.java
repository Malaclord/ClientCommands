package com.malaclord.clientcommands.client.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;

import java.util.concurrent.CompletableFuture;

public class PotionTypeArgumentType implements ArgumentType<PotionTypes> {

    @Override
    public PotionTypes parse(StringReader reader) throws CommandSyntaxException {
        return PotionTypes.getType(reader.readString());
    }

    public static PotionTypeArgumentType potionType() {
        return new PotionTypeArgumentType();
    }


    public static PotionTypes getPotionType(final CommandContext<?> context, String name) {
        return context.getArgument(name,PotionTypes.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(PotionTypes.getTypeNames(),builder);
    }

}
