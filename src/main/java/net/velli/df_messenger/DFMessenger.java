package net.velli.df_messenger;

import com.google.common.collect.Lists;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class DFMessenger implements ClientModInitializer {

    public static final String MODID = "df_messenger";
    public static MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, Identifier.of(DFMessenger.MODID, "after_chat"), MessageHandler::render);
        DFMKeyBinds.init();

    }

    public static void sendCommand(String command) {
        Objects.requireNonNull(MC.getNetworkHandler()).sendChatCommand(command);
    }

    public static List<OrderedText> wrapLines(StringVisitable text, int width) {
        return MC.textRenderer.wrapLines(text, width);
    }
}
