package net.velli.df_messenger;

import net.velli.scelli.widget.interfaces.ScrollableWidget;
import net.velli.scelli.widget.widgets.Widget;
import net.velli.scelli.widget.widgets.containers.VerticalListWidget;

public class DFMListWidget extends VerticalListWidget {
    @Override
    public void scrollChildren(int amount) {
        for(Widget<?> widget : this.getWidgets()) {
            if (widget instanceof ScrollableWidget sw && widget.isHovered()) {
                sw.onScroll(amount);
            }
        }
    }
}
