import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

class ImageAction extends JLabel {
    private Image image;
    private int panelWidth, panelHeight;
    private double angle = 0; 
    private Timer timer;

    public ImageAction(Image image, int panelWidth, int panelHeight) {
        this.image = image;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;

        timer = new Timer(16, e -> {
            angle += Math.toRadians(1); 
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int centerX = panelWidth / 2;
        int centerY = panelHeight / 2;

        AffineTransform transform = new AffineTransform();
        transform.translate(centerX, centerY);
        transform.rotate(angle);
        transform.translate(-image.getWidth(null) / 2, -image.getHeight(null) / 2);
        g2d.drawImage(image, transform, null);
    }
}
