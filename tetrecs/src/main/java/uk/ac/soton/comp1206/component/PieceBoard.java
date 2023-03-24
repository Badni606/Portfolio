package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseEvent;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * UI component which displays gamePieces in a 3x3 grid
 */
public class PieceBoard extends GameBoard{

  /**
   * Creates a PieceBoard of size specified by dimension
   * @param width width of grid
   * @param height height of grid
   */
  public PieceBoard(double width, double height) {
    super(3, 3, width, height);
  }

  /**
   * Displayes a gamePiece on the 3x3 board, clears previous gamePiece
   * @param gamePiece Piece to be displayed
   */
  public void displayBlock(GamePiece gamePiece){
    for(int x=0;x<3;x++){
      for(int y = 0;y<3;y++){
        grid.set(x,y,0);
      }
    }
    grid.playPiece(gamePiece,1,1);
  }

  /**
   * Notifies right click listener of a right click event
   * @param event mouse event
   * @param block block clicked on
   */
  protected void blockClicked(MouseEvent event, GameBlock block){
    if(rightClickedListener != null){
      rightClickedListener.rightClicked();
    }
  }
}
