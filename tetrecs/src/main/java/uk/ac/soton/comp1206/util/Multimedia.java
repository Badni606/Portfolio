package uk.ac.soton.comp1206.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for playing sound media
 */
public class Multimedia {
  private static final Logger logger = LogManager.getLogger(Multimedia.class);
  private static MediaPlayer player1;
  private static MediaPlayer player2;

  /**
   * Play a sound
   * @param file Name of sound file
   */
  public static void playSound(String file){
    try {
      player1 = new MediaPlayer(new Media(Multimedia.class.getResource("/sounds/" + file).toExternalForm()));
      player1.play();
      logger.info("playing sound "+file);
    }
    catch(Exception e){
      logger.error("Sound file "+file+" not found");
    }
  }

  /**
   * Play music indefinitely
   * @param file Name of music file to play
   */
  public static void playMusic(String file){
    try {
      if(player2 != null){
        player2.stop();
      }
      player2 = new MediaPlayer(new Media(Multimedia.class.getResource("/music/" + file).toExternalForm()));
      logger.info("file found");
      player2.setCycleCount(MediaPlayer.INDEFINITE);
      player2.play();
      logger.info("playing music "+file);
    }
    catch(Exception e){
      logger.error("Sound file "+file+" not found. "+e.toString());

    }
  }


}
