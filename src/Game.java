import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.StringConverter;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class Game {
    private Pool pool;
    private static ArrayList<Player> players = new ArrayList<Player>();
    private ArrayList<Player> activePlayers = new ArrayList<Player>();
    private static ArrayList<Player> winners = new ArrayList<Player>();
    private String playerName;
    private int playerCount;
    private static Pane pane;

    private String state;
    private int subState;            // subState reference to index in players [0,1,2,3,4]
    private static final double CARD_POSITION = 20;

    private List<Integer> availableSeat = new ArrayList<>(Arrays.asList(1,2,3,4,5));
    private List<String> availableOption = new ArrayList<>(Arrays.asList("fold","check","bet","call","raise"));
    private static HashMap<Integer, Point> bettingTagPosition = new HashMap<Integer, Point>();
    private static HashMap<Integer, Point> bettingAmountPosition = new HashMap<Integer, Point>();

    private String latestDecision;
    private int highestBetBalance;
    private int additionalAmount;
    private boolean round;
    private ArrayList<HBox> selectedCard = new ArrayList<HBox>();   // keep selected card
    ArrayList<Node> nodes = new ArrayList<Node>();
    private Label poolLabel;
    private boolean drawOver;
    private Player winner;

    @FXML private Label roundLabel;
    @FXML private Button fold;
    @FXML private Button check;
    @FXML private Button bet;
    @FXML private Button call;
    @FXML private Button raise;
    @FXML private Slider slider;
    @FXML private TextField inputText;
    @FXML private Button redrawBtn;
    @FXML private Button endTurnBtn;

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
        selectedCard.clear();
        drawOver = false;

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

        pool = new Pool();
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
                state = "cumulate";
                subState = 0;
            }
            else {
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
        redrawBtn.setOnAction((ActionEvent e) -> {
            System.out.println("==================REMOVE==================");
            System.out.println("==================REMOVE==================");
            System.out.println("==================REMOVE==================");
            System.out.println("MUST REMOVE " + selectedCard);
            // remove GUI component
            for (HBox selected : selectedCard)
            {
                player.removeImgCards(selected);
            }
            // remove card for this player
            String matchCard = null;
            ArrayList<Card> cards = player.getCardOnHand().getCards();
            for (int i = cards.size()-1; i > -1; i--)
            {
                matchCard = cards.get(i).getCardLetter() + "_" + cards.get(i).getCardType();
                for (HBox selected : selectedCard)
                {
                    if (matchCard.equals(selected.getId()))
                    {
                        player.getCardOnHand().removeCard(cards.get(i));
                    }
                }
            }
            System.out.println(player.getImgCards());
            System.out.println(player.getCardOnHand().getCards());
            state = "discardCard";
        });
        endTurnBtn.setOnAction((ActionEvent e) -> {
            System.out.println("==================END TURN==================");
            System.out.println("==================END TURN==================");
            System.out.println("==================END TURN==================");
            subState++;
            state = "draw";
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
    private void update()
    {
        // other thread
        switch (state)
        {
            case "deal":
                dealCard();
                break;
            case "afterDeal":
                delayTime(1000);
                break;
            case "delayFold":
                delayTime(1000);
                break;
            case "delayDraw":
                delayTime(1000);
                break;
            case "betting":
                System.out.println("UPDATE - BETTING PHASE : substate = " + subState);
                if (hasWinner())
                {
                    System.out.println("WE GOT WINNER");
                    state = "winner";
                }
                // All players already take a turn, check if met terminating condition
                // else re-betting round
                else {
                    if (subState == players.size())
                    {
                        round = true;
                        subState = 0;
                        if (isBettingOver())
                            state = "cumulate";
                        else
                            bettingRound(players.get(subState));
                    }
                    else if (subState < players.size())
                    {
                        bettingRound(players.get(subState));
                    }
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

            // TODO: must find winner here
            case "winner":
                System.out.println("WE HAVE THE WINNER!!!!");
                break;

            case "cumulate":
                System.out.println("UPDATE - CUMULATE PHASE");
                break;

            case "draw":
                System.out.println("UPDATE - DRAWING PHASE : substate = " + subState);
                if (hasWinner())
                {
                    state = "winner";
                }
                else
                {
                    controlButton(true);
                    redrawBtn.setDisable(false);
                    endTurnBtn.setDisable(false);
                    // Finish drawing round
                    if (subState == players.size())
                    {
                        subState = 0;
                        round = false;
                        drawOver = true;
                        state = "betting";
                        bettingRound(players.get(subState));
                    }
                    else if (subState < players.size())
                    {
                        drawingRound(players.get(subState));
                        System.out.println("drawingTurn substate is added to " + subState);
                    }
                }
                break;

            case "discardCard" :
                System.out.println("UPDATE - DISCARD PHASE");
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
    private void redraw()
    {
        switch (state)
        {
            case "winner" :
                winnerUI();
                state = "pause";
                break;

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
                //roundLabel.setText("Betting Round");
                roundLabel.setVisible(true);
                state = "betting";
                break;

            case "betting":
                roundLabel.setText("Betting Round");
                bettingUI(players.get(subState));
                break;

            // Fold always follow by cardDisappearTime
            // then back to betting like others
            case "fold":
                foldUI(players.get(subState));
                state = "delayFold";
                break;

            case "delayFold":
                // discard cards of inactive player
                for (Node node : nodes)
                {
                    node.setVisible(false);
                }
                state = "betting";
                subState++;
                System.out.println("DELAY : subState is added to " + subState);
                break;

            // won't need to do anything
            case "check" :
//                if ((isBettingOver()) && (round))
//                    state = "cumulate";
//                else
//                {
                    updateBalanceUI(players.get(subState), "CHECK");
                    state = "betting";
                    subState++;
                    System.out.println("CHECK : subState is added to " + subState);
//                }
                break;

            // UpdateBalanceUI, also maybe display call
            case "call" :
                updateBalanceUI(players.get(subState), "CALL");
                state = "betting";
                subState++;
                System.out.println("CALL : subState is added to " + subState);
                break;

            case "bet" :
                updateBalanceUI(players.get(subState), "BET");
                state = "betting";
                subState++;
                System.out.println("BET : subState is added to " + subState);
                break;

            case "raise" :
                updateBalanceUI(players.get(subState), "RAISE");
                state = "betting";
                subState++;
                System.out.println("RAISE : subState is added to " + subState);
                break;

            case "cumulate" :
                updatePoolUI();
                // TODO: pause state must be changed to new game
                if (drawOver)
                    state = "winner";
                else
                    state = "draw";
                break;

            // TODO: temporary pause state to see function of drawing card for actual player
            case "draw" :
                roundLabel.setText("Drawing Round");
                enableSelectableCard();
                break;

            case "discardCard" :
                roundLabel.setText("Drawing Round");
                enableSelectableCard();
                System.out.print("DISCARD in UI; ");
                discardUI();
                state = "delayDraw";
                break;

            case "delayDraw" :
                for (Node node : nodes)
                {
                    node.setVisible(false);
                }
                state = "draw";
                break;
        }

    }

    // discard any card within nodes array, don't need to know who is the owner
    private void discardUI()
    {
        nodes.clear();
        for (int i = pane.getChildren().size()-1; i >= 0; i--)
        {
            Node node = pane.getChildren().get(i);
            for (HBox selected : selectedCard)
            {
                if (node.equals(selected))
                {
                    System.out.println("FOUND!");
                    // Play animation of fold
                    Path path = new Path();
                    path.getElements().add(new MoveTo(node.getTranslateX(),node.getTranslateY()));
                    path.getElements().add(new LineTo(500,140));
                    PathTransition pathTransition = new PathTransition();
                    pathTransition.setPath(path);
                    pathTransition.setNode(node);
                    pathTransition.play();
                    // Keep data for disable visibility later
                    nodes.add(node);
                }
            }
        }
    }

    // Used in drawing phase
    private void enableSelectableCard()
    {
        for (Player player : players)
        {
            if (!(player instanceof Bot))
            {
                for (HBox card : player.getImgCards())
                {
                    DropShadow dsHover = new DropShadow( 15, Color.rgb(243,188,46) );
                    card.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) -> {
                        if ( newValue )
                            card.setEffect(dsHover);
                        else
                            card.setEffect(null);
                    });

                    // set selectable only for player to use in drawing phase
                    if (!(player instanceof Bot)) {
                        card.setOnMouseEntered(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                card.requestFocus();
                            }
                        });
                        card.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                selectedCard.clear();
                                DropShadow dsClick = new DropShadow( 15, Color.rgb(156,39,6) );
                                card.setEffect(dsClick);
                                selectedCard.add(card);
                                System.out.println(card.getId());
                            }
                        });
                    }
                }
            }
        }
    }

    // TODO: WINNER UI is not completed
    private void winnerUI()
    {
        int poolValue = pool.getPool();
        // Get bet balance to add to the pool
        // then reset to use in the next betting round
        for (Player player : players)
        {
            poolValue += player.getBetBalance();
            player.setBetBalance(0);
            player.getBetTag().setVisible(false);
            player.getAmountTag().setVisible(false);
        }
        pool.setPool(poolValue);
        poolLabel.setText(Integer.toString(pool.getPool()));

        // Play animation here : move from current position to the winner's position
        Path path = new Path();
        path.getElements().add(new MoveTo(0, 0));

        // find position we must move to
        double x = 0.0;
        double y = 0.0;
        if (winner != null)
        {
            switch (winner.getSeat())
            {
                case 1:
                    x = +280;
                    y = -120;
                    break;
                case 2:
                    x = +280;
                    y = +170;
                    break;
                case 3:
                    x = 0;
                    y = +230;
                    break;
                case 4:
                    x = -280;
                    y = +170;
                    break;
                case 5:
                    x = -280;
                    y = -120;
                    break;
            }
        }

        path.getElements().add(new LineTo(x, y));
        PathTransition pathTransition = new PathTransition();
        pathTransition.setPath(path);
        pathTransition.setNode(poolLabel);
        pathTransition.play();

        // Update total balance for winner
        if (winner != null)
        {
            winner.setBalance(winner.getBalance() + pool.getPool());
            winner.setBalanceLabel();
        }
        else
        {
            System.out.println("WINNER is not decided yet");
        }

        // TODO: update roundLabel and call all cards back to dealer, then deal card again
    }

    private void updatePoolUI()
    {
        int poolValue = pool.getPool();
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
        ArrayList<HBox> cards = player.getImgCards();
        for (int i = cards.size()-1; i > -1; i--)
        {
            for (Node node : pane.getChildren())
            {
                if (node.equals(cards.get(i)))
                {
                    // Play animation of fold
                    Path path = new Path();
                    path.getElements().add(new MoveTo(node.getTranslateX(),node.getTranslateY()));
                    path.getElements().add(new LineTo(500,140));
                    PathTransition pathTransition = new PathTransition();
                    pathTransition.setPath(path);
                    pathTransition.setNode(node);
                    pathTransition.play();
                    // Keep data for disable visibility later
                    nodes.add(node);
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

    private void dealCardUI(Player player, int i)
    {
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

        // set id for this card, so that we know which card is selected
        HBox layout = new HBox();
        layout.setId(player.getCard(i).getCardLetter() + "_" + player.getCard(i).getCardType());
        layout.getChildren().addAll(blend);

        player.addImgCards(layout);

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
        pathTransition.setPath(path);
        pathTransition.setNode(layout);
        pathTransition.play();

        pane.getChildren().add(layout);
    }

    private void bettingUI(Player player)
    {
        System.out.println("");
        System.out.println("Animation for " + player.getName() + " Must add one to subState");
        System.out.println("");
        // Add one to subState to go to the next player
        // TODO: this is not add one, but subState value must come from index of active user
        subState++;
        System.out.println("BETTING UI : subStated is added to " + subState);
    }

    // Deal card phase
    private void dealCard()
    {
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

    private void bettingRound(Player player)
    {
        // check for active status, if inactive - don't do anything
        if (player.isActive())
        {
            // Setup available options
            controlButton(true);
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
//                System.out.print("RANDOM FOR " + player.getName() + " ; ");
                latestDecision = player.bettingTurn(availableOption);

                switch (latestDecision)
                {
                    // TODO: change 40 to random amount of money
                    case "bet" :
                        player.setBetBalance(player.getBetBalance() + 40);
                        System.out.println(player.getName() + " make a bet");
                        break;
                    // TODO: change 40 to random amount of money
                    case "raise" :
                        player.setBetBalance(player.getBetBalance() + 40);
                        System.out.println(player.getName() + " make a raise");
                        break;
                    // See bet balance of previous active player
                    // to determine amount
                    case "call" :
                        int previousBetBalance = player.getBetBalance();
                        player.setBetBalance(highestBetBalance);
                        additionalAmount = player.getBetBalance() - previousBetBalance;
                        player.setBalance(player.getBalance() - additionalAmount);
                        System.out.println(player.getName() + " make a call");
                        break;
                    // Nothing happen to this player
                    case "check" :
                        if (round)
                            state = "cumulate";
                        else
                        {
                            System.out.println(player.getName() + " has checked");
                            state = "check";
                        }
                        break;
                    // Set active to false
                    // also the bet balance for this player will be loss
                    case "fold" :
                        player.setActive(false);
                        System.out.println(player.getName() + " make a fold");
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

    private void drawingRound(Player player)
    {
        // check for active status, if inactive - don't do anything
        if (player.isActive())
        {
            if (player instanceof Bot)
            {
                controlButton(true);
                selectedCard.clear();
                System.out.println("+++++++++++++++++DRAWING++++++++++++++++++++++");
                System.out.print("RANDOM FOR " + player.getName() );

                for (HBox card : player.drawingTurn())
                {
                    selectedCard.add(card);
                }

                System.out.println("; selected card = " + selectedCard);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                subState++;
                state = "discardCard";
            }
            // Player's turn
            else  {
                System.out.println("+++++++++++++++++DRAWING++++++++++++++++++++++");
                state = "pause";
            }
        }
        else
            subState++;
    }

    // Check for any active user
    // TODO: add the only active user to winner
    private boolean hasWinner()
    {
        int numberOfActivePlayer = 0;
        for (Player player : players)
        {
            if (player.isActive()) {
                numberOfActivePlayer++;
                winner = player;
            }
        }

        if (numberOfActivePlayer > 1) {
            winner = null;
            return false;
        }
        else {
            return true;
        }
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

    private void delayTime(long time)
    {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void controlButton(boolean bool)
    {
        bet.setDisable(bool);
        call.setDisable(bool);
        check.setDisable(bool);
        fold.setDisable(bool);
        raise.setDisable(bool);
        slider.setDisable(bool);
        inputText.setDisable(bool);
        redrawBtn.setDisable(bool);
        endTurnBtn.setDisable(bool);
    }


    // In the end list of available seat will be left
    private int randomSeat()
    {
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
