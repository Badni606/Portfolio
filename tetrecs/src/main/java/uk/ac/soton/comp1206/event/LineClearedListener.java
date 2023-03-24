package uk.ac.soton.comp1206.event;

import java.util.Set;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * Used to handle the event of a line being cleared
 */
public interface LineClearedListener {

  /**
   * Handle a line clear event
   * @param cords coordinates of gameBlocks to be cleared
   */
  void fadeOut(Set<GameBlockCoordinate> cords);

}
