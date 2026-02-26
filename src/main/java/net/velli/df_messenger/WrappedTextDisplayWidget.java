package net.velli.df_messenger;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.velli.scelli.Scelli;
import net.velli.scelli.widget.widgets.Alignment;
import net.velli.scelli.widget.widgets.TextDisplayWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class WrappedTextDisplayWidget extends TextDisplayWidget {

    private int textWidth = 0;

    @Override
    public void renderMain(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = Scelli.MC.textRenderer;
        if (textWidth == 0) textWidth = width();
        int offsetY = 0;
        List<OrderedText> renderLines = new ArrayList<>();
        for (OrderedText line : lines) {
            MutableText text = Text.empty();
            StringBuilder sb = new StringBuilder();
            AtomicReference<Style> oldStyle = new AtomicReference<>();
            AtomicReference<Style> curStyle = new AtomicReference<>();
            line.accept((index, style, codePoint) -> {
                curStyle.set(style);
                String s = new String(Character.toChars(codePoint));
                if (oldStyle.get() == null || oldStyle.get() == style) {
                    sb.append(s);
                } else {
                    text.append(Text.literal(sb.toString()).setStyle(oldStyle.get()));
                    sb.setLength(0);
                    sb.append(s);
                }
                oldStyle.set(style);
                return true;
            });
            text.append(Text.literal(sb.toString()).setStyle(curStyle.get()));
            renderLines.addAll(textRenderer.wrapLines(text, textWidth));
        }
        for (OrderedText line : renderLines) {
            int offsetX;
            if (Objects.equals(alignment, Alignment.LEFT)) offsetX = 0;
            else if (Objects.equals(alignment, Alignment.RIGHT)) offsetX = width() - textRenderer.getWidth(line);
            else offsetX = width() / 2 - textRenderer.getWidth(line) / 2;
            context.drawText(textRenderer, line, offsetX, offsetY, 0xFFFFFFFF, true);
            offsetY += textRenderer.fontHeight + 1;
        }
    }


    @Override
    public void hover(int mouseX, int mouseY, boolean active) {
        super.hover(mouseX, mouseY, active);
        TextRenderer textRenderer = Scelli.MC.textRenderer;
        List<OrderedText> renderLines = new ArrayList<>();
        for (OrderedText line : lines) {
            StringBuilder sb = new StringBuilder();
            line.accept(((index, style, codePoint) -> {
                String s = new String(Character.toChars(codePoint));
                sb.append(s);
                return true;
            }));
            renderLines.addAll(textRenderer.wrapLines(Text.literal(sb.toString()), textWidth));
        }
        withDimensions(width(), (renderLines.size() * (Scelli.MC.textRenderer.fontHeight + 1)) - 1, true);
    }

    @Override
    public WrappedTextDisplayWidget getWidget() {
        return this;
    }

    public WrappedTextDisplayWidget withTextWidth(int textWidth) {
        this.textWidth = textWidth;
        return getWidget();
    }
}
