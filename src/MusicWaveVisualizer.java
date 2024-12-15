import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import javazoom.jl.decoder.*;
import javazoom.jl.player.Player;

public class MusicWaveVisualizer extends JPanel {
    private final ArrayList<Integer> amplitudes = new ArrayList<>();
    private final int width = 400;
    private final int height = 300;
    private Thread visualizationThread;
    private volatile boolean isRunning;
    private boolean isPlaying = true;
    private String currentFilePath;


    public MusicWaveVisualizer() {
        setPreferredSize(new Dimension(width, 100));
        setBackground(Color.BLACK);
    }

    public void playAndVisualize(String filePath) {
        isRunning = true;
        visualizationThread = new Thread(() -> {
            try {
                FileInputStream fileInputStream = new FileInputStream(filePath);
                Player player = new Player(fileInputStream);

                InputStream mp3Stream = new FileInputStream(filePath);
                Bitstream bitstream = new Bitstream(mp3Stream);
                Decoder decoder = new Decoder();

                Header header;
                while (isRunning && isPlaying && (header = bitstream.readFrame()) != null) {
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

                    SwingUtilities.invokeLater(this::repaint);

                    bitstream.closeFrame();
                    long frameDuration = (long) header.ms_per_frame();
                    Thread.sleep(frameDuration);
                }
                player.close();
                bitstream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        visualizationThread.start();
    }

    public synchronized void setPlaying(boolean isPlaying) {
        this.isRunning = isPlaying;
        if (!isRunning) {
            stopCurrentVisualization();
        }  else {
            if (visualizationThread == null || !visualizationThread.isAlive()) {
                playAndVisualize(currentFilePath);
            }
        }
    }

    public synchronized void updateWaveData(String filePath) {
        currentFilePath = filePath;
        amplitudes.clear();
        repaint();
        playAndVisualize(filePath);
    }

    private synchronized void stopCurrentVisualization() {
        isRunning = false;
        if (visualizationThread != null && visualizationThread.isAlive()) {
            try {
                visualizationThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);

        if (amplitudes.isEmpty()) {
            return;
        }

        int startX = (int) (width * 0.1);
        int endX = (int) (width * 0.9);

        for (int i = 0; i < amplitudes.size(); i++) {
            int value = amplitudes.get(i);
            int barHeight = (int) ((value / 32768.0) * height);
            int barX = startX + (endX - startX) * i / (width - 1);
            int barY = getHeight() / 2 - barHeight / 2;
            g.drawLine(barX, barY, barX, barY + barHeight);
        }
    }
}




