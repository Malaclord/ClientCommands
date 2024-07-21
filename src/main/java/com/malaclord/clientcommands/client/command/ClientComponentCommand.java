package com.malaclord.clientcommands.client.command;

import com.malaclord.clientcommands.client.command.argument.ItemComponentsArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.DynamicOps;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.*;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.stream.Stream;

import static com.malaclord.clientcommands.client.ClientCommandsClient.isGameModeNotCreative;
import static com.malaclord.clientcommands.client.ClientCommandsClient.syncInventory;
import static com.malaclord.clientcommands.client.util.PlayerMessage.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientComponentCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("client").then(literal("components")
                .then(literal("merge").then(argument("components", ItemComponentsArgumentType.itemComponents(registryAccess))
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayer();
                            var item = player.getInventory().getMainHandStack();
                            var componentChanges = ItemComponentsArgumentType.getComponents(ctx,"components");

                            if (isGameModeNotCreative(player)) {
                                sendNotInCreativeMessage(player);
                                return 0;
                            }

                            if (item.isEmpty()) {
                                error(player,"You need to hold an item for this command to work!");
                                return 0;
                            }

                            item.applyChanges(componentChanges);

                            success(player,"Merged components!",ctx.getInput());

                            syncInventory();

                            return 1;
                        })
                ))
                .then(literal("set").then(argument("components", ItemComponentsArgumentType.itemComponents(registryAccess))
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayer();
                            var item = player.getInventory().getMainHandStack();
                            var componentChanges = ItemComponentsArgumentType.getComponents(ctx,"components");

                            if (isGameModeNotCreative(player)) {
                                sendNotInCreativeMessage(player);
                                return 0;
                            }

                            if (item.isEmpty()) {
                                error(player,"You need to hold an item for this command to work!");
                                return 0;
                            }

                            var builder = ComponentChanges.builder();

                            for (var entry : Registries.DATA_COMPONENT_TYPE.getEntrySet()) {
                                builder.remove(entry.getValue());
                            }

                            item.applyChanges(builder.build());

                            item.applyChanges(componentChanges);

                            success(player,"Set components!",ctx.getInput());

                            syncInventory();

                            return 1;
                        })
                ))
                .then(literal("get")
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayer();
                            var item = player.getInventory().getMainHandStack();

                            if (item.isEmpty()) {
                                error(player,"You need to hold an item for this command to work!");
                                return 0;
                            }

                            MutableText text = Text.empty();

                            text.append("[");

                            var componentsText = componentsAsString(registryAccess,item.getComponents());

                            text.append(componentsText);

                            text.append("]");

                            player.sendMessage(text);

                            syncInventory();

                            return 1;
                        })
                )
        ));
    }


    private static MutableText componentsAsString(RegistryWrapper.WrapperLookup registries, ComponentMap components) {
        DynamicOps<NbtElement> dynamicOps = registries.getOps(NbtOps.INSTANCE);

        MutableText text = Text.empty();

        components.stream().forEach((entry) -> {
            ComponentType<?> componentType = entry.type();
            Identifier identifier = Registries.DATA_COMPONENT_TYPE.getId(componentType);
            if (identifier == null) {
                Stream.empty();
            } else {
                Optional<?> optional = Optional.of(entry.value());
                Component<?> component = Component.of(componentType, optional.get());
                component.encode(dynamicOps).result().ifPresent((value) -> {
                    text.append(Text.of(identifier).copy().formatted(Formatting.AQUA));
                    text.append("=");
                    text.append(Text.of(value.asString()).copy().formatted(Formatting.GRAY));
                    text.append(",");
                });
            }
        });

        return text;

    }
}
