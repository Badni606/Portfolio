package uk.ac.soton.comp1206.scene;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * A scene for displaying or create multiplayer lobbies and chat in lobbies
 */
public class LobbyScene extends BaseScene{

  private static final Logger logger = LogManager.getLogger(LobbyScene.class);

  private Communicator communicator;

  ScrollPane scrollPane1;
  ScrollPane chatScrollPane1;
  VBox channelButtons;
  VBox users;
  VBox panel1_5;
  TextFlow messages;

  private boolean toggleChat = false;
  private SimpleBooleanProperty isHost = new SimpleBooleanProperty(false);
  SimpleStringProperty pane23Header = new SimpleStringProperty("Lobbies");

  SimpleStringProperty nickName = new SimpleStringProperty();

  Timer timer = new Timer();
  TimerTask task;

  /**
   * Create a lobbyScene in window
   * @param gameWindow Window to display scene on
   */
  public LobbyScene(GameWindow gameWindow){
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
  }

  @Override
  public void initialise() {
    commsListMode();

    //handle communication msgs
    communicator.addListener((message)->{
      //list of channels received
      if(message.startsWith("CHANNELS")){
        logger.info("Getting Channels");
        Platform.runLater(() -> displayChannelButs(message.substring(9)));
      }//join channel confirmation
      else if(message.startsWith("JOIN")) {
        logger.info("Joining channel");
        Platform.runLater(() -> joinChannel(message.substring(5)));
      }//error received
      else if(message.startsWith("ERROR")) {

      }//receive list of users in channel
      else if(message.startsWith("USERS")){
        logger.info("Getting users in channel");
        Platform.runLater(() -> updateUserList(message.substring(6)));
      }//leave lobby confirmation
      else if(message.startsWith("PARTED")){
        isHost.set(false);
        Platform.runLater(() -> setLobbySearching());
      }//message received
      else if(message.startsWith("MSG")){
        Platform.runLater(() -> recieveMsg(message.substring(4)));
      }//When made host of lobby
      else if(message.equals("HOST")){
        Platform.runLater(() -> isHost.set(true));
      }//Confirmation of a new nick name
      else if(message.startsWith("NICK")){
        Platform.runLater(() -> {
          if(!message.contains(":")) {
            nickName.set(message.substring(5));
          }
        });
      }
      else if(message.startsWith("START")){
        Platform.runLater(() -> startGame());
      }
    });

    //escape key press
    scene.setOnKeyPressed((key)-> {
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

    var stackPane = new StackPane();
    stackPane.setMaxWidth(gameWindow.getWidth());
    stackPane.setMaxHeight(gameWindow.getHeight());
    stackPane.getStyleClass().add("menu-background");
    root.getChildren().add(stackPane);

    nickName.set("Guest");

    //panes
    HBox hBox1 = new HBox();
    stackPane.getChildren().add(hBox1);
    VBox pane23 = new VBox();
    pane23.getStyleClass().add("smallStats");

    //left pane
    VBox pane1 = new VBox();
    pane1.getStyleClass().add("smallStats");

    Label nickNameTXT = new Label("Nick Name");
    nickNameTXT.setAlignment(Pos.CENTER);

    Label currentNickUI = new Label();
    currentNickUI.textProperty().bind(nickName);

    TextField nickNameSet = new TextField();
    nickNameSet.setPromptText("Enter nick name");
    nickNameSet.setOnKeyPressed(key -> {
      if(key.getCode() == KeyCode.ENTER) {
        communicator.send("NICK " + nickNameSet.getText());
      }
    });
    panel1_5 = new VBox();
    panel1_5.getStyleClass().add("smallStats");
    panel1_5.setAlignment(Pos.CENTER);

    Label createLobby = new Label("Create Lobby");
    createLobby.setAlignment(Pos.CENTER);
    TextField createLobbyName = new TextField();
    createLobbyName.setOnKeyPressed(key -> {
      if(key.getCode() == KeyCode.ENTER){
        logger.info("Attempting to create lobby");
        communicator.send("CREATE "+createLobbyName.getText());
        createLobbyName.clear();
      }
    });
    createLobbyName.setPromptText("Enter Lobby name");
    pane1.getChildren().addAll(nickNameTXT,currentNickUI,nickNameSet,panel1_5);
    panel1_5.getChildren().addAll(createLobby,createLobbyName);


    //top right pane
    Label title = new Label();
    title.textProperty().bind(pane23Header);
    title.setAlignment(Pos.CENTER);
    scrollPane1 = new ScrollPane();
    scrollPane1.setPrefViewportHeight(gameWindow.getHeight()*0.75);
    scrollPane1.setPrefViewportWidth(gameWindow.getWidth()*0.75);
    scrollPane1.getStyleClass().add("scroller");
    channelButtons = new VBox();
    scrollPane1.setContent(channelButtons);
    pane23.getChildren().addAll(title, scrollPane1);

    //bottom right pane (chat)
    chatScrollPane1 = new ScrollPane();
    messages = new TextFlow();
    //messages.getStyleClass().add("messages");
    chatScrollPane1.setPrefViewportWidth(gameWindow.getWidth()*0.75);
    chatScrollPane1.setPrefViewportHeight(gameWindow.getHeight()*0.2);
    chatScrollPane1.getStyleClass().add("chatScroller");
    chatScrollPane1.setContent(messages);
    TextField chatmsgToSend = new TextField();
    chatmsgToSend.setPromptText("Enter message");
    chatmsgToSend.setOnKeyPressed((key) -> {
      if(key.getCode() == KeyCode.ENTER && toggleChat){
        sendMsg(chatmsgToSend.getText());
        chatmsgToSend.clear();
      }
    });
    pane23.getChildren().addAll(chatScrollPane1,chatmsgToSend);

    //adding elements to layouts
    scrollPane1.setContent(channelButtons);
    hBox1.getChildren().addAll(pane1,pane23);

    isHost.addListener((observable, oldValue, newValue) -> {
      if (newValue == true) {
        Button startGame = new Button("Start Game");
        startGame.setAlignment(Pos.CENTER);
        startGame.getStyleClass().add("channelItem");
        startGame.setOnAction(event -> {
          communicator.send("START");
        });
        panel1_5.getChildren().add(startGame);
        pane23Header.set(pane23Header.getValue() + "    Host");
      }
    });
  }

  /**
   * Add received message from communicator to TextFlow
   * @param substring Message received
   */
  private void recieveMsg(String substring) {
    Text msgReceived = new Text(substring+"\n");
    msgReceived.getStyleClass().add("messagesText");
    msgReceived.setFill(Color.WHITE);
    messages.getChildren().add(msgReceived);
    chatScrollPane1.setVvalue(1.0f);
  }

  /**
   * Send a message
   * @param text Message to send
   */
  private void sendMsg(String text) {
    communicator.send("MSG "+text);
  }

  /**
   * Set up UI for when in lobby searching mode
   */
  private void setLobbySearching() {
    //reset timer for looping LIST
    task.cancel();

    commsListMode();

    //Set up UI
    pane23Header.set("Lobbies");
    panel1_5.getChildren().clear();
    Label createLobby = new Label("Create Lobby");
    createLobby.setAlignment(Pos.CENTER);
    TextField createLobbyName = new TextField();
    createLobbyName.setPromptText("Enter Lobby name");
    createLobbyName.setOnKeyPressed(key -> {
      if(key.getCode() == KeyCode.ENTER){
        logger.info("Attempting to create lobby");
        communicator.send("CREATE "+createLobbyName.getText());
        createLobbyName.clear();
      }
    });
    panel1_5.getChildren().addAll(createLobby,createLobbyName);
    scrollPane1.setContent(channelButtons);
  }

  /**
   * Set up UI for when in a lobby
   */
  private void setInLobby(){
    //reset timer for looping USERS
    task.cancel();
    task = new TimerTask() {
      @Override
      public void run() {
        communicator.send("USERS");
      }
    };
    timer.scheduleAtFixedRate(task,1,10000);

    //Set up UI
    panel1_5.getChildren().clear();
    Button leaveLobby = new Button("Leave lobby");
    //leave lobby button
    leaveLobby.getStyleClass().add("channelItem");
    leaveLobby.setAlignment(Pos.CENTER);
    leaveLobby.setOnAction(even -> {
      logger.info("Leaving lobby");
      toggleChat = false;
      communicator.send("PART");
    });
    panel1_5.getChildren().add(leaveLobby);
    toggleChat = true;
  }

  /**
   * Refresh list of channels
   * @param names list of channels currently available
   */
  private void displayChannelButs(String names) {
    channelButtons.getChildren().clear();
    String[] lines = names.split("\n");
    for(String line:lines){
      addChannelButton(line);
    }
  }

  /**
   * Add a channel as a button to the list channels
   * @param channel Name of channel
   */
  private void addChannelButton(String channel) {
    Button channelBut = new Button(channel);
    channelBut.getStyleClass().add("channelItem");
    channelBut.setOnAction(event -> {
      communicator.send("JOIN "+channel);
    });
    channelButtons.getChildren().add(channelBut);
  }

  /**
   * Called when user joins a lobby, loads the respective UI and sets up timer for calling USERS
   * @param channel
   */
  private void joinChannel(String channel){
    pane23Header.set("Users in lobby: "+channel);
    users = new VBox();
    users.getStyleClass().add("smallStats");
    scrollPane1.setContent(users);
    setInLobby();
  }

  /**
   * clears and then displays the list of users in the currently joined channel
   * @param list list of user in channel
   */
  private void updateUserList(String list){
    users.getChildren().clear();
    logger.info("update list called");
    String[] lines = list.split("\n");
    for(String line:lines){
      users.getChildren().add(new Label(line));
    }

  }

  /**
   * Assigns task loop to and calls LIST from communicator
   */
  private void commsListMode(){
    task = new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(() -> communicator.send("LIST"));
      }
    };
    timer.scheduleAtFixedRate(task,1,30000);
  }

  /**
   * Starts an online game
   */
  private void startGame(){
    logger.info("Starting online game");
    task.cancel();
    timer.cancel();
    communicator.clearListeners();
    gameWindow.loadScene(new MultiplayerScene(gameWindow, nickName.get()));
  }

}
