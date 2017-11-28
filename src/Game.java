import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class Game {
    private Pool pool;
    private int lowestBalance;
    private ArrayList<Player> players = new ArrayList<Player>();
    private ArrayList<Player> activePlayers = new ArrayList<Player>();
    private String playerName;
    private int playerCount;
    private static Pane pane;

    private String state;
    private int subState;       // subState reference to index in players [0,1,2,3,4]
    private static final double CARD_POSITION = 20;

    private List<Integer> availableSeat = new ArrayList<>(Arrays.asList(1,2,3,4,5));
    private List<String> availableOption = new ArrayList<>(Arrays.asList("fold","check","bet","call","raise"));

    private String latestDecision;
    ArrayList<Node> nodes = new ArrayList<Node>();

    @FXML private Label bettingRoundLabel;
    @FXML private Button fold;
    @FXML private Button check;
    @FXML private Button bet;
    @FXML private Button call;
    @FXML private Button raise;
    @FXML private Slider slider;
    @FXML private TextField inputText;

    // Handle game's flow
    private void update() {

        // other thread
        switch (state)
        {
            case "deal":
                dealCard();
                break;
            case "afterDeal":
                delayTime(2000);
                break;
            case "delay":
                delayTime(1000);
                break;
            // TODO: betting round must not be over, unless all active player bet the same amount of money
            case "betting":
                if (subState < players.size())
                {
                    // TODO: Check for active player
                    bettingRound(players.get(subState));
                }
                else
                {
                    // reset subState to prepare for the next round
                    subState = 0;
                    state = "pause";
                }
                break;

            // Pause state is for waiting for player response
            // TODO: if player has chose set state back to betting
            case "pause":
                System.out.println("WAITING FOR PLAYER TO CHOOSE!");
                break;
            case "fold":
                System.out.println("HAS CHOSE FOLD");
                break;
            case "check":
                System.out.println("HAS CHOSE CHECK");
                break;
            case "call":
                System.out.println("HAS CHOSE CALL");
                break;
            case "bet" :
                System.out.println("HAS CHOSE BET");
                break;
            case "raise":
                System.out.println("HAS CHOSE RAISE");
                break;
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                redraw();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        update();
                    }
                }).start();
            }
        });
    }

    // Return null means there is no active player other than this player
    // this player will be the winner
    public Player findPreviousActivePlayer(Player player)
    {
        int currentIndex = players.indexOf(player);
        int index = currentIndex - 1; // index of previous player
        int round = 0;

        for (int i = index; i > -2 && i < 5; i--)
        {
            if (i == index)
                round++;
            // Already loop through the whole array of players
            // yet, not found any active player
            if (round == 2)
                break;
            // round index back
            if (i == -1)
                i = 4;
            // found previous player that still active in game
            if (players.get(i).isActive())
                return players.get(i);
        }
        return null;
    }

    public void bettingRound(Player player) {
        Player previousPlayer = null;
        // first player of betting round won't
        // have call and raise function
        if (player.getSeat() == 1)
        {
            availableOption = Arrays.asList("fold","check","bet");
            call.setVisible(false);
            raise.setVisible(false);
        }
        else
        {
            availableOption = Arrays.asList("fold","check","bet","call","raise");
            call.setVisible(true);
            raise.setVisible(true);
            previousPlayer = findPreviousActivePlayer(player);
        }

        if (player instanceof Bot)
        {
            controlButton(true);
            System.out.println("++++++++++++++++++++++++++++++++++");
            System.out.println("RANDOM FOR " + player.getName() );
            latestDecision = player.bettingTurn(availableOption);

            switch (latestDecision)
            {
                // TODO: change 40 to random amount of money
                case "bet" :
                    player.setBetBalance(player.getBetBalance() + 40);
                    System.out.println(player.getName() + " make a bet; bet balance now = " + player.getBetBalance());
                    break;
                // TODO: change 40 to random amount of money
                case "raise" :
                    player.setBetBalance(player.getBetBalance() + 40);
                    System.out.println(player.getName() + " make a raise; bet balance now = " + player.getBetBalance());
                    break;
                // See bet balance of previous active player
                // to determine amount
                case "call" :
                    player.setBetBalance(previousPlayer.getBetBalance());
                    System.out.println(player.getName() + " make a call; bet balance now = " + player.getBetBalance());
                    break;
                // Nothing happen to this player
                case "check" :
                    break;
                // Set active to false
                // also the bet balance for this player will be loss
                case "fold" :
                    player.setActive(false);
                    player.setBalance(player.getBalance() - player.getBetBalance());
                    System.out.println(player.getName() + " make a fold; bet balance now = " + player.getBetBalance()
                        + " current balance = " + player.getBalance());
                    break;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // TODO: Not sure yet that we should set state here or where
            state = latestDecision;
        }
        else
        {
            // Player's turn
            controlButton(false);
            state = "pause";
        }
    }

    // UI
    private void redraw() {

        switch (state)
        {
            case "deal":
                for (Player player : players)
                {
                    for (int i = 0; i < 5; i++)
                    {
                        dealCardUI(player, i);
                    }
                }
                state = "afterDeal";
                break;

            case "afterDeal":
                bettingRoundLabel.setVisible(true);
                state = "betting";
                break;

            case "betting":
                bettingUI(players.get(subState));
                break;

            // Fold always follow by cardDisappearTime
            // then back to betting like others
            case "fold":
                foldUI(players.get(subState));
                state = "delay";
                break;

            // TODO: don't forget to increment substate in other operation
            // TODO: WARNING!!! subState must go the next active user
            case "delay":
                for (Node node : nodes)
                {
                    node.setVisible(false);
                }
                state = "betting";
                subState++;
                break;

            // won't need to do anything
            case "check" :
                state = "betting";
                break;

            case "call" :
                updateBalanceUI(players.get(subState));
                state = "betting";
                break;

            case "bet" :
                updateBalanceUI(players.get(subState));
                state = "betting";
                break;

            case "raise" :
                updateBalanceUI(players.get(subState));
                state = "betting";
                break;

        }

    }

    // fold animation
    private void foldUI(Player player)
    {
        for (int i = 0; i < 5; i++)
        {
            String id = player.getName() + " " + i;
            for (Node node : pane.getChildren())
            {
                if (node.getId() != null)
                {
                    if (node.getId().equals(id))
                    {
                        // Play animation of fold
                        Path path = new Path();
                        path.getElements().add(new MoveTo(node.getTranslateX(),node.getTranslateY()));
                        path.getElements().add(new LineTo(500,140));
                        PathTransition pathTransition = new PathTransition();
                        pathTransition.setPath(path);
                        pathTransition.setNode(node);
                        pathTransition.play();
                        nodes.add(node);
                    }
                }
            }
        }

        // TODO: change style of nameTag for inactive player
        // TODO: problem - don't know which nameTag belongs to this player
//        ArrayList<Node> tags = new ArrayList<Node>();
//        System.out.println(pane.getChildren());
//        // change nameTag style to indicate that this player is inactive
//        for (Node node : pane.getChildren()) {
//            if (node.getId() != null)
//            {
//                if (node.getId().equals("nameTag"))
//                {
//                    node.getStyleClass().add("inactiveNameTag");
//                    tags.add(node);
//                }
//            }
//        }
//
//        tags.get(players.indexOf(player)).setVisible(false);
    }

    // update balance animation
    private void updateBalanceUI(Player player)
    {

    }

    private void dealCardUI(Player player, int i) {
        // always create both side of card
        ImageView bottom = new ImageView(new javafx.scene.image.Image("img/" + player.getCard(i).getCardLetter()
                + "_" + player.getCard(i).getCardType() + ".png"));
        ImageView top = new ImageView(new Image("img/back_card.png"));

        // set visibility
        if (player instanceof Bot)
            bottom.setVisible(false);
        else
            top.setVisible(false);

        Group blend = new Group(
                bottom,
                top
        );

        HBox layout = new HBox();
        layout.setId(player.getName() + " " + i);
        layout.getChildren().addAll(blend);

        // Find position by calculating from number of element in cards
        double x = 0;
        if (i == 0) {
            x = player.getPosition().getX() - 40;
        } else if (i == 1) {
            x = player.getPosition().getX() - 20;
        } else if (i == 2) {
            x = player.getPosition().getX();
        } else if (i == 3) {
            x = player.getPosition().getX() + 20;
        } else if (i == 4) {
            x = player.getPosition().getX() + 40;
        }

        // Play animation here
        Path path = new Path();
        path.getElements().add(new MoveTo(466,140));
        path.getElements().add(new LineTo(x, player.getPosition().getY() - (bottom.getImage().getHeight()/2)));
        PathTransition pathTransition = new PathTransition();
        //pathTransition.setDuration(Duration.millis(1000));
        pathTransition.setPath(path);
        pathTransition.setNode(layout);
        pathTransition.play();

        pane.getChildren().add(layout);
    }

    private void bettingUI(Player player) {
        System.out.println("");
        System.out.println("Animation for " + player.getName() + " Must add one to subState");
        System.out.println("");
        // Add one to subState to go to the next player
        // TODO: this is not add one, but subState value must come from index of active user
        subState++;
    }

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

        // Will be visible when it is a time for betting round
        bettingRoundLabel.setVisible(false);

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

        // Set initial state
        state = "deal";
        subState = 0;
        controlButton(true);

        // Add action listener to every button
        check.setOnAction((ActionEvent e) -> {
            state = "check";
        });
        call.setOnAction((ActionEvent e) -> {
            state = "call";
        });
        fold.setOnAction((ActionEvent e) -> {
            state = "fold";
        });
        raise.setOnAction((ActionEvent e) -> {
            state = "call";
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }).start();
    }

    // Deal card phase
    public void dealCard() {
        // Each player getting five cards
        for (int i = 0; i < 5; i++) {
            for (Player player : players) {
                Card randCard = Deck.getInstance().getRandomCard();
                player.addCardOnHand(randCard);
            }
        }

        // debugging loop
        for (Player p : players) {
            System.out.println(p.getName());
            p.cardOnHand.printAll();
            System.out.println("==========================");
        }
    }

    public void delayTime(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
