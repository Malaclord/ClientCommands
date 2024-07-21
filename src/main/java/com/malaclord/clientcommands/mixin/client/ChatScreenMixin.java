package com.malaclord.clientcommands.mixin.client;

import com.malaclord.clientcommands.client.IChatInputSuggestorMixedin;
import com.malaclord.clientcommands.client.IChatScreenMixedin;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.malaclord.clientcommands.client.util.PlayerMessage.WARN_COLOR;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen implements IChatScreenMixedin {
    @Unique
    private TextWidget textWidget;

    @Unique
    private static final Text MESSAGE = Text.literal("[!] Message length over vanilla maximum!").withColor(WARN_COLOR);

    @Shadow
    protected TextFieldWidget chatField;

    @Shadow
    ChatInputSuggestor chatInputSuggestor;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void initInjected(CallbackInfo ci) {
        textWidget = new TextWidget(chatField.getX(), chatField.getY()-12, textRenderer.getWidth(MESSAGE),8, MESSAGE
                ,this.textRenderer);
        textWidget.alignLeft();
        textWidget.visible = false;
        textWidget.setTooltip(Tooltip.of(Text.literal("If you send the server a message / command, it might kick you!").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY).withFormatting(Formatting.ITALIC))));
        addDrawableChild(textWidget);
    }

    @ModifyArg(method = "init", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setMaxLength(I)V"), index = 0)
    private int injected(int ignored) {
        return 32500;
    }

    @Redirect(method = "normalize", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;truncateChat(Ljava/lang/String;)Ljava/lang/String;"))
    private String injected(String text) {
        return text;
    }

    @Unique
    private int messageNum;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderInjected(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var newMessage = MESSAGE.copy().append(Text.literal(" ("+chatField.getText().length()+")").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
        textWidget.setMessage(newMessage);
        textWidget.setWidth(textRenderer.getWidth(newMessage));
        textWidget.visible = chatField.getText().length() > 256;

        if (!chatInputSuggestor.isOpen() && ((IChatInputSuggestorMixedin) chatInputSuggestor).noChatLimit$getX() < textWidget.getX() + textWidget.getWidth()) textWidget.setY(chatField.getY() - 12 - messageNum * 12); else textWidget.setY(chatField.getY() - 12);
    }

    @Override
    public void noChatLimit$setMessagesNum(int thing) {
        messageNum = thing;
    }
}

