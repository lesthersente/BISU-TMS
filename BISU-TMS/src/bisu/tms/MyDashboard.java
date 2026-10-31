
package bisu.tms;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MyDashboard extends JFrame {

    private static final Color PURPLE = new Color(33, 18, 90);
    private static final Color GOLD = new Color(247, 194, 56);
    private static final Color BACK = new Color(244, 245, 250);

    private final TournamentService service;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);
    private final JPanel statsGrid =
        new JPanel(new GridLayout(2, 2, 18, 18));

    private final DefaultTableModel teamModel = model(
        "ID", "Competitor / Team", "Sport / Esport",
        "Type", "Coach / Captain"
    );

    private final DefaultTableModel tournamentModel = model(
        "ID", "Tournament", "Sport / Esport", "Status"
    );

    private final DefaultTableModel matchModel = model(
        "ID", "Round", "Match", "Competitor A",
        "Competitor B", "Score", "Winner", "Status"
    );

    private final DefaultTableModel resultModel = model(
        "Tournament", "Round", "Competitor A",
        "Competitor B", "Score", "Winner"
    );

    private final JTable teamTable = table(teamModel);
    private final JTable tournamentTable = table(tournamentModel);
    private final JTable matchTable = table(matchModel);

    private final BracketCanvas bracketCanvas = new BracketCanvas();
    private final JTextArea dsaArea = new JTextArea();
    private final JLabel selectedLabel =
        new JLabel("No tournament selected");

    private String searchText = "";

    public MyDashboard(String username) {
        super("BISU-TMS | Dashboard");

        try {
            service = new TournamentService();
        } catch (SQLException ex) {
            throw new IllegalStateException(
                "Unable to load tournament data", ex
            );
        }

        setSize(1200, 760);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(makeSidebar(), BorderLayout.WEST);
        add(makeContent(username), BorderLayout.CENTER);

        showScreen("Dashboard");
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(29);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        table.getTableHeader().setFont(
            new Font("SansSerif", Font.BOLD, 13)
        );
        return table;
    }

    private JButton button(String name, Runnable job) {
        JButton button = new JButton(name);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.addActionListener(e -> job.run());
        return button;
    }

    // ========================================
    // SIDEBAR
    // ========================================

    private JPanel makeSidebar() {
        JPanel left = new JPanel();

        left.setPreferredSize(new Dimension(230, 0));
        left.setBackground(PURPLE);
        left.setBorder(
            BorderFactory.createEmptyBorder(22, 13, 22, 13)
        );

        left.setLayout(
            new BoxLayout(left, BoxLayout.Y_AXIS)
        );

        java.net.URL url = getClass().getResource(
            "/bisu/tms/assets/bisu_logo.png"
        );

        JLabel logo = url == null
            ? new JLabel("BISU")
            : new JLabel(new ImageIcon(
                new ImageIcon(url).getImage().getScaledInstance(
                    88, 88, Image.SCALE_SMOOTH
                )
            ));

        logo.setForeground(GOLD);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(logo);
        left.add(Box.createVerticalStrut(10));

        JLabel name = new JLabel("BISU-TMS");
        name.setForeground(GOLD);
        name.setFont(
            new Font("SansSerif", Font.BOLD, 23)
        );
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(name);
        left.add(Box.createVerticalStrut(24));

        String[] navigation = {
            "Dashboard",
            "Competitors",
            "Tournaments",
            "Matches",
            "Bracket",
            "Results",
            "DSA Demo"
        };

        for (String nav : navigation) {
            JButton b = button(nav, () -> showScreen(nav));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
            );
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(55, 38, 116));
            b.setFocusPainted(false);

            left.add(b);
            left.add(Box.createVerticalStrut(9));
        }

        left.add(Box.createVerticalGlue());

        JLabel credit = new JLabel(
            "Developed by: BOYSAGING"
        );
        credit.setForeground(GOLD);
        credit.setFont(
            new Font("SansSerif", Font.PLAIN, 12)
        );
        credit.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(credit);
        left.add(Box.createVerticalStrut(10));

        JButton logout = button("Logout", () -> {
            new LoginForm().setVisible(true);
            dispose();
        });

        logout.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 40)
        );
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(logout);
        return left;
    }

    // ========================================
    // MAIN CONTENT
    // ========================================

    private JPanel makeContent(String username) {
        JPanel main = new JPanel(
            new BorderLayout(0, 16)
        );

        main.setBackground(BACK);
        main.setBorder(
            BorderFactory.createEmptyBorder(22, 26, 24, 26)
        );

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel heading = new JLabel(
            "BISU TOURNAMENT MANAGEMENT SYSTEM"
        );
        heading.setFont(
            new Font("SansSerif", Font.BOLD, 23)
        );
        heading.setForeground(PURPLE);

        header.add(heading, BorderLayout.WEST);
        header.add(
            new JLabel("Admin: " + username),
            BorderLayout.EAST
        );

        main.add(header, BorderLayout.NORTH);
        screens.setOpaque(false);

        screens.add(makeDashboard(), "Dashboard");
        screens.add(makeCompetitors(), "Competitors");
        screens.add(makeTournaments(), "Tournaments");
        screens.add(makeMatches(), "Matches");
        screens.add(makeBracket(), "Bracket");
        screens.add(makeResults(), "Results");
        screens.add(makeDsaDemo(), "DSA Demo");

        main.add(screens, BorderLayout.CENTER);
        return main;
    }

    private JPanel page(
            String title,
            JPanel toolbar,
            JComponent center) {

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(PURPLE);
        titleLabel.setFont(
            new Font("SansSerif", Font.BOLD, 20)
        );

        top.add(titleLabel, BorderLayout.NORTH);

        if (toolbar != null) {
            top.add(toolbar, BorderLayout.CENTER);
        }

        p.add(top, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);

        return p;
    }

    private JPanel toolbar() {
        JPanel p = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 8, 12)
        );
        p.setOpaque(false);
        return p;
    }

    // ========================================
    // DASHBOARD
    // ========================================

    private JPanel makeDashboard() {
        JPanel p = page(
            "Dashboard Overview", null, statsGrid
        );

        statsGrid.setOpaque(false);

        JPanel foot = toolbar();
        foot.add(button(
            "Refresh Dashboard", this::refreshDashboard
        ));

        p.add(foot, BorderLayout.SOUTH);
        return p;
    }

    private JPanel statCard(String label, int number) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);

        p.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(220, 221, 230)
                ),
                BorderFactory.createEmptyBorder(
                    20, 20, 20, 20
                )
            )
        );

        JLabel n = new JLabel(
            String.valueOf(number),
            SwingConstants.CENTER
        );
        n.setFont(
            new Font("SansSerif", Font.BOLD, 48)
        );
        n.setForeground(PURPLE);

        JLabel title = new JLabel(
            label, SwingConstants.CENTER
        );
        title.setFont(
            new Font("SansSerif", Font.BOLD, 16)
        );

        p.add(n, BorderLayout.CENTER);
        p.add(title, BorderLayout.SOUTH);

        return p;
    }

    private void refreshDashboard() {
        try {
            statsGrid.removeAll();

            statsGrid.add(statCard(
                "Registered Competitors / Teams",
                service.count("teams")
            ));

            statsGrid.add(statCard(
                "Tournaments",
                service.count("tournaments")
            ));

            statsGrid.add(statCard(
                "Total Matches",
                service.count("matches")
            ));

            statsGrid.add(statCard(
                "Completed Matches",
                service.completedCount()
            ));

            statsGrid.revalidate();
            statsGrid.repaint();

        } catch (SQLException ex) {
            error(ex);
        }
    }

    // ========================================
    // COMPETITOR MANAGEMENT
    // ========================================

    private JPanel makeCompetitors() {
        JPanel bar = toolbar();

        bar.add(button(
            "Add Competitor",
            () -> editCompetitor(null)
        ));

        bar.add(button("Edit", () -> {
            Team t = service.findCompetitor(
                selectedId(teamTable)
            );
            if (t != null) {
                editCompetitor(t);
            }
        }));

        bar.add(button(
            "Delete", this::deleteCompetitor
        ));

        bar.add(button("Search", () -> {
            String q = JOptionPane.showInputDialog(
                this,
                "Name, sport, type, or competitor ID:",
                searchText
            );

            if (q != null) {
                searchText = q;
                refreshCompetitors();
            }
        }));

        bar.add(button("Show All", () -> {
            searchText = "";
            refreshCompetitors();
        }));

        bar.add(button("Load 8 Demo Teams", () ->
            execute(
                () -> service.loadSampleCompetitors(),
                "Sample Mobile Legends teams created."
            )
        ));

        return page(
            "Competitor and Team Registration",
            bar,
            new JScrollPane(teamTable)
        );
    }

    private void editCompetitor(Team existing) {
        JTextField name = new JTextField(
            existing == null ? "" : existing.name, 23
        );
        JTextField coach = new JTextField(
            existing == null ? "" : existing.coach, 23
        );

        JComboBox<String> sport = new JComboBox<>(
            new String[]{
                "Basketball",
                "Volleyball",
                "Badminton",
                "Chess",
                "Table Tennis",
                "Mobile Legends",
                "Valorant",
                "Other"
            }
        );

        sport.setEditable(true);

        if (existing != null) {
            sport.setSelectedItem(existing.sport);
        }

        JComboBox<String> type = new JComboBox<>(
            new String[]{"TEAM", "INDIVIDUAL"}
        );

        if (existing != null) {
            type.setSelectedItem(existing.type);
        }

        JPanel form = new JPanel(
            new GridLayout(0, 2, 8, 12)
        );

        form.add(new JLabel("Competitor / Team Name:"));
        form.add(name);
        form.add(new JLabel("Sport / Esport:"));
        form.add(sport);
        form.add(new JLabel("Entry Type:"));
        form.add(type);
        form.add(new JLabel("Coach / Captain (optional):"));
        form.add(coach);

        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            existing == null
                ? "Register Competitor"
                : "Edit Competitor",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String sportName = String.valueOf(
            sport.getSelectedItem()
        ).trim();

        execute(() -> {
            if (existing == null) {
                service.addCompetitor(
                    name.getText(),
                    sportName,
                    String.valueOf(type.getSelectedItem()),
                    coach.getText()
                );
            } else {
                service.editCompetitor(
                    existing.id,
                    name.getText(),
                    sportName,
                    String.valueOf(type.getSelectedItem()),
                    coach.getText()
                );
            }
        }, "Competitor saved.");
    }

    private void deleteCompetitor() {
        int id = selectedId(teamTable);

        if (id < 0) {
            return;
        }

        if (JOptionPane.showConfirmDialog(
                this,
                "Delete this competitor?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
            ) != JOptionPane.YES_OPTION) {
            return;
        }

        execute(
            () -> service.deleteCompetitor(id),
            "Competitor deleted."
        );
    }

    private void refreshCompetitors() {
        try {
            service.reloadCompetitors();
            teamModel.setRowCount(0);

            for (Team t : service.competitors(searchText)) {
                teamModel.addRow(new Object[]{
                    t.id,
                    t.name,
                    t.sport,
                    t.type,
                    t.coach
                });
            }

        } catch (SQLException ex) {
            error(ex);
        }
    }

    // ========================================
    // TOURNAMENT MANAGEMENT
    // ========================================

    private JPanel makeTournaments() {
        JPanel bar = toolbar();

        bar.add(button(
            "Create Tournament", this::createTournament
        ));

        bar.add(button(
            "Select Tournament", this::selectTournament
        ));

        bar.add(button(
            "Refresh", this::refreshTournaments
        ));

        return page(
            "Tournament Management (4 or 8 entrants)",
            bar,
            new JScrollPane(tournamentTable)
        );
    }

    private void createTournament() {
        try {
            service.reloadCompetitors();

            List<Team> all = service.competitors("");

            if (all.size() < 4) {
                info("Register at least four competitors first.");
                return;
            }

            Set<String> sports = new TreeSet<>(
                String.CASE_INSENSITIVE_ORDER
            );

            for (Team t : all) {
                sports.add(t.sport);
            }

            JTextField name = new JTextField(
                "BISU Championship", 22
            );

            JComboBox<String> sport = new JComboBox<>(
                sports.toArray(new String[0])
            );

            JPanel form = new JPanel(
                new GridLayout(0, 2, 8, 10)
            );

            form.add(new JLabel("Tournament Name:"));
            form.add(name);
            form.add(new JLabel("Sport / Esport:"));
            form.add(sport);

            if (JOptionPane.showConfirmDialog(
                    this,
                    form,
                    "New Tournament",
                    JOptionPane.OK_CANCEL_OPTION
                ) != JOptionPane.OK_OPTION) {
                return;
            }

            String chosenSport = String.valueOf(
                sport.getSelectedItem()
            );

            DefaultListModel<Team> listModel =
                new DefaultListModel<>();

            for (Team t : all) {
                if (t.sport.equalsIgnoreCase(chosenSport)) {
                    listModel.addElement(t);
                }
            }

            JList<Team> entrants = new JList<>(listModel);
            entrants.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
            );
            entrants.setVisibleRowCount(10);

            JPanel chooser = new JPanel(
                new BorderLayout(5, 8)
            );

            chooser.add(
                new JLabel(
                    "Select exactly 4 or 8 entrants of the same type:"
                ),
                BorderLayout.NORTH
            );

            chooser.add(
                new JScrollPane(entrants),
                BorderLayout.CENTER
            );

            if (JOptionPane.showConfirmDialog(
                    this,
                    chooser,
                    "Choose Entrants",
                    JOptionPane.OK_CANCEL_OPTION
                ) != JOptionPane.OK_OPTION) {
                return;
            }

            List<Integer> ids = new ArrayList<>();

            for (Team t : entrants.getSelectedValuesList()) {
                ids.add(t.id);
            }

            execute(
                () -> service.createTournament(
                    name.getText(),
                    chosenSport,
                    ids
                ),
                "Tournament created and first-round matches generated."
            );

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void selectTournament() {
        int id = selectedId(tournamentTable);

        if (id < 0) {
            return;
        }

        execute(
            () -> service.selectTournament(id),
            "Tournament selected. Open Matches or Bracket."
        );
    }

    private void refreshTournaments() {
        try {
            tournamentModel.setRowCount(0);

            for (Tournament t : service.tournaments()) {
                tournamentModel.addRow(new Object[]{
                    t.id,
                    t.name,
                    t.sport,
                    t.status
                });
            }

        } catch (SQLException ex) {
            error(ex);
        }
    }

    // ========================================
    // MATCH MANAGEMENT AND SCORING
    // ========================================

    private JPanel makeMatches() {
        JPanel bar = toolbar();

        bar.add(button(
            "Next Match (Queue Peek)", this::peekMatch
        ));

        bar.add(button(
            "Record Next Result", this::recordMatch
        ));

        bar.add(button(
            "Undo Last Result (Stack Pop)", this::undoMatch
        ));

        bar.add(button(
            "Refresh", this::refreshMatches
        ));

        JPanel content = new JPanel(
            new BorderLayout(0, 10)
        );
        content.setOpaque(false);

        selectedLabel.setFont(
            new Font("SansSerif", Font.BOLD, 14)
        );
        selectedLabel.setForeground(PURPLE);

        content.add(selectedLabel, BorderLayout.NORTH);
        content.add(
            new JScrollPane(matchTable),
            BorderLayout.CENTER
        );

        return page(
            "Match Queue and Scoring", bar, content
        );
    }

    private void peekMatch() {
        Match m = service.nextMatch();

        if (m == null) {
            info("No pending match. Select an active tournament.");
            return;
        }

        info(
            "Next (FIFO): Match #" + m.id +
            " - " + service.competitorName(m.teamA) +
            " vs " + service.competitorName(m.teamB)
        );
    }

    // Manual official winner selection
    private void recordMatch() {
        Match m = service.nextMatch();

        if (m == null) {
            info("No pending match to record.");
            return;
        }

        String nameA = service.competitorName(m.teamA);
        String nameB = service.competitorName(m.teamB);

        JTextField scoreA = new JTextField(8);
        JTextField scoreB = new JTextField(8);

        JComboBox<String> winner = new JComboBox<>(
            new String[]{nameA, nameB}
        );
        winner.setSelectedIndex(-1);

        JPanel form = new JPanel(
            new GridLayout(0, 2, 8, 10)
        );

        form.add(new JLabel(nameA + " score:"));
        form.add(scoreA);
        form.add(new JLabel(nameB + " score:"));
        form.add(scoreB);
        form.add(new JLabel("Official winner:"));
        form.add(winner);

        if (JOptionPane.showConfirmDialog(
                this,
                form,
                "Record Match #" + m.id + " (FIFO)",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            ) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int a = Integer.parseInt(
                scoreA.getText().trim()
            );
            int b = Integer.parseInt(
                scoreB.getText().trim()
            );

            if (a < 0 || b < 0) {
                info("Scores cannot be negative.");
                return;
            }

            if (winner.getSelectedIndex() < 0) {
                info("Select the official winner.");
                return;
            }

            int winnerId = winner.getSelectedIndex() == 0
                ? m.teamA
                : m.teamB;

            String winnerName = service.competitorName(
                winnerId
            );

            int ok = JOptionPane.showConfirmDialog(
                this,
                nameA + ": " + a + "\n" +
                nameB + ": " + b + "\n" +
                "Official winner: " + winnerName +
                "\nConfirm result?",
                "Confirm Match Winner",
                JOptionPane.YES_NO_OPTION
            );

            if (ok == JOptionPane.YES_OPTION) {
                execute(
                    () -> service.recordNextResult(
                        a, b, winnerId
                    ),
                    "Result saved. Winner: " + winnerName
                );
            }

        } catch (NumberFormatException ex) {
            info("Enter valid whole-number scores.");
        }
    }

    private void undoMatch() {
        if (JOptionPane.showConfirmDialog(
                this,
                "Undo the last recorded result?",
                "Undo",
                JOptionPane.YES_NO_OPTION
            ) != JOptionPane.YES_OPTION) {
            return;
        }

        execute(
            () -> service.undoLastResult(),
            "Last result undone in SQLite and Stack."
        );
    }

    private void refreshMatches() {
        try {
            matchModel.setRowCount(0);

            Tournament t = service.selectedTournament();

            selectedLabel.setText(
                t == null
                    ? "Select a tournament first in Tournaments."
                    : "Selected: " + t.name +
                      " | " + t.status +
                      " | Queue: " + service.queueSize() +
                      " | Undo: " + service.undoSize()
            );

            if (t == null) {
                return;
            }

            for (Match m : service.matches(t.id)) {
                String score = m.scoreA == null
                    ? "-"
                    : m.scoreA + " - " + m.scoreB;

                matchModel.addRow(new Object[]{
                    m.id,
                    m.round,
                    m.matchNumber,
                    service.competitorName(m.teamA),
                    service.competitorName(m.teamB),
                    score,
                    m.winner == null
                        ? "-"
                        : service.competitorName(m.winner),
                    m.status
                });
            }

        } catch (SQLException ex) {
            error(ex);
        }
    }

    // ========================================
    // COMPACT GRAPHICAL TOURNAMENT BRACKET
    // ========================================

    private JPanel makeBracket() {
        JScrollPane scroll = new JScrollPane(bracketCanvas);

        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getHorizontalScrollBar().setUnitIncrement(20);

        JPanel panel = page(
            "Tournament Bracket", null, scroll
        );

        JPanel bar = toolbar();

        bar.add(button(
            "Refresh Bracket", this::refreshBracket
        ));

        panel.add(bar, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshBracket() {
        try {
            Tournament t = service.selectedTournament();

            if (t == null) {
                bracketCanvas.showEmpty(
                    "Select a tournament from Tournaments first."
                );
            } else {
                bracketCanvas.showTournament(
                    t, service.matches(t.id)
                );
            }

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private class BracketCanvas extends JPanel {

        private final Color PAPER = new Color(250, 250, 250);
        private final Color TILE = new Color(224, 224, 224);
        private final Color INK = new Color(25, 25, 25);
        private final Color WINNER = new Color(31, 190, 42);

        // SMALLER BRACKET DIMENSIONS
        private static final int BOX_W = 172;
        private static final int BOX_H = 38;
        private static final int X0 = 20;
        private static final int DX = 224;

        private Tournament tournament;
        private List<Match> matches = Collections.emptyList();

        private String message =
            "Select a tournament to see its bracket.";

        BracketCanvas() {
            setBackground(PAPER);
            setPreferredSize(new Dimension(910, 590));
        }

        void showEmpty(String text) {
            tournament = null;
            matches = Collections.emptyList();
            message = text;

            setPreferredSize(new Dimension(910, 590));
            revalidate();
            repaint();
        }

        void showTournament(
                Tournament t,
                List<Match> records) {

            tournament = t;
            matches = new ArrayList<>(records);

            int count = 0;

            for (Match m : matches) {
                if (m.round == 1) {
                    count++;
                }
            }

            setPreferredSize(
                new Dimension(
                    count > 2 ? 910 : 700,
                    count > 2 ? 590 : 450
                )
            );

            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                );

                // BRACKET TITLE
                g.setColor(INK);
                g.setFont(
                    new Font("SansSerif", Font.BOLD, 20)
                );
                g.drawString(
                    "TOURNAMENT BRACKET", X0, 30
                );

                g.setColor(WINNER);
                g.fillRect(X0, 39, 110, 5);

                if (tournament == null) {
                    g.setColor(INK);
                    g.setFont(
                        new Font("SansSerif", Font.PLAIN, 13)
                    );
                    g.drawString(message, X0, 95);
                    return;
                }

                // TOURNAMENT DETAILS
                g.setColor(new Color(80, 80, 80));
                g.setFont(
                    new Font("SansSerif", Font.PLAIN, 12)
                );

                g.drawString(
                    shorten(
                        g,
                        tournament.name + " | " +
                        tournament.sport + " | " +
                        tournament.status,
                        850
                    ),
                    X0, 65
                );

                // ORGANIZE MATCHES BY ROUND
                Map<Integer, List<Match>> rounds =
                    new TreeMap<>();

                for (Match m : matches) {
                    rounds.computeIfAbsent(
                        m.round,
                        key -> new ArrayList<>()
                    ).add(m);
                }

                for (List<Match> group : rounds.values()) {
                    group.sort(
                        Comparator.comparingInt(
                            m -> m.matchNumber
                        )
                    );
                }

                List<Match> first = rounds.getOrDefault(
                    1,
                    Collections.emptyList()
                );

                if (first.isEmpty()) {
                    g.drawString(
                        "No matches for this tournament.",
                        X0, 105
                    );
                    return;
                }

                int entrants = first.size() * 2;

                String[] headings = entrants == 8
                    ? new String[]{
                        "QUARTERFINALS",
                        "SEMIFINALS",
                        "FINAL",
                        "CHAMPION"
                    }
                    : new String[]{
                        "SEMIFINALS",
                        "FINAL",
                        "CHAMPION"
                    };

                g.setFont(
                    new Font("SansSerif", Font.BOLD, 11)
                );

                for (int j = 0; j < headings.length; j++) {
                    g.drawString(
                        headings[j],
                        X0 + j * DX + 5,
                        92
                    );
                }

                // FIRST ROUND COMPETITORS
                List<Integer> centers = new ArrayList<>();

                int base = entrants == 8 ? 121 : 138;

                for (int i = 0; i < entrants; i++) {
                    Match m = first.get(i / 2);

                    int y = base +
                        (i / 2) *
                        (entrants == 8 ? 108 : 123) +
                        (i % 2) * 46;

                    boolean isA = (i % 2 == 0);

                    Integer id = isA
                        ? m.teamA
                        : m.teamB;

                    Integer score = isA
                        ? m.scoreA
                        : m.scoreB;

                    drawBox(
                        g,
                        X0,
                        y,
                        service.competitorName(id),
                        score == null
                            ? ""
                            : "Score: " + score,
                        false
                    );

                    centers.add(y + BOX_H / 2);
                }

                // CONNECTING LINES AND ADVANCEMENT
                int round = 1;

                while (centers.size() > 1) {
                    List<Integer> next = new ArrayList<>();

                    List<Match> completed =
                        rounds.getOrDefault(
                            round,
                            Collections.emptyList()
                        );

                    int targetX = X0 + round * DX;
                    int sourceX =
                        X0 + (round - 1) * DX + BOX_W;
                    int turnX = targetX - 27;

                    for (
                        int i = 0;
                        i < centers.size();
                        i += 2
                    ) {
                        int upper = centers.get(i);
                        int lower = centers.get(i + 1);
                        int middle = (upper + lower) / 2;

                        g.setColor(INK);
                        g.setStroke(
                            new BasicStroke(1.6f)
                        );

                        g.drawLine(
                            sourceX, upper,
                            turnX, upper
                        );
                        g.drawLine(
                            sourceX, lower,
                            turnX, lower
                        );
                        g.drawLine(
                            turnX, upper,
                            turnX, lower
                        );
                        g.drawLine(
                            turnX, middle,
                            targetX, middle
                        );

                        Match feeder =
                            (i / 2 < completed.size())
                                ? completed.get(i / 2)
                                : null;

                        boolean decided =
                            feeder != null &&
                            feeder.winner != null;

                        String label = decided
                            ? service.competitorName(
                                feeder.winner
                            )
                            : "TBD";

                        String score =
                            feeder != null &&
                            feeder.scoreA != null &&
                            feeder.scoreB != null
                                ? "Match: " +
                                  feeder.scoreA +
                                  " - " +
                                  feeder.scoreB
                                : "";

                        drawBox(
                            g,
                            targetX,
                            middle - BOX_H / 2,
                            label,
                            score,
                            decided
                        );

                        next.add(middle);
                    }

                    centers = next;
                    round++;
                }

                // CHAMPIONSHIP TROPHY
                if (centers.size() == 1) {
                    drawTrophy(
                        g,
                        X0 +
                        (headings.length - 1) * DX +
                        70,
                        centers.get(0) + 57
                    );
                }

            } finally {
                g.dispose();
            }
        }

        // SMALL TEAM BOX WITH SCORE
        private void drawBox(
                Graphics2D g,
                int x,
                int y,
                String name,
                String score,
                boolean winning) {

            g.setColor(TILE);
            g.fillRoundRect(
                x, y, BOX_W, BOX_H, 4, 4
            );

            g.setColor(
                winning ? WINNER : INK
            );
            g.fillRect(
                x, y, 9, BOX_H
            );

            g.setColor(INK);
            g.setFont(
                new Font("SansSerif", Font.BOLD, 11)
            );

            String label = shorten(
                g,
                name == null ? "TBD" : name,
                BOX_W - 25
            );

            g.drawString(
                label,
                x + 15,
                y + 15
            );

            if (score != null && !score.isEmpty()) {
                g.setFont(
                    new Font("SansSerif", Font.PLAIN, 10)
                );
                g.setColor(
                    new Color(65, 65, 65)
                );

                g.drawString(
                    shorten(g, score, BOX_W - 25),
                    x + 15,
                    y + 30
                );
            }
        }

        // SHORTEN LONG TEAM NAMES
        private String shorten(
                Graphics2D g,
                String text,
                int limit) {

            FontMetrics fm = g.getFontMetrics();

            if (fm.stringWidth(text) <= limit) {
                return text;
            }

            while (
                text.length() > 0 &&
                fm.stringWidth(text + "...") > limit
            ) {
                text = text.substring(
                    0, text.length() - 1
                );
            }

            return text + "...";
        }

        // SMALL CHAMPIONSHIP TROPHY
        private void drawTrophy(
                Graphics2D g,
                int x,
                int y) {

            Graphics2D t =
                (Graphics2D) g.create();

            try {
                t.translate(x, y);
                t.scale(0.55, 0.55);

                t.setColor(INK);
                t.setStroke(
                    new BasicStroke(4f)
                );

                t.fillRoundRect(
                    0, 0, 72, 50, 8, 8
                );

                t.drawArc(
                    -21, 1, 30, 36, 70, 220
                );
                t.drawArc(
                    63, 1, 30, 36, -110, 220
                );

                t.fillRect(32, 48, 9, 26);
                t.fillRect(14, 73, 45, 9);
                t.fillRect(3, 83, 67, 10);

                t.setColor(PAPER);
                t.fillRect(33, 7, 7, 10);

            } finally {
                t.dispose();
            }
        }
    }

    // ========================================
    // RESULTS
    // ========================================

    private JPanel makeResults() {
        JPanel p = page(
            "Completed Match Results",
            null,
            new JScrollPane(table(resultModel))
        );

        JPanel bar = toolbar();
        bar.add(button(
            "Refresh Results", this::refreshResults
        ));

        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private void refreshResults() {
        try {
            resultModel.setRowCount(0);

            for (Tournament t : service.tournaments()) {
                for (Match m : service.matches(t.id)) {
                    if ("COMPLETED".equals(m.status)) {
                        resultModel.addRow(new Object[]{
                            t.name,
                            m.round,
                            service.competitorName(m.teamA),
                            service.competitorName(m.teamB),
                            m.scoreA + " - " + m.scoreB,
                            service.competitorName(m.winner)
                        });
                    }
                }
            }

        } catch (SQLException ex) {
            error(ex);
        }
    }

    // ========================================
    // DATA STRUCTURES DEMONSTRATION
    // ========================================

    private JPanel makeDsaDemo() {
        dsaArea.setEditable(false);
        dsaArea.setFont(
            new Font("Monospaced", Font.PLAIN, 15)
        );

        JPanel p = page(
            "Data Structures Demonstration",
            null,
            new JScrollPane(dsaArea)
        );

        JPanel bar = toolbar();

        bar.add(button(
            "HashMap: Search by ID",
            () -> {
                String id = JOptionPane.showInputDialog(
                    this,
                    "Enter competitor ID:"
                );

                if (id == null) {
                    return;
                }

                try {
                    Team t = service.findCompetitor(
                        Integer.parseInt(id.trim())
                    );

                    info(
                        t == null
                            ? "Not found in HashMap."
                            : "HashMap.get(): " +
                              t.name + " | " +
                              t.sport + " | " +
                              t.type
                    );

                } catch (NumberFormatException ex) {
                    info("Enter a numeric ID.");
                }
            }
        ));

        bar.add(button(
            "Queue: Peek", this::peekMatch
        ));

        bar.add(button(
            "Refresh DSA Status", this::refreshDsa
        ));

        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private void refreshDsa() {
        Match next = service.nextMatch();

        dsaArea.setText(
            "THREE ACTIVE DATA STRUCTURES\n" +
            "============================\n\n" +

            "1. HASHMAP<Integer, Team> [built in]\n" +
            "   Stored competitors: " +
            service.indexSize() + "\n" +
            "   Operation: get(ID), put(ID,team), search\n" +
            "   Advantage: average O(1) lookup\n" +
            "   Limitation: unordered\n\n" +

            "2. CUSTOM LINKED QUEUE [MANUALLY CODED]\n" +
            "   Pending matches: " +
            service.queueSize() + "\n" +
            "   Head: " +
            (next == null
                ? "none"
                : service.competitorName(next.teamA) +
                  " vs " +
                  service.competitorName(next.teamB)
            ) + "\n" +
            "   Operations: enqueue, dequeue, peek, isEmpty\n" +
            "   Advantage: FIFO O(1) operations\n" +
            "   Limitation: sequential access\n\n" +

            "3. STACK<UndoAction> [built in]\n" +
            "   Undo history entries: " +
            service.undoSize() + "\n" +
            "   Operations: push after scoring, pop on Undo\n" +
            "   Advantage: LIFO undo\n" +
            "   Limitation: latest action only\n\n" +

            "NOTE: Match scores, tournaments, and competitors\n" +
            "persist in SQLite.\n" +
            "Undo history resets on app restart or switching tournaments.\n"
        );
    }

    // ========================================
    // UTILITIES
    // ========================================

    private int selectedId(JTable table) {
        int row = table.getSelectedRow();

        if (row < 0) {
            info("Select a row first.");
            return -1;
        }

        return (Integer) table.getValueAt(row, 0);
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws SQLException;
    }

    private void execute(
            SqlAction action,
            String success) {

        try {
            action.run();

            refreshCompetitors();
            refreshTournaments();
            refreshMatches();
            refreshBracket();
            refreshResults();
            refreshDashboard();
            refreshDsa();

            info(success);

        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void showScreen(String name) {
        switch (name) {
            case "Dashboard":
                refreshDashboard();
                break;

            case "Competitors":
                refreshCompetitors();
                break;

            case "Tournaments":
                refreshTournaments();
                break;

            case "Matches":
                refreshMatches();
                break;

            case "Bracket":
                refreshBracket();
                break;

            case "Results":
                refreshResults();
                break;

            case "DSA Demo":
                refreshDsa();
                break;

            default:
                break;
        }

        cardLayout.show(screens, name);
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "BISU-TMS",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(
            this,
            ex.getMessage(),
            "BISU-TMS Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
