/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.Collections;
import java.util.List;
import static loa.Piece.*;

/** An automated Player.
 *  @author Abel Feleke
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /**
     * Copies board and makes move.
     * @param board game board
     * @param move a move applied to the board
     * @return the new board
     */
    Board pointerBoard(Board board, Move move) {
        Board b = new Board();
        b.copyFrom(board);
        b.makeMove(move);
        return b;
    }

    /**
     * Measure of how well AI is performing.
     * @param board gameboard
     * @return measurement
     */
    int heuristic(Board board) {
        if (board.gameOver()) {
            if (board.winner() == side()) {
                return WINNING_VALUE;
            }
            if (board.winner() == side().opposite()) {
                return -WINNING_VALUE;
            }
        }
        int count = 0;
        int numP = board.numPieces(side());
        int opponentNumP = board.numPieces(side().opposite());
        List<Integer> seq = board.getRegionSizes(side());
        List<Integer> opponentSeq = board.getRegionSizes(side().opposite());
        int measure = Collections.max(seq);
        int opponentMeasure = Collections.max(opponentSeq);
        if (seq.size() > opponentSeq.size()) {
            count += 1;
        }
        if (measure > opponentMeasure) {
            count += 1;
        }
        if (numP > opponentNumP) {
            count += 1;
        }
        return count;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0) {
            return heuristic(board);
        }
        int winningScore;
        if (sense == 1) {
            winningScore = -INFTY;
        } else {
            winningScore = INFTY;
        }
        List<Move> movesList = board.legalMoves();
        for (Move move : movesList) {
            Board nextBoard = pointerBoard(board, move);
            int  currScore = findMove(nextBoard, depth - 1,
                    false, sense * -1, alpha, beta);
            if (sense == 1) {
                alpha = max(currScore, alpha);
                if (currScore > winningScore) {
                    winningScore = currScore;
                    if (saveMove) {
                        _foundMove = move;
                    }
                }
            } else {
                beta = min(currScore, beta);
                if (currScore < winningScore) {
                    winningScore = currScore;
                    if (saveMove) {
                        _foundMove = move;
                    }
                }
            }
            if (alpha >= beta) {
                break;
            }
        }
        return winningScore;
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 4;
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

}
