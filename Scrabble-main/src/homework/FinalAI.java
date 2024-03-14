package homework;
/** Fionn Ensor-McDermott, Kaelan Spann, Julian Diaz, Graehm Alberty */

import scrabble.*;
import edu.princeton.cs.algs4.*;
import java.util.*;

/**
 * Based on the Dumb AI that picks the highest-scoring one-tile move. Plays a two-tile move on the first turn. Exchanges all of its
 * letters if it can't find any other move.
 *
 * Goal of this AI is to check number of vowels in hand, then if vowels > half the hand, play the highest value combination
 * If vowels < half the hand, play the highest value with the fewest vowels that is above the mean of hand values
 * If neither possible, just play something.
 */
public class FinalAI implements ScrabbleAI {

    // This is a value to limit how many words it will check so to not take too much time
    private static int timeComplexity = 100;

    /**
     * When exchanging, always exchange everything.
     */
    private static final boolean[] ALL_TILES = {true, true, true, true, true, true, true};

    /**
     * The GateKeeper through which this Incrementalist accesses the Board.
     */
    private GateKeeper gateKeeper;

    @Override
    public void setGateKeeper(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }

    @Override
    public ScrabbleMove chooseMove() {
        if (gateKeeper.getSquare(Location.CENTER) == Board.DOUBLE_WORD_SCORE) {
            //return findTwoTileMove();
            return multyTileMove();
        }
        //return findOneTileMove();
        return multyTileMove();
    }

