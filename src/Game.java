import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class Game {
    private Pool pool;
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
    private static HashMap<Integer, Point> bettingTagPosition = new HashMap<Integer, Point>();
    private static HashMap<Integer, Point> bettingAmountPosition = new HashMap<Integer, Point>();

    private String latestDecision;
    private int highestBetBalance;
    private int additionalAmount;
    private boolean round;
    ArrayList<Node> nodes = new ArrayList<Node>();

    private Label poolLabel;

    @FXML private Label roundLabel;
    @FXML private Button fold;
    @FXML private Button check;
    @FXML private Button bet;
    @FXML private Button call;
    @FXML private Button raise;
    @FXML private Slider slider;
    @FXML private TextField inputText;

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

        bettingTagPosition.put(1, new Point(690, 310));
        bettingTagPosition.put(2, new Point(660, 410));
        bettingTagPosition.put(3, new Point(490, 440));
        bettingTagPosition.put(4, new Point(340, 410));
        bettingTagPosition.put(5, new Point(310, 310));

        bettingAmountPosition.put(1, new Point(650, 280));
        bettingAmountPosition.put(2, new Point(690, 380));
        bettingAmountPosition.put(3, new Point(500, 410));
        bettingAmountPosition.put(4, new Point(310, 380));
        bettingAmountPosition.put(5, new Point(350, 280));

        // Will be visible when it is a time for betting round
        roundLabel.setVisible(false);

        // Binding slider to textField, with specified format by converter
        StringConverter<Number> converter = new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                if (object != null)
                    return Integer.toString((int) Math.round(slider.valueProperty().doubleValue()));
                else
                    return null;
            }

            @Override
            public Double fromString(String string) {
                Double d = Double.parseDouble(string);
                inputText.textProperty().setValue(Integer.toString((int) Math.round(d)));
                return d;
            }
        };
        inputText.textProperty().bindBidirectional(slider.valueProperty(), converter);

        GraphicWindow.getStage().setScene(scene);

        pool = new Pool(0);
        poolLabel = new Label("TEST");
        poolLabel.setLayoutX(490);
        poolLabel.setLayoutY(350);
        pane.getChildren().add(poolLabel);

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
            // every player already take a turn - end this betting round
            // start drawing phase
            if (round) {
                System.out.println("CUMULATE");
                state = "cumulate";
            }
            else{
                System.out.println("CHECK");
                state = "check";
            }
        });
        call.setOnAction((ActionEvent e) -> {
            int previousBetBalance = player.getBetBalance();
            player.setBetBalance(highestBetBalance);
            additionalAmount = player.getBetBalance() - previousBetBalance;
            player.setBalance(player.getBalance() - additionalAmount);
            state = "call";
        });
        fold.setOnAction((ActionEvent e) -> {
            player.setActive(false);
            state = "fold";
        });
        raise.setOnAction((ActionEvent e) -> {
            int previousBetBalance = player.getBetBalance();
            player.setBetBalance(player.getBetBalance() + Integer.parseInt(inputText.getText()));
            player.setBalance(player.getBalance() - Integer.parseInt(inputText.getText()));
            state = "raise";
        });
        bet.setOnAction((ActionEvent e) -> {
            player.setBetBalance(Integer.parseInt(inputText.getText()));
            player.setBalance(player.getBalance() - player.getBetBalance());
            state = "bet";
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }).start();
    }

    // Check for terminate condition of betting turn
    // all active players must have same amount of bet balance
    private boolean isBettingOver()
    {
        int betBalance = 0;

        // This loop is for get the initial betBalance to compare with others
        for (Player player : players)
        {
            if (player.isActive())
            {
                betBalance = player.getBetBalance();
                break;
            }
        }
        // Compare with the rest
        for (Player player : players)
        {
            if (player.isActive())
            {
                if (player.getBetBalance() != betBalance)
                    return false;
            }
        }
        return true;
    }

    // Handle game's flow
    private void update() {

        // other thread
        switch (state)
        {
            case "deal":
                dealCard();
                break;
            case "afterDeal":
                delayTime(1500);
                break;
            case "delay":
                delayTime(1000);
                break;
            case "betting":
                if (hasWinner())
                {
                    state = "winner";
                }
                // All players already take a turn, check if met terminating condition
                // else re-betting round
                if (subState == players.size())
                {
                    round = true;
                    if (isBettingOver())
                        state = "cumulate";
                    else
                        subState = 0;
                }
                if (subState < players.size())
                {
                    bettingRound(players.get(subState));
                }
                break;

            // Pause state is for waiting for player response
            case "pause":
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
            case "winner":
                System.out.println("WE HAVE THE WINNER!!!!");
                break;

            case "cumulate":
                break;

            case "draw":
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

    // Get highest bet balance for all player that still active in the game
    private int getHighestBetBalance()
    {
        int highestBetBalance = 0;
        for (Player player : players)
        {
            if (player.isActive())
            {
                if (player.getBetBalance() > highestBetBalance)
                    highestBetBalance = player.getBetBalance();
            }
        }
        return highestBetBalance;
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
                roundLabel.setText("Betting Round");
                roundLabel.setVisible(true);
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

            case "delay":
                // discard cards of inactive player
                for (Node node : nodes)
                {
                    node.setVisible(false);
                }
                state = "betting";
                subState++;
                break;

            // won't need to do anything
            case "check" :
                if ((isBettingOver()) && (round))
                    state = "cumulate";
                else
                {
                    updateBalanceUI(players.get(subState), "CHECK");
                    state = "betting";
                    subState++;
                }
                break;

            // UpdateBalanceUI, also maybe display call
            case "call" :
                updateBalanceUI(players.get(subState), "CALL");
                state = "betting";
                subState++;
                break;

            case "bet" :
                updateBalanceUI(players.get(subState), "BET");
                state = "betting";
                subState++;
                break;

            case "raise" :
                updateBalanceUI(players.get(subState), "RAISE");
                state = "betting";
                subState++;
                break;

            case "cumulate" :
                updatePoolUI();
                state = "draw";
                break;

            case "draw" :
                roundLabel.setText("Drawing Round");
                break;
        }

    }

    private void updatePoolUI()
    {
        int poolValue = 0;
        // Get bet balance to add to the pool
        // then reset to use in the next betting round
        for (Player player : players)
        {
            poolValue += player.getBetBalance();
            player.setBetBalance(0);
            player.getBetTag().setVisible(false);
            player.getAmountTag().setVisible(false);
        }
        System.out.println("poolValue = " + poolValue);
        pool.setPool(poolValue);
        poolLabel.setText(Integer.toString(pool.getPool()));


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

        // Change style of name tag for this player
        player.setNameTagInactive();
    }

    // update balance animation
    private void updateBalanceUI(Player player, String action)
    {
        Label betTag = player.getBetTag();
        Label amountTag = player.getAmountTag();
        betTag.setVisible(true);
        amountTag.setVisible(true);
        betTag.setText(action);
        amountTag.setText(Integer.toString(player.getBetBalance()));
        player.setBalanceLabel();
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

    // Deal card phase
    private void dealCard() {
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

    private void bettingRound(Player player) {
        // check for active status, if inactive - don't do anything
        if (player.isActive())
        {
            // Setup available options
            highestBetBalance = getHighestBetBalance();
            if (highestBetBalance == 0)
            {
                availableOption = Arrays.asList("fold","check","bet");
                fold.setDisable(false);
                check.setDisable(false);
                bet.setDisable(false);
                call.setDisable(true);
                raise.setDisable(true);
                slider.setDisable(false);
                inputText.setDisable(false);
            }
            else if (player.getBetBalance() == highestBetBalance)
            {
                availableOption = Arrays.asList("fold","check","raise");
                fold.setDisable(false);
                check.setDisable(false);
                call.setDisable(true);
                raise.setDisable(false);
                bet.setDisable(true);
                slider.setDisable(false);
                inputText.setDisable(false);
            }
            else
            {
                availableOption = Arrays.asList("fold","call","raise");
                fold.setDisable(false);
                call.setDisable(false);
                raise.setDisable(false);
                bet.setDisable(true);
                check.setDisable(true);
                slider.setDisable(false);
                inputText.setDisable(false);
            }

            // Setup slider
            int lowestBalance = getLowestBalance();
            slider.setMin(0);
            slider.setMax(lowestBalance);

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
                        int previousBetBalance = player.getBetBalance();
                        player.setBetBalance(highestBetBalance);
                        additionalAmount = player.getBetBalance() - previousBetBalance;
                        player.setBalance(player.getBalance() - additionalAmount);
                        System.out.println(player.getName() + " make a call; bet balance now = " + player.getBetBalance());
                        break;
                    // Nothing happen to this player
                    case "check" :
                        if (round)
                            state = "cumulate";
                        else
                        {
                            System.out.println(player.getName() + " has checked; bet balance now = " + player.getBetBalance());
                            state = "check";
                        }
                        break;
                    // Set active to false
                    // also the bet balance for this player will be loss
                    case "fold" :
                        player.setActive(false);
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
            else // Player's turn
                state = "pause";

        }
    }

    // Check for any active user
    private boolean hasWinner()
    {
        int numberOfActivePlayer = 0;
        for (Player player : players)
        {
            if (player.isActive())
                numberOfActivePlayer++;
        }
        if (numberOfActivePlayer > 1)
            return false;
        else
            return true;
    }

    private int getLowestBalance()
    {
        // 500 is start money for every player
        int lowestBalance = 500;
        for (Player player : players)
        {
            if (player.isActive())
            {
                if (player.getBalance() < lowestBalance)
                    lowestBalance = player.getBalance();
            }
        }
        return lowestBalance;
    }

    private void delayTime(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void controlButton(boolean bool) {
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

    public static Point getBettingTagPosition(Player player)
    {
        return bettingTagPosition.get(player.getSeat());
    }

    public static Point getBettingAmountPosition(Player player)
    {
        return bettingAmountPosition.get(player.getSeat());
    }

}
