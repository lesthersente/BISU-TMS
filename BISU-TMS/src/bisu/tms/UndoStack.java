package bisu.tms;

import java.util.Stack;

/** LIFO history of results recorded during this running session. */
public class UndoStack {
    public static class Action {
        public final int tournamentId, matchId, round;
        public Action(int tournamentId, int matchId, int round) {
            this.tournamentId = tournamentId;
            this.matchId = matchId;
            this.round = round;
        }
    }

    private final Stack<Action> actions = new Stack<>();

    public void push(Action action) { actions.push(action); }
    public Action peek() { return actions.isEmpty() ? null : actions.peek(); }
    public Action pop() { return actions.isEmpty() ? null : actions.pop(); }
    public int size() { return actions.size(); }
    public boolean isEmpty() { return actions.isEmpty(); }
    public void clear() { actions.clear(); }
}
