import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import javazoom.jl.decoder.*;
import javazoom.jl.player.Player;

public class MusicWaveVisualizer extends JPanel {
    private final ArrayList<Integer> amplitudes = new ArrayList<>();
    private final int width = 400; // Chiều rộng của bảng sóng
    private final int height = 300; // Chiều cao của bảng sóng

    public MusicWaveVisualizer() {
        setPreferredSize(new Dimension(width, 100)); // Đặt kích thước của JPanel cho bảng sóng
        setBackground(Color.BLACK);
    }

    public void playAndVisualize(String filePath) {
        System.out.println("lalala: " + filePath);
        new Thread(() -> {
            try {
                FileInputStream fileInputStream = new FileInputStream(filePath);
                Player player = new Player(fileInputStream);

                InputStream mp3Stream = new FileInputStream(filePath);
                Bitstream bitstream = new Bitstream(mp3Stream);
                Decoder decoder = new Decoder();

                Header header;
                while ((header = bitstream.readFrame()) != null) {
                    SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                    short[] samples = output.getBuffer();

                    int sum = 0;
                    for (short sample : samples) {
                        sum += Math.abs(sample);
                    }

                    if (amplitudes.size() >= width) {
                        amplitudes.remove(0);
                    }
                    amplitudes.add(sum / samples.length);

                    // Cập nhật bảng sóng
                    SwingUtilities.invokeLater(this::repaint);

                    bitstream.closeFrame();
                    long frameDuration = (long) header.ms_per_frame();
                    Thread.sleep(frameDuration);
                }

                player.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);

        if (amplitudes.isEmpty()) {
            return;
        }

        int step = Math.max(1, amplitudes.size() / width);
        int startX = (int) (width * 0.1); // Bắt đầu tại 10% chiều rộng
        int endX = (int) (width * 0.9); // Kết thúc tại 90% chiều rộng

        for (int i = 0; i < amplitudes.size(); i++) {
            int value = amplitudes.get(i);
            int barHeight = (int) ((value / 32768.0) * height); // Normalize amplitude
            int barX = startX + (endX - startX) * i / (width - 1);
            int barY = getHeight() / 2 - barHeight / 2;
            g.drawLine(barX, barY, barX, barY + barHeight);
        }
        repaint();
    }
}




