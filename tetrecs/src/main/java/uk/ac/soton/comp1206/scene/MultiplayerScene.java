package uk.ac.soton.comp1206.scene;

import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.CyclicQueue;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerScene extends ChallengeScene{
  private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

  private Communicator communicator;

  private SimpleStringProperty lastMsg = new SimpleStringProperty("Press T to send message");

  VBox leaderBoardVBox;

  Timer timer = new Timer();
  TimerTask task = new TimerTask() {
    @Override
    public void run() {
      communicator.send("SCORES");
    }
  };

  /**
   * State of whether player is typing or playing
   */
  private boolean typing;

  /**
   * List of players and their statuses
   */
  private String playerData;
  private String playerName;

  /**
   * Create a new Multiplayer game scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow,String playerName) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
    this.playerName = playerName;
  }

  @Override
  public void setupGame() {
    logger.info("Setting up multiplayer game");
    game = new MultiplayerGame(5,5,communicator);
  }

  @Override
  public void build() {
    super.build();

    //right panel leaderboard
    leaderBoardVBox = new VBox();
    leaderBoardVBox.getStyleClass().add("smallLeaderBoard");
    rightPanel.getChildren().addAll(new Label("leaderboard"),leaderBoardVBox);

    //bottom of panel
    Label msgLabel = new Label();
    msgLabel.textProperty().bind(lastMsg);
    msgLabel.getStyleClass().add("inGameChat");

    bottomVBox.getChildren().add(msgLabel);
  }

  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    game.start();
    timer.scheduleAtFixedRate(task,1,5000);
    scene.setOnKeyPressed((key) -> keyPressed(key));
    communicator.addListener(this::communicatorHandler);
  }

  /**
   * Handles the received messages from communicator
   * @param message
   */
  private void communicatorHandler(String message) {
    if(message.startsWith("SCORES")){
      Platform.runLater(() -> updateScores(message.substring(7)));
    }
    else if(message.startsWith("MSG")){
      Platform.runLater(() -> lastMsg.set(message.substring(4)));
    }
  }

  /**
   * Updates the VBox to display the current players and their status
   * @param substring
   */
  private void updateScores(String substring) {
    leaderBoardVBox.getChildren().clear();
    playerData = substring;
    String[] lines  = substring.split("\n");
    //add each player's stats
    for(String line:lines){
      String[] split = line.split(":");
      //if player is dead or left the game
      if(split[2].equals("DEAD")){
        leaderBoardVBox.getChildren().add(new Label(split[0]+":"+split[2]));
      }//if player is still playing
      else{
        leaderBoardVBox.getChildren().add(new Label(split[0]+":"+split[1]));
      }
    }
  }

  @Override
  protected void keyPressed(KeyEvent keyEvent) {
    if(typing){
      logger.info("Controls disabled");
      return;
    }
    super.keyPressed(keyEvent);
    if(keyEvent.getCode() == KeyCode.T){
      typing = true;
      TextField msgBar = new TextField();
      msgBar.setPromptText("Enter message");
      msgBar.setOnKeyPressed(key -> {
        if(key.getCode() == KeyCode.ENTER){
          communicator.send("MSG "+msgBar.getText());
          bottomVBox.getChildren().remove(msgBar);
          typing = false;
        }
      });
      bottomVBox.getChildren().add(msgBar);
      msgBar.requestFocus();
    }
    else if(keyEvent.getCode() == KeyCode.ESCAPE){
      communicator.clearListeners();
      communicator.send("DIE");
    }
  }

  @Override
  protected void gameLoop(int milliseconds) {
    if(game.getLivesProp().get() == 0){
      task.cancel();
      timer.cancel();
      communicator.send("DIE");
      gameWindow.loadScene(new MultiplayerScoreScene(gameWindow,0,playerData,playerName));
    }
    else {
      logger.info("UI timebar reset");
      timeBar.setHeight(gameWindow.getHeight() / 2);
      var length = new KeyValue(timeBar.heightProperty(), 0);
      var frame = new KeyFrame(Duration.millis(milliseconds), length);
      Timeline timeline = new Timeline(frame);
      timeline.play();
    }
  }
}
