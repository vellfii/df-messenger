package net.velli.df_messenger;

import com.ibm.icu.text.BidiTransform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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
}
