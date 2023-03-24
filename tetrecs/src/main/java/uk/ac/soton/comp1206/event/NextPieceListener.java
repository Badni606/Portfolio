package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Listens and handles the game generating a new piece after placing another
 */
public interface NextPieceListener {

  /**
   * Handles a new piece generated event
   * @param gamePiece
   */
  void nextPiece(GamePiece gamePiece,GamePiece followingPiece);


}
