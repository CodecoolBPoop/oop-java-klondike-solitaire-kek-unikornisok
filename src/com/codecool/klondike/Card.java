package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private CardSuit suit;
    private CardRank rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(CardSuit suit, CardRank rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public CardSuit getSuit() {
        return suit;
    }

    public CardRank getRank() {
        return rank;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "" + suit + rank;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + rank + " of " + suit;
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        if (card2 == null) {
            return true;
        }

        if (card1.suit == CardSuit.HEARTS && card2.suit == CardSuit.DIAMONDS || card1.suit == CardSuit.DIAMONDS && card2.suit == CardSuit.HEARTS || card1.suit == card2.suit) {
            return false;
        } else if (card1.suit == CardSuit.SPADES && card2.suit == CardSuit.CLUBS || card1.suit == CardSuit.CLUBS && card2.suit == CardSuit.SPADES || card1.suit == card2.suit) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();

        for (CardSuit suit : CardSuit.values()) {
            for (CardRank rank : CardRank.values()) {
                result.add(new Card(suit, rank, true));
            }
        }
        Collections.shuffle(result);
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/Mojipower_Blue-Unicorn_1.jpg");
        String suitName = "";
        String rankName = "";
        for (CardSuit suit : CardSuit.values()) {
            switch (suit) {
                case HEARTS:
                    suitName = "hearts";
                    break;
                case DIAMONDS:
                    suitName = "diamonds";
                    break;
                case SPADES:
                    suitName = "spades";
                    break;
                case CLUBS:
                    suitName = "clubs";
                    break;
            }
            for (CardRank rank : CardRank.values()) {
                switch (rank) {
                    case ACE:
                        rankName = "1";
                        break;
                    case TWO:
                        rankName = "2";
                        break;
                    case THREE:
                        rankName = "3";
                        break;
                    case FOUR:
                        rankName = "4";
                        break;
                    case FIVE:
                        rankName = "5";
                        break;
                    case SIX:
                        rankName = "6";
                        break;
                    case SEVEN:
                        rankName = "7";
                        break;
                    case EIGHT:
                        rankName = "8";
                        break;
                    case NINE:
                        rankName = "9";
                        break;
                    case TEN:
                        rankName = "10";
                        break;
                    case JACK:
                        rankName = "11";
                        break;
                    case QUEEN:
                        rankName = "12";
                        break;
                    case KING:
                        rankName = "13";
                        break;
                }
                String cardName = suitName + rankName;
                String cardId = "" + suit + rank;
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }


        }
    public static boolean sortFoundationPile(Card card1, Card card2) {
        CardRank rank = card1.getRank();
        if (card2 == null && card1.getRank() == CardRank.ACE) {
            return true;
        }
        if(card2 != null && CardRank.values()[rank.ordinal() - 1] == card2.getRank() && card1.getSuit() == card2.getSuit()) {
            return true;
        }
        else {
            return false;
        }
    }

    public enum CardSuit {
        HEARTS,
        DIAMONDS,
        SPADES,
        CLUBS
    }

    public enum CardRank {
        ACE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(11),
        QUEEN(12),
        KING(13);


        CardRank(int i) {
        }

    }

}
