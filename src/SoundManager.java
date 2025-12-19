import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

public class SoundManager {
    private static Map<String, Clip> soundCache = new HashMap<>();
    private static boolean isMuted = false;
    private static float volume = 1.0f; // 0.0 (mute) - 1.0 (max)

    
    public static final String DICE = "dice";
    public static final String LADDER = "ladder";
    public static final String WIN = "win";
    public static final String MAGIC = "magic"; 
    public static final String SLOT = "slot";
    public static final String BGM = "bgm"; 

    public static void init() {
        load(DICE, "dice");
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
        if (isMuted) return;
        Clip clip = soundCache.get(key);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            setClipVolume(clip, volume);
            clip.start();
        }
    }

    public static void playLoop(String key) {
        if (isMuted) return;
        Clip clip = soundCache.get(key);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            setClipVolume(clip, volume);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }
        public static void setVolume(float v) {
            volume = Math.max(0f, Math.min(1f, v));
            for (Clip clip : soundCache.values()) {
                setClipVolume(clip, volume);
            }
        }

        public static float getVolume() {
            return volume;
        }

        private static void setClipVolume(Clip clip, float vol) {
            try {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gain.getMinimum();
                float max = gain.getMaximum();
                float dB = min + (max - min) * vol;
                gain.setValue(dB);
            } catch (Exception e) {
                // ignore if not supported
            }
        }
    
    public static void setMuted(boolean muted) {
        isMuted = muted;
        
        Clip bgm = soundCache.get(BGM);
        if (bgm != null) {
            if (muted) bgm.stop();
            else bgm.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
    
    public static boolean isMuted() { return isMuted; }

    public static void stop(String key) {
        Clip clip = soundCache.get(key);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}
