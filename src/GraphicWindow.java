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

public class GraphicWindow extends Application {
    private static Game game;
    private static Pane pane;
    private static Stage stage;
    private static HashMap<Integer, Point> seatPosition = new HashMap<Integer, Point>();

    @FXML private TextField name;
    @FXML private ComboBox<String> playerCount;
    @FXML private Button nextButton;
    @FXML private List<Button> seatButton;
    @FXML private VBox firstScene;
    @FXML private VBox secondScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //can now use the stage in other methods
        stage = primaryStage;

        pane = new Pane();
        Scene scene = new Scene(pane, 1000, 800);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Main.fxml"));
        fxmlLoader.setRoot(pane);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        /* Keep the position of each seat in HashMap */
        seatPosition.put(1, new Point(795,230));
        seatPosition.put(2, new Point(795,520));
        seatPosition.put(3, new Point(500,580));
        seatPosition.put(4, new Point(210,520));
        seatPosition.put(5, new Point(210,230));

        name.setText("Dummy");
        // populate the combo box with number of player choices.
        playerCount.getItems().setAll("2","3","4","5");
        // select 3 player as default
        playerCount.getSelectionModel().select(1);
        // hide the second scene
        secondScene.setVisible(false);

        // hide button in the first scene
        // also adding event handler for each button
        for (int i = 0; i < seatButton.size(); i++) {
            seatButton.get(i).setVisible(false);
            int seat = i+1;
            seatButton.get(i).setOnAction(event -> {
                game = new Game(name.getText(), Integer.parseInt(playerCount.getValue()),
                        seat, seatPosition.get(seat).getLocation());
            });
        }

        nextButton.setOnAction(event -> {
            firstScene.setVisible(false);
            secondScene.setVisible(true);
            for (int i = 0; i < seatButton.size(); i++) {
                seatButton.get(i).setVisible(true);
            }
        });
        
        primaryStage.setTitle("Five Card Draw Poker Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static Stage getStage() {
        return stage;
    }

    // return clone version of seatPosition
    public static Point getSeatPosition(Integer seat) {
        return seatPosition.get(seat);
//        HashMap<Integer, Point> clone = (HashMap<Integer, Point>)seatPosition.clone();
//        return clone;
    }



    public static void main(String[] args) {
        launch(args);
    }
}
