import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

public class CustomSliderUI extends BasicSliderUI {
    public CustomSliderUI(JSlider slider) {
        super(slider);
    }

    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        int thumbRadius = 10;
        g2.setColor(Color.WHITE);
        g2.fillOval(thumbRect.x, thumbRect.y, thumbRadius * 2, thumbRadius * 2);

        g2.dispose();
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int trackHeight = 6;
        int cy = trackRect.y + (trackRect.height / 2) - (trackHeight / 2);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(trackRect.x, cy, trackRect.width, trackHeight, trackHeight, trackHeight);

        int progressWidth = thumbRect.x + thumbRect.width / 2 - trackRect.x;
        g2.setColor(Color.BLUE);
        g2.fillRoundRect(trackRect.x, cy, progressWidth, trackHeight, trackHeight, trackHeight);

        g2.dispose();
    }
}
