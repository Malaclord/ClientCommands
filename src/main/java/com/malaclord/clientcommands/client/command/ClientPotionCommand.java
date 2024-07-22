package com.malaclord.clientcommands.client.command;

import com.malaclord.clientcommands.client.command.argument.PotionTypeArgumentType;
import com.malaclord.clientcommands.client.command.argument.PotionTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.malaclord.clientcommands.client.ClientCommandsClient.*;
import static com.malaclord.clientcommands.client.util.PlayerMessage.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

@SuppressWarnings("unchecked")
public class ClientPotionCommand {
    private static final Function<Potion, Text> POTION_NAME = potion -> Text.translatable("commands.client.potion.potion_name",Text.translatable(potion.getEffects().getFirst().getTranslationKey()));
    private static final Function<Potion, Text> SPLASH_POTION_NAME = potion -> Text.translatable("commands.client.potion.splash_potion_name",Text.translatable(potion.getEffects().getFirst().getTranslationKey()));
    private static final Function<Potion, Text> LINGERING_POTION_NAME = potion -> Text.translatable("commands.client.potion.lingering_potion_name",Text.translatable(potion.getEffects().getFirst().getTranslationKey()));
    private static final Function<ItemStack, Text> CREATE_SUCCESS_MESSAGE = itemStack -> Text.translatable("commands.client.potion.create.success", itemStack.getName());
    private static final Function<ItemStack, Text> MODIFY_SUCCESS_MESSAGE = itemStack -> Text.translatable("commands.client.potion.modify.success", itemStack.getName());
    private static final Text NOT_HOLDING_POTION_MESSAGE = Text.translatable("commands.client.potion.not_holding_potion");
    private static final Function<MessageData, Text> MODIFY_REMOVE_EFFECT_SUCCESS_MESSAGE = data -> Text.translatable("commands.client.potion.modify.effect.remove.success", data.effect.value().getName(), data.itemStack.getName());
    private static final Function<MessageData, Text> MODIFY_REMOVE_EFFECT_NO_EFFECT_MESSAGE = data -> Text.translatable("commands.client.potion.modify.effect.remove.no_effect", data.itemStack.getName() ,data.effect.value().getName());
    private static final Function<MessageData, Text> MODIFY_SET_EFFECT_SUCCESS_MESSAGE = data -> Text.translatable("commands.client.potion.modify.effect.set.success", data.effect.value().getName(), data.itemStack.getName(), data.amplifier, data.duration);

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("client")
                .then(literal("potion")
                        .then(literal("create")
                                .then(argument("type", PotionTypeArgumentType.potionType())
                                        .then(argument("effect", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT))
                                                .then(argument("amplifier", IntegerArgumentType.integer(0,255))
                                                        .then(literal("infinite")
                                                                .then(argument("hideParticles", BoolArgumentType.bool())
                                                                        .executes(ClientPotionCommand::executeCreate)
                                                                )
                                                                .executes(ClientPotionCommand::executeCreate)
                                                        )
                                                        .then(argument("duration",IntegerArgumentType.integer(0,1000000))
                                                                .then(argument("hideParticles", BoolArgumentType.bool())
                                                                        .executes(ClientPotionCommand::executeCreate)
                                                                )
                                                                .executes(ClientPotionCommand::executeCreate)
                                                        )
                                                        .executes(ClientPotionCommand::executeCreate)
                                                )
                                                .executes(ClientPotionCommand::executeCreate)
                                        )
                                )
                        )
                        .then(literal("modify")
                                .then(literal("effect")
                                        .then(literal("set")
                                                .then(argument("effect", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT))
                                                        .then(argument("amplifier", IntegerArgumentType.integer(0,255))
                                                                .then(literal("infinite")
                                                                        .then(argument("hideParticles", BoolArgumentType.bool())
                                                                                .executes(ClientPotionCommand::executeModifyEffect)
                                                                        )
                                                                        .executes(ClientPotionCommand::executeModifyEffect)
                                                                )
                                                                .then(argument("duration",IntegerArgumentType.integer(0,1000000))
                                                                        .then(argument("hideParticles", BoolArgumentType.bool())
                                                                                .executes(ClientPotionCommand::executeModifyEffect)
                                                                        )
                                                                        .executes(ClientPotionCommand::executeModifyEffect)
                                                                )
                                                        )
                                                )
                                        )
                                        .then(literal("remove")
                                                .then(argument("effect", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT))
                                                        .executes(ClientPotionCommand::executeRemoveEffect)
                                                )
                                        )
                                )
                                .then(literal("color")
                                        .then(argument("color", ColorArgumentType.color())
                                                .executes(ClientPotionCommand::executeModifyColor)
                                        )
                                        .then(literal("hex")
                                                .then(argument("colorHex", StringArgumentType.string())
                                                        .executes(ClientPotionCommand::executeModifyColorHex)
                                                )
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



    private static int executeCreate(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player;

        if ((player = MinecraftClient.getInstance().player) == null) return 0;
        if (checkNotCreative(player)) return 0;

        StatusEffect effect = RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>) (Object) context,"effect").value();
        PotionTypes type = PotionTypeArgumentType.getPotionType(context,"type");
        var args = getArguments(context);

        StatusEffectInstance effectInstance = new StatusEffectInstance(RegistryEntry.of(effect),args.duration,args.amplifier,false,args.showParticles);

        Potion potion = new Potion(effectInstance);

        Item item;

        Text name;

        switch (type) {
            case LINGER -> { item = Items.LINGERING_POTION; name = LINGERING_POTION_NAME.apply(potion);}
            case SPLASH -> { item = Items.SPLASH_POTION; name = SPLASH_POTION_NAME.apply(potion);}
            default -> { item = Items.POTION; name = POTION_NAME.apply(potion);}
        }

        ItemStack potionStack = new ItemStack(item);

        PotionContentsComponent pcc = PotionContentsComponent.DEFAULT;

        for (StatusEffectInstance sei : potion.getEffects()) {
            pcc = pcc.with(sei);
        }

        potionStack.set(DataComponentTypes.POTION_CONTENTS,pcc);
        potionStack.set(DataComponentTypes.ITEM_NAME, name);

        setColor(potionStack,potion.getEffects().getFirst().getEffectType().value().getColor());

        sendNoSpaceOrSuccess(player,potionStack, CREATE_SUCCESS_MESSAGE.apply(potionStack),context.getInput());

        player.giveItemStack(potionStack);

        syncInventory();

        return 1;
    }

    private static int executeModifyEffect(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player;

        if ((player = MinecraftClient.getInstance().player) == null) return 0;
        if (checkNotCreative(player)) return 0;

        var effect = RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>) (Object) context,"effect");
        var args = getArguments(context);

        ItemStack potion = heldPotion(player);

        if (potion == null) {
            error(player,NOT_HOLDING_POTION_MESSAGE);
            return 0;
        }

        var pcc = potion.get(DataComponentTypes.POTION_CONTENTS);

        if (pcc == null) return 0;

        var customEffect = pcc.customEffects().stream().filter(p ->
            p.getEffectType() == effect
        ).findFirst();

        customEffect.ifPresent(statusEffectInstance -> pcc.customEffects().remove(statusEffectInstance));

        potion.set(DataComponentTypes.POTION_CONTENTS,pcc.with(new StatusEffectInstance(effect,args.duration,args.amplifier,false,args.showParticles,true)));

        success(player,MODIFY_SET_EFFECT_SUCCESS_MESSAGE.apply(new MessageData(potion,effect, args.amplifier, args.duration)),context.getInput());

        syncInventory();

        return 1;
    }

    private static int executeModifyType(CommandContext<?> context) {
        ClientPlayerEntity player;

        if ((player = MinecraftClient.getInstance().player) == null) return 0;
        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        PotionTypes type = PotionTypeArgumentType.getPotionType(context,"type");

        Item item;

        switch (type) {
            case LINGER -> item = Items.LINGERING_POTION;
            case SPLASH -> item = Items.SPLASH_POTION;
            default -> item = Items.POTION;
        }

        ItemStack potion = heldPotion(player);

        if (potion == null) {
            error(player,NOT_HOLDING_POTION_MESSAGE);
            return 0;
        }

        ItemStack newPotionStack = potion.copyComponentsToNewStack(item,potion.getCount());

        player.getInventory().setStack(player.getInventory().getSlotWithStack(potion),newPotionStack);

        success(player,MODIFY_SUCCESS_MESSAGE.apply(potion),context.getInput());

        syncInventory();

        return 1;
    }

    private static int executeModifyColor(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        ItemStack potion = heldPotion(player);

        if (potion == null) {
            error(player,NOT_HOLDING_POTION_MESSAGE);
            return 0;
        }

        Formatting color = ColorArgumentType.getColor((CommandContext<ServerCommandSource>) (Object) context,"color");

        int colorInt;


        if (color == Formatting.RESET) colorInt = Objects.requireNonNull(potion.get(DataComponentTypes.POTION_CONTENTS)).getEffects().iterator().next().getEffectType().value().getColor();
        else colorInt = color.getColorValue();

        setColor(potion,colorInt);

        success(player,MODIFY_SUCCESS_MESSAGE.apply(potion),context.getInput());

        syncInventory();

        return 1;
    }

    private static int executeModifyColorHex(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        ItemStack potion = heldPotion(player);

        if (potion == null) {
            error(player,NOT_HOLDING_POTION_MESSAGE);
            return 0;
        }

        String colorHex = StringArgumentType.getString(context,"colorHex").toLowerCase().trim();

        int colorInt;

        try {
            colorInt = Integer.parseInt(colorHex, 16);
        } catch (NumberFormatException ignored) {
            error(player,"Hex format incorrect!");
            return 0;
        }

        setColor(potion,colorInt);

        success(player,MODIFY_SUCCESS_MESSAGE.apply(potion),context.getInput());

        syncInventory();

        return 1;
    }

    @SuppressWarnings("unchecked")
    private static int executeRemoveEffect(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (checkNotCreative(player)) return 0;

        RegistryEntry.Reference<StatusEffect> effect = RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>) (Object) context,"effect");

        ItemStack potion = heldPotion(player);

        if (potion == null) {
            error(player,NOT_HOLDING_POTION_MESSAGE);
            return 0;
        }

        var pcc = potion.get(DataComponentTypes.POTION_CONTENTS);

        if (pcc == null) return 0;

        if (pcc.customEffects().stream().noneMatch(p -> p.getEffectType() == effect)) {
            error(player,MODIFY_REMOVE_EFFECT_NO_EFFECT_MESSAGE.apply(new MessageData(potion,effect,0, 0)));
            return 0;
        }

        var customEffect = pcc.customEffects().stream().filter(p ->
                p.getEffectType() == effect
        ).findFirst();

        if (customEffect.isEmpty()) return 0;

        pcc.customEffects().remove(customEffect.get());

        success(player,MODIFY_REMOVE_EFFECT_SUCCESS_MESSAGE.apply(new MessageData(potion,effect,0, 0)),context.getInput());

        syncInventory();

        return 1;
    }

    private static final List<Item> potionTypes = List.of(Items.POTION,Items.SPLASH_POTION,Items.LINGERING_POTION);

    private static ItemStack heldPotion(ClientPlayerEntity player) {
        ItemStack potionStack = player.getInventory().getMainHandStack();

        if (!potionTypes.contains(potionStack.getItem())) return null;

        return potionStack;
    }

    private static void setColor(ItemStack potion, int color) {
        PotionContentsComponent pcc;
        pcc = new PotionContentsComponent(Optional.empty(),Optional.of(color), Objects.requireNonNull(potion.get(DataComponentTypes.POTION_CONTENTS)).customEffects());

        potion.set(DataComponentTypes.POTION_CONTENTS,pcc);

    }

    private static PotionCommandArguments getArguments(CommandContext<FabricClientCommandSource> ctx) {
        int amplifier;

        try {
            amplifier = IntegerArgumentType.getInteger(ctx,"amplifier");
        } catch (Exception ignored) {
            amplifier = 0;
        }

        int duration;

        try {
            duration = IntegerArgumentType.getInteger(ctx,"duration") * 20;
        } catch (Exception ignored) {
            duration = -1;
        }

        boolean showParticles;

        try {
            showParticles = !BoolArgumentType.getBool(ctx,"hideParticles");
        } catch (Exception ignored) {
            showParticles = true;
        }

        return new PotionCommandArguments(amplifier,duration,showParticles);
    }

    private record PotionCommandArguments(int amplifier, int duration, boolean showParticles) {}
    private record MessageData(ItemStack itemStack, RegistryEntry<StatusEffect> effect, double amplifier, int duration) {}
}
