import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Time;
import java.util.*;
import java.util.List;


public class Game {
    private Pool pool;
    private int currentRaiseValue;
    private int lowestBalance;
    private ArrayList<Player> players = new ArrayList<Player>();
    private String playerName;
    private int playerCount;
    private static Pane pane;

    private List<Integer> availableSeat = new ArrayList<>(Arrays.asList(1,2,3,4,5));

    @FXML
    private Button fold;

    @FXML
    private Button check;

    @FXML
    private Button bet;

    @FXML
    private Button call;

    @FXML
    private Button raise;

    @FXML
    private Slider slider;

    @FXML
    private TextField inputText;

//    update() {
//        // other thread
//        // ..
//
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                redraw();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        update();
//                    }
//                })
//            }
//        });
//    }

    public Game(String playerName, int playerCount, int seat, Point location) {
        this.playerName = playerName;
        this.playerCount = playerCount;

        pane = new Pane();
        Scene scene = new Scene(pane, 1000, 800);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Game2.fxml"));
        fxmlLoader.setRoot(pane);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                update();
//            }
//        }).start();

        GraphicWindow.getStage().setScene(scene);

        // create instance of player at the selected position first
        Player player = new Player(playerName, seat, location, 500, true);
        players.add(player);
        // first remove taken seat by player
        availableSeat.remove((Object)seat);

        // create other bots
        for (int i = 0; i < playerCount-1; i++) {
            int pos = randomSeat();
            String name = "Player" + (i+1);
            Bot bot = new Bot(name, pos, GraphicWindow.getSeatPosition(pos), 500, true);
            availableSeat.remove((Object)pos);
            players.add(bot);
        }

        // Sorting an arraylist of player by ascending number of seat
        // so that dealer can distribute card in correct order
        Collections.sort(players);

        // Deal card phase
        dealCard();


        // First betting round
        //bettingRound();
    }

    public void dealCard() {
        // Each player getting five cards
        for (int i = 0; i < 5; i++) {
            for (Player player : players) {
                Card randCard = Deck.getInstance().getRandomCard();
                player.addCardOnHand(randCard);
            }
        }

        for (Player p : players) {
            System.out.println(p.getName());
            p.cardOnHand.printAll();
            System.out.println("==========================");
        }
    }

    public void bettingRound() {
        for (Player p : players) {
            if (p instanceof Bot) {
                controlButton(false);
            } else {
                controlButton(true);
            }
            p.bettingTurn();
        }
    }

    public void controlButton(boolean bool) {
        bet.setDisable(bool);
        call.setDisable(bool);
        check.setDisable(bool);
        fold.setDisable(bool);
        raise.setDisable(bool);
        slider.setDisable(bool);
        inputText.setDisable(bool);
    }

    // In the end list of available seat will be left
    private int randomSeat() {
        int random = 0;
        random = (int)(Math.random() * (availableSeat.size()-1));
        return availableSeat.get(random);
    }

    public static Pane getPane() {
        return pane;
    }

}
