package uk.ac.soton.comp1206.scene;

import java.util.stream.Stream;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.util.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        BorderPane mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //title
        var buttons = new VBox();

        ImageView title = new ImageView(new Image(getClass().getResource("/images/TetrECS.png").toExternalForm()));
        title.setPreserveRatio(true);
        title.setFitHeight(80);
        BorderPane.setAlignment(title,Pos.CENTER);
        buttons.getChildren().add(title);
        RotateTransition rotateAnimation = new RotateTransition(Duration.millis(3000),title);
        rotateAnimation.setFromAngle(-20);
        rotateAnimation.setToAngle(20);
        rotateAnimation.setCycleCount(Timeline.INDEFINITE);
        rotateAnimation.setAutoReverse(true);
        rotateAnimation.setRate(0.5);
        rotateAnimation.play();


        //For now, let us just add a button that starts the game. I'm sure you'll do something way better.

        var playButton = new Button("Play");
        buttons.getChildren().add(playButton);

        //Bind the button action to the startGame method in the menu
        playButton.setOnAction(this::startGame);

        //play menu music
        Multimedia.playMusic("menu.mp3");

        //instruction scene button
        Button instructionScene = new Button("How To Play");
        instructionScene.setOnAction(event -> gameWindow.loadScene(new InstructionScene(gameWindow)));
        //multiplayer button
        Button multiplayer = new Button("Multiplayer");
        multiplayer.setOnAction( event -> gameWindow.loadScene(new LobbyScene(gameWindow)));
        //exit button
        Button exit = new Button("Exit");
        exit.setOnAction( event -> {logger.info("Exiting App from menu");System.exit(0);});
        //add buttons to menu scene
        buttons.getChildren().addAll(multiplayer,instructionScene,exit);
        mainPane.setCenter(buttons);
        buttons.setAlignment(Pos.CENTER);
        //apply css to buttons
        Stream.of(playButton,instructionScene,exit,multiplayer).forEach(button -> button.getStyleClass().add("menuItem"));

    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

}
