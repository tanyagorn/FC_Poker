import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 *   Include main() to execute program, launch GUI
 *   then get information to initialize poker game.
 *
 *   Created by TC group, 6 December 2017
 */
public class GraphicWindow extends Application
{
    /** Container for GUI components */
    private static Pane pane;

    /** Top-level container that hosts a Scene */
    private static Stage stage;

    /** Holding position for each seat point */
    private static HashMap<Integer, Point> seatPosition = new HashMap<Integer, Point>();

    /** GUI components loading from .FXML file */
    @FXML private TextField name;
    @FXML private ComboBox<String> playerCount;
    @FXML private Button nextButton;
    @FXML private List<Button> seatButton;
    @FXML private VBox firstScene;
    @FXML private VBox secondScene;

    /**
     *  Launch JavaFX application
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        //can now use the stage in other methods
        stage = primaryStage;

        pane = new Pane();
        Scene scene = new Scene(pane, 1000, 800);

        // Load .fxml file to get GUI components
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Main.fxml"));
        fxmlLoader.setRoot(pane);
        fxmlLoader.setController(this);

        try
        {
            fxmlLoader.load();
        } catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }

        // Keep the position of each seat in HashMap
        seatPosition.put(1, new Point(795,230));
        seatPosition.put(2, new Point(795,520));
        seatPosition.put(3, new Point(500,580));
        seatPosition.put(4, new Point(210,520));
        seatPosition.put(5, new Point(210,230));

        // Set initial value
        name.setText("Dummy");
        // populate the combo box with number of player choices.
        playerCount.getItems().setAll("2","3","4","5");
        // select 3 player as default
        playerCount.getSelectionModel().select(1);
        // hide the second scene
        secondScene.setVisible(false);

        // hide button in the first scene
        // also adding event handler for each button
        for (int i = 0; i < seatButton.size(); i++)
        {
            seatButton.get(i).setVisible(false);
            int seat = i+1;
            seatButton.get(i).setOnAction(event ->
            {
                new Game(name.getText(), Integer.parseInt(playerCount.getValue()),
                        seat, seatPosition.get(seat).getLocation());
            });
        }

        // when NEXT is clicked, display scene with option for selecting seat
        nextButton.setOnAction(event ->
        {
            firstScene.setVisible(false);
            secondScene.setVisible(true);
            for (int i = 0; i < seatButton.size(); i++)
            {
                seatButton.get(i).setVisible(true);
            }
        });
        
        primaryStage.setTitle("Five Card Draw Poker Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * return stage to host another scene
     * @return top-level container
     */
    public static Stage getStage()
    {
        return stage;
    }

    /**
     * return point of seat position
     * @param  seat which seat
     * @return seat position
     */
    public static Point getSeatPosition(Integer seat)
    {
        return seatPosition.get(seat);
    }

    /**
     * main method which launch javafx application
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}
