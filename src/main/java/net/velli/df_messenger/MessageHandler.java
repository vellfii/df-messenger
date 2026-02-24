package net.velli.df_messenger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.text.ObjectTextContent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.object.PlayerTextObjectContents;
import net.minecraft.text.object.TextObjectContents;
import net.velli.scelli.ScreenHandler;
import net.velli.scelli.widget.widgets.Alignment;
import net.velli.scelli.widget.widgets.StringInputData;
import net.velli.scelli.widget.widgets.TextDisplayWidget;
import net.velli.scelli.widget.widgets.Widgets;
import net.velli.scelli.widget.widgets.containers.ContainerWidget;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandler extends ContainerWidget<MessageHandler> {

    public static final MessageHandler instance = new MessageHandler();
    public final NotificationWidget notificationWidget = Widgets.create(NotificationWidget::new, 0, 30, 150, 25).withAlignment(Alignment.TOPRIGHT);

    public static float timer = 0f;

    public static boolean expectingLocate = false;
    public static String queuedPlayer = "";

    public MessageHandler() {
        addWidgets(notificationWidget);
    }

    public static final Pattern INC_REGEX = Pattern.compile("\\[(.+) → You] (.+)");
    public static final Pattern OUT_REGEX = Pattern.compile("\\[You → (.+)] (.+)");

    public static final Pattern LOCATE_SPAWN_REGEX = Pattern.compile(" {39}\\n(.+) currently at spawn\\n→ Server: (.+)\\n {39}");

    public static boolean packet(Packet<?> packet) {
        if (packet instanceof LoginHelloS2CPacket || packet instanceof GameStateChangeS2CPacket) MessageData.loadMessages();
        if (!(packet instanceof GameMessageS2CPacket(Text msgText, boolean overlay))) return false;
        System.out.println(msgText);
        String string = msgText.getString();

        if (expectingLocate) {
            expectingLocate = false;
            if (!string.equals("Error: Could not find that player.")) {
                if (MessageData.instance.messages.get(queuedPlayer) == null) {
                    MessageData.instance.messages.put(queuedPlayer, new ArrayList<>());
                    MessageData.instance.messages.get(queuedPlayer).add("");
                }
                MessageScreen.setPlayer(queuedPlayer);
                return true;
            }
        }

        Matcher inc = INC_REGEX.matcher(string);
        if (inc.find()) {
            String sender = inc.group(1);
            String message = inc.group(2);
            if (MessageData.instance.messages.get(sender) == null) {
                MessageData.instance.messages.put(sender, new ArrayList<>());
                MessageData.instance.messages.get(sender).add("");
            }
            MessageData.instance.messages.get(sender).add(sender + "§" + message);
            timer = 4f;
            if (instance.notificationWidget.getWidgets().getFirst() instanceof TextDisplayWidget tdw) {
                Text header = Text.object(
                        new PlayerTextObjectContents(ProfileComponent.ofDynamic(inc.group(1)), true))
                        .append(Text.literal(" " + inc.group(1) + ":"));
                tdw.setLines(header);
                for (OrderedText line : DFMessenger.wrapLines(Text.literal(message), 125)) {
                    tdw.addLine(line);
                }
            }
            if (!(DFMessenger.MC.currentScreen instanceof MessageScreen) || sender.equals(MessageScreen.player)) {
                MessageScreen.setPlayer(sender);
            }
            MessageScreen.updatePlayerList();
            MessageData.saveMessages();
            return true;
        }
        Matcher out = OUT_REGEX.matcher(string);
        if (out.find()) {
            String receiver = out.group(1);
            String message = out.group(2);
            if (MessageData.instance.messages.get(receiver) == null) {
                MessageData.instance.messages.put(receiver, new ArrayList<>());
                MessageData.instance.messages.get(receiver).add("");
            }
            MessageData.instance.messages.get(receiver).add("You§" + message);
            MessageScreen.setPlayer(receiver);
            MessageScreen.updatePlayerList();
            MessageData.saveMessages();
            return true;
        }

        return false;
    }

    public static void render(DrawContext context, RenderTickCounter renderTickCounter) {
        instance.withDimensions(MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight(), true);
        MessageHandler.instance.render(context, 0, 0);
    }

    @Override
    public void renderMain(DrawContext context, int mouseX, int mouseY, float delta) {
        if (timer > 0) {
            timer = Math.max(0, timer - delta);
            notificationWidget.withPosition(0, notificationWidget.y(), false);
        } else notificationWidget.withPosition(notificationWidget.width(), notificationWidget.y(), false);
        super.renderMain(context, mouseX, mouseY, delta);
    }

    @Override
    public MessageHandler getWidget() {
        return this;
    }

    public void onKeyPressed() {
        MessageScreen.updatePlayerList();
        ScreenHandler.openScreen(new MessageScreen());
    }
}
