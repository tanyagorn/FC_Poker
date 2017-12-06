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

    /** Container for GUI components */
    private static Pane pane;

    /** holding current state; critical for game's flow */
    private String state;

    /** holding which turn; critical for assigning turn to each player
     * subState reference to index in players [0,1,2,3,4] */
    private int subState;

    /** use in randomly find available seat */
    private List<Integer> availableSeat = new ArrayList<>(Arrays.asList(1,2,3,4,5));

    /** list of available options of betting turn */
    private List<String> availableOption = new ArrayList<>(Arrays.asList("fold","check","bet","call","raise"));

    /** holding x,y coordinates of betting tag position */
    private static HashMap<Integer, Point> bettingTagPosition = new HashMap<Integer, Point>();

    /** holding x,y coordinates of betting amount position */
    private static HashMap<Integer, Point> bettingAmountPosition = new HashMap<Integer, Point>();

    /** additional amount must be used to update balance for player*/
    private int additionalAmount;

    /** keep track when all player has already take a turn */
    private boolean round;

    /** keep track of drawing round is over or not to end game */
    private boolean drawOver;

    /** used for display amount of pool value */
    private Label poolLabel;

    /** Keep track which card is selected in drawing turn */
    private ArrayList<HBox> selectedCard = new ArrayList<HBox>();

    /** Keep track of cards that soon will be removed from pane */
    ArrayList<Node> nodes = new ArrayList<Node>();

    /** GUI components loading from .FXML file */
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

    /**
     * Constructor of game instance, create all player instances
     * and initialize all value needed to control game's flow
     * @param playerName        name of actual player
     * @param playerCount       number of all players
     * @param seat              seat of actual player
     * @param location          seat position of actual player
     */
    public Game(String playerName, int playerCount, int seat, Point location)
    {
        pane = new Pane();
        Scene scene = new Scene(pane, 1000, 800);

        // Load .fxml file to set scene
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Game.fxml"));
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


        // Initialize pool
        pool = new Pool();
        poolLabel = new Label("");
        poolLabel.setLayoutX(490);
        poolLabel.setLayoutY(350);
        pane.getChildren().add(poolLabel);

        setInitialState();

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
            player.setBetBalance(getHighestBetBalance());
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

        // Other thread to handle game's flow
        // There are two threads - one responsible for
        // updating UI, one for updating model
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                updateModel();
            }
        }).start();
    }

    /**
     * re-initialize state for new game
     */
    private void setInitialState()
    {
        state = "deal";
        subState = 0;
        roundLabel.setVisible(false);
        selectedCard.clear();
        drawOver = false;
        round = false;
        winners.clear();
        pool.setPool(0);
        controlButton(true);
    }

    /**
     * Check for terminate condition of betting turn
     * all active players must have same amount of bet balance
     * @return boolean indicates betting turn is over or not
     */
    private boolean isBettingOver()
    {
        int betBalance = 0;

        // This loop is for getting the initial betBalance to compare with others
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

    /**
     * Handle game's flow and update model data
     */
    private void updateModel()
    {
        // different state has different behavior
        switch (state)
        {
            case "deal":
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


            case "delayReturn":
                delayTime(500);
                break;

            case "delayDisplayCard":
                delayTime(3000);
                break;

            case "betting":
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

            case "draw":
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

            case "newGame":
                // Finish collect all cards, INIT NEW GAME
                if (subState == players.size())
                {
                    setInitialState();
                    dealCard();
                    for (Player player : players)
                    {
                        player.setActive(true);
                    }
                }
                break;
        }

        // Back to other thread to update UI, must call for this
        // thread to be used later, so we will always switch
        // between update model and update UI
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                redraw();
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateModel();
                    }
                }).start();
            }
        });
    }

    /**
     * Get highest bet balance for all player that still active in the game
     * used to determine bet value
     * @return highest bet balance of current active player
     */
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

    /**
     * Make UI consistent with game's flow (updateModel)
     */
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
                slider.setMax(getLowestBalance());
                break;

            // UpdateBalanceUI, also maybe display call
            case "call" :
                updateBalanceUI(players.get(subState), "CALL");
                state = "betting";
                subState++;
                System.out.println("CALL : subState is added to " + subState);
                slider.setMin(0);
                slider.setMax(getLowestBalance());
                break;

            case "bet" :
                updateBalanceUI(players.get(subState), "BET");
                state = "betting";
                subState++;
                System.out.println("BET : subState is added to " + subState);
                slider.setMin(0);
                slider.setMax(getLowestBalance());
                break;

            case "raise" :
                updateBalanceUI(players.get(subState), "RAISE");
                state = "betting";
                subState++;
                System.out.println("RAISE : subState is added to " + subState);
                slider.setMin(0);
                slider.setMax(getLowestBalance());
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
                updateCardOnHandUI(players.get(subState));
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
                poolLabel.setTranslateX(0);
                poolLabel.setTranslateY(0);
                state = "newGame";
                subState++;
                break;
        }

    }


    /**
     * play animation of discarding card to dealer, add soon to be removed
     * card to nodes array, so we can remove them later without knowing who
     * is the owner (we can get node position instead of player position)
     * @param player  owner of card
     */
    private void foldUI(Player player)
    {
        ArrayList<HBox> cards = player.getImgCards();
        for (int i = cards.size()-1; i > -1; i--)
        {
            for (Node node : pane.getChildren())
            {
                if (node.equals(cards.get(i)))
                {
                    cardToDealerAnimation(node);
                }
            }
        }

        // Change style of name tag for this player
        player.setNameTagInactive();
    }

    /**
     * remove all objects of card GUI from the pane
     * @param player  which player
     */
    private void updateCardOnHandUI(Player player)
    {
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

    /**
     * Call all cards back to dealer, remove data of cards for this player
     * @param player
     */
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

    /**
     * play animation of discarding card to dealer, add soon to be removed
     * card to nodes array, so we can remove them later without knowing who
     * is the owner (we can get node position instead of player position)
     */
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
                    cardToDealerAnimation(node);
                }
            }
        }
    }

    /**
     * Play animation of path from card's current position
     * to dealer's position
     * @param node  for getting current position of card
     */
    private void cardToDealerAnimation(Node node)
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

    /**
     * Enable selectable event in drawing turn.
     * when mouse is hovering over card object, the card will
     * glow highilight to indicate which card is selected
     */
    private void enableSelectableCard()
    {
        for (Player player : players)
        {
            if (!(player instanceof Bot))
            {
                for (HBox card : player.getImgCards())
                {
                    DropShadow dsHover = new DropShadow( 15, Color.rgb(0,0,255) );
                    card.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ) ->
                    {
                        if ( newValue )
                            card.setEffect(dsHover);
                        else
                            card.setEffect(null);
                    });

                    // set selectable only for player to use in drawing phase
                    if (!(player instanceof Bot))
                    {
                        card.setOnMouseEntered(new EventHandler<MouseEvent>()
                        {
                            @Override
                            public void handle(MouseEvent event)
                            {
                                card.requestFocus();
                            }
                        });
                        card.setOnMouseClicked(new EventHandler<MouseEvent>()
                        {
                            @Override
                            public void handle(MouseEvent event)
                            {
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

    /**
     * Mirror event flip card in reality, flip card for all players
     * to see their hand at the end of game
     */
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

    /**
     * Assign pool to the player with best hand at the end of game
     */
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

        // Play animation move from current position to the winner's position
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
        // TODO: in case there are multiple players win the pool
        else
        {

        }
    }

    /**
     * Must re-compute position to create path from pool's location
     * to winner's location because the location of pool is automatically
     * set to (0,0) due to JavaFx document
     * @param player    which player
     * @return coordinate x,y
     */
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

    /***
     * Update pool value
     */
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
        pool.setPool(poolValue);
        poolLabel.setText(Integer.toString(pool.getPool()));
    }

    /**
     * Update balance UI, display which action that player has chose
     * also the amount of bet money for that action
     * @param player  which player
     * @param action  which action
     */
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

    /**
     * Initialize card object and play animation dealer distribute
     * card for particular player
     * @param player    which player
     * @param i         which card
     */
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

    /**
     * Helper function preventing program from unknown crash
     * automatically add 1 to subState (just move to the next player)
     * @param player which player
     */
    private void bettingUI(Player player)
    {
        subState++;
    }

    /**
     *  Dealer deal card for every player
     */
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

        // card on hand must be sorted to be used to calculate
        // for rate of hand
        for (Player player : players)
        {
            player.getCardOnHand().sortCard();
        }
    }

    /**
     * Decision making to take a turn in betting round
     * the decision is depended on how best card on hand is
     * if rate is high, bot will be more likely to make higher bet
     * @param player    which player
     */
    private void bettingRound(Player player)
    {
        // check for active status, if inactive - don't do anything
        if (player.isActive())
        {
            // Setup available options
            controlButton(true);
            int highestBetBalance = getHighestBetBalance();
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

            // betting turn actually happen here
            if (player instanceof Bot)
            {
                controlButton(true);
                String latestDecision = player.bettingTurn(availableOption);

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

                        // calculate appropriated amount of bet money respect to
                        // how best of card on hand
                        betValue = player.getBalance() * (( randomNum * 10 ));
                        betValue = betValue/100;

                        if (betValue > getLowestBalance())
                            betValue = getLowestBalance();

                        player.setBetBalance(previousBetBalance + betValue);
                        additionalAmount = player.getBetBalance() - previousBetBalance;
                        player.setBalance(player.getBalance() - additionalAmount);
                        break;
                    case "raise" :
                        previousBetBalance = player.getBetBalance();
                        range = player.getScore();
                        randomNum =  rn.nextInt(range) + 1;
                        int current = player.getBalance() - highestBetBalance;

                        // calculate appropriated amount of bet money respect to
                        // how best of card on hand
                        if (current > 0)
                        {
                            betValue = current * (( randomNum * 10));
                            betValue = betValue/100;
                        }
                        else
                            betValue = 0;

                        if (betValue > getLowestBalance())
                            betValue = getLowestBalance();

                        player.setBetBalance(highestBetBalance + betValue);
                        additionalAmount = player.getBetBalance() - previousBetBalance;
                        player.setBalance(player.getBalance() - additionalAmount);
                        break;
                    // See bet balance of previous active player
                    // to determine amount
                    case "call" :
                        previousBetBalance = player.getBetBalance();
                        player.setBetBalance(highestBetBalance);
                        additionalAmount = player.getBetBalance() - previousBetBalance;
                        player.setBalance(player.getBalance() - additionalAmount);
                        break;
                    // Nothing happen to this player
                    case "check" :
                        if (round)
                            state = "cumulate";
                        else
                            state = "check";
                        break;
                    // Set active to false
                    // also the bet balance for this player will be loss
                    case "fold" :
                        player.setActive(false);
                        break;
                }

                // Mirror the reality by delay time for bot's making decision
                delayTime(1000);

                state = latestDecision;
            }
            // Player's turn : pause state waiting for any event trigger
            else
                state = "pause";
        }
    }

    /**
     * Decision making to take a turn in drawing round
     * cards to be discarded is obtained from calculating
     * by rating of card
     * @param player    which player
     */
    private void drawingRound(Player player)
    {
        // check for active status, if inactive - don't do anything
        if (player.isActive())
        {
            if (player instanceof Bot)
            {
                controlButton(true);
                selectedCard.clear();

                // Get list of to be discarded cards
                for (HBox card : player.drawingTurn())
                {
                    selectedCard.add(card);
                }

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

                // Mirror the reality by delay time for bot's making decision
                delayTime(1000);

                state = "discardCard";
            }
            // Player's turn
            else
                state = "pause";
        }
        // if not use newDraw, must add one to subState to go to next player
        // because normally newDraw will add one to subState
        else
            subState++;
    }

    /**
     * Check for any active player, if there is only one
     * active user remains in the game - that player is the winner
     * @return  true for getting winner or else return false
     */
    private boolean hasWinner()
    {
        int numberOfActivePlayer = 0;
        for (Player player : players)
        {
            if (player.isActive())
            {
                numberOfActivePlayer++;
                winners.add(player);
            }
        }

        if (numberOfActivePlayer > 1)
        {
            winners.clear();
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Get possible bet value, bet value must not
     * greater than the minimum current balance of all player
     * @return  lowest bet balance
     */
    private int getLowestBalance()
    {
        int lowestBalance = 3000;
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

    /**
     * Delay time
     * @param time  time in millisecond
     */
    private void delayTime(long time)
    {
        try
        {
            Thread.sleep(time);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Control visibility of all GUI components
     * @param bool
     */
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

    /**
     * randomly return available seat number
     * @return seat number
     */
    private int randomSeat()
    {
        int random = 0;
        random = (int)(Math.random() * (availableSeat.size()-1));
        return availableSeat.get(random);
    }

    /**
     * return pane
     * @return pane
     */
    public static Pane getPane()
    {
        return pane;
    }

    /**
     * get betting tag position for particular player
     * @param player    which player
     * @return coordinate x,y
     */
    public static Point getBettingTagPosition(Player player)
    {
        return bettingTagPosition.get(player.getSeat());
    }

    /**
     * get betting amount position for particular player
     * @param player    which player
     * @return coordinate x,y
     */
    public static Point getBettingAmountPosition(Player player)
    {
        return bettingAmountPosition.get(player.getSeat());
    }

}
