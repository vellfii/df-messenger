package net.velli.df_messenger;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.Text;
import net.minecraft.text.object.PlayerTextObjectContents;
import net.velli.scelli.Scelli;
import net.velli.scelli.screen.WidgetContainerScreen;
import net.velli.scelli.widget.widgets.*;
import net.velli.scelli.widget.widgets.containers.VerticalListWidget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageScreen extends WidgetContainerScreen {
    private static final VerticalListWidget playerList = Widgets.create(VerticalListWidget::new, 0, 0, 100, 280).getWidget();

    private static final VerticalListWidget messageBox = Widgets.create(VerticalListWidget::new, 0, 0, 285, 247).getWidget()
            .withPadding(2, 4, 4, 2)
            .reversed();

    private static final TextDisplayWidget messageBoxHeader = Widgets.create(TextDisplayWidget::new, 0, 0, 285, 9).withTextAlignment(Alignment.CENTER);

    public static String player = null;

    private static final StringInputWidget inputBox = Widgets.create(StringInputWidget::new, 0, 0, 285, 16)
            .withConfirmEvent((stringInputWidget) -> {
        DFMessenger.sendCommand("msg " + player + " " + stringInputWidget.getString());
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
        updatePlayerList();
    }

    public static void setPlayer(String name) {
        List<String> msgs = MessageData.instance.messages.get(name);
        if (msgs == null) return;
        messageBox.clearWidgets();
        for (String msg : msgs) {
            List<String> split = List.of(msg.split("§"));
            TextDisplayWidget msgWidget = Widgets.create(TextDisplayWidget::new, 0, 0, 270, 9);
            Text line;
            if (split.size() < 2) {
                line = Text.literal(msg);
                msgWidget.withTextAlignment(Alignment.CENTER);
            }
            else line = Text.literal(split.get(1));
            if (split.getFirst().equals("You")) {
                msgWidget.withTextAlignment(Alignment.RIGHT);
                line = Text.literal(split.get(1)).withColor(0xFFb7fcff);
            }
            msgWidget.setLines(new ArrayList<>(DFMessenger.wrapLines(line, 140)));
            messageBox.addWidgets(msgWidget);
        }
        player = name;
        Text header = Text.object(
                new PlayerTextObjectContents(ProfileComponent.ofDynamic(name), true))
                .append(Text.literal(" " + name));
//        messageBox.setScrollAmount(0);
        messageBoxHeader.setLines(header);
    }

    public static void updatePlayerList() {
        List<String> players = new ArrayList<>(MessageData.instance.messages.keySet());
        playerList.clearWidgets();
        playerList.addWidgets(Widgets.create(StringInputWidget::new, 0, 0, 90, 16)
                .withConfirmEvent(stringInputWidget -> {
                String player = stringInputWidget.getString();
                if (player.isBlank()) player = "blingledinglesporpus";
                DFMessenger.sendCommand("locate " + player);
                MessageHandler.queuedPlayer = player;
                MessageHandler.expectingLocate = true;
                stringInputWidget.setString("");
        }));
        if (players.isEmpty()) return;
        for (String player : players) {
            final String finPlayer = player;
            ButtonWidget playerWidget = Widgets.create(ButtonWidget::new, 0, 0, 90, 16);
            Text line = Text.literal(player).withColor(0xffc8c8c8);
            if (MessageScreen.player != null && MessageScreen.player.equals(player)) line = Text.literal(player);
            playerWidget.withText(line);
            playerList.addWidgets(playerWidget.withClickEvent(new ButtonWidget.ClickEvent() {
                final String player = finPlayer;
                @Override
                public void onClick(ButtonWidget buttonWidget, int i, int i1) {
                    if (buttonWidget.isHovered()) MessageScreen.setPlayer(player);
                }

                @Override
                public void onRelease(ButtonWidget buttonWidget, int i, int i1) {}
            }));
        }
    }
}
