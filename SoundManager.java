import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static Map<String, Clip> soundCache = new HashMap<>();

    // Sound Keys
    public static final String DICE = "dice";
    public static final String STEP = "step";
    public static final String SNAKE = "snake";
    public static final String LADDER = "ladder";
    public static final String WIN = "win";
    public static final String MAGIC = "magic"; // For Dijkstra/Prime
    public static final String SLOT = "slot";
    public static final String BGM = "bgm"; // Background Music

    /**
     * Initialize and load sounds.
     * Place .wav files in the 'sounds' folder with these names:
     * - dice.wav
     * - step.wav
     * - snake.wav
     * - ladder.wav
     * - win.wav
     * - magic.wav
     * - slot.wav
     * - bgm.wav
     */
    public static void init() {
        load(DICE, "dice");
        load(STEP, "step");
        load(SNAKE, "snake");
        load(LADDER, "ladder");
        load(WIN, "win");
        load(MAGIC, "magic");
        load(SLOT, "slot");
        load(BGM, "bgm");
    }

    private static void load(String key, String baseName) {
        String wavPath = "sounds/" + baseName + ".wav";
        String mp3Path = "sounds/" + baseName + ".mp3";
        
        File fWav = new File(wavPath);
        File fMp3 = new File(mp3Path);

        if (fWav.exists()) {
            loadClip(key, fWav);
        } else if (fMp3.exists()) {
            System.out.println("Found " + mp3Path + ". Note: Java Swing natively supports .wav. If this fails, please convert to .wav.");
            loadClip(key, fMp3);
        } else {
            System.out.println("Sound file missing: " + wavPath + " (or .mp3)");
        }
    }

    private static void loadClip(String key, File f) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            soundCache.put(key, clip);
            System.out.println("Loaded sound: " + f.getName());
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Format not supported for: " + f.getName() + ". Please convert to .wav (PCM).");
        } catch (Exception e) {
            System.err.println("Error loading sound " + f.getName() + ": " + e.getMessage());
        }
    }

    public static void play(String key) {
        Clip clip = soundCache.get(key);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public static void playLoop(String key) {
        Clip clip = soundCache.get(key);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }

    public static void stop(String key) {
        Clip clip = soundCache.get(key);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}
