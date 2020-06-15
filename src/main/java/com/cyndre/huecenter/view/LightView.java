package com.cyndre.huecenter.view;

import javax.swing.*;
import java.awt.*;

public class LightView extends JComponent {
    public static final Dimension DEFAULT_DIMENSIONS = new Dimension(50, 50);

    private final Dimension dimensions;
    private final String name;
    private final Color backgroundColor;
    private final Color textColor;

    public LightView(final Color light, final String name) {
        this(light, name, DEFAULT_DIMENSIONS);
    }

    public LightView(final Color color, final String name, final Dimension dimensions) {
        this.backgroundColor = color;
        this.textColor = contrastColor(color);
        this.name = name;
        this.dimensions = dimensions;
        setPreferredSize(this.dimensions);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(this.backgroundColor);
        g.fillRect(0, 0, this.dimensions.width, this.dimensions.height);
        g.setColor(this.textColor);
        g.drawString(name, this.dimensions.width / 2, this.dimensions.height / 2);
    }

    // from https://stackoverflow.com/questions/1855884/determine-font-color-based-on-background-color
    private static Color contrastColor(final Color color) {
        // Counting the perceptive luminance - human eye favors green color...
        double luminance = ((0.299 * color.getRed()) + (0.587 * color.getGreen()) + (0.114 * color.getBlue()))/255;

        return (luminance > 0.5) ? Color.black : Color.white;
    }
}
