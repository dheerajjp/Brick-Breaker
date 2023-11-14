package brickBreaker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame obj = new JFrame();
            Gameplay gamePlay = new Gameplay();
            obj.setBounds(10, 10, 700, 600);
            obj.setTitle("Breakout Ball");
            obj.setResizable(false);
            obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            obj.add(gamePlay);

            // Add a key listener to the JFrame to capture keyboard events
            obj.addKeyListener(gamePlay);

            // Set focusable to true to ensure the JFrame can receive keyboard input
            obj.setFocusable(true);

            obj.setVisible(true);

            // Play background music
            playBackgroundMusic();
        });
    }

    // Method to play background music
    private static void playBackgroundMusic() {
        try {
            File audioFile = new File("Sounds/mixkit-fun-and-games-6.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            Clip backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);

            Timer timer = new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (!backgroundMusic.isRunning()) {
                        backgroundMusic.close();
                        System.exit(0);
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
}
