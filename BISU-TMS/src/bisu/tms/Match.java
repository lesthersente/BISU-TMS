
package bisu.tms;

public class Match {

    public final int id;
    public final int tournamentId;
    public final int round;
    public final int matchNumber;

    public final Integer teamA;
    public final Integer teamB;

    public final Integer scoreA;
    public final Integer scoreB;
    public final Integer winner;

    public final String status;

    public Match(
            int id,
            int tournamentId,
            int round,
            int matchNumber,
            Integer teamA,
            Integer teamB,
            Integer scoreA,
            Integer scoreB,
            Integer winner,
            String status) {

        this.id = id;
        this.tournamentId = tournamentId;
        this.round = round;
        this.matchNumber = matchNumber;

        this.teamA = teamA;
        this.teamB = teamB;

        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.winner = winner;

        this.status = status;
    }
}
