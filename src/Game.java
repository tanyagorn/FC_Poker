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

/**
 *   Game class controls game flow, handle events, responsible
 *   to assign turn to the players.
 *
 *   Created by TC group, 6 December 2017
 */
public class Game {
    /** Instance of pool to be give to the winner at the end of game */
    private Pool pool;

    /** Holding all player instances */
    private static ArrayList<Player> players = new ArrayList<Player>();

    /** Holding list of winner players */
    private static ArrayList<Player> winners = new ArrayList<Player>();

    private static Pane pane;

    private String state;
    private int subState;            // subState reference to index in players [0,1,2,3,4]

    private List<Integer> availableSeat = new ArrayList<>(Arrays.asList(1,2,3,4,5));
    private List<String> availableOption = new ArrayList<>(Arrays.asList("fold","check","bet","call","raise"));
    private static HashMap<Integer, Point> bettingTagPosition = new HashMap<Integer, Point>();
    private static HashMap<Integer, Point> bettingAmountPosition = new HashMap<Integer, Point>();

    private String latestDecision;
    private int additionalAmount;
    private boolean round;
    private ArrayList<HBox> selectedCard = new ArrayList<HBox>();   // keep selected card
    ArrayList<Node> nodes = new ArrayList<Node>();
    private boolean drawOver;
    private Player winner;
    private int highestBetBalance;
    private Label poolLabel;

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

