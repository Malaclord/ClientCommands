package com.malaclord.clientcommands.client.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;





public class ItemComponentStringReader {
    static final DynamicCommandExceptionType UNKNOWN_COMPONENT_EXCEPTION = new DynamicCommandExceptionType((id) -> Text.stringifiedTranslatable("arguments.item.component.unknown", id));
    static final Dynamic2CommandExceptionType MALFORMED_COMPONENT_EXCEPTION = new Dynamic2CommandExceptionType((type, error) -> Text.stringifiedTranslatable("arguments.item.component.malformed", type, error));
    static final SimpleCommandExceptionType COMPONENT_EXPECTED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.item.component.expected"));
    static final DynamicCommandExceptionType REPEATED_COMPONENT_EXCEPTION = new DynamicCommandExceptionType((type) -> Text.stringifiedTranslatable("arguments.item.component.repeated", type));
    public static final char OPEN_SQUARE_BRACKET = '[';
    public static final char CLOSED_SQUARE_BRACKET = ']';
    public static final char COMMA = ',';
    public static final char EQUAL_SIGN = '=';
    public static final char EXCLAMATION_MARK = '!';
    static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_DEFAULT = SuggestionsBuilder::buildFuture;
    final RegistryWrapper.Impl<Item> itemRegistry;
    final DynamicOps<NbtElement> nbtOps;

    public ItemComponentStringReader(RegistryWrapper.WrapperLookup registryLookup) {
        this.itemRegistry = registryLookup.getWrapperOrThrow(RegistryKeys.ITEM);
        this.nbtOps = registryLookup.getOps(NbtOps.INSTANCE);
    }

    public ItemComponentsResult consume(StringReader reader) throws CommandSyntaxException {
        final ComponentChanges.Builder builder = ComponentChanges.builder();
        this.consume(reader, new Callbacks() {

            public <T> void onComponentAdded(ComponentType<T> type, T value) {
                builder.add(type, value);
            }

            public <T> void onComponentRemoved(ComponentType<T> type) {
                builder.remove(type);
            }
        });
        ComponentChanges componentChanges = builder.build();
        validate();
        return new ItemComponentsResult(componentChanges);
    }

    private static void validate() {

    }

    public void consume(StringReader reader, Callbacks callbacks) throws CommandSyntaxException {
        int i = reader.getCursor();

        try {
            (new Reader(reader, callbacks)).read();
        } catch (CommandSyntaxException var5) {
            reader.setCursor(i);
            throw var5;
        }
    }

    public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        SuggestionCallbacks suggestionCallbacks = new SuggestionCallbacks();
        Reader reader = new Reader(stringReader, suggestionCallbacks);

        try {
            reader.read();
        } catch (CommandSyntaxException ignored) {
        }

