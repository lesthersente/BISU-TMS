package bisu.tms;

public class Team {
    public final int id;
    public final String name, sport, type, coach;

    public Team(int id, String name, String sport, String type, String coach) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.type = type;
        this.coach = coach;
    }

    @Override
    public String toString() {
        return id + " - " + name + " (" + type + ")";
    }
}
