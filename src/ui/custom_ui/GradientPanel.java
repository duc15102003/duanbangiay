package ui.custom_ui;

import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.JPanel;

public class GradientPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // nền gradient sáng (không dùng màu đen)
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(170, 210, 255),
                w, h, new Color(220, 180, 255)
        );
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // wave 1
        Path2D wave1 = new Path2D.Double();
        wave1.moveTo(0, h * 0.6);

        for (int x = 0; x <= w; x += 20) {
            double y = h * 0.6 + Math.sin(x * 0.01) * 40;
            wave1.lineTo(x, y);
        }

        wave1.lineTo(w, h);
        wave1.lineTo(0, h);
        wave1.closePath();

        GradientPaint aurora1 = new GradientPaint(
                0, (int) (h * 0.5), new Color(120, 255, 220, 120),
                w, h, new Color(120, 180, 255, 120)
        );

        g2.setPaint(aurora1);
        g2.fill(wave1);

        // wave 2
        Path2D wave2 = new Path2D.Double();
        wave2.moveTo(0, h * 0.4);

        for (int x = 0; x <= w; x += 20) {
            double y = h * 0.4 + Math.sin(x * 0.015 + 1) * 30;
            wave2.lineTo(x, y);
        }

        wave2.lineTo(w, h);
        wave2.lineTo(0, h);
        wave2.closePath();

        GradientPaint aurora2 = new GradientPaint(
                0, (int) (h * 0.3), new Color(255, 180, 255, 120),
                w, h, new Color(150, 255, 255, 120)
        );

        g2.setPaint(aurora2);
        g2.fill(wave2);
    }
}