
package bisu.tms;

import java.sql.*;
import java.util.*;

/**
 * BISU Tournament Management System
 *
 * Handles competitors, tournaments, match results,
 * automatic advancement, and database operations.
 *
 * DATA STRUCTURES:
 * 1. HashMap - Competitor lookup
 * 2. Custom Linked Queue - Pending match processing
 * 3. Stack - Undo match results
 */
public class TournamentService {

    // DATA STRUCTURE 1: HASHMAP
    private final HashMap<Integer, Team> competitorIndex =
            new HashMap<>();

    // DATA STRUCTURE 2: MANUALLY IMPLEMENTED QUEUE
    private final CustomMatchQueue matchQueue =
            new CustomMatchQueue();

    // DATA STRUCTURE 3: STACK
    private final UndoStack undoStack =
            new UndoStack();

    private int selectedTournamentId = -1;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    public TournamentService() throws SQLException {
        ensureEntryTypeColumn();
        reloadCompetitors();
    }

    // ========================================
    // DATABASE COMPATIBILITY
    // ========================================

    private void ensureEntryTypeColumn()
            throws SQLException {

        try (Connection c = Database.getConnection();
             Statement st = c.createStatement()) {

            boolean exists = false;

            try (ResultSet rs = st.executeQuery(
                    "PRAGMA table_info(teams)")) {

                while (rs.next()) {
                    if ("entry_type".equalsIgnoreCase(
                            rs.getString("name"))) {
                        exists = true;
                    }
                }
            }

            if (!exists) {
                st.executeUpdate(
                    "ALTER TABLE teams ADD COLUMN " +
                    "entry_type TEXT NOT NULL DEFAULT 'TEAM'"
                );
            }
        }
    }

    // ========================================
    // HASHMAP: LOAD COMPETITORS
    // ========================================

