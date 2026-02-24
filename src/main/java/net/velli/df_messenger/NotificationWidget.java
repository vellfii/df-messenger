package net.velli.df_messenger;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.velli.scelli.widget.widgets.Alignment;
import net.velli.scelli.widget.widgets.TextDisplayWidget;
import net.velli.scelli.widget.widgets.Widgets;
import net.velli.scelli.widget.widgets.containers.ContainerWidget;

public class NotificationWidget extends ContainerWidget<NotificationWidget> {

    private TextDisplayWidget text = Widgets.create(TextDisplayWidget::new, 5, 3, 150, 100);

    public NotificationWidget() {
        addWidgets(text);
    }

    @Override
    public void renderMain(DrawContext context, int mouseX, int mouseY, float delta) {
        withDimensions(text.width() + 10, (10 * text.lines().size()) + 6, true);

        context.fill(0, 0, width(), height(), 0x66000000);
        super.renderMain(context, mouseX, mouseY, delta);
    }

    @Override
    public NotificationWidget getWidget() {
        return this;
    }
}
