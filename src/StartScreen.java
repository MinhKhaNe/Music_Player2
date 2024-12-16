import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StartScreen extends JFrame {

    public StartScreen() {
        setTitle("");
        setSize(400, 600);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(Color.BLACK);

        ImageIcon beginImage = new ImageIcon("src/image/avatar.png");
        JLabel imageLabel = new JLabel(beginImage, JLabel.CENTER);
        add(imageLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        buttonPanel.setBackground(Color.BLACK);
        JButton startButton = new JButton("Click to start");
        startButton.setFont(new Font("Dialog", Font.PLAIN, 24));
        startButton.setBackground(Color.BLACK);
        startButton.setForeground(Color.WHITE);
        startButton.setBorder(BorderFactory.createEmptyBorder());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new MusicPlayerGUI().setVisible(true);
                dispose(); //
            }
        });

        buttonPanel.add(startButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}


