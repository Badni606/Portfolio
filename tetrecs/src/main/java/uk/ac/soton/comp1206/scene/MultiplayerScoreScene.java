package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoresList;
import uk.ac.soton.comp1206.util.Multimedia;

/**
 * Scene in charge of displaying scores after an online game
 */
public class MultiplayerScoreScene extends ScoreScene{

  private static final Logger logger = LogManager.getLogger(MultiplayerScoreScene.class);
  private String playerData;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   * @param gameScore
   */
  public MultiplayerScoreScene(GameWindow gameWindow, int gameScore,String endGameScores,String playerName) {
    super(gameWindow, gameScore);
    playerData = endGameScores;
    username = playerName;
  }



  @Override
  public void build() {
    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
    StackPane stack = new StackPane();
    stack.setMaxWidth(gameWindow.getWidth());
    stack.setMaxHeight(gameWindow.getHeight());
    stack.getStyleClass().add("menu-background");
    root.getChildren().add(stack);

    //change music
    Multimedia.playMusic("end.wav");

    //set up communicator handler
    communicator.addListener(this::handleCommunication);

    //score lists
    ScoresList matchScoreTable = new ScoresList(localScores);
    //matchScoreTable.setAlignment(Pos.CENTER);
    ScoresList remoteScoreTable = new ScoresList(remoteScores);
    //remoteScoreTable.setAlignment(Pos.CENTER);

    //UI top bar
    BorderPane main = new BorderPane();
    Label title = new Label("Match Scores");
    main.setTop(title);
    main.getStyleClass().add("mediumStats");
    BorderPane.setAlignment(title, Pos.CENTER);
    stack.getChildren().add(main);

    //middle pane
    HBox tables = new HBox(matchScoreTable,remoteScoreTable);
    tables.setAlignment(Pos.CENTER);
    HBox h2 = new HBox(new Label("Match Scores"),new Label("Online Scores"));
    h2.setSpacing(125);
    h2.setAlignment(Pos.CENTER);
    h2.getStyleClass().add("smallStats");
    VBox headers = new VBox(h2,tables);
    headers.getStyleClass().add("smallStats");
    headers.setAlignment(Pos.CENTER);

    main.setCenter(headers);
    loadScores();
    loadOnlineScores();

  }

  @Override
  protected void loadScores() {
    String[] lines = playerData.split("\n");
    for(String line:lines){
      String[] split = line.split(":");
      localScores.add(new Pair<>(split[0],Integer.parseInt(split[1])));
    }
  }
}

