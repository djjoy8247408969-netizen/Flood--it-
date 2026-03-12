import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class FloodItPro extends JFrame {

    Color[] ALL_COLORS = {
            Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.MAGENTA, Color.ORANGE
    };

    String[] COLOR_NAMES = {"Red", "Green", "Blue", "Yellow", "Magenta", "Orange"};

    int SIZE = 10;
    int COLOR_COUNT = 6;
    int CELL = 35;

    int[][] grid;
    int moves = 0;

    boolean vsBot = false;
    boolean playerTurn = true;
    boolean gameOver = false;

    Stack<int[][]> undo = new Stack<>();
    Stack<int[][]> redo = new Stack<>();

    Board board;

    JLabel movesLabel;
    JLabel hintLabel;

    JComboBox<String> modeBox;
    JComboBox<Integer> sizeBox;
    JComboBox<Integer> colorBox;

    JButton undoBtn, redoBtn;

    final String TEAM_NAME = "Alpha Coders";

    public FloodItPro() {
        setTitle("Flood-It Pro (Divide & Conquer Optimized)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createToolbar();
        createCenterBoard();
        createBottomPanel();

        newGame();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createToolbar() {
        JPanel bar = new JPanel();

        modeBox = new JComboBox<>(new String[]{"Single Player", "Player vs Bot"});
        sizeBox = new JComboBox<>();
        for (int i = 4; i <= 14; i++) sizeBox.addItem(i);

        colorBox = new JComboBox<>();
        for (int i = 2; i <= 6; i++) colorBox.addItem(i);

        JButton newBtn = new JButton("New Game");
        undoBtn = new JButton("Undo");
        redoBtn = new JButton("Redo");
        JButton hintBtn = new JButton("D&C Hint");

        newBtn.addActionListener(e -> newGame());
        undoBtn.addActionListener(e -> undoMove());
        redoBtn.addActionListener(e -> redoMove());
        hintBtn.addActionListener(e -> showDivideConquerHint());

        bar.add(new JLabel("Mode"));
        bar.add(modeBox);
        bar.add(new JLabel("Size"));
        bar.add(sizeBox);
        bar.add(new JLabel("Colors"));
        bar.add(colorBox);
        bar.add(newBtn);
        bar.add(undoBtn);
        bar.add(redoBtn);
        bar.add(hintBtn);

        add(bar, BorderLayout.NORTH);
    }

    private void createCenterBoard() {
        board = new Board();
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(board);
        add(wrapper, BorderLayout.CENTER);
    }

    private void createBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());

        movesLabel = new JLabel("", JLabel.CENTER);
        movesLabel.setFont(new Font("Arial", Font.BOLD, 16));

        hintLabel = new JLabel(" ", JLabel.CENTER);

        bottom.add(movesLabel, BorderLayout.CENTER);
        bottom.add(hintLabel, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);
    }

    private void newGame() {
        SIZE = (int) sizeBox.getSelectedItem();
        COLOR_COUNT = (int) colorBox.getSelectedItem();
        vsBot = modeBox.getSelectedIndex() == 1;

        grid = new int[SIZE][SIZE];
        Random r = new Random();

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                grid[i][j] = r.nextInt(COLOR_COUNT);

        undo.clear();
        redo.clear();
        moves = 0;
        gameOver = false;
        playerTurn = true;

        undoBtn.setEnabled(false);
        redoBtn.setEnabled(false);

        board.setPreferredSize(new Dimension(SIZE * CELL, SIZE * CELL));
        pack();
        updateMoves();
        repaint();
    }

    // =========================
    // DIVIDE & CONQUER HINT
    // =========================

    private void showDivideConquerHint() {
        if (gameOver) return;

        int best = getBestColorDC();

        if (best != -1)
            hintLabel.setText("D&C Hint: Try " + COLOR_NAMES[best]);
        else
            hintLabel.setText("No good move found.");
    }

    private int getBestColorDC() {

        boolean[][] region = new boolean[SIZE][SIZE];
        Queue<Point> q = new LinkedList<>();

        int oldColor = grid[0][0];
        q.add(new Point(0, 0));
        region[0][0] = true;

        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};

        // BFS to find current region
        while (!q.isEmpty()) {
            Point p = q.poll();

            for (int[] d : dir) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];

                if (nx >= 0 && ny >= 0 && nx < SIZE && ny < SIZE
                        && !region[nx][ny]
                        && grid[nx][ny] == oldColor) {

                    region[nx][ny] = true;
                    q.add(new Point(nx, ny));
                }
            }
        }

        int[] counts = countBoundaryDC(0, SIZE - 1, 0, SIZE - 1, region);

        int bestColor = -1;
        int max = -1;

        for (int i = 0; i < COLOR_COUNT; i++) {
            if (i != oldColor && counts[i] > max) {
                max = counts[i];
                bestColor = i;
            }
        }

        return bestColor;
    }

    private int[] countBoundaryDC(int r1, int r2, int c1, int c2, boolean[][] region) {

        int[] result = new int[COLOR_COUNT];

        // Optimization: Stop recursion for small blocks
        if ((r2 - r1 <= 2) && (c2 - c1 <= 2)) {

            int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};

            for (int i = r1; i <= r2; i++) {
                for (int j = c1; j <= c2; j++) {

                    if (region[i][j]) continue;

                    for (int[] d : dir) {
                        int ni = i + d[0];
                        int nj = j + d[1];

                        if (ni >= 0 && nj >= 0 && ni < SIZE && nj < SIZE
                                && region[ni][nj]) {

                            result[grid[i][j]]++;
                            break;
                        }
                    }
                }
            }
            return result;
        }

        int midR = (r1 + r2) / 2;
        int midC = (c1 + c2) / 2;

        int[] q1 = countBoundaryDC(r1, midR, c1, midC, region);
        int[] q2 = countBoundaryDC(r1, midR, midC + 1, c2, region);
        int[] q3 = countBoundaryDC(midR + 1, r2, c1, midC, region);
        int[] q4 = countBoundaryDC(midR + 1, r2, midC + 1, c2, region);

        for (int i = 0; i < COLOR_COUNT; i++)
            result[i] = q1[i] + q2[i] + q3[i] + q4[i];

        return result;
    }

    // =========================
    // FLOOD FILL (returns true if board changed)
    // =========================

    boolean floodFill(int color) {

        int old = grid[0][0];
        if (old == color || gameOver) return false;

        saveState();
        undoBtn.setEnabled(true);
        redoBtn.setEnabled(false);

        boolean[][] visited = new boolean[SIZE][SIZE];
        Queue<Point> q = new LinkedList<>();
        q.add(new Point(0, 0));
        visited[0][0] = true;

        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};

        while (!q.isEmpty()) {
            Point p = q.poll();
            grid[p.x][p.y] = color;

            for (int[] d : dir) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];

                if (nx >= 0 && ny >= 0 && nx < SIZE && ny < SIZE
                        && !visited[nx][ny]
                        && grid[nx][ny] == old) {

                    visited[nx][ny] = true;
                    q.add(new Point(nx, ny));
                }
            }
        }
        return true;
    }

    // =========================
    // BOT MOVE
    // =========================

    void botMove() {
        int color = getBestColorDC();
        if (color == -1) return;

        floodFill(color);
        moves++;
        playerTurn = true;
        checkEnd();
        repaint();
    }

    // =========================
    // UNDO / REDO
    // =========================

    void saveState() {
        undo.push(copyGrid());
        redo.clear();
    }

    void undoMove() {
        if (!undo.empty() && !gameOver) {
            redo.push(copyGrid());
            grid = undo.pop();
            moves--;
            redoBtn.setEnabled(true);
            undoBtn.setEnabled(!undo.empty());
            updateMoves();
            repaint();
        }
    }

    void redoMove() {
        if (!redo.empty() && !gameOver) {
            undo.push(copyGrid());
            grid = redo.pop();
            moves++;
            undoBtn.setEnabled(true);
            redoBtn.setEnabled(!redo.empty());
            updateMoves();
            repaint();
        }
    }

    int[][] copyGrid() {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            copy[i] = Arrays.copyOf(grid[i], SIZE);
        return copy;
    }

    // =========================
    // GAME STATUS
    // =========================

    void checkEnd() {
        updateMoves();
        if (isSolved()) {
            gameOver = true;
            JOptionPane.showMessageDialog(this,
                    "🎉 You Won in " + moves + " moves!");
        }
    }

    boolean isSolved() {
        int c = grid[0][0];
        for (int[] row : grid)
            for (int x : row)
                if (x != c) return false;
        return true;
    }

    void updateMoves() {
        movesLabel.setText("Moves: " + moves + "    |    Team: " + TEAM_NAME);
    }

    // =========================
    // BOARD CLASS
    // =========================

    class Board extends JPanel {

        Board() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    if (gameOver || !playerTurn) return;

                    int row = e.getY() / CELL;
                    int col = e.getX() / CELL;

                    if (row < 0 || col < 0 || row >= SIZE || col >= SIZE)
                        return;

                    // Only count the move if the flood fill actually changed the board
                    if (floodFill(grid[row][col])) {
                        moves++;
                        playerTurn = !vsBot;
                        checkEnd();
                        repaint();

                        if (vsBot && !gameOver)
                            botMove();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {

                    g.setColor(ALL_COLORS[grid[i][j]]);
                    g.fillRect(j * CELL, i * CELL, CELL, CELL);

                    g.setColor(Color.BLACK);
                    g.drawRect(j * CELL, i * CELL, CELL, CELL);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FloodItPro::new);
    }
}