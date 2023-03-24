package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoresList;
import uk.ac.soton.comp1206.util.Multimedia;

/**
 * Scene which displays the high scores at the end of a game, also prompts the player to add  new score if they have accomplished a new highscore
 */
public class ScoreScene extends BaseScene{

  private static final Logger logger = LogManager.getLogger(ScoreScene.class);

  /**
   * Score achieved last game
   */
  protected int lastScore;

  //local scores
  private ObservableList<Pair<String,Integer>> observableList = FXCollections.observableArrayList();
  protected SimpleListProperty<Pair<String,Integer>> localScores = new SimpleListProperty<Pair<String,Integer>>(observableList);
  private ArrayList<Pair<String,Integer>> tempLocalScores = new ArrayList<>();
  //remote scores
  private ObservableList<Pair<String,Integer>> obseravableList2 = FXCollections.observableArrayList();
  protected SimpleListProperty<Pair<String,Integer>> remoteScores = new SimpleListProperty<Pair<String, Integer>>(obseravableList2);

  protected Communicator communicator;

  /**
   * Username of player
   */
  String username;


  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoreScene(GameWindow gameWindow, int gameScore) {
    super(gameWindow);
    lastScore = gameScore;
    communicator = gameWindow.getCommunicator();
    logger.info("Creating Score scene");
  }

  @Override
  public void initialise() {
    //on exit of scene
    scene.setOnKeyPressed((key) ->{
      if(key.getCode() == KeyCode.ESCAPE){
        communicator.clearListeners();
        gameWindow.startMenu();
      }
    });
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

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

    //prepare scores
    loadScores();

    ScoresList scoreTable = new ScoresList(localScores);
    ScoresList remoteScoreTable = new ScoresList(remoteScores);

    BorderPane main = new BorderPane();
    Label title = new Label("High Scores");
    main.setTop(title);
    main.getStyleClass().add("mediumStats");
    BorderPane.setAlignment(title,Pos.CENTER);

    stack.getChildren().add(main);

    logger.info("Last game's score is {}",lastScore);
    //if new high score enter user name and update local scores save and compare with online
    if(lastScore > tempLocalScores.get(9).getValue()){
      logger.info("New highscore detected");
      //new highscore detected
      Label promt = new Label("New Highscore!");
      promt.setAlignment(Pos.CENTER);
      TextField name = new TextField("Enter name");
      VBox promts = new VBox(promt,name);
      promts.setAlignment(Pos.CENTER);
      promts.getStyleClass().add("smallStats");
      main.setCenter(promts);

      //on press of enter, reveal new score list and update file
      name.setOnKeyPressed((event) -> {
        if(event .getCode() == KeyCode.ENTER) {
          username = name.getText();
          sortScores(lastScore,username);
          main.getChildren().remove(promts);
          HBox tables = new HBox(scoreTable,remoteScoreTable);
          tables.setAlignment(Pos.CENTER);
          HBox h2 = new HBox(new Label("Local Score"),new Label("Online Scores"));
          h2.setSpacing(125);
          h2.setAlignment(Pos.CENTER);
          h2.getStyleClass().add("smallStats");
          Label lastGameScore = new Label("Score: "+lastScore);
          lastGameScore.setAlignment(Pos.CENTER);
          VBox headers = new VBox(lastGameScore,h2,tables);
          headers.getStyleClass().add("smallStats");
          headers.setAlignment(Pos.CENTER);
          main.setCenter(headers);
          loadOnlineScores();
          writeScores();
        }
      });

    }//if no new high score, just display high scores
    else{
      updateLists(tempLocalScores,localScores);
      HBox tables = new HBox(scoreTable,remoteScoreTable);
      tables.setAlignment(Pos.CENTER);
      HBox h2 = new HBox(new Label("Local Score"),new Label("Online Scores"));
      h2.setSpacing(125);
      h2.setAlignment(Pos.CENTER);
      h2.getStyleClass().add("smallStats");
      Label lastGameScore = new Label("Score: "+lastScore);
      lastGameScore.setAlignment(Pos.CENTER);
      VBox headers = new VBox(lastGameScore,h2,tables);
      headers.getStyleClass().add("smallStats");
      headers.setAlignment(Pos.CENTER);
      main.setCenter(headers);
      loadOnlineScores();
    }
  }

  /**
   * loads local scores from scores.txt file
   */
  protected void loadScores(){
    logger.info("loading scores");
    File scoreFile = new File("./scores.txt");
    try {
      //if there isn't a scores file then do not do anything
      if(scoreFile.exists()) {
        BufferedReader reader = new BufferedReader(new FileReader(scoreFile));
        String line;
        while ((line = reader.readLine()) != null) {
          String[] split = line.split(":");
          tempLocalScores.add(new Pair<>(split[0], Integer.parseInt(split[1])));
        }
        reader.close();
      }
      else{
        writeScores();
      }
    }
    catch (Exception e){
      logger.error("error loading scores from file");
    }
  }

  /**
   * writes local scores to the score.txt file
   */
  private void writeScores(){
    logger.info("writing scores to file");
    File scoreFile = new File("./scores.txt");
    try {
      //if no scores file exists, make a new one with default values
      if(!scoreFile.exists()){
        logger.info("no scores.txt file found so new one will be made with default entries");
        scoreFile.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(scoreFile));
        for(int i=10;i>0;i--){
          writer.write("default:"+i*1000);
          writer.newLine();
          tempLocalScores.add(new Pair<>("default",i*1000));
        }
        writer.close();
      }//write score entries in list
      else {
        BufferedWriter writer = new BufferedWriter(new FileWriter(scoreFile));
        for (Pair<String, Integer> entry : localScores) {
          writer.write(entry.getKey()+":"+entry.getValue());
          writer.newLine();
        }
        writer.close();
      }
    }
    catch(Exception e){
      logger.error("Error writing scores to file");
    }
  }

  /**
   * Inserts a new score to the local score list and sorts them in descending order
   * @param gameScore new score to add
   * @param name name of the player which made the new score to add
   */
  private void sortScores(int gameScore,String name){
    int index = 8;
    while(true) {
      if(gameScore < tempLocalScores.get(index).getValue()){
        for(int i = 9;i>index+1;i--){
          tempLocalScores.set(i,tempLocalScores.get(i-1));
        }
        tempLocalScores.set(index+1,new Pair<>(name,gameScore));
        break;
      }
      index--;
    }
    updateLists(tempLocalScores,localScores);
  }

  /**
   * send request for remote high scores
   */
  protected void loadOnlineScores(){
    communicator.send("HISCORES");
  }

  /**
   * handles messages received from communicator in Score scene
   * @param message
   */
  protected void handleCommunication(String message){
    //handle receiving high scores
    if(message.startsWith("HISCORES")){
      logger.info("list of remote score received");
      Platform.runLater(() -> receiveMessage(message.substring(9)));

    }//confirmation of new high score being received
    else if (message.startsWith("NEWSCORE")) {
      logger.info("New high score sent and confirmation received");
      communicator.send("HISCORES");
    }
  }

  /**
   * Whether the online high scores have been compared to last game's score
   */
  boolean checked = false;
  /**
   * Adds online scores to the remote scores list as well as updating the online scores with a new high score if needed
   * @param message
   */
  void receiveMessage(String message){
    String[] lines = message.split("\n");
    for(String entries:lines){
      String[] split = entries.split(":");
      //check for new online high score
      if(!checked){
        if(Integer.parseInt(split[1]) < lastScore) {
          checked = true;
          //upload new highscore to server if one is present
          communicator.send("HISCORE " + username + ":" + lastScore);
          break;
        }
      }
      else {//update remote scores list
        Platform.runLater(()->remoteScores.add(new Pair<>(split[0], Integer.parseInt(split[1]))));
      }
    }
    if(!checked) {
      checked = true;
      logger.info("looping receive messages");
      receiveMessage(message);
    }
  }

    /**
   * Copy's the contents of one list into another
   * @param temp content to be copied
   * @param property target list to copy into
   */
  void updateLists(ArrayList<Pair<String,Integer>> temp,SimpleListProperty<Pair<String,Integer>> property){
    for(Pair<String,Integer> entry:temp){
      property.add(entry);
    }
  }

}
