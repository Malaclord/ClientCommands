package com.malaclord.clientcommands.mixin.client;

import com.malaclord.clientcommands.client.IChatInputSuggestorMixedin;
import com.malaclord.clientcommands.client.IChatScreenMixedin;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin implements IChatInputSuggestorMixedin {
    @Shadow
    @Final
    private List<OrderedText> messages;

    @Shadow @Final private Screen owner;

    @Shadow private int x;

    @Inject(method = "renderMessages", at = @At("HEAD"))
    private void refreshInjected(CallbackInfo ci) {
        if (owner instanceof IChatScreenMixedin) ((IChatScreenMixedin) owner).noChatLimit$setMessagesNum(messages.toArray().length);
    }

    //@Redirect(method = "sortSuggestions", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", ordinal = 1))
    //public <E> ArrayList<E> injected() {
//
    //    return null;
    //}

    @Override
    public int noChatLimit$getX() {
        return x;
    }
}
