package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.CyclicQueue;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.util.Multimedia;

/**
 * The MultiplayerGame class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class MultiplayerGame extends Game{

  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

  private boolean firstPiece = true;
  private boolean secondPiece = true;
  Communicator communicator;

  /**
   * Cyclic queue for storing piece values received from communicator
   */
  CyclicQueue queue = new CyclicQueue(10);
  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
  }

  @Override
  public void start() {
    communicator.addListener(this::commsHandling);

    //load i number of pieces immediately as buffer
    for(int i =0;i<4;i++){
      communicator.send("PIECE");
    }
    super.start();

  }

  /**
   * Handles received messages from communicator
   * @param message Message received
   */
  private void commsHandling(String message) {
    if(message.startsWith("PIECE")){
      Platform.runLater(() -> {
        loadPiece(Integer.parseInt(message.substring(6)));
        if(firstPiece || secondPiece){
          if(!firstPiece){
            nextPiece();
            secondPiece = false;
          }
          else {
            followingPiece = spawnPiece();
            firstPiece = false;
          }
        }
      }
      );
    }

  }

  /**
   * Enqueues a piece value to the queue
   * @param valueOfPiece
   */
  private void loadPiece(int valueOfPiece) {
    queue.enqueue(valueOfPiece);
  }

  @Override
  public GamePiece spawnPiece() {
    logger.info("Piece being dequeued");
    communicator.send("PIECE");
    return GamePiece.createPiece(queue.dequeue());
  }

  @Override
  public void blockClicked(GameBlock gameBlock) {
    //Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    if(grid.canPlayPiece(currentPiece,x,y)){
      grid.playPiece(currentPiece,x,y);
      Multimedia.playSound("place.wav");
      logger.info("Piece added to grid");
      nextPiece();
      logger.info("next piece completed");
      afterPiece();
      task.cancel();
      setTimer();
      //send current board to server
      StringBuilder board = new StringBuilder();
      board.append("BOARD ");
      for(int i = 0;i<5;i++){
        for(int j = 5;j<5;j++){
          board.append(grid.get(i,j)+" ");
        }
      }
      communicator.send(board.toString());
    }
    else{
      Multimedia.playSound("fail.wav");
    }
  }

  @Override
  public void initialiseGame() {

  }

  @Override
  protected void gameLoop() {
    super.gameLoop();
    communicator.send("LIVES "+lives.get());
  }

  @Override
  protected void score(int lines, int blocks) {
    super.score(lines, blocks);
    communicator.send("SCORE "+score.get());
  }

}