    /**
     * Technically this tries to make a two-letter word by playing one tile; it won't find words that simply add a
     * tile to the end of an existing word.
     */
    private ScrabbleMove findMove() {
        ArrayList<Character> hand = gateKeeper.getHand();
        PlayWord bestMove = null;
        HashSet<Character> b = consider_board(); // creates set of letters from board
        ArrayList<String> w = checkWordlist(b, gateKeeper.getHand()); // adds possible words to w
        System.out.println("Number of possible words: " + w.size());
        int bestScore = -1;

        for (int i = 0; i < hand.size(); i++) {
            char c = hand.get(i);
            if (c == '_') {
                c = 'E'; // This could be improved slightly by trying all possibilities for the blank
            }
            if (w.size() > timeComplexity) {
                for (String word : new String[]{c + " ", " " + c}) {
                    for (int row = 0; row < Board.WIDTH; row++) {
                        for (int col = 0; col < Board.WIDTH; col++) {
                            Location location = new Location(row, col);
                            for (Location direction : new Location[]{Location.HORIZONTAL, Location.VERTICAL}) {
                                try {
                                    gateKeeper.verifyLegality(word, location, direction);
                                    int score = gateKeeper.score(word, location, direction);
                                    if (score > bestScore) {
                                        bestScore = score;
                                        bestMove = new PlayWord(word, location, direction);
                                    }
                                } catch (IllegalMoveException e) {
                                    // It wasn't legal; go on to the next one
                                }
                            }
                        }
                    }
                }
            } else {
                for (String word : w) {
                    //System.out.println("testing "+ word);
                    for (int row = 0; row < Board.WIDTH; row++) {
                        for (int col = 0; col < Board.WIDTH; col++) {
                            Location location = new Location(row, col);
                            for (Location direction : new Location[]{Location.HORIZONTAL, Location.VERTICAL}) {
                                try {
                                    gateKeeper.verifyLegality(word, location, direction);
                                    int score = gateKeeper.score(word, location, direction);
                                    if (score > bestScore) {
                                        bestScore = score;
                                        bestMove = new PlayWord(word, location, direction);
                                    }
                                } catch (IllegalMoveException e) {
                                    // It wasn't legal; go on to the next one
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestMove != null) {
            return bestMove;
        }

        return new ExchangeTiles(ALL_TILES);
    }

    /**
     * Attempts to play multiple tiles on the Scrabble board, considering all possibilities for blank tiles.
     * If successful, returns the best move based on the current state of the game.
     * If playing two tiles fails, falls back on the optimal move for playing a single tile.
     *
     * @return The best move determined based on the current state of the game.
     */
    private ScrabbleMove multyTileMove() {
        ArrayList<Character> hand = gateKeeper.getHand();
        PlayWord bestMove = null;
        int bestScore = -1;

        // Try playing multiple tiles with varying lengths
        for (int length = 1; length <= hand.size(); length++) {
            for (int startIndex = 0; startIndex <= hand.size() - length; startIndex++) {
                String word = buildWord(hand, startIndex, length);
                for (int row = 0; row < Board.WIDTH; row++) {
                    for (int col = 0; col < Board.WIDTH; col++) {
                        Location location = new Location(row, col);
                        for (Location direction : new Location[]{Location.HORIZONTAL, Location.VERTICAL}) {
                            try {
                                gateKeeper.verifyLegality(word, location, direction);
                                int score = gateKeeper.score(word, location, direction);
                                if (score > bestScore) {
                                    bestScore = score;
                                    bestMove = new PlayWord(word, location, direction);
                                }
                            } catch (IllegalMoveException e) {
                                // It wasn't legal; go on to the next one
                            }
                        }
                    }
                }
            }
        }

        // If no valid move was found, fall back on singleTileMoveOptimal
        if (bestMove == null) {
            //return singleTileMoveOptimal();
            return findMove();
        }

        return bestMove;
    }

    /**
     * Builds a word of a specified length starting from a given index in the hand.
     * Considers all possibilities for blank tiles in the word.
     *
     * @param hand       The current hand of tiles.
     * @param startIndex The starting index in the hand.
     * @param length     The length of the word to build.
     * @return The constructed word with all possibilities for blank tiles considered.
     */
    private String buildWord(ArrayList<Character> hand, int startIndex, int length) {
        StringBuilder wordBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char currentTile = hand.get(startIndex + i);

            if (currentTile == '_') {
                // Try all possibilities for the blank tile
                for (char possibleBlank = 'A'; possibleBlank <= 'Z'; possibleBlank++) {
                    wordBuilder.append(possibleBlank);
                }
            } else {
                wordBuilder.append(currentTile);
            }
        }

        return wordBuilder.toString();
    }

    /**
     * Evaluates the legality and scores of a given word at various locations and directions on the Scrabble board.
     * Updates the best move and score based on the evaluation.
     *
     * @param word The word to be evaluated.
     */
    private PlayWord evaluateMove(String word) {
        PlayWord bestMove = null;
        int bestScore = -1;

        for (int row = 0; row < Board.WIDTH; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                Location location = new Location(row, col);

                for (Location direction : new Location[]{Location.HORIZONTAL, Location.VERTICAL}) {
                    try {
                        gateKeeper.verifyLegality(word, location, direction);
                        int score = gateKeeper.score(word, location, direction);

                        if (score > bestScore) {
                            bestScore = score;
                            bestMove = new PlayWord(word, location, direction);
                        }
                    } catch (IllegalMoveException e) {
                        // It wasn't legal; go on to the next one
                    }
                }
            }
        }

        return bestMove;
    }

    /**
     * This is the other code
     */
    private HashSet<Character> consider_board(){

        // add hand letters
        HashSet<Character> s = new HashSet<>();

        for (int row = 0; row < Board.WIDTH; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                Location location = new Location(row, col);
                if(location.isOnBoard()) s.add(gateKeeper.getSquare(location));
            }
        }

        return s;
    }

    /** Tests if the word from the wordlist can be played by looping through the characters */
    private static boolean testWord(HashSet<Character> board, ArrayList<Character> hand, String word) {

        boolean inPlay = false ;
        int i, index = -1 ;

        for(i = 0; i < word.length(); i++) { // loop through char in words
            if(board.contains(word.charAt(i))) { // if one letter is shared in the board, word can possibly be put on board
                inPlay = true ;
                index = i ; // word letter is at i in the word
                break ;
            }
        }

        int inHand = 0 ;
        if(inPlay) {
            for (i = 0; i < word.length(); i++) { // check if hand can fill in the rest of the word
                if (hand.contains(word.charAt(i)) && i != index) inHand++ ;
                if (inHand == word.length() - 1) return true ; // hand can be filled in with one letter on board and rest from hand
            }
        }

        return false ;
    }

    /** Checks if the word is a possible option in the wordlist */
    private static ArrayList<String> checkWordlist(HashSet<Character> b, ArrayList<Character> hand) {

        ArrayList<String> w = new ArrayList<>() ;
        In in = new In("words.txt") ;

        for (String word : in.readAllLines()) {// reads through entire text
            if(testWord(b, hand, word)) { // if it's a possible word, add to array
                w.add(word) ;
            }
        }

        return w ;
    }
}
