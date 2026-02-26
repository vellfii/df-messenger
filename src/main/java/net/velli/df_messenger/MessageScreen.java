package net.velli.df_messenger;

import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.object.PlayerTextObjectContents;
import net.velli.scelli.screen.WidgetContainerScreen;
import net.velli.scelli.widget.widgets.*;
import net.velli.scelli.widget.widgets.containers.VerticalListWidget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageScreen extends WidgetContainerScreen {
    private static final VerticalListWidget playerList = Widgets.create(VerticalListWidget::new, 0, 0, 100, 280).getWidget();

    static final VerticalListWidget messageBox = Widgets.create(VerticalListWidget::new, 0, 0, 285, 247).getWidget()
            .withPadding(2, 4, 4, 2)
            .reversed();

    private static final TextDisplayWidget messageBoxHeader = Widgets.create(TextDisplayWidget::new, 0, 0, 285, 9).withTextAlignment(Alignment.CENTER);

    public static String player = "";
    private static long time = 0;
    private static boolean yourMessage = false;

    private static final List<String> months = List.of(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
    );

    private static final StringInputWidget inputBox = Widgets.create(StringInputWidget::new, 0, 0, 285, 16)
            .withConfirmEvent((stringInputWidget) -> {
        if (stringInputWidget.getString().isEmpty()) {
            return;
        }
        DFMessenger.sendCommand("msg " + player + " " + stringInputWidget.getString() + "\u200C");
        stringInputWidget.clearString();
    });

    protected MessageScreen() {
        super(null);
        addWidgets(
                Widgets.create(VerticalListWidget::new, 0, 0, 400, 300)
                        .addWidgets(playerList)
                        .newColumn()
                        .addWidgets(messageBoxHeader, messageBox, inputBox)
                        .withPadding(5, 10, 4, 5)
                        .withAlignment(Alignment.CENTER)
        );
        inputBox.selected = true;
        updatePlayerList();
    }

    public static void setPlayer(String name) {
        List<String> msgs = MessageData.instance.messages.get(name);
        if (msgs == null) msgs = new ArrayList<>();
        messageBox.clearWidgets();
        time = 0;
        for (String msg : msgs) {
            List<String> split = List.of(msg.split("§"));
            if (split.size() <= 2) {
                messageBox.addWidgets(legacyMessageParse(split));
            } else {
                messageBox.addWidgets(messageParse(split));
            }
        }
        messageBox.setScrollAmount(0);
        player = name;
        Text header = Text.object(
                new PlayerTextObjectContents(ProfileComponent.ofDynamic(name), true))
                .append(Text.literal(" " + name));
        messageBoxHeader.setLines(header);
    }

    private static TextDisplayWidget messageParse(List<String> split) {
        WrappedTextDisplayWidget msgWidget = ((WrappedTextDisplayWidget) Widgets.create(WrappedTextDisplayWidget::new, 0, 0, 270, 9)).withTextWidth(140);

        Date oldDate = new Date(time);
        Date date = new Date(Long.parseLong(split.getFirst()));
        boolean lastWasYours = yourMessage;
        time = Long.parseLong(split.getFirst());
        yourMessage = split.get(1).equals("Y");

        Text line;
        if (split.get(1).equals("Y")) {
            msgWidget.withTextAlignment(Alignment.RIGHT);
            line = Text.literal(split.get(2)).withColor(0xFFb7fcff);
        } else {
            if (!split.get(2).endsWith("\u200C")) {
                line = Text.literal(split.get(2)).withColor(0xffADFFAF);
            } else {
                line = Text.literal(split.get(2)).withColor(0xFFb7fcff);
            }
        }

        List<String> oldDateDetails = List.of(oldDate.toString().split(" "));
        List<String> dateDetails = List.of(date.toString().split(" "));
        List<String> oldHourMinuteSecond = List.of(oldDateDetails.get(3).split(":"));
        List<String> hourMinuteSecond = List.of(dateDetails.get(3).split(":"));

        ArrayList<OrderedText> lines = new ArrayList<>();

        if (Integer.parseInt(dateDetails.get(5)) > Integer.parseInt(oldDateDetails.get(5))
                || months.indexOf(dateDetails.get(1)) > months.indexOf(oldDateDetails.get(1))
                || Integer.parseInt(dateDetails.get(2)) > Integer.parseInt(oldDateDetails.get(2))) {
            messageBox.addWidgets(
                    Widgets.create(TextDisplayWidget::new, 0, 0, 270, 9)
                            .setLines(Text.literal(new SimpleDateFormat("MMMM d, yyyy").format(date)).withColor(0xff666666))
                            .withTextAlignment(Alignment.CENTER)
            );

            lines.add(Text.literal(
                    (yourMessage ? DFMessenger.MC.player.getName().getString() : split.get(1)) + " at " +
                            new SimpleDateFormat("hh:mm a").format(date)).withColor(0xff888888).asOrderedText());
        } else if (Integer.parseInt(hourMinuteSecond.getFirst()) > Integer.parseInt(oldHourMinuteSecond.getFirst())
                || Integer.parseInt(hourMinuteSecond.get(1)) > Integer.parseInt(oldHourMinuteSecond.get(1)) + 2
                || lastWasYours != yourMessage) {
            lines.add(Text.literal(
                    (yourMessage ? DFMessenger.MC.player.getName().getString() : split.get(1)) + " at " +
                    new SimpleDateFormat("hh:mm a").format(date)).withColor(0xff888888).asOrderedText());
        }

        lines.add(line.asOrderedText());
        msgWidget.setLines(lines);
        return msgWidget;
    }

    private static TextDisplayWidget legacyMessageParse(List<String> split) {
        TextDisplayWidget msgWidget = Widgets.create(TextDisplayWidget::new, 0, 0, 270, 9);
        Text line;
        if (split.size() < 2) {
            line = Text.literal("");
            msgWidget.withTextAlignment(Alignment.CENTER);
        }
        else line = Text.literal(split.get(1));
        if (split.getFirst().equals("You")) {
            msgWidget.withTextAlignment(Alignment.RIGHT);
            line = Text.literal(split.get(1)).withColor(0xFFb7fcff);
        }
        msgWidget.setLines(new ArrayList<>(DFMessenger.wrapLines(line, 140)));
        return msgWidget;
    }

    public static void updatePlayerList() {
        List<String> players = MessageData.instance.playerOrder;
        playerList.clearWidgets();
        playerList.addWidgets(Widgets.create(StringInputWidget::new, 0, 0, 90, 16)
                .withConfirmEvent(stringInputWidget -> {
                String player = stringInputWidget.getString();
                if (player.isBlank()) player = "blingledinglesporpus";
                DFMessenger.sendCommand("locate " + player);
                MessageHandler.expectingLocate = true;
                stringInputWidget.setString("");
        }));
        if (players.isEmpty()) return;
        for (String player : players) {
            final String finPlayer = player;
            ButtonWidget playerWidget = Widgets.create(ButtonWidget::new, 0, 0, 90, 16);
            Text line = Text.literal(player).withColor(0xffaaaaaa);
            if (MessageScreen.player.equals(player)) line = Text.literal(player);
            playerWidget.withText(line);
            playerList.addWidgets(playerWidget.withClickEvent(new ButtonWidget.ClickEvent() {
                final String player = finPlayer;
                @Override
                public void onClick(ButtonWidget buttonWidget, int i, int i1) {
                    if (buttonWidget.isHovered()) {
                        setPlayer(player);
                        updatePlayerList();
                    }
                }

                @Override
                public void onRelease(ButtonWidget buttonWidget, int i, int i1) {}
            }));
        }
    }
}
