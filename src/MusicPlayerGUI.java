import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;


public class MusicPlayerGUI extends JFrame {
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;
    private boolean replayingSong = false;
    private boolean isPlaying = false;
    private boolean replayImage=false;
    private boolean isFavorite=false;
    private Timer timer;
    private boolean timerFinished;
    private JFileChooser jFileChooser;
    private JLabel songTitle, songArtist, songImage;
    private JPanel playbackBtns;
    private JSlider playbackSlider;
    private JButton replayButton;
    private JButton favoriteButton;
    private JButton timerButton;
    private MusicWaveVisualizer musicWave;


    public MusicPlayerGUI(){
        super("Basic Music Player");

        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);

        jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("src/assets"));
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();

        musicWave = new MusicWaveVisualizer();
        add(musicWave, BorderLayout.SOUTH);
    }

    private void addGuiComponents(){
        addToolbar();

        songImage = new JLabel(loadImage("src/image/avatar.png"));
        songImage.setBounds(0, 50, getWidth() - 20, 225);
        add(songImage);

        songTitle = new JLabel("Song title");
        songTitle.setBounds(80, 285, getWidth() - 100, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 17));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.LEFT);
        add(songTitle);

        songArtist = new JLabel("Singer name");
        songArtist.setBounds(80, 305, getWidth() - 100, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 12));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.LEFT);
        add(songArtist);

        File favoriteFile = new File("src/assets/Favorite.txt");
        favoriteButton = new JButton(loadImage("src/image/heart.png"));
        favoriteButton.setBounds(getWidth() - 100, 285, 50, 50);
        favoriteButton.setBorderPainted(false);
        favoriteButton.setBackground(null);
        favoriteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String songInfo = songTitle.getText();
                isFavorite = !isFavorite;
                try {
                    if (isFavorite) {
                        addSongToFavorites(favoriteFile, songInfo);
                        System.out.println("like");
                    } else {
                        removeSongFromFavorites(favoriteFile, songInfo);
                        System.out.println("unlike");
                    }
                    updateFavoriteSong();
                }catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(favoriteButton);

        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setUI(new CustomSliderUI(playbackSlider));
        playbackSlider.setBounds(getWidth()/2 - 300/2, 335, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JSlider source = (JSlider) e.getSource();
                int frame = source.getValue();
                musicPlayer.setCurrentFrame(frame);
                musicPlayer.setCurrentTimeInMilli((int) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));
                musicPlayer.playCurrentSong();
                enablePauseButtonDisablePlayButton();
            }
        });
        add(playbackSlider);
        addPlaybackBtns();
    }

    private void addSongToFavorites(File file, String songInfo) throws IOException {
        ArrayList<String> existingSongs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                existingSongs.add(line.trim());
            }
        }

        if (!existingSongs.contains("src/assets/"+songInfo + ".mp3")) {
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write("src/assets/"+songInfo + ".mp3\n");
            }
        }
    }

    private void removeSongFromFavorites(File file, String songInfo) throws IOException {
        ArrayList<String> remainingLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().equals("src/assets/"+songInfo + ".mp3")) {
                    remainingLines.add(line);
                }
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : remainingLines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private void addToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);
        toolBar.setFloatable(false);

        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        JMenuItem loadSong = new JMenuItem("Load Song");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    Song song = new Song(selectedFile.getPath());

                    musicPlayer.loadSong(song);

                    updateSongTitleAndArtist(song);

                    updateSongImage(song);

                    updatePlaybackSlider(song);

                    enablePauseButtonDisablePlayButton();

                    isFavorite = false;
                    updateFavoriteSong();

                }
            }
        });
        songMenu.add(loadSong);

        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // load music playlist dialog
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    musicPlayer.stopSong();
                    musicPlayer.loadPlaylist(selectedFile);
                    Song song = musicPlayer.getCurrentSong();
                    updateSongImage(song);
                }
            }
        });
        playlistMenu.add(loadPlaylist);

        JMenu favoriteMenu = new JMenu("Favorite Song");
        menuBar.add(favoriteMenu);
        JMenuItem playFavoriteSong = new JMenuItem("Play Favorite Song");
        playFavoriteSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Favorite", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    musicPlayer.stopSong();
                    musicPlayer.loadPlaylist(selectedFile);

                    Song song = musicPlayer.getCurrentSong();
                    System.out.println(song.getSongTitle());
                    updateSongImage(song);

                }
            }
        });
        favoriteMenu.add(playFavoriteSong);

        add(toolBar);
    }

    private void addPlaybackBtns(){
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 385, getWidth() - 10, 80);
        playbackBtns.setBackground(null);

        replayButton = new JButton(loadImage("src/image/replay.png"));
        replayButton.setBorderPainted(false);
        replayButton.setBackground(null);
        replayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replayImage = !replayImage;
                updateReplayImage();
                musicPlayer.replaySong();
            }
        });
        playbackBtns.add(replayButton);

        JButton prevButton = new JButton(loadImage("src/image/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.prevSong();
            }
        });
        playbackBtns.add(prevButton);

        JButton playButton = new JButton(loadImage("src/image/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePauseButtonDisablePlayButton();
                musicPlayer.playCurrentSong();
                isPlaying=true;
                musicWave.setPlaying(isPlaying);
            }
        });
        playbackBtns.add(playButton);

        JButton pauseButton = new JButton(loadImage("src/image/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePlayButtonDisablePauseButton();
                musicPlayer.pauseSong();
                isPlaying=false;
                musicWave.setPlaying(isPlaying);
            }
        });
        playbackBtns.add(pauseButton);

        JButton nextButton = new JButton(loadImage("src/image/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.nextSong();
            }
        });
        playbackBtns.add(nextButton);

        timerButton = new JButton(loadImage("src/image/timer.png"));
        timerButton.setBorderPainted(false);
        timerButton.setBackground(null);
        timerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTimerDialog();
            }
        });
        playbackBtns.add(timerButton);

        add(playbackBtns);
    }

    private void showTimerDialog() {
        JDialog timerDialog = new JDialog((Frame) null, "Set Timer", true);
        timerDialog.setSize(300, 200);
        timerDialog.setLayout(new BorderLayout());

        JTextField timeDisplayField = new JTextField("0:00", 10);
        timeDisplayField.setHorizontalAlignment(SwingConstants.CENTER);
        timeDisplayField.setFont(new Font("Dialog", Font.BOLD, 30));
        timerDialog.add(timeDisplayField, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        bottomPanel.add(okButton);
        bottomPanel.add(cancelButton);
        timerDialog.add(bottomPanel, BorderLayout.SOUTH);

        okButton.addActionListener(e -> {
            String input = timeDisplayField.getText().trim();
            String[] timeParts = input.split(":");

            if (timeParts.length == 2) {
                try {
                    int minutes = Integer.parseInt(timeParts[0].trim());
                    int seconds = Integer.parseInt(timeParts[1].trim());

                    if (minutes >= 0 && seconds >= 0 && seconds < 60) {
                        setTimer(minutes * 60 + seconds, timeDisplayField);
                        timerFinished=true;
                        timerDialog.dispose();
                        updateTimer();
                    } else {
                        JOptionPane.showMessageDialog(timerDialog, "Invalid time input!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(timerDialog, "Invalid number format!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(timerDialog, "Invalid time format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> {
            timerDialog.dispose();
            timerFinished=false;
            updateTimer();
        });

        timerDialog.setLocationRelativeTo(null);
        timerDialog.setVisible(true);
    }

    public void updateTimer(){
        if(timerFinished){
            timerButton.setIcon(loadImage("src/image/timer1.png"));
        }else{
            timerButton.setIcon(loadImage("src/image/timer.png"));
        }
    }

    private void setTimer(int totalSeconds, JTextField timeDisplayField) {
        timeDisplayField.setText(formatTime(totalSeconds));

        Timer currentTimer = new Timer(1000, e -> {
            int remainingTime = convertToSeconds(timeDisplayField.getText().trim());
            remainingTime--;

            if (remainingTime <= 0) {
                ((Timer)e.getSource()).stop();
                enablePlayButtonDisablePauseButton();

                musicPlayer.pauseSong();
                isPlaying=false;
                musicWave.setPlaying(isPlaying);
                timerFinished=false;
                updateTimer();
            } else {
                timeDisplayField.setText(formatTime(remainingTime));
            }
        });
        currentTimer.setInitialDelay(0);
        currentTimer.start();
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    private int convertToSeconds(String time) {
        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return minutes * 60 + seconds;
    }

    public void approval(){
        replayingSong=true;
        File selectedFile = jFileChooser.getSelectedFile();

        if(replayingSong){
            Song song = new Song(selectedFile.getPath());
            musicPlayer.loadSong(song);
            updatePlaybackSlider(song);

            replayingSong = false;
            replayImage=false;
            updateReplayImage();

            String mp3FilePath = "src/assets/" + song.getSongTitle() + ".mp3";
            musicWave.updateWaveData(mp3FilePath);
            startPlaying();
        }
    }

    public void setPlaybackSliderValue(int frame){
        playbackSlider.setValue(frame);
    }

    public void updateSongTitleAndArtist(Song song) {
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    public void updateSongImage(Song song){
        remove(songImage);

        ImageIcon icon = new ImageIcon("src/image/" + song.getSongTitle() + ".png");
        ImageAction imageAction = new ImageAction(icon.getImage(), getWidth() - 20, 225);
        songImage = imageAction;
        songImage.setBounds(0, 50, getWidth() - 20, 225);

        add(songImage);

        revalidate();
        repaint();

        String mp3FilePath = "src/assets/" + song.getSongTitle() + ".mp3";
        musicWave.updateWaveData(mp3FilePath);
        startPlaying();
    }

    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }

    public void startPlaying() {
        if (musicWave != null) {
            remove(musicWave);
        }

        musicWave = new MusicWaveVisualizer();
        musicWave.setBounds(0, 450, 400, 100); 

        setLayout(null);
        add(musicWave);
        revalidate();
        repaint();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(400, 600);
        setLocationRelativeTo(null);
        setVisible(true);
        getContentPane().setBackground(FRAME_COLOR);

        MusicPlayer musicPlayer = getMusicPlayer();
        Song song = musicPlayer.getCurrentSong();

        String mp3FilePath = "src/assets/" + song.getSongTitle() + ".mp3";
        musicWave.updateWaveData(mp3FilePath);
    }

    public void updateReplayImage() {
        if (replayImage) {
            replayButton.setIcon(loadImage("src/image/replay1.png"));
        } else {
            replayButton.setIcon(loadImage("src/image/replay.png"));
        }
    }

    public void updateFavoriteSong() {
        if (isFavorite) {
            favoriteButton.setIcon(loadImage("src/image/heart1.png"));
        } else {
            favoriteButton.setIcon(loadImage("src/image/heart.png"));
        }
    }

    public void updatePlaybackSlider(Song song){
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        JLabel labelEnd =  new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);

        playbackSlider.addChangeListener(e -> {
            int sliderValue = playbackSlider.getValue();

            int currentMilliseconds = (int) (sliderValue / song.getFrameRatePerMilliseconds());
            int currentSeconds = currentMilliseconds / 1000;

            labelBeginning.setText(song.formatTime(currentSeconds));
        });

        timer = new Timer(1000, new ActionListener() {
            int currentMilliseconds = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                currentMilliseconds += 1000;

                playbackSlider.setValue((int) (currentMilliseconds * song.getFrameRatePerMilliseconds()));

                labelBeginning.setText(song.formatTime(currentMilliseconds / 1000));

                if (currentMilliseconds >= song.getMp3File().getLengthInMilliseconds()) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });

        if (!isPlaying) {
            timer.stop();
        } else {
            timer.start();
        }
    }

    public void enablePauseButtonDisablePlayButton(){
        JButton playButton = (JButton) playbackBtns.getComponent(2);
        JButton pauseButton = (JButton) playbackBtns.getComponent(3);

        playButton.setVisible(false);
        playButton.setEnabled(false);

        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }

    public void enablePlayButtonDisablePauseButton(){
        JButton playButton = (JButton) playbackBtns.getComponent(2);
        JButton pauseButton = (JButton) playbackBtns.getComponent(3);

        playButton.setVisible(true);
        playButton.setEnabled(true);

        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private ImageIcon loadImage(String imagePath){
        try{
            BufferedImage image = ImageIO.read(new File(imagePath));
            return new ImageIcon(image);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
