package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.collections.ObservableArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private Pile sourcePile;

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        Card card = (Card) e.getSource();
        sourcePile = card.getContainingPile();
        if (!card.isFaceDown()) {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
        }
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (!card.isFaceDown()) {
            if (activePile.getPileType() == Pile.PileType.STOCK)
                return;
            double offsetX = e.getSceneX() - dragStartX;
            double offsetY = e.getSceneY() - dragStartY;

            draggedCards.clear();

            List<Card> cardsToDrag = FXCollections.observableArrayList();
            cardsToDrag = activePile.getCards();
            boolean needsToBeDragged = false;
            for (int i = 0; i < cardsToDrag.size(); i++) {
                if (cardsToDrag.get(i) == card) {
                    needsToBeDragged = true;
                }
                if (needsToBeDragged) {
                    draggedCards.add(cardsToDrag.get(i));

                    cardsToDrag.get(i).getDropShadow().setRadius(20);
                    cardsToDrag.get(i).getDropShadow().setOffsetX(10);
                    cardsToDrag.get(i).getDropShadow().setOffsetY(10);

                    cardsToDrag.get(i).toFront();
                    cardsToDrag.get(i).setTranslateX(offsetX);
                    cardsToDrag.get(i).setTranslateY(offsetY);
                }
            }
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        List<Pile> allPiles = foundationPiles;
        allPiles.addAll(tableauPiles);
        Pile foundationPile = getValidIntersectingPile(card, allPiles);
        if (foundationPile == null) {
            foundationPile = card.getContainingPile();
        }

        if (isMoveValid(card, foundationPile)) {
            handleValidMove(card, foundationPile);
            if (sourcePile.getCards().size() - draggedCards.size() > 0){
                if (sourcePile.getNthCard(draggedCards.size()).isFaceDown()) {
                    sourcePile.getNthCard(draggedCards.size()).flip();
                }
            }
        }
        else {
            draggedCards.forEach(MouseUtil::slideBack);
        }
        draggedCards.clear();
    };

    public boolean isGameWon() {
        for (int i = 0; i < tableauPiles.size(); i++) {
            if (tableauPiles.get(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        for(int i = discardPile.numOfCards(); i > 0; i--){
            discardPile.getTopCard().moveToPile(stockPile);
            stockPile.getTopCard().flip();
        }
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType().equals(Pile.PileType.FOUNDATION)) {
            if (destPile.numOfCards() == 0 && card.getRank().getRank() == 1) {
                return true;
            }

            Card destPileLastCard = destPile.getTopCard();

            if (destPileLastCard.getRank().getRank() + 1 == card.getRank().getRank()) {
                return true;
            }
        }
        else if (destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            Card destPileLastCard = destPile.getTopCard();

            if (destPile.numOfCards() == 0) {
                if (card.getRank().getRank() != 13) {
                    return false;
                }
                else {
                    return true;
                }
            }

            Card.CardRank rank = destPileLastCard.getRank();

            if (Card.isOppositeColor(card, destPileLastCard) && rank.getRank() == (card.getRank().getRank() + 1)) {
                return true;
            }
        }

        return false;
    }
    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) && isOverPile(card, pile) && isMoveValid(card, pile)) {
                result = pile;
                break;
            }
        }
        isGameWon();
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        Card.CardRank rank = card.getRank();
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION) && Card.sortFoundationPile(card, destPile.getTopCard())) {
                msg = String.format("Placed %s to the foundation.", card);
                MouseUtil.slideToDest(draggedCards, destPile);
            }
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU) && card.getRank().equals(Card.CardRank.KING)) {
                msg = String.format("Placed %s to a new pile.", card);
                MouseUtil.slideToDest(draggedCards, destPile);
            } else {
                draggedCards.forEach(MouseUtil::slideBack);
            }
        } else if (rank.getRank() == 13) {
            MouseUtil.slideToDest(draggedCards, destPile);
        }
        else if (!destPile.isEmpty() && Card.CardRank.values()[rank.ordinal() + 1] == destPile.getTopCard().getRank()) {
            MouseUtil.slideToDest(draggedCards, destPile);
        }
        else if(destPile.getPileType().equals(Pile.PileType.FOUNDATION) && Card.sortFoundationPile(card, destPile.getTopCard())) {
            msg = String.format("Placed %s to the foundation.", card);
            MouseUtil.slideToDest(draggedCards, destPile);
        }
        else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
            draggedCards.forEach(MouseUtil::slideBack);
        }
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        int cardCounter = 0;
        for (int i = 0; i < tableauPiles.size(); i++) {
            for (int j = 7 - i; j < 8; j++) {
                tableauPiles.get(i).addCard(deck.get(cardCounter));
                addMouseEventHandlers(deck.get(cardCounter));
                getChildren().add(deck.get(cardCounter));
                cardCounter++;
            }
            tableauPiles.get(i).getTopCard().flip();
        }

        for (int i = deck.size() - 1; i > 27; i--) {
            stockPile.addCard(deck.get(i));
            addMouseEventHandlers(deck.get(i));
            getChildren().add(deck.get(i));
            cardCounter++;
        }
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
