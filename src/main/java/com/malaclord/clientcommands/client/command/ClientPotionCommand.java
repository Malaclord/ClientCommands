package com.malaclord.clientcommands.client.command;

import com.malaclord.clientcommands.client.command.argument.PotionTypeArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.malaclord.clientcommands.client.ClientCommandsClient.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ClientPotionCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        RequiredArgumentBuilder<FabricClientCommandSource, RegistryEntry.Reference<StatusEffect>> effectArg = argument("effect", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT));

        dispatcher.register(literal("client")
                .then(literal("potion")
                        .then(literal("create")
                                .then(argument("type", PotionTypeArgumentType.potionType())
                                        .then(effectArg
                                                .then(argument("amplifier", IntegerArgumentType.integer(0,255))
                                                        .then(literal("infinite")
                                                                .then(argument("showParticles", BoolArgumentType.bool())
                                                                        .executes(ClientPotionCommand::executeCreate)
                                                                )
                                                                .executes(ClientPotionCommand::executeCreate)
                                                        )
                                                        .then(argument("duration",IntegerArgumentType.integer(0,1000000))
                                                                .then(argument("showParticles", BoolArgumentType.bool())
                                                                        .executes(ClientPotionCommand::executeCreate)
                                                                )
                                                                .executes(ClientPotionCommand::executeCreate)
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("modify")
                                .then(literal("effect")
                                        .then(literal("set")
                                                .then(effectArg
                                                        .then(argument("amplifier", IntegerArgumentType.integer(0,255))
                                                                .then(literal("infinite")
                                                                        .then(argument("showParticles", BoolArgumentType.bool())
                                                                                .executes(ClientPotionCommand::executeModifyEffect)
                                                                        )
                                                                        .executes(ClientPotionCommand::executeModifyEffect)
                                                                )
                                                                .then(argument("duration",IntegerArgumentType.integer(0,1000000))
                                                                        .then(argument("showParticles", BoolArgumentType.bool())
                                                                                .executes(ClientPotionCommand::executeModifyEffect)
                                                                        )
                                                                        .executes(ClientPotionCommand::executeModifyEffect)
                                                                )
                                                        )
                                                )
                                        )
                                        .then(literal("remove")
                                                .then(effectArg
                                                        .executes(ClientPotionCommand::executeRemoveEffect)
                                                )
                                        )
                                )
                                .then(literal("color")
                                        .then(argument("color", StringArgumentType.string())
                                                .executes(ClientPotionCommand::executeModifyColor)
                                        )
                                )
                                .then(literal("type")
                                        .then(argument("type", PotionTypeArgumentType.potionType())
                                                .executes(ClientPotionCommand::executeModifyType)
                                        )
                                )
                        )
                )
        );
    }

    private static int executeCreate(CommandContext<?> context) throws CommandSyntaxException {
        ClientPlayerEntity player;

        System.out.println("0");

        if ((player = MinecraftClient.getInstance().player) == null) return 0;
        if (isGameModeNotCreative()) {
            sendNotInCreativeMessage();
            return 0;
        }

        player.sendMessage(Text.literal("1"));

        StatusEffect effect = RegistryEntryArgumentType.getStatusEffect((CommandContext<ServerCommandSource>) context,"effect").value();
        int amplifier = IntegerArgumentType.getInteger(context,"amplifier");
        int duration = IntegerArgumentType.getInteger(context,"duration");
        boolean showParticles;

        try {
            showParticles = BoolArgumentType.getBool(context,"showParticles");
        } catch (Exception ignored) {
            showParticles = true;
        }

        player.sendMessage(Text.literal("2"));


        StatusEffectInstance effectInstance = new StatusEffectInstance(effect,amplifier,duration,false,showParticles);

        Potion potion = new Potion();

        potion.getEffects().add(effectInstance);

        ItemStack potionStack = new ItemStack(Items.POTION);

        PotionUtil.setPotion(potionStack,potion);

        player.sendMessage(Text.literal("3"));


        player.giveItemStack(potionStack);

        syncInventory();

        player.sendMessage(Text.literal("4"));


        return 1;
    }

    private static int executeModifyEffect(CommandContext<FabricClientCommandSource> context) {

        return 1;
    }

    private static int executeModifyType(CommandContext<FabricClientCommandSource> context) {

        return 1;
    }

    private static int executeModifyColor(CommandContext<FabricClientCommandSource> context) {

        return 1;
    }

    private static int executeRemoveEffect(CommandContext<FabricClientCommandSource> context) {

        return 1;
    }
}