    public Game(String playerName, int playerCount, int seat, Point location)
    {
        pane = new Pane();
        Scene scene = new Scene(pane, 1000, 800);

        // Load .fxml file to set scene
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Game2.fxml"));
        fxmlLoader.setRoot(pane);
        fxmlLoader.setController(this);

        try
        {
            fxmlLoader.load();
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }

        // Add default value for information needed to draw object
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

        // Binding slider to textField, with specified format by converter
        StringConverter<Number> converter = new StringConverter<Number>()
        {
            @Override
            public String toString(Number object)
            {
                if (object != null)
                    return Integer.toString((int) Math.round(slider.valueProperty().doubleValue()));
                else
                    return null;
            }

            @Override
            public Double fromString(String string)
            {
                Double d = Double.parseDouble(string);
                inputText.textProperty().setValue(Integer.toString((int) Math.round(d)));
                return d;
            }
        };
        // binding value of input text for amount of money to slider
        inputText.textProperty().bindBidirectional(slider.valueProperty(), converter);

        GraphicWindow.getStage().setScene(scene);

        // create instance of player at the selected position first
        Player player = new Player(playerName, seat, location, 500, true);
        players.add(player);
        // first remove taken seat by player
        availableSeat.remove((Object)seat);

        // create other bots
        for (int i = 0; i < playerCount-1; i++)
        {
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
        roundLabel.setVisible(false);
        selectedCard.clear();
        drawOver = false;
        controlButton(true);

        // Initialize pool
        pool = new Pool();
        poolLabel = new Label("");
        poolLabel.setLayoutX(490);
        poolLabel.setLayoutY(350);
        pane.getChildren().add(poolLabel);

        // Add action listener to every button to capture actual player event
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
        call.setOnAction((ActionEvent e) ->
        {
            int previousBetBalance = player.getBetBalance();
            player.setBetBalance(highestBetBalance);
            additionalAmount = player.getBetBalance() - previousBetBalance;
            player.setBalance(player.getBalance() - additionalAmount);
            state = "call";
        });
        fold.setOnAction((ActionEvent e) ->
        {
            player.setActive(false);
            state = "fold";
        });
        raise.setOnAction((ActionEvent e) ->
        {
            int previousBetBalance = player.getBetBalance();
            if (getHighestBetBalance() + Integer.parseInt(inputText.getText()) > player.getBalance())
            {
                player.setBetBalance(player.getBalance());
                player.setBalance(0);
            }
            else
            {
                player.setBetBalance(getHighestBetBalance() + Integer.parseInt(inputText.getText()));
                additionalAmount = player.getBetBalance() - previousBetBalance;
                player.setBalance(player.getBalance() - additionalAmount);
            }
            state = "raise";
        });
        bet.setOnAction((ActionEvent e) ->
        {
            player.setBetBalance(Integer.parseInt(inputText.getText()));
            player.setBalance(player.getBalance() - player.getBetBalance());
            state = "bet";
        });
        redrawBtn.setOnAction((ActionEvent e) ->
        {
            // remove GUI component
            for (HBox selected : selectedCard)
                player.removeImgCards(selected);


            // remove card for this player
            String matchCard = null;
            ArrayList<Card> cards = player.getCardOnHand().getCards();
            for (int i = cards.size()-1; i > -1; i--)
            {
                matchCard = cards.get(i).getCardLetter() + "_" + cards.get(i).getCardType();
                for (HBox selected : selectedCard)
                {
                    if (matchCard.equals(selected.getId()))
                        player.getCardOnHand().removeCard(cards.get(i));
                }
            }
            // draw new card - update data in model
            Card randCard = Deck.getInstance().getRandomCard();
            player.addCardOnHand(randCard);
            state = "discardCard";
        });
        endTurnBtn.setOnAction((ActionEvent e) ->
        {
            player.cardOnHand.sortCard();
            state = "newDraw";
        });

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
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
                System.out.println("UPDATE - DEAL");
                dealCard();
                break;
            case "afterDeal":
                delayTime(500);
                break;
            case "delayFold":
                delayTime(500);
                break;
            case "delayDraw":
                delayTime(500);
                break;
            case "delayReturn":
                delayTime(500);
                break;
            case "delayDisplayCard":
                delayTime(3000);
                break;
            case "betting":
                System.out.println("UPDATE - BETTING PHASE : substate = " + subState);
                if (hasWinner())
                {
                    System.out.println("WE GOT WINNER");
                    state = "showCard";
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
                    state = "showCard";
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
                        state = "afterDeal";
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

            case "newDraw":
                System.out.println("UPDATE - NEW DRAW");
                break;

            case "newGame":
                // Finish collect all cards, INIT NEW GAME
                if (subState == players.size())
                {
                    subState = 0;
                    round = false;
                    drawOver = false;
                    state = "deal";
                    winner = null;
                    pool.setPool(0);
                    dealCard();
                    for (Player player : players)
                    {
                        player.setActive(true);
                    }
                    System.out.println("UPDATE - MUST INIT NEW GAME");
                }
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
                System.out.println("REDRAW : HAS WINNER");
                winnerUI();
                subState = 0;
                state = "newGame";
                break;

            case "showCard" :
                System.out.println("REDRAW - show card");
                displayAllCardsUI();
                state = "delayDisplayCard";
                break;

            case "delayDisplayCard" :
                System.out.println("REDRAW - delay display card");
                state = "winner";
                break;

            case "deal":
                subState = 0;
                for (Player player : players)
                {
                    for (int i = 0; i < 5; i++)
                    {
                        dealCardUI(player, i);
                    }
                }
                System.out.println(poolLabel.getTranslateX() + "," + poolLabel.getTranslateY());
                state = "afterDeal";
                break;

            case "afterDeal":
                roundLabel.setText("Betting Round");
                roundLabel.setVisible(true);
                state = "betting";
                break;

            case "betting":
                roundLabel.setText("Betting Round");
                poolLabel.setTranslateX(0);
                poolLabel.setTranslateY(0);
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
                updateBalanceUI(players.get(subState), "CHECK");
                state = "betting";
                subState++;
                System.out.println("CHECK : subState is added to " + subState);
                slider.setMin(0);
                slider.setMax(getLimitBet());
                break;

            // UpdateBalanceUI, also maybe display call
            case "call" :
                updateBalanceUI(players.get(subState), "CALL");
                state = "betting";
                subState++;
                System.out.println("CALL : subState is added to " + subState);
                slider.setMin(0);
                slider.setMax(getLimitBet());
                break;

            case "bet" :
                updateBalanceUI(players.get(subState), "BET");
                state = "betting";
                subState++;
                System.out.println("BET : subState is added to " + subState);
                slider.setMin(0);
                slider.setMax(getLimitBet());
                break;

            case "raise" :
                updateBalanceUI(players.get(subState), "RAISE");
                state = "betting";
                subState++;
                System.out.println("RAISE : subState is added to " + subState);
                slider.setMin(0);
                slider.setMax(getLimitBet());
                break;

            case "cumulate" :
                updatePoolUI();
                enableSelectableCard();
                // TODO: pause state must be changed to new game
                if (drawOver)
                    state = "showCard";
                else
                    state = "draw";
                break;

            // TODO: temporary pause state to see function of drawing card for actual player
            case "draw" :
                roundLabel.setText("Drawing Round");
                poolLabel.setTranslateX(0);
                poolLabel.setTranslateY(0);
                System.out.println(poolLabel.getTranslateX() + "," + poolLabel.getTranslateY());
                break;

            case "discardCard" :
                roundLabel.setText("Drawing Round");
                System.out.print("DISCARD in UI; ");
                discardUI();
                state = "delayDraw";
                break;

            case "delayDraw" :
                for (Node node : nodes)
                {
                    node.setVisible(false);
                }
                // current player
                if (players.get(subState) instanceof Bot)
                    state = "newDraw";
                else
                    state = "draw";
                break;

            case "newDraw" :
                updateCardOnHand(players.get(subState));
                for (int i = 0; i < 5; i++)
                {
                    dealCardUI(players.get(subState), i);
                }
                state = "draw";
                subState++;
                break;

            case "newGame" :
                roundLabel.setText("");
                returnCardUI(players.get(subState));
                state = "delayReturn";
                break;

            case "delayReturn" :
                for (Node node : nodes)
                {
                    node.setVisible(false);
                }
                poolLabel.setText("0");
                state = "newGame";
                subState++;
                break;
        }

    }

    // Call all cards back to dealer, remove data of cards for this player
    private void returnCardUI(Player player)
    {
        nodes.clear();
        for (int i = pane.getChildren().size()-1; i >= 0; i--)
        {
            Node node = pane.getChildren().get(i);
            for (HBox card : player.getImgCards())
            {
                if (node.equals(card))
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
        // clear GUI, also card data for this player
        player.getImgCards().clear();
        player.getCardOnHand().getCards().clear();
    }

    private void updateCardOnHand(Player player)
    {
        System.out.println("REDRAW - UPDATE CARD ON HAND");
        // remove all
        for (HBox card : player.getImgCards())
        {
            for (int i = pane.getChildren().size()-1; i >= 0; i--)
            {
                if (pane.getChildren().get(i).equals(card))
                {
                    pane.getChildren().remove(pane.getChildren().get(i));
                }
            }
        }
        player.getImgCards().clear();
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
                    DropShadow dsHover = new DropShadow( 15, Color.rgb(0,0,255) );
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

    private void displayAllCardsUI()
    {
        for (Player player : players)
        {
            if (player.isActive())
            {
                for (HBox card : player.getImgCards())
                {
                    for (Node node : card.getChildren())
                    {
                        Group group = (Group)node;
                        for(Node inGroup : group.getChildren())
                        {
                            if (inGroup.getId().equals("front"))
                                inGroup.setVisible(true);
                            else
                                inGroup.setVisible(false);
                        }
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

        // Find winner here
        ArrayList<Player> activePlayers = new ArrayList<>();
        for (Player player : players)
        {
            if (player.isActive())
                activePlayers.add(player);
        }
        ArrayList<Player> winners = new ArrayList<>();
        winners = WinnerFinder.findWinner(activePlayers);
        if (winners.size() == 1)
        {
            winners.get(0).setBalance(winners.get(0).getBalance() + pool.getPool());
            winners.get(0).setBalanceLabel();
            Point point = getWinnerPosition(winners.get(0));

            path.getElements().add(new LineTo(point.getX(), point.getY()));
            PathTransition pathTransition = new PathTransition();
            pathTransition.setPath(path);
            pathTransition.setNode(poolLabel);
            pathTransition.play();
        }
        // TODO: must split pool here
        else
        {

        }
    }

    private Point getWinnerPosition(Player player)
    {
        Point point = new Point();
        switch (player.getSeat())
        {
            case 1:
                point.x = +280;
                point.y = -120;
                break;
            case 2:
                point.x = +280;
                point.y = +170;
                break;
            case 3:
                point.x = 0;
                point.y = +230;
                break;
            case 4:
                point.x = -280;
                point.y = +170;
                break;
            case 5:
                point.x = -280;
                point.y = -120;
                break;
        }
        return point;
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
        bottom.setId("front");
        top.setId("back");

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
        if (i == 0)
            x = player.getPosition().getX() - 40;
        else if (i == 1)
            x = player.getPosition().getX() - 20;
        else if (i == 2)
            x = player.getPosition().getX();
        else if (i == 3)
            x = player.getPosition().getX() + 20;
        else if (i == 4)
            x = player.getPosition().getX() + 40;

        // Play animation here
        Path path = new Path();
        path.getElements().add(new MoveTo(466, 140));
        path.getElements().add(new LineTo(x, player.getPosition().getY() - (bottom.getImage().getHeight() / 2)));
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
        Deck.getInstance().reInitDeck();
        // Each player getting five cards
        for (int i = 0; i < 5; i++) {
            for (Player player : players)
            {
                Card randCard = Deck.getInstance().getRandomCard();
                player.addCardOnHand(randCard);
            }
        }

        for (Player player : players)
        {
            player.getCardOnHand().sortCard();
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
                latestDecision = player.bettingTurn(availableOption);

                int betValue = 0;
                Random rn = new Random();
                int range = 0;
                int randomNum = 0;
                int previousBetBalance = 0;
                switch (latestDecision)
                {
                    case "bet" :
                        previousBetBalance = player.getBetBalance();
                        range = player.getScore();
                        randomNum =  rn.nextInt(range) + 1;

                        betValue = player.getBalance() * (( randomNum * 10 ));
                        betValue = betValue/100;

                        if (betValue > getLimitBet())
                            betValue = getLimitBet();

                        player.setBetBalance(previousBetBalance + betValue);
                        additionalAmount = player.getBetBalance() - previousBetBalance;
                        player.setBalance(player.getBalance() - additionalAmount);
                        System.out.println(player.getName() + " make a bet");
                        break;
                    case "raise" :
                        previousBetBalance = player.getBetBalance();
                        range = player.getScore();
                        randomNum =  rn.nextInt(range) + 1;

                        int current = player.getBalance() - highestBetBalance;
                        if (current > 0)
                        {
                            betValue = current * (( randomNum * 10 ));
                            betValue = betValue/100;
                        }
                        else
                        {
                            betValue = 0;
                        }

                        //current * bet
                        if (betValue > getLimitBet()) {
                            betValue = getLimitBet();
                        }

                        System.out.println("=======================================");
                        System.out.println("bet value is raise : " + (highestBetBalance+betValue));
                        System.out.println("=======================================");
                        player.setBetBalance(highestBetBalance + betValue);
                        additionalAmount = player.getBetBalance() - previousBetBalance;
                        player.setBalance(player.getBalance() - additionalAmount);
                        System.out.println(player.getName() + " make a raise");
                        break;
                    // See bet balance of previous active player
                    // to determine amount
                    case "call" :
                        previousBetBalance = player.getBetBalance();
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
                    Thread.sleep(1000);
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

                // deal new card
                for (int i = 0; i < selectedCard.size(); i++)
                {
                    Card randCard = Deck.getInstance().getRandomCard();
                    player.addCardOnHand(randCard);
                }
                player.cardOnHand.sortCard();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //subState++;
                state = "discardCard";
            }
            // Player's turn
            else  {
                System.out.println("+++++++++++++++++DRAWING++++++++++++++++++++++");
//                controlButton(true);
//                redrawBtn.setDisable(false);
//                endTurnBtn.setDisable(false);
                state = "pause";
            }
        }
        // if not use newDraw, must add one to substate to go to next player
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
            if (player.isActive())
            {
                numberOfActivePlayer++;
                winner = player;
            }
        }

        if (numberOfActivePlayer > 1)
        {
            winner = null;
            return false;
        }
        else
        {
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

    public static Pane getPane()
    {
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

    // Return min bet
    private int getLimitBet()
    {
        int min = 10000;
        for (Player player : players)
        {
            if (player.isActive())
            {
                if (player.getBalance() < min)
                    min = player.getBalance();
            }
        }
        return min;
    }


}
