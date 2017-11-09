import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


import java.awt.*;
import java.util.ArrayList;
import java.util.TreeSet;

public class CardOnHand {
    private static final double CARD_POSITION = 20;

    private ArrayList<Card> cards = new ArrayList<Card>();

    public void addCard(Card card, Point position, boolean visibility) {
        cards.add(card);

        // always create both side of card
        ImageView bottom = new ImageView(new Image("img/" + card.getCardLetter() + "_" + card.getCardType() + ".png"));
        ImageView top = new ImageView(new Image("img/back_card.png"));

        if (visibility) // for player
            top.setVisible(false);
        else
            bottom.setVisible(false);

        Group blend = new Group(
                bottom,
                top
        );

        HBox layout = new HBox();
        layout.getChildren().addAll(
                //new ImageView(new Image("img/" + card.getCardLetter() + "_" + card.getCardType() + ".png")),
                blend
                //new ImageView(new Image("img/" + card.getCardLetter() + "_" + card.getCardType() + ".png"))
        );
        //layout.setPadding(new Insets(10));


        ImageView imageView = new ImageView();
        Image image = new Image("img/" + card.getCardLetter() + "_" + card.getCardType() + ".png");
        imageView.setImage(image);

        // Find position by calculating from number of element in cards
        double x = 0;
        if (cards.size() == 1) {
            x = position.getX() - 40;
        } else if (cards.size() == 2) {
            x = position.getX() - 20;
        } else if (cards.size() == 3) {
            x = position.getX();
        } else if (cards.size() == 4) {
            x = position.getX() + 20;
        } else if (cards.size() == 5) {
            x = position.getX() + 40;
        }

        // Play animation here
        Path path = new Path();
        path.getElements().add(new MoveTo(466,140));
        path.getElements().add(new LineTo(x, position.getY() - (image.getHeight()/2)));
        PathTransition pathTransition = new PathTransition();
        //pathTransition.setDuration(Duration.millis(1000));
        pathTransition.setPath(path);
        pathTransition.setNode(layout);
        pathTransition.play();

        Game.getPane().getChildren().add(layout);
    }

    public void removeCard(Card card) {
        cards.remove(card);
    }

    public void printAll() {
        for (Card card : cards) {
            System.out.println(card.toString());
        }
    }


}
