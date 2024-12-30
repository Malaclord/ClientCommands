package com.malaclord.clientcommands.client.command;

import com.malaclord.clientcommands.client.command.argument.ClientBlockPosArgumentType;
import com.malaclord.clientcommands.client.util.Lerp3D;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.server.command.ServerCommandSource;
import org.joml.Vector3d;

import static com.malaclord.clientcommands.client.ClientCommandsClient.checkNotCreative;
import static com.malaclord.clientcommands.client.ClientCommandsClient.syncInventory;
import static com.malaclord.clientcommands.client.command.ClientGiveCommand.SUCCESS_MESSAGE;
import static com.malaclord.clientcommands.client.util.PlayerMessage.sendNoSpaceOrSuccess;
import static com.malaclord.clientcommands.client.util.PlayerMessage.sendNotInCreativeMessage;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WireCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("client").then(literal("wire").then(
                argument("from", ClientBlockPosArgumentType.blockPos()).then(
                        argument("to", ClientBlockPosArgumentType.blockPos()).then(
                                argument("hang", DoubleArgumentType.doubleArg())
                .executes(WireCommand::execute)
        )))));
    }

    public static int execute(CommandContext<FabricClientCommandSource> ctx) {
        var p = ctx.getSource().getPlayer();

        if (checkNotCreative(p)) {
            return 1;
        }

        var from = ClientBlockPosArgumentType.getBlockPos(ctx,"from");
        var to = ClientBlockPosArgumentType.getBlockPos(ctx,"to");
        var hang = DoubleArgumentType.getDouble((CommandContext<ServerCommandSource>) (Object) ctx,"hang");

        for (int i = 0; i < 2; i++) {
            var item = new ItemStack(Items.ALLAY_SPAWN_EGG);
            var nbt = new NbtCompound();
            nbt.putString("id","minecraft:allay");
            nbt.putBoolean("Silent",true);
            nbt.putBoolean("NoAI",true);
            nbt.putBoolean("PersistenceRequired",true);

            var activeEffects = new NbtList();
            var invis = new NbtCompound();
            invis.putString("id","minecraft:invisibility");
            invis.putInt("amplifier",0);
            invis.putInt("duration",-1);
            invis.putBoolean("show_particles",false);
            activeEffects.add(invis);
            nbt.put("active_effects", activeEffects);

            var rot = new NbtList();
            rot.add(NbtFloat.of(360));
            rot.add(NbtFloat.of(0));
            nbt.put("Rotation",rot);

            var a = Lerp3D.lerp3D(new double[] {from.getX(), from.getY(), from.getZ()},new double[] {to.getX(), to.getY(), to.getZ()},
                    (Math.tanh((from.getY()-to.getY())/10d)+1d)/2d
            );
            var middlePos = new Vector3d(a[0]+.5,Math.min(from.getY(),to.getY()),a[2]+.5-0.03125);

            var pos = new NbtList();
            pos.add(NbtDouble.of(middlePos.x));
            pos.add(NbtDouble.of(middlePos.y-hang));
            pos.add(NbtDouble.of(middlePos.z));
            nbt.put("Pos",pos);

            var end = i == 0 ? from : to;

            nbt.putIntArray("leash", new int[]{end.getX(), end.getY(), end.getZ()});

            item.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(nbt));

            sendNoSpaceOrSuccess(p,item,SUCCESS_MESSAGE.apply(item),ctx.getInput());
            p.giveItemStack(item);
        }

        syncInventory();

        return 0;
    }
}