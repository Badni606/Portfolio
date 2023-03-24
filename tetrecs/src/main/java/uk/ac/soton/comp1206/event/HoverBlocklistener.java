package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * A listener for when a game block is hovered over
 */
public interface HoverBlocklistener {

  /**
   * Handles the event of a game block being hovered over
   * @param gameBlock
   */
  public void hoverEnter(GameBlock gameBlock);

}