    public void reloadCompetitors()
            throws SQLException {

        competitorIndex.clear();

        String sql =
            "SELECT id, name, sport, coach, entry_type " +
            "FROM teams ORDER BY id";

        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                Team team = new Team(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("sport"),
                    rs.getString("entry_type"),
                    rs.getString("coach")
                );

                competitorIndex.put(team.id, team);
            }
        }
    }

    // HASHMAP SEARCH
    public Team findCompetitor(int id) {
        return competitorIndex.get(id);
    }

    // DSA STATUS
    public int indexSize() {
        return competitorIndex.size();
    }

    public int queueSize() {
        return matchQueue.size();
    }

    public int undoSize() {
        return undoStack.size();
    }

    public int selectedTournamentId() {
        return selectedTournamentId;
    }

    public Match nextMatch() {
        return matchQueue.peek();
    }

    // ========================================
    // SEARCH AND LIST COMPETITORS
    // ========================================

    public List<Team> competitors(String query) {

        String q = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        List<Team> found = new ArrayList<>();

        if (!q.isEmpty()) {

            try {
                Team exact = competitorIndex.get(
                    Integer.parseInt(q)
                );

                if (exact != null) {
                    found.add(exact);
                    return found;
                }

            } catch (NumberFormatException ignored) {
            }
        }

        for (Team t : competitorIndex.values()) {

            if (q.isEmpty() ||
                t.name.toLowerCase(Locale.ROOT).contains(q) ||
                t.sport.toLowerCase(Locale.ROOT).contains(q) ||
                t.type.toLowerCase(Locale.ROOT).contains(q)) {

                found.add(t);
            }
        }

        found.sort(
            Comparator.comparingInt(t -> t.id)
        );

        return found;
    }

    // ========================================
    // VALIDATION
    // ========================================

    private void validateCompetitor(
            String name,
            String sport,
            String type) throws SQLException {

        if (name == null || name.trim().isEmpty() ||
            sport == null || sport.trim().isEmpty()) {

            throw new SQLException(
                "Name and sport/esport cannot be blank."
            );
        }

        if (!"TEAM".equals(type) &&
            !"INDIVIDUAL".equals(type)) {

            throw new SQLException(
                "Choose Team or Individual."
            );
        }
    }

    // ========================================
    // ADD COMPETITOR
    // ========================================

    public void addCompetitor(
            String name,
            String sport,
            String type,
            String coach) throws SQLException {

        validateCompetitor(name, sport, type);

        String sql =
            "INSERT INTO teams " +
            "(name, sport, coach, entry_type) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection c = Database.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, sport.trim());
            ps.setString(
                3, coach == null ? "" : coach.trim()
            );
            ps.setString(4, type);

            ps.executeUpdate();
        }

        reloadCompetitors();
    }

    // ========================================
    // EDIT COMPETITOR
    // ========================================

    public void editCompetitor(
            int id,
            String name,
            String sport,
            String type,
            String coach) throws SQLException {

        validateCompetitor(name, sport, type);

        Team old = findCompetitor(id);

        if (old == null) {
            throw new SQLException(
                "Competitor not found."
            );
        }

        if (!old.sport.equalsIgnoreCase(sport.trim()) ||
            !old.type.equals(type)) {

            try (Connection c = Database.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM tournament_teams " +
                    "WHERE team_id=?")) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next() && rs.getInt(1) > 0) {

                        throw new SQLException(
                            "Cannot change sport/type for an " +
                            "entrant already assigned to a tournament."
                        );
                    }
                }
            }
        }

        String sql =
            "UPDATE teams SET name=?, sport=?, " +
            "coach=?, entry_type=? WHERE id=?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, sport.trim());
            ps.setString(
                3, coach == null ? "" : coach.trim()
            );
            ps.setString(4, type);
            ps.setInt(5, id);

            ps.executeUpdate();
        }

        reloadCompetitors();
    }

    // ========================================
    // DELETE COMPETITOR
    // ========================================

    public void deleteCompetitor(int id)
            throws SQLException {

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM teams WHERE id=?")) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                    "Competitor not found."
                );
            }
        }

        reloadCompetitors();
    }

    // ========================================
    // SAMPLE COMPETITORS
    // ========================================

    public void loadSampleCompetitors()
            throws SQLException {

        for (int i = 1; i <= 8; i++) {

            String name = "Demo ML Squad " + i;

            boolean exists = false;

            for (Team team : competitorIndex.values()) {

                if (team.name.equalsIgnoreCase(name)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                addCompetitor(
                    name,
                    "Mobile Legends",
                    "TEAM",
                    "Demo Captain " + i
                );
            }
        }
    }

    // ========================================
    // LIST TOURNAMENTS
    // ========================================

    public List<Tournament> tournaments()
            throws SQLException {

        List<Tournament> list = new ArrayList<>();

        String sql =
            "SELECT id, name, sport, status " +
            "FROM tournaments ORDER BY id DESC";

        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                list.add(new Tournament(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("sport"),
                    rs.getString("status")
                ));
            }
        }

        return list;
    }

    // ========================================
    // SELECTED TOURNAMENT
    // ========================================

    public Tournament selectedTournament()
            throws SQLException {

        for (Tournament t : tournaments()) {

            if (t.id == selectedTournamentId) {
                return t;
            }
        }

        return null;
    }

    // ========================================
    // INSERT MATCH
    // ========================================

    private void insertMatch(
            Connection c,
            int tournamentId,
            int round,
            int number,
            int a,
            int b) throws SQLException {

        String sql =
            "INSERT INTO matches " +
            "(tournament_id, round, match_number, " +
            "team_a_id, team_b_id, status) " +
            "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (PreparedStatement ps =
                     c.prepareStatement(sql)) {

            ps.setInt(1, tournamentId);
            ps.setInt(2, round);
            ps.setInt(3, number);
            ps.setInt(4, a);
            ps.setInt(5, b);

            ps.executeUpdate();
        }
    }

    // ========================================
    // CREATE TOURNAMENT
    // ========================================

    public int createTournament(
            String name,
            String sport,
            List<Integer> ids) throws SQLException {

        if (name == null || name.trim().isEmpty() ||
            sport == null || sport.trim().isEmpty()) {

            throw new SQLException(
                "Enter a tournament name and sport."
            );
        }

        if (ids == null ||
            (ids.size() != 4 && ids.size() != 8) ||
            new HashSet<>(ids).size() != ids.size()) {

            throw new SQLException(
                "Select exactly 4 or 8 different entrants."
            );
        }

        String type = null;

        for (int id : ids) {

            Team t = findCompetitor(id);

            if (t == null ||
                !t.sport.equalsIgnoreCase(sport.trim())) {

                throw new SQLException(
                    "All entrants must belong to the selected sport."
                );
            }

            if (type == null) {
                type = t.type;
            }

            if (!type.equals(t.type)) {

                throw new SQLException(
                    "Do not mix individual and team entrants."
                );
            }
        }

        int tournamentId;

        try (Connection c = Database.getConnection()) {

            c.setAutoCommit(false);

            try {

                String insertTournament =
                    "INSERT INTO tournaments " +
                    "(name, sport, status) " +
                    "VALUES (?, ?, 'ACTIVE')";

                try (PreparedStatement ps =
                         c.prepareStatement(insertTournament)) {

                    ps.setString(1, name.trim());
                    ps.setString(2, sport.trim());
                    ps.executeUpdate();
                }

                try (Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery(
                         "SELECT last_insert_rowid()")) {

                    if (!rs.next()) {
                        throw new SQLException(
                            "Unable to get tournament ID."
                        );
                    }

                    tournamentId = rs.getInt(1);
                }

                String registrationSql =
                    "INSERT INTO tournament_teams " +
                    "(tournament_id, team_id, seed) " +
                    "VALUES (?, ?, ?)";

                try (PreparedStatement ps =
                         c.prepareStatement(registrationSql)) {

                    for (int i = 0; i < ids.size(); i++) {

                        ps.setInt(1, tournamentId);
                        ps.setInt(2, ids.get(i));
                        ps.setInt(3, i + 1);

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                // Generate Round 1 matches
                for (int i = 0; i < ids.size(); i += 2) {

                    insertMatch(
                        c,
                        tournamentId,
                        1,
                        i / 2 + 1,
                        ids.get(i),
                        ids.get(i + 1)
                    );
                }

                c.commit();

            } catch (SQLException ex) {

                c.rollback();
                throw ex;
            }
        }

        selectTournament(tournamentId);

        return tournamentId;
    }

    // ========================================
    // SELECT TOURNAMENT
    // ========================================

    public void selectTournament(int id)
            throws SQLException {

        boolean exists = false;

        for (Tournament t : tournaments()) {

            if (t.id == id) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            throw new SQLException(
                "Tournament not found."
            );
        }

        selectedTournamentId = id;

        // Undo history applies to current selection
        undoStack.clear();

        rebuildQueue();
    }

    // ========================================
    // DATABASE MATCH READING
    // ========================================

    private static Integer nullableInt(
            ResultSet rs,
            String column) throws SQLException {

        int value = rs.getInt(column);

        return rs.wasNull() ? null : value;
    }

    private Match readMatch(ResultSet rs)
            throws SQLException {

        return new Match(
            rs.getInt("id"),
            rs.getInt("tournament_id"),
            rs.getInt("round"),
            rs.getInt("match_number"),

            nullableInt(rs, "team_a_id"),
            nullableInt(rs, "team_b_id"),

            nullableInt(rs, "score_a"),
            nullableInt(rs, "score_b"),

            nullableInt(rs, "winner_id"),
            rs.getString("status")
        );
    }

    // ========================================
    // GET TOURNAMENT MATCHES
    // ========================================

    public List<Match> matches(int tournamentId)
            throws SQLException {

        List<Match> list = new ArrayList<>();

        String sql =
            "SELECT * FROM matches " +
            "WHERE tournament_id=? " +
            "ORDER BY round, match_number";

        try (Connection c = Database.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(sql)) {

            ps.setInt(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(readMatch(rs));
                }
            }
        }

        return list;
    }

    // ========================================
    // CUSTOM QUEUE: REBUILD
    // ========================================

    public void rebuildQueue()
            throws SQLException {

        matchQueue.clear();

        if (selectedTournamentId <= 0) {
            return;
        }

        for (Match m : matches(selectedTournamentId)) {

            if ("PENDING".equalsIgnoreCase(m.status)) {
                matchQueue.enqueue(m);
            }
        }
    }

    // ========================================
    // RECORD MATCH RESULT
    // ========================================

    
public void recordNextResult(
        int scoreA,
        int scoreB,
        int selectedWinnerId) throws SQLException {

    Match first = matchQueue.peek();

    if (first == null) {
        throw new SQLException("No pending match.");
    }

    if (scoreA < 0 || scoreB < 0) {
        throw new SQLException("Scores cannot be negative.");
    }

    if (first.teamA == null || first.teamB == null) {
        throw new SQLException("Both competitors must be assigned.");
    }

    if (selectedWinnerId != first.teamA &&
        selectedWinnerId != first.teamB) {
        throw new SQLException("Select a valid match winner.");
    }

    try (Connection c = Database.getConnection()) {
        c.setAutoCommit(false);

        try {
            String sql =
                "UPDATE matches SET score_a=?, score_b=?, " +
                "winner_id=?, status='COMPLETED' " +
                "WHERE id=? AND tournament_id=? " +
                "AND status='PENDING'";

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, scoreA);
                ps.setInt(2, scoreB);
                ps.setInt(3, selectedWinnerId);
                ps.setInt(4, first.id);
                ps.setInt(5, selectedTournamentId);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Match is no longer pending.");
                }
            }

            List<Integer> winners = new ArrayList<>();
            boolean roundComplete = true;

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT status, winner_id FROM matches " +
                    "WHERE tournament_id=? AND round=? " +
                    "ORDER BY match_number")) {

                ps.setInt(1, selectedTournamentId);
                ps.setInt(2, first.round);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!"COMPLETED".equals(rs.getString("status"))) {
                            roundComplete = false;
                        } else {
                            winners.add(rs.getInt("winner_id"));
                        }
                    }
                }
            }

            if (roundComplete) {
                if (winners.size() == 1) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE tournaments SET status='COMPLETED' " +
                            "WHERE id=?")) {
                        ps.setInt(1, selectedTournamentId);
                        ps.executeUpdate();
                    }
                } else {
                    for (int i = 0; i < winners.size(); i += 2) {
                        insertMatch(
                            c,
                            selectedTournamentId,
                            first.round + 1,
                            i / 2 + 1,
                            winners.get(i),
                            winners.get(i + 1)
                        );
                    }
                }
            }

            c.commit();

        } catch (SQLException ex) {
            c.rollback();
            throw ex;
        }
    }

    matchQueue.dequeue();

    undoStack.push(
        new UndoStack.Action(
            selectedTournamentId,
            first.id,
            first.round
        )
    );

    rebuildQueue();
}


    // ========================================
    // UNDO LAST RESULT USING STACK
    // ========================================

    public void undoLastResult()
            throws SQLException {

        UndoStack.Action action =
            undoStack.peek();

        if (action == null ||
            action.tournamentId != selectedTournamentId) {

            throw new SQLException(
                "Nothing to undo in this session."
            );
        }

        try (Connection c = Database.getConnection()) {

            c.setAutoCommit(false);

            try {

                // Remove next-round matches generated by this result
                String deleteSql =
                    "DELETE FROM matches " +
                    "WHERE tournament_id=? AND round>?";

                try (PreparedStatement ps =
                         c.prepareStatement(deleteSql)) {

                    ps.setInt(1, action.tournamentId);
                    ps.setInt(2, action.round);

                    ps.executeUpdate();
                }

                // Restore previous match to pending
                String restoreSql =
                    "UPDATE matches SET " +
                    "score_a=NULL, score_b=NULL, " +
                    "winner_id=NULL, status='PENDING' " +
                    "WHERE id=? AND tournament_id=?";

                try (PreparedStatement ps =
                         c.prepareStatement(restoreSql)) {

                    ps.setInt(1, action.matchId);
                    ps.setInt(2, action.tournamentId);

                    if (ps.executeUpdate() != 1) {

                        throw new SQLException(
                            "Match not found."
                        );
                    }
                }

                // Tournament becomes active again
                try (PreparedStatement ps =
                         c.prepareStatement(
                             "UPDATE tournaments " +
                             "SET status='ACTIVE' WHERE id=?")) {

                    ps.setInt(1, action.tournamentId);
                    ps.executeUpdate();
                }

                c.commit();

            } catch (SQLException ex) {

                c.rollback();
                throw ex;
            }
        }

        // LIFO pop after successful database update
        undoStack.pop();

        // Restore correct pending match order
        rebuildQueue();
    }

    // ========================================
    // DASHBOARD STATISTICS
    // ========================================

    public int count(String table)
            throws SQLException {

        if (!Arrays.asList(
                "teams",
                "tournaments",
                "matches").contains(table)) {

            throw new SQLException(
                "Invalid statistics table."
            );
        }

        String sql =
            "SELECT COUNT(*) FROM " + table;

        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int completedCount()
            throws SQLException {

        String sql =
            "SELECT COUNT(*) FROM matches " +
            "WHERE status='COMPLETED'";

        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ========================================
    // HASHMAP: COMPETITOR NAME LOOKUP
    // ========================================

    public String competitorName(Integer id) {

        if (id == null) {
            return "TBD";
        }

        Team team = competitorIndex.get(id);

        return team == null
            ? "#" + id
            : team.name;
    }
}
