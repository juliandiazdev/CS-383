import edu.princeton.cs.algs4.In;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

import java.util.ArrayList;

/**
 * Dumb AI that picks the highest-scoring one-tile move. Plays a two-tile move on the first turn. Exchanges all of its
 * letters if it can't find any other move.
 */
public class PlayerOne implements ScrabbleAI {

    /** When exchanging, always exchange everything. */
    private static final boolean[] ALL_TILES = {true, true, true, true, true, true, true};
    // This array is the same size as the hand

    /** The GateKeeper through which this Incrementalist accesses the Board. */
    private GateKeeper gateKeeper;
    // Initializing an object from the GateKeeper Class

    @Override
    public void setGateKeeper(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }
    // Setting the gatekeeper for the next move

    @Override
    public ScrabbleMove chooseMove() {
        if (gateKeeper.getSquare(Location.CENTER) == Board.DOUBLE_WORD_SCORE) {
            return findTwoTileMove(); // Look for words
        }
        return findOneTileMove();
    }

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

    /** Not given */
    private static boolean check_word(HashSet<Character> board, ArrayList<Character> hand, String word){

        boolean on_board = false;
        int i, board_char_index = -1;

        for(i = 0; i < word.length(); i++){ // loop through char in words
            if(board.contains(word.charAt(i))){ // if one letter is shared in the board, word can possibly be put on board
                on_board = true;
                board_char_index = i; // word letter is at i in the word
                break;
            }
        }
        int hand_relation = 0;
        if(on_board) {
            for (i = 0; i < word.length(); i++) { // check if hand can fill in the rest of the word
                if (hand.contains(word.charAt(i)) && i != board_char_index) hand_relation++;
                if (hand_relation == word.length() - 1) return true; // hand can be filled in with one letter on board and rest from hand
            }
        }
        return false;
    }

    /** Also not given */
    private static ArrayList<String> check_dict(HashSet<Character> b, ArrayList<Character> hand){

        ArrayList<String> w = new ArrayList<>();
        In in = new In("words.txt");
        for (String word : in.readAllLines()) {// reads through entire text
            if(check_word(b, hand, word)){ // if its a possible word, add to array
                w.add(word);
            }
        }
        return w;
    }

    /** This is necessary for the first turn, as one-letter words are not allowed. */
    private ScrabbleMove findTwoTileMove() {
        ArrayList<Character> hand = gateKeeper.getHand();
        String bestWord = null;
        int bestScore = -1;
        for (int i = 0; i < hand.size(); i++) {
            for (int j = 0; j < hand.size(); j++) {
                if (i != j) {
                    try {
                        char a = hand.get(i);
                        if (a == '_') {
                            a = 'E'; // This could be improved slightly by trying all possibilities for the blank
                        }
                        char b = hand.get(j);
                        if (b == '_') {
                            b = 'E'; // This could be improved slightly by trying all possibilities for the blank
                        }
                        String word = "" + a + b;
                        gateKeeper.verifyLegality(word, Location.CENTER, Location.HORIZONTAL);
                        int score = gateKeeper.score(word, Location.CENTER, Location.HORIZONTAL);
                        if (score > bestScore) {
                            bestScore = score;
                            bestWord = word;
                        }
                    } catch (IllegalMoveException e) {
                        // It wasn't legal; go on to the next one
                    }
                }
            }
        }
        if (bestScore > -1) {
            return new PlayWord(bestWord, Location.CENTER, Location.HORIZONTAL);
        }
        return new ExchangeTiles(ALL_TILES);
    }
    /**
     * Technically this tries to make a two-letter word by playing one tile; it won't find words that simply add a
     * tile to the end of an existing word.
     */
    private ScrabbleMove findOneTileMove() {
        ArrayList<Character> hand = gateKeeper.getHand();
        PlayWord bestMove = null;
        HashSet<Character> b = consider_board(); // creates set of letters from board
        ArrayList<String> w = check_dict(b, gateKeeper.getHand()); // adds possible words to w
        System.out.println("size of possible words: " + w.size());
        int bestScore = -1;
        for (int i = 0; i < hand.size(); i++) {
            char c = hand.get(i);
            if (c == '_') {
                c = 'E'; // This could be improved slightly by trying all possibilities for the blank
            }
            for(String word : w){
                System.out.println("testing "+ word);
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
        if (bestMove != null) {
            return bestMove;
        }
        return new ExchangeTiles(ALL_TILES);
    }

}
