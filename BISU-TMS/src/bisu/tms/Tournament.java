
package bisu.tms;

public class Tournament {

    public final int id;
    public final String name;
    public final String sport;
    public final String status;

    public Tournament(int id, String name, String sport, String status) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " (" + sport + ")";
    }
}
