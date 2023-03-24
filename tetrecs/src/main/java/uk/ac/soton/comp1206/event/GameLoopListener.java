package uk.ac.soton.comp1206.event;

/**
 * Listener for game loop
 */
public interface GameLoopListener {

  /**
   * Handle game loop event
   * @param milliseconds Duration of next game loop
   */
  void gameLoop(int milliseconds);
}
