package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.util.Multimedia;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected Game game;

    private PieceBoard currentPiece;
    private PieceBoard followingPiece;

    //keyboard controls tracker
    private GameBoard board;
    private int x;
    private int y;

    //time bar
    Rectangle timeBar;
    SimpleIntegerProperty highScoreProp = new SimpleIntegerProperty();
    protected VBox rightPanel;
    protected VBox bottomVBox;


    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        //listener attachment
        board.setOnBlockClick(this::blockClicked);
        board.setOnRightClick(this::rightClicked);
        game.setNextPieceListener(this::nextPiece);
        board.setHoverBlocklistener(this::hoverEnter);
        game.setLineClearedListener(this::fadeOut);
        game.setGameLoopListener(this::gameLoop);


        //display variables
        Label scoreT = new Label("Score:");
        Label score = new Label();
        score.textProperty().bind(game.getScoreProp().asString());
        HBox scorePack = new HBox(scoreT,score);
        scorePack.getStyleClass().add("mediumStats");

        getHighScore();
        Label highScoreT = new Label("High Score:");
        Label highScore = new Label();
        highScore.textProperty().bind(highScoreProp.asString());
        HBox highScorePack = new HBox(highScoreT,highScore);
        highScorePack.getStyleClass().add("smallStats");

        Label levelT = new Label("Level:");
        Label level = new Label();
        level.textProperty().bind(game.getLevelProp().asString());
        HBox levelPack = new HBox(levelT,level);
        levelPack.getStyleClass().add("smallStats");

        Label multiT = new Label("Multiplier:");
        Label multi = new Label();
        multi.textProperty().bind(game.getMultiplierProp().asString());
        HBox multiPack = new HBox(multiT,multi);
        multiPack.getStyleClass().add("mediumStats");
        multiPack.setAlignment(Pos.CENTER);

        Label livesT = new Label("Lives:");
        Label lives = new Label();
        lives.textProperty().bind(game.getLivesProp().asString());
        HBox livesPack = new HBox(livesT,lives);
        livesPack.getStyleClass().add("smallStats");

        //top of border panel
        var topBar = new HBox(livesPack,scorePack,highScorePack);
        livesPack.setAlignment(Pos.CENTER_LEFT);
        scorePack.setAlignment(Pos.CENTER);
        highScorePack.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(livesPack, Priority.ALWAYS);
        HBox.setHgrow(highScorePack, Priority.ALWAYS);
        mainPane.setTop(topBar);

        bottomVBox = new VBox(multiPack);
        bottomVBox.setAlignment(Pos.CENTER);
        bottomVBox.getStyleClass().add("inGameChat");
        //bottom of border panel
        mainPane.setBottom(bottomVBox);

        //background music
        Multimedia.playMusic("game.wav");

        //right pane, piece boards and level
        currentPiece = new PieceBoard(gameWindow.getHeight()/4,gameWindow.getHeight()/4);
        currentPiece.setAlignment(Pos.CENTER);
        currentPiece.setOnRightClick(this::rightClicked);
        followingPiece = new PieceBoard(gameWindow.getHeight()/4,gameWindow.getHeight()/4);
        followingPiece.setAlignment(Pos.CENTER);

        rightPanel = new VBox(new Label("Upcoming Piece"),followingPiece,new Label("Current Piece"),currentPiece,levelPack);
        rightPanel.getStyleClass().add("smallStats");
        mainPane.setRight(rightPanel);

        //left pane, time bar
        timeBar = new Rectangle(20,gameWindow.getHeight()/2, Color.GREEN);
        mainPane.setLeft(timeBar);
        BorderPane.setAlignment(timeBar,Pos.CENTER_LEFT);
        gameLoop(12000);

        //initialise x and y for control tracking
        x = 2;
        y = 2;
        board.getBlock(x,y).paintIndicator();
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
        //update indicator
        moveIndicator(gameBlock.getX(), gameBlock.getY());
        x = gameBlock.getX();
        y=gameBlock.getY();
        //update highscore if current score has surpassed it
        if(highScoreProp.get() < game.getScoreProp().get()){
            highScoreProp.set(game.getScoreProp().getValue());
        }
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        scene.setOnKeyPressed((key) -> keyPressed(key));
    }

    /**
     * Handles next piece event
     * @param gamePiece New current gamePiece in game
     * @param followingPiece New following gamePiece in game
     */
    private void nextPiece(GamePiece gamePiece,GamePiece followingPiece){
        currentPiece.displayBlock(gamePiece);
        currentPiece.getBlock(1,1).paintCircleIndicator();
        this.followingPiece.displayBlock(followingPiece);
    }

    private void rightClicked(){
        game.rotateCurrentPiece(1);
    }

    /**
     * Method that checks the key pressed and executes methods respectively
     * @param keyEvent
     */
    protected void keyPressed(KeyEvent keyEvent){
        logger.info("Key {} pressed",keyEvent.getCode().getName());
        //place current piece
        if(keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.X){
            game.blockClicked(board.getBlock(x,y));
        }//move up
        else if(keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W){
            if(y>0){
                moveIndicator(x,y-1);
                y--;
            }
        }//move down
        else if(keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S){
            if(y<4){
                moveIndicator(x,y+1);
                y++;
            }
        }//move left
        else if(keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A){
            if(x>0){
                moveIndicator(x-1,y);
                x--;
            }
        }//move right
        else if(keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D){
            if(x<4){
                moveIndicator(x+1,y);
                x++;
            }
        }//swap current and upcoming pieces
        else if(keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R){
            game.swapCurrentPiece();
        }//back to menu
        else if(keyEvent.getCode() == KeyCode.ESCAPE) {
            game.stopTimer();
            gameWindow.startMenu();
        }//rotate current piece left
        else if(keyEvent.getCode() == KeyCode.Q || keyEvent.getCode() == KeyCode.Z || keyEvent.getCode() == KeyCode.BRACELEFT){
            game.rotateCurrentPiece(3);
        }//rotate current piece right
        else if(keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C || keyEvent.getCode() == KeyCode.BRACERIGHT){
            game.rotateCurrentPiece(1);
        }
    }

    /**
     * highlights a specified game block and unhighlights the previously highlighted block
     * @param newX
     * @param newY
     */
    private void moveIndicator(int newX, int newY){
        try {
            board.getBlock(newX, newY).paintIndicator();
            board.getBlock(x, y).paint();
        }
        catch(ArrayIndexOutOfBoundsException e){
            logger.error("Trying to go out of bounds of game board");
        }
    }

    /**
     * Handle mouse cursor hovering on game block
     * @param gameBlock Game block being hovered on
     */
    public void hoverEnter(GameBlock gameBlock){
        moveIndicator(gameBlock.getX(),gameBlock.getY());
        x=gameBlock.getX();
        y=gameBlock.getY();
    }

    /**
     * Removes list of gameBlocks at specified coordinates with a fade out animation
     * @param cords List of coordinates where block to be removed are on the grid
     */
     private void fadeOut(Set<GameBlockCoordinate> cords){
        board.fadeOut(cords);
     }

    /**
     * Resets time and changes scene if game is over
     * @param milliseconds
     */
     protected void gameLoop(int milliseconds){
         if(game.getLivesProp().get() == 0){
             gameWindow.startScores(game.getScoreProp().get());
         }
         else {
             logger.info("UI timebar reset");
             timeBar.setFill(Color.GREEN);
             timeBar.setHeight(gameWindow.getHeight() / 2);
             var length = new KeyValue(timeBar.heightProperty(), 0);
             var frame = new KeyFrame(Duration.millis(milliseconds), length,new KeyValue(timeBar.fillProperty(),Color.RED));

             Timeline timeline = new Timeline(frame);
             timeline.play();
         }
     }

    /**
     * gets high score from scores.txt to display on the challeneg scene
     */
    private void getHighScore(){
        File scores = new File("./scores.txt");
        if(scores.exists()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(scores));
                String entry = reader.readLine();
                String[] split = entry.split(":");
                highScoreProp.set(Integer.parseInt(split[1]));
                reader.close();
            }
            catch (Exception e){
                logger.info("Error loading high score from file");
                highScoreProp.set(10000);
            }
        }
        else{
            logger.info("No scores.txt file made yet so default highscore shall be implemented");
            highScoreProp.set(10000);
        }

     }
}