import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class AudioPlayer {
    /**
     * Play audio
     * @param filePath audio track path
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public static void playSound(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            System.err.println("[Error] Unsupported audio format: " + filePath);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[Error] Failed to load audio file: " + filePath);
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("[Error] Audio line unavailable: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