        return suggestionCallbacks.getSuggestions(builder, stringReader);
    }

    public interface Callbacks {

        default <T> void onComponentAdded(ComponentType<T> type, T value) {
        }

        default <T> void onComponentRemoved(ComponentType<T> type) {
        }

        default void setSuggestor(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestor) {
        }
    }

    public record ItemComponentsResult(ComponentChanges components) {

        public ComponentChanges components() {
            return this.components;
        }
    }

    private class Reader {
        private final StringReader reader;
        private final Callbacks callbacks;

        Reader(final StringReader reader, final Callbacks callbacks) {
            this.reader = reader;
            this.callbacks = callbacks;
        }

        public void read() throws CommandSyntaxException {
            this.callbacks.setSuggestor(this::suggestBracket);
            if (this.reader.canRead() && this.reader.peek() == OPEN_SQUARE_BRACKET) {
                this.callbacks.setSuggestor(ItemComponentStringReader.SUGGEST_DEFAULT);
                this.readComponents();
            }

        }

        private void readComponents() throws CommandSyntaxException {
            this.reader.expect(OPEN_SQUARE_BRACKET);
            this.callbacks.setSuggestor(this::suggestComponents);
            Set<ComponentType<?>> set = new ReferenceArraySet<>();

            while(this.reader.canRead() && this.reader.peek() != CLOSED_SQUARE_BRACKET) {
                this.reader.skipWhitespace();
                ComponentType<?> componentType;
                if (this.reader.canRead() && this.reader.peek() == EXCLAMATION_MARK) {
                    this.reader.skip();
                    this.callbacks.setSuggestor(this::suggestComponentsToRemove);
                    componentType = readComponentType(this.reader);
                    if (!set.add(componentType)) {
                        throw ItemComponentStringReader.REPEATED_COMPONENT_EXCEPTION.create(componentType);
                    }

                    this.callbacks.onComponentRemoved(componentType);
                    this.callbacks.setSuggestor(ItemComponentStringReader.SUGGEST_DEFAULT);
                    this.reader.skipWhitespace();
                } else {
                    componentType = readComponentType(this.reader);
                    if (!set.add(componentType)) {
                        throw ItemComponentStringReader.REPEATED_COMPONENT_EXCEPTION.create(componentType);
                    }

                    this.callbacks.setSuggestor(this::suggestEqual);
                    this.reader.skipWhitespace();
                    this.reader.expect(EQUAL_SIGN);
                    this.callbacks.setSuggestor(ItemComponentStringReader.SUGGEST_DEFAULT);
                    this.reader.skipWhitespace();
                    this.readComponentValue(componentType);
                    this.reader.skipWhitespace();
                }

                this.callbacks.setSuggestor(this::suggestEndOfComponent);
                if (!this.reader.canRead() || this.reader.peek() != ',') {
                    break;
                }

                this.reader.skip();
                this.reader.skipWhitespace();
                this.callbacks.setSuggestor(this::suggestComponents);
                if (!this.reader.canRead()) {
                    throw ItemComponentStringReader.COMPONENT_EXPECTED_EXCEPTION.createWithContext(this.reader);
                }
            }

            this.reader.expect(CLOSED_SQUARE_BRACKET);
            this.callbacks.setSuggestor(ItemComponentStringReader.SUGGEST_DEFAULT);
        }

        public static ComponentType<?> readComponentType(StringReader reader) throws CommandSyntaxException {
            if (!reader.canRead()) {
                throw ItemComponentStringReader.COMPONENT_EXPECTED_EXCEPTION.createWithContext(reader);
            } else {
                int i = reader.getCursor();
                Identifier identifier = Identifier.fromCommandInput(reader);
                ComponentType<?> componentType = Registries.DATA_COMPONENT_TYPE.get(identifier);
                if (componentType != null && !componentType.shouldSkipSerialization()) {
                    return componentType;
                } else {
                    reader.setCursor(i);
                    throw ItemComponentStringReader.UNKNOWN_COMPONENT_EXCEPTION.createWithContext(reader, identifier);
                }
            }
        }

        private <T> void readComponentValue(ComponentType<T> type) throws CommandSyntaxException {
            int i = this.reader.getCursor();
            NbtElement nbtElement = (new StringNbtReader(this.reader)).parseElement();
            DataResult<T> dataResult = type.getCodecOrThrow().parse(ItemComponentStringReader.this.nbtOps, nbtElement);
            this.callbacks.onComponentAdded(type, dataResult.getOrThrow((error) -> {
                this.reader.setCursor(i);
                return ItemComponentStringReader.MALFORMED_COMPONENT_EXCEPTION.createWithContext(this.reader, type.toString(), error);
            }));
        }

        private CompletableFuture<Suggestions> suggestBracket(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf(OPEN_SQUARE_BRACKET));
            }

            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestEndOfComponent(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf(COMMA));
                builder.suggest(String.valueOf(CLOSED_SQUARE_BRACKET));
            }

            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestEqual(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf(EQUAL_SIGN));
            }

            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestComponents(SuggestionsBuilder builder) {
            builder.suggest(String.valueOf('!'));
            return this.suggestComponents(builder, String.valueOf(EQUAL_SIGN));
        }

        private CompletableFuture<Suggestions> suggestComponentsToRemove(SuggestionsBuilder builder) {
            return this.suggestComponents(builder, "");
        }

        private CompletableFuture<Suggestions> suggestComponents(SuggestionsBuilder builder, String suffix) {
            String string = builder.getRemaining().toLowerCase(Locale.ROOT);
            CommandSource.forEachMatching(Registries.DATA_COMPONENT_TYPE.getEntrySet(), string, (entry) -> entry.getKey().getValue(), (entry) -> {
                ComponentType<?> componentType = entry.getValue();
                if (componentType.getCodec() != null) {
                    Identifier identifier = entry.getKey().getValue();
                    String var10001 = String.valueOf(identifier);
                    builder.suggest(var10001 + suffix);
                }

            });
            return builder.buildFuture();
        }
    }

    private static class SuggestionCallbacks implements Callbacks {
        private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestor;

        SuggestionCallbacks() {
            this.suggestor = ItemComponentStringReader.SUGGEST_DEFAULT;
        }

        public void setSuggestor(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestor) {
            this.suggestor = suggestor;
        }

        public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder, StringReader reader) {
            return this.suggestor.apply(builder.createOffset(reader.getCursor()));
        }
    }
}
