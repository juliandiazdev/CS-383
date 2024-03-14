package homework;

import scrabble.*;

import java.util.ArrayList;

/**
 * Based on the Dumb AI that picks the highest-scoring one-tile move. Plays a two-tile move on the first turn. Exchanges all of its
 * letters if it can't find any other move.
 *
 * Goal of this AI is to check number of vowels in hand, then if vowels > half the hand, play the highest value combination
 * If vowels < half the hand, play the highest value with the fewest vowels that is above the mean of hand values
 * If neither possible, just play something.
 */
public class BestestBoy implements ScrabbleAI {

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
            //return twoTileMoveOptimal();
            return multyTileMove();
        }
        //return findOneTileMove();
        //return singleTileMoveOptimal();
        return multyTileMove();
    }

    /**
     * This is necessary for the first turn, as one-letter words are not allowed.
     */
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
     * Makes an optimal move when playing two tiles in Scrabble.
     *
     * @return bestMove based on current state of the game
     */
    private ScrabbleMove twoTileMoveOptimal() {
        ArrayList<Character> hand = gateKeeper.getHand();
        PlayWord bestMove = null;
        String bestWord = null;
        int bestScore = -1;

        for (int i = 0; i < hand.size(); i++) {
            for (int j = 0; j < hand.size(); j++) {
                if (i != j) {
                    try {
                        char a = hand.get(i);
                        if (a == '_') {
                            // Designed to loop through all possible tiles, but since it is only if '_'
                            // we moved on to not waste too much time.
                            for (char possibleBlank = 'A'; possibleBlank <= 'Z'; possibleBlank++) {
                                String word = "" + possibleBlank;
                                PlayWord currentMove = evaluateMove(word);
                            }
                        }
                        char b = hand.get(j);
                        if (b == '_') {
                            // Designed to loop through all possible tiles, but since it is only if '_'
                            // we moved on to not waste too much time.
                            for (char possibleBlank = 'A'; possibleBlank <= 'Z'; possibleBlank++) {
                                String word = "" + possibleBlank;
                                PlayWord currentMove = evaluateMove(word);
                            }
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
        int bestScore = -1;
        for (int i = 0; i < hand.size(); i++) {
            char c = hand.get(i);
            if (c == '_') {
                c = 'E'; // This could be improved slightly by trying all possibilities for the blank
            }

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
        }
        if (bestMove != null) {
            return bestMove;
        }
        return new ExchangeTiles(ALL_TILES);
    }

    /**
     * Makes an optimal move when playing a single tile in Scrabble.
     *
     * @return bestMove based on current state of the game
     */
    private ScrabbleMove singleTileMoveOptimal() {
        ArrayList<Character> hand = gateKeeper.getHand();
        PlayWord bestMove = null;
        int bestScore = -1;

        for (int i = 0; i < hand.size(); i++) {
            char c = hand.get(i);

            if (c == '_') {
                // Designed to loop through all possible tiles, but since it is only if '_'
                // we moved on to not waste too much time.
                for (char possibleBlank = 'A'; possibleBlank <= 'Z'; possibleBlank++) {
                    String word = "" + possibleBlank;
                    PlayWord currentMove = evaluateMove(word);
                }
            } else {
                String word = "" + c;
                PlayWord currentMove = evaluateMove(word);
            }

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
        }

        if (bestMove != null) {
            return bestMove;
        }

        return new ExchangeTiles(ALL_TILES);
    }

    // helper function for running through and testing tiles
    // not in use
    private char bestChar() {
        ArrayList<Character> hand = gateKeeper.getHand();
        char c = 'E';
        PlayWord bestMove = null;
        int bestScore = -1;
        for (char possibleBlank = 'A'; possibleBlank <= 'Z'; possibleBlank++) {
            String word = "" + possibleBlank;
            PlayWord currentMove = evaluateMove(word);
        }

        return 'c';
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
            return findOneTileMove();
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
}
