package net.velli.df_messenger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.text.object.PlayerTextObjectContents;
import net.velli.scelli.ScreenHandler;
import net.velli.scelli.widget.widgets.Alignment;
import net.velli.scelli.widget.widgets.TextDisplayWidget;
import net.velli.scelli.widget.widgets.Widgets;
import net.velli.scelli.widget.widgets.containers.ContainerWidget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandler extends ContainerWidget<MessageHandler> {

    public static final MessageHandler instance = new MessageHandler();
    public final NotificationWidget notificationWidget = Widgets.create(NotificationWidget::new, 0, 30, 150, 25).withAlignment(Alignment.TOPRIGHT);

    public static float timer = 0f;

    public static boolean expectingWhois = false;
    public static boolean checkingPlayerOnline = false;
    private static boolean killNextShulkerSound = false;

    public MessageHandler() {
        addWidgets(notificationWidget);
    }

    public static final Pattern INC_REGEX = Pattern.compile("\\[([^\\[\\]]*) → You] (.+)");
    public static final Pattern OUT_REGEX = Pattern.compile("\\[You → ([^\\[\\]]*)] (.+)");

    public static final Pattern LOCATE_SPAWN_REGEX = Pattern.compile(" {39}\\n(.+) (is|are) currently at spawn\\n→ Server: (.+)\\n {39}");
    public static final Pattern LOCATE_PLOT_REGEX = Pattern.compile(" {39}\\n(.+) (is|are) currently (.+) on:\\n\\n→ (.+)\\n?(.+)?\\n→ Owner: (.+)\\n→ Server: (.+)\\n {39}");

    public static final Pattern WHOIS_REGEX = Pattern.compile(" {39}\\nProfile of ([^ ]+) (\\(.+\\))*?\\n\\n→ Ranks: (.+)\\n→ Badges: (.+)\\n→ Joined: (.+)\\n(.+\\n+)*? {39}");

    public static boolean packet(Packet<?> packet) {
        if (packet instanceof LoginHelloS2CPacket || packet instanceof GameStateChangeS2CPacket) {
            MessageData.loadMessages();
        }
        if (packet instanceof PlaySoundFromEntityS2CPacket sound) {
            if (sound.getSound().getIdAsString().equals("minecraft:entity.shulker.hurt_closed")) {
                if (killNextShulkerSound) {
                    killNextShulkerSound = false;
                    return true;
                }
            }
        }
        if (!(packet instanceof GameMessageS2CPacket(Text msgText, boolean overlay))) {
            return false;
        }
        String string = msgText.getString();

        if (expectingWhois) {
            expectingWhois = false;
            Matcher whois = WHOIS_REGEX.matcher(string);
            if (whois.find()) {
                String player = whois.group(1).equals("You") ? DFMessenger.MC.player.getName().getString() : whois.group(1);
                MessageScreen.setPlayer(player);
                MessageScreen.updatePlayerList();
                return true;
            }
//            Matcher locate = LOCATE_SPAWN_REGEX.matcher(string);
//            if (locate.find()) {
//                String player = locate.group(1).equals("You") ? DFMessenger.MC.player.getName().getString() : locate.group(1);
//                MessageScreen.setPlayer(player);
//                MessageScreen.updatePlayerList();
//                return true;
//            }
//            locate = LOCATE_PLOT_REGEX.matcher(string);
//            if (locate.find()) {
//                String player = locate.group(1).equals("You") ? DFMessenger.MC.player.getName().getString() : locate.group(1);
//                MessageScreen.setPlayer(player);
//                MessageScreen.updatePlayerList();
//                return true;
//            }
        }

        if (checkingPlayerOnline) {
            Matcher locate = LOCATE_SPAWN_REGEX.matcher(string);
            boolean found = false;
            if (locate.find()) {
                MessageScreen.onlineColors.put(MessageScreen.player, 0xFF00FF00);
                found = true;
            }
            locate = LOCATE_PLOT_REGEX.matcher(string);
            if (locate.find()) {
                MessageScreen.onlineColors.put(MessageScreen.player, 0xFF00FF00);
                found = true;
            }
            if (string.matches("Error: Could not find that player.")) {
                MessageScreen.onlineColors.put(MessageScreen.player, 0xFFFF0000);
                found = true;
            }
            if (found) {
                checkingPlayerOnline = false;
                killNextShulkerSound = true;
                Text header = Text.object(
                                new PlayerTextObjectContents(ProfileComponent.ofDynamic(MessageScreen.player), true))
                        .append(Text.literal(" " + MessageScreen.player))
                        .append(Text.literal(" ⏺").withColor(MessageScreen.onlineColors.getOrDefault(MessageScreen.player, 0xFF444444)));
                MessageScreen.messageBoxHeader.setLines(header);
                return true;
            }
        }

        Matcher inc = INC_REGEX.matcher(string);
        if (inc.find()) {
            String sender = inc.group(1);
            String message = inc.group(2);
            MessageData.addOrBumpPlayer(sender);
            MessageScreen.onlineColors.put(sender, 0xFF00FF00);
            MessageData.instance.messages.get(sender).add(System.currentTimeMillis() + "§" + sender + "§" + message);
            timer = 4f;
            if (instance.notificationWidget.getWidgets().getFirst() instanceof TextDisplayWidget tdw) {
                Text header = Text.object(
                        new PlayerTextObjectContents(ProfileComponent.ofDynamic(sender), true))
                        .append(Text.literal(" " + sender + ":"));
                tdw.setLines(header);
                tdw.addLine(Text.literal(message));
            }
            if (!(DFMessenger.MC.currentScreen instanceof MessageScreen) || sender.equals(MessageScreen.player)) {
                MessageScreen.setPlayer(sender);
            }
            MessageScreen.messageBox.setScrollAmount(0);
            MessageScreen.updatePlayerList();
            MessageData.saveMessages();
            return true;
        }
        Matcher out = OUT_REGEX.matcher(string);
        if (out.find()) {
            String receiver = out.group(1);
            String message = out.group(2);
            MessageData.addOrBumpPlayer(receiver);
            MessageData.instance.messages.get(receiver).add(System.currentTimeMillis() + "§Y§" + message);
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
