package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * A custom UI component used to display scores
 */
public class ScoresList extends VBox {
  SimpleListProperty<Pair<String,Integer>> scoreList = new SimpleListProperty<Pair<String,Integer>>();

  /**
   * Constructor for a score list UI
   * @param listProp List of scores to bind to and display
   */
  public ScoresList(SimpleListProperty listProp){
    scoreList.bind(listProp);
    //when list updated reveal new entry
    scoreList.addListener((ListChangeListener<? super Pair<String, Integer>>) change -> {
      while(change.next()) {
        if(change.wasAdded()) {
          reveal(change);
        }
      }
    });

    this.setMinWidth(300);
  }

  /**
   * animates and displays the scores to the ui
   */
  public void reveal(Change<? extends Pair<String, Integer>> change){
    var scoreEntry = new Label(change.getAddedSubList().get(0).getKey()+" : "+change.getAddedSubList().get(0).getValue());

    scoreEntry.getStyleClass().add("scoreList");
    scoreEntry.setAlignment(Pos.CENTER);
    scoreEntry.setPrefWidth(299);

    //animation
    FadeTransition entryAnim = new FadeTransition(Duration.millis(2000),scoreEntry);
    entryAnim.setFromValue(0);
    entryAnim.setToValue(1);
    entryAnim.play();
    this.getChildren().add(scoreEntry);

  }


}
