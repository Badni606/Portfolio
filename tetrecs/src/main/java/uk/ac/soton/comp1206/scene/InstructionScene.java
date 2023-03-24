package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * Scene for displaying scores at the end of a challenge
 */
public class InstructionScene extends BaseScene{
  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instruction Scene");
  }

  @Override
  public void initialise() {
    scene.setOnKeyPressed((key) ->{
      if(key.getCode() == KeyCode.ESCAPE){
        gameWindow.startMenu();
      }
    });
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    StackPane stack = new StackPane();
    stack.setMaxWidth(gameWindow.getWidth());
    stack.setMaxHeight(gameWindow.getHeight());
    stack.getStyleClass().add("menu-background");
    root.getChildren().add(stack);

    VBox vbox = new VBox();
    vbox.setAlignment(Pos.CENTER);
    stack.getChildren().add(vbox);

    Text title = new Text("Instructions");
    title.getStyleClass().add("title");
    vbox.getChildren().add(title);

    //loading image
    try {
      Image image = new Image(getClass().getResource("/images/Instructions.png").toExternalForm());
      ImageView imageView = new ImageView(image);
      imageView.setPreserveRatio(true);
      imageView.setFitHeight(gameWindow.getHeight()/2);
      vbox.getChildren().add(imageView);

    } catch (Exception e) {
      logger.error("Image file not found");
    }

    //blocks
    GridPane piecesGrid = new GridPane();
    piecesGrid.setHgap(7);
    piecesGrid.setVgap(7);
    vbox.getChildren().add(piecesGrid);
    piecesGrid.setAlignment(Pos.CENTER);
    int piecesShown = 0;
    while(piecesShown<15) {
      for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 3; j++) {
          PieceBoard threeByThree = new PieceBoard(50, 50);
          piecesGrid.add(threeByThree, i, j);
          threeByThree.displayBlock(GamePiece.createPiece(piecesShown));
          piecesShown++;
        }
      }
    }
  }
}
