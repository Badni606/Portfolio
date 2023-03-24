package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.util.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);
    /**
     * Current gamePiece to be played
     */
    protected GamePiece currentPiece;
    /**
     * Following gamePiece to be played
     */
    protected GamePiece followingPiece;
    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    //variables to keep track of
    protected SimpleIntegerProperty score = new SimpleIntegerProperty(0);
    protected SimpleIntegerProperty level = new SimpleIntegerProperty(0);
    protected SimpleIntegerProperty lives = new SimpleIntegerProperty(3);
    protected SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    private NextPieceListener nextPieceListener;

    private LineClearedListener lineClearedListener;

    private GameLoopListener gameLoopListener;

    //timer and taske
    Timer timer = new Timer("Timer");
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Platform.runLater(() -> gameLoop());
        }
    };

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        timer.schedule(task,gameTimerDelay());
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        followingPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if(grid.canPlayPiece(currentPiece,x,y)){
            grid.playPiece(currentPiece,x,y);
            Multimedia.playSound("place.wav");
            nextPiece();
            afterPiece();
            setTimer();
        }
        else{
            Multimedia.playSound("fail.wav");
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    public IntegerProperty getScoreProp(){
        return score;
    }

    public IntegerProperty getLevelProp(){
        return level;
    }

    public IntegerProperty getLivesProp(){
        return lives;
    }

    public IntegerProperty getMultiplierProp(){
        return multiplier;
    }
    /**
     * Generates a random piece from the 15 available
     * @return  Random GamePiece
     */
    public GamePiece spawnPiece(){
        GamePiece newPiece = GamePiece.createPiece(new Random().nextInt(15));
        return newPiece;
    }

    /**
     * Replaces the current piece with a new one
     */
    public void nextPiece(){
        currentPiece = new GamePiece(followingPiece.toString(),followingPiece.getBlocks(),followingPiece.getValue());
        followingPiece = spawnPiece();
        logger.info("New piece generated: "+followingPiece.toString());
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * Clears lines in the grid and calls score() to update score
     */
    public void afterPiece(){
        var blocksToRmv = new HashSet<GameBlockCoordinate>();
        int linesRemoved = 0;
        //check for vertical lines
        for(int x=0;x<rows;x++){
            int filled = 0;
            for(int y=0;y<cols;y++){
                if(grid.get(x,y)>0){
                    filled++;
                }
            }
            //add blocks to remove
            if(filled==cols){
                linesRemoved++;
                for(int y=0;y<cols;y++){
                    blocksToRmv.add(new GameBlockCoordinate(x,y));
                }
            }
        }
        //check for horizontal lines
        for(int y=0;y<rows;y++){
            int filled = 0;
            for(int x=0;x<cols;x++){
                if(grid.get(x,y)>0){
                    filled++;
                }
            }
            //add blocks to remove
            if(filled==cols){
                linesRemoved++;
                for(int x=0;x<cols;x++){
                    blocksToRmv.add(new GameBlockCoordinate(x,y));
                }
            }
        }
        //remove lines, set values on grid to 0, update score
        if(linesRemoved>0) {
            score(linesRemoved,blocksToRmv.size());
            multiplier.set(multiplier.get()+1);
            for (GameBlockCoordinate cords: blocksToRmv) {
                grid.set(cords.getX(), cords.getY(), 0);
                logger.info("Lines removed");
            }
            if(lineClearedListener != null) {
                lineClearedListener.fadeOut(blocksToRmv);
            }
            Multimedia.playSound("clear.wav");
        }
        else{
            multiplier.set(1);
        }

    }

    /**
     * Calculate and update score
     * @param lines Number of lines that were cleared
     * @param blocks Number of grid blocks cleared
     */
    protected void score(int lines, int blocks){
        int scoreAdd = lines*blocks*10*multiplier.get();
        score.set(score.get() + scoreAdd);
        level.set((score.get() / 1000));

        logger.info("score added: "+scoreAdd+" new score is"+score.get());
    }

    public void setNextPieceListener(NextPieceListener nextPieceListener){
        this.nextPieceListener = nextPieceListener;
    }

    public void setLineClearedListener(LineClearedListener lineClearedListener){
        this.lineClearedListener = lineClearedListener;
    }

    public void setGameLoopListener(GameLoopListener gameLoopListener){
        this.gameLoopListener = gameLoopListener;
    }

    /**
     * Rotates the current piece a specified number of times and alerts the next piece listener to update the ui
     */
    public void rotateCurrentPiece(int rotations){
        logger.info("Current Piece rotated");
        Multimedia.playSound("rotate.wav");
        currentPiece.rotate(rotations);
        //display new rotated piece
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * Swaps the current and following pieces
     */
    public void swapCurrentPiece() {
        var temp = currentPiece;
        currentPiece = followingPiece;
        followingPiece = temp;
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * Calculates the new game loop duration
     * @return Duration of new game loop in milliseconds
     */
    public int gameTimerDelay(){
        if(level.get() >= 19){
            return 2500;
        }
        else{
            return (12000 - (500 * level.get()));
        }
    }

    /**
     * Called when time limit is reached, reduces lives by 1, resets multiplier to 1 and calls the for the next piece to be loaded
     */
    protected void gameLoop(){
        logger.info("Time limit reached");
        lives.set(lives.get()-1);
        logger.info("1 life lost");
        if(lives.get() > 0) {
            nextPiece();
        }
        multiplier.set(1);
        setTimer();
    }

    /**
     * Cancel current game loop and reset it with new game loop duration
     */
    protected void setTimer(){
        task.cancel();
        task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> gameLoop());
            }
        };
        int time = gameTimerDelay();
        logger.info("Timer reset, new duration: {}ms",time);
        //listener alerted
        gameLoopListener.gameLoop(time);
        if(lives.get() != 0){
            timer.schedule(task,time);
        }

    }

    /**
     * Stops the current task and cancels the timer
     */
    public void stopTimer(){
        task.cancel();
        timer.cancel();
    }

}
