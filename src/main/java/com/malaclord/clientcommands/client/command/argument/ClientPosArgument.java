package com.malaclord.clientcommands.client.command.argument;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface ClientPosArgument {
    Vec3d getPos(FabricClientCommandSource source);

    Vec2f getRotation(FabricClientCommandSource source);

    default BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
        return BlockPos.ofFloored(this.getPos(source));
    }

    boolean isXRelative();

    boolean isYRelative();

    boolean isZRelative();
}
