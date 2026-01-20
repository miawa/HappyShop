package ci553.happyshop.utility;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public final class SoundManager {

    private static AudioClip click;
    private static AudioClip success;
    private static AudioClip purchaseSuccess;
    private static AudioClip error;
    private static MediaPlayer music;

    private SoundManager() {}

    static {
        click = load("click.mp3");
        success = load("success.mp3");
        purchaseSuccess = load("purchaseSuccess.mp3");
        error = load("error.mp3");
        loadMusic ("backgroundMusic.mp3");
        
    }

    private static AudioClip load(String path) {
        URL url = SoundManager.class
                .getClassLoader()
                .getResource(path);
        if (url == null) {
            System.err.println("Missing sound: " + path);
            return null;
        }
        return new AudioClip(url.toString());
    }

    private static void loadMusic(String path){
        System.out.println("Debug: Music playing");
        URL url = SoundManager.class
                .getClassLoader()
                .getResource(path);
        if (url == null) {
            System.err.println("Missing music: " + path);
            return;
        }
        Media media = new Media(url.toString());
        music = new MediaPlayer(media);
        music.setCycleCount(MediaPlayer.INDEFINITE); 
        music.setVolume(0.25); 
        music.play(); 

    }

    public static void click() {
        System.out.println("Debug: Click playing");
        if (click != null) click.play();
    }

    public static void success() {
        System.out.println("Debug: Success playing");
        if (success != null) success.play();
    }

    public static void purchase() {
        System.out.println("Debug: Purchase playing");
        if (purchaseSuccess != null) purchaseSuccess.play();
    }

    public static void error() {
        System.out.println("Debug: Error playing");
        if (error != null) error.play();
    }
}
