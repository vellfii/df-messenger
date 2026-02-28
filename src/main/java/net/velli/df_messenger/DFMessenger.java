package net.velli.df_messenger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public class DFMessenger implements ClientModInitializer {

    public static final String MODID = "df_messenger";
    public static MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, Identifier.of(DFMessenger.MODID, "after_chat"), DFMessenger::render);
        DFMKeyBinds.init();

    }

    public static void render(DrawContext context, RenderTickCounter renderTickCounter) {
        MessageHandler.render(context, renderTickCounter);
    }

    public static void sendCommand(String command) {
        Objects.requireNonNull(MC.getNetworkHandler()).sendChatCommand(command);
    }

    public static List<OrderedText> wrapLines(StringVisitable text, int width) {
        return MC.textRenderer.wrapLines(text, width);
    }

    public static Text textFromOrdered(OrderedText orderedText) {
        StyleCharacterVisitor styles = new StyleCharacterVisitor();
        orderedText.accept(styles);
        if (styles.oldStyle != null) {
            styles.text.append(Text.literal(styles.sb.toString()).setStyle(styles.oldStyle));
        } else {
            styles.text.append(Text.literal(styles.sb.toString()));
        }
        return styles.text;
    }

    private static class StyleCharacterVisitor implements CharacterVisitor {

        public Style oldStyle;
        public StringBuilder sb = new StringBuilder();
        public MutableText text = Text.empty();

        @Override
        public boolean accept(int index, Style style, int codePoint) {
            String s = new String(Character.toChars(codePoint));
            if (oldStyle == style) {
                sb.append(s);
            } else if (oldStyle != null) {
                text.append(Text.literal(sb.toString()).setStyle(oldStyle));
                sb.setLength(0);
                sb.append(s);
            } else {
                text.append(Text.literal(s).setStyle(style));
            }
            oldStyle = style;

            return true;
        }
    }
}
