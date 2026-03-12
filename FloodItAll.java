import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

// ============================================================
//  FloodItAll — Five grids in one window
//  Grid 1: Bucket Sort + BFS  (Bot)
//  Grid 2: Divide & Conquer + Greedy  (Bot)
//  Grid 3: Backtracking  (Bot)  ← logic from FloodItBt
//  Grid 4: Dynamic Programming  (Bot)
//  Grid 5: Human Player
// ============================================================

public class FloodItAll extends JFrame {

    // ── shared palette ──────────────────────────────────────
    static final Color[] ALL_COLORS = {
        new Color(220, 50,  50),   // Red
        new Color(50,  180, 80),   // Green
        new Color(50,  100, 220),  // Blue
        new Color(240, 200, 40),   // Yellow
        new Color(180, 60,  220),  // Magenta
        new Color(240, 130, 30)    // Orange
    };

    static final String[] COLOR_NAMES = {
        "Red","Green","Blue","Yellow","Magenta","Orange"
    };

    // ── config state (shared) ────────────────────────────────
    int SIZE        = 6;
    int COLOR_COUNT = 6;
    final int CELL  = 55;   // cell pixel size

    int[][] sharedGrid;     // the one random board all grids copy from

    // ── toolbar widgets ──────────────────────────────────────
    JComboBox<Integer> sizeBox;
    JComboBox<Integer> colorBox;
    JLabel statusBar;

    // ── the five grid panels ─────────────────────────────────
    BFSGrid       g1;
    DCGrid        g2;
    BTGrid        g3;
    DPGrid        g4;
    HumanGrid     g5;

    // ════════════════════════════════════════════════════════
    public FloodItAll() {
        setTitle("Flood-It  |  All Five Algorithms");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(18, 18, 28));

        buildToolbar();
        buildGridArea();
        buildStatusBar();

        newGame();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── toolbar ─────────────────────────────────────────────
    private void buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        bar.setBackground(new Color(26, 26, 40));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(60, 60, 100)));

        JLabel title = new JLabel("⬛ FLOOD-IT");
        title.setFont(new Font("Monospaced", Font.BOLD, 18));
        title.setForeground(new Color(130, 200, 255));

        sizeBox = new JComboBox<>();
        for (int i = 4; i <= 6; i++) sizeBox.addItem(i);
        sizeBox.setSelectedItem(6);
        styleCombo(sizeBox);

        colorBox = new JComboBox<>();
        for (int i = 2; i <= 6; i++) colorBox.addItem(i);
        colorBox.setSelectedItem(6);
        styleCombo(colorBox);

        JButton newBtn = styledButton("▶  NEW GAME");
        newBtn.addActionListener(e -> newGame());

        bar.add(title);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(styledLabel("Grid Size:"));
        bar.add(sizeBox);
        bar.add(styledLabel("Colors:"));
        bar.add(colorBox);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(newBtn);

        add(bar, BorderLayout.NORTH);
    }

    private void buildGridArea() {
        g1 = new BFSGrid();
        g2 = new DCGrid();
        g3 = new BTGrid();
        g4 = new DPGrid();
        g5 = new HumanGrid();

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 14));
        topRow.setOpaque(false);
        topRow.add(wrapGrid(g1, "① Bucket Sort + BFS",     new Color(50, 200, 150)));
        topRow.add(wrapGrid(g2, "② Divide & Conquer",       new Color(100, 160, 255)));
        topRow.add(wrapGrid(g3, "③ Backtracking",           new Color(255, 140, 80)));

        JPanel botRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 14));
        botRow.setOpaque(false);
        botRow.add(wrapGrid(g4, "④ Dynamic Programming",    new Color(200, 100, 255)));
        botRow.add(wrapGrid(g5, "⑤ Human Player  🎮",       new Color(255, 210, 60)));

        JPanel all = new JPanel();
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.setOpaque(false);
        all.add(topRow);
        all.add(botRow);

        JScrollPane scroll = new JScrollPane(all);
        scroll.getViewport().setBackground(new Color(18, 18, 28));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel wrapGrid(JPanel inner, String label, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(28, 28, 44));
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(accent, 2, 12),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lbl = new JLabel(label, JLabel.CENTER);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        lbl.setForeground(accent);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        card.add(lbl,   BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private void buildStatusBar() {
        statusBar = new JLabel("  Ready — press NEW GAME", JLabel.LEFT);
        statusBar.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusBar.setForeground(new Color(120, 120, 160));
        statusBar.setBackground(new Color(20, 20, 32));
        statusBar.setOpaque(true);
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        add(statusBar, BorderLayout.SOUTH);
    }

    // ── start / restart game ─────────────────────────────────
    void newGame() {
        SIZE        = (int) sizeBox.getSelectedItem();
        COLOR_COUNT = (int) colorBox.getSelectedItem();

        sharedGrid = new int[SIZE][SIZE];
        Random rnd = new Random();
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                sharedGrid[i][j] = rnd.nextInt(COLOR_COUNT);

        int dim = SIZE * CELL;

        g1.init(copyOf(sharedGrid), SIZE, COLOR_COUNT, dim);
        g2.init(copyOf(sharedGrid), SIZE, COLOR_COUNT, dim);
        g3.init(copyOf(sharedGrid), SIZE, COLOR_COUNT, dim);
        g4.init(copyOf(sharedGrid), SIZE, COLOR_COUNT, dim);
        g5.init(copyOf(sharedGrid), SIZE, COLOR_COUNT, dim);

        pack();
        statusBar.setText("  New game started — " + SIZE + "×" + SIZE
                + "  |  " + COLOR_COUNT + " colors");

        javax.swing.Timer t = new javax.swing.Timer(400, null);
        t.addActionListener(e -> {
            g1.startSolving();
            g2.startSolving();
            g3.startSolving();
            g4.startSolving();
            t.stop();
        });
        t.setRepeats(false);
        t.start();
    }

    // ── helpers ──────────────────────────────────────────────
    static int[][] copyOf(int[][] src) {
        int n = src.length;
        int[][] c = new int[n][n];
        for (int i = 0; i < n; i++)
            c[i] = Arrays.copyOf(src[i], n);
        return c;
    }

    static void floodFill(int[][] grid, int sz, int color) {
        int old = grid[0][0];
        if (old == color) return;
        boolean[][] vis = new boolean[sz][sz];
        Queue<int[]> q = new LinkedList<>();
        q.add(new int[]{0, 0});
        vis[0][0] = true;
        int[][] d = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!q.isEmpty()) {
            int[] p = q.poll();
            grid[p[0]][p[1]] = color;
            for (int[] dir : d) {
                int nx = p[0]+dir[0], ny = p[1]+dir[1];
                if (nx>=0 && ny>=0 && nx<sz && ny<sz
                        && !vis[nx][ny] && grid[nx][ny]==old) {
                    vis[nx][ny] = true;
                    q.add(new int[]{nx, ny});
                }
            }
        }
    }

    static boolean isSolved(int[][] grid, int sz) {
        int c = grid[0][0];
        for (int[] row : grid) for (int x : row) if (x != c) return false;
        return true;
    }

    // styling helpers
    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(170, 170, 200));
        l.setFont(new Font("Monospaced", Font.PLAIN, 13));
        return l;
    }

    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Monospaced", Font.BOLD, 13));
        b.setForeground(new Color(20, 20, 40));
        b.setBackground(new Color(80, 200, 160));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleCombo(JComboBox<?> c) {
        c.setFont(new Font("Monospaced", Font.PLAIN, 13));
        c.setForeground(new Color(220, 220, 255));
        c.setBackground(new Color(40, 40, 60));
        c.setPreferredSize(new Dimension(70, 28));
    }

    // ════════════════════════════════════════════════════════
    //  BASE BOT GRID
    // ════════════════════════════════════════════════════════
    abstract class BotGrid extends JPanel {

        int[][] grid;
        int SIZE, COLORS;
        int moves = 0;
        boolean done = false;
        java.util.List<Integer> moveSequence = new ArrayList<>();
        int playIndex = 0;
        javax.swing.Timer animTimer;
        JLabel moveLabel;

        BotGrid() {
            setOpaque(false);
            setLayout(new BorderLayout());
            moveLabel = new JLabel("Moves: 0", JLabel.CENTER);
            moveLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
            moveLabel.setForeground(new Color(200, 200, 255));
            add(moveLabel, BorderLayout.SOUTH);
        }

        void init(int[][] g, int sz, int colors, int dim) {
            if (animTimer != null) animTimer.stop();
            grid   = g;
            SIZE   = sz;
            COLORS = colors;
            moves  = 0;
            done   = false;
            moveSequence.clear();
            playIndex = 0;
            Dimension d = new Dimension(dim, dim + 24);
            setPreferredSize(d);
            moveLabel.setText("Moves: 0");
            repaint();
        }

        void startSolving() {
            int[][] copy = copyOf(grid);
            moveSequence.clear();
            solve(copy, moveSequence);
            playIndex = 0;
            grid = copyOf(sharedGrid);
            animateNext();
        }

        abstract void solve(int[][] board, java.util.List<Integer> seq);

        void animateNext() {
            if (animTimer != null) animTimer.stop();
            if (playIndex >= moveSequence.size()) {
                done = true;
                moveLabel.setForeground(new Color(80, 240, 140));
                return;
            }
            animTimer = new javax.swing.Timer(340, null);
            animTimer.addActionListener(e -> {
                if (playIndex < moveSequence.size()) {
                    int c = moveSequence.get(playIndex++);
                    floodFill(grid, SIZE, c);
                    moves++;
                    moveLabel.setText("Moves: " + moves);
                    repaint();
                    if (isSolved(grid, SIZE)) {
                        done = true;
                        moveLabel.setForeground(new Color(80, 240, 140));
                        ((javax.swing.Timer)e.getSource()).stop();
                    }
                } else {
                    ((javax.swing.Timer)e.getSource()).stop();
                }
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (grid == null) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    Color c = ALL_COLORS[grid[i][j]];
                    g2.setColor(c);
                    g2.fillRoundRect(j*CELL+1, i*CELL+1, CELL-2, CELL-2, 8, 8);
                    g2.setColor(new Color(0,0,0,60));
                    g2.drawRoundRect(j*CELL+1, i*CELL+1, CELL-2, CELL-2, 8, 8);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  GRID 1 — Bucket Sort + BFS
    // ════════════════════════════════════════════════════════
    class BFSGrid extends BotGrid {

        @Override
        void solve(int[][] board, java.util.List<Integer> seq) {
            int safety = SIZE * SIZE * COLORS + 50;
            while (!isSolved(board, SIZE) && safety-- > 0) {
                int best = bfsBestMove(board);
                seq.add(best);
                floodFill(board, SIZE, best);
            }
        }

        int bfsBestMove(int[][] board) {
            int[] buckets = new int[COLORS];
            boolean[][] vis = new boolean[SIZE][SIZE];
            Queue<int[]> q = new LinkedList<>();
            int cur = board[0][0];
            q.add(new int[]{0,0}); vis[0][0] = true;
            int[][] d = {{1,0},{-1,0},{0,1},{0,-1}};
            while (!q.isEmpty()) {
                int[] p = q.poll();
                for (int[] dir : d) {
                    int nx=p[0]+dir[0], ny=p[1]+dir[1];
                    if (nx<0||ny<0||nx>=SIZE||ny>=SIZE) continue;
                    if (board[nx][ny] == cur && !vis[nx][ny]) {
                        vis[nx][ny] = true; q.add(new int[]{nx,ny});
                    } else if (board[nx][ny] != cur) {
                        buckets[board[nx][ny]]++;
                    }
                }
            }
            int best = -1, max = -1;
            for (int c = 0; c < COLORS; c++) {
                if (c == cur) continue;
                if (buckets[c] > max) { max = buckets[c]; best = c; }
            }
            return (best == -1) ? (cur + 1) % COLORS : best;
        }
    }

    // ════════════════════════════════════════════════════════
    //  GRID 2 — Divide & Conquer + Greedy
    // ════════════════════════════════════════════════════════
    class DCGrid extends BotGrid {

        @Override
        void solve(int[][] board, java.util.List<Integer> seq) {
            int safety = SIZE * SIZE * COLORS + 50;
            while (!isSolved(board, SIZE) && safety-- > 0) {
                int best = dcBestMove(board);
                seq.add(best);
                floodFill(board, SIZE, best);
            }
        }

        int dcBestMove(int[][] board) {
            boolean[][] region = currentRegion(board);
            int[] counts = countBoundaryDC(0, SIZE-1, 0, SIZE-1, board, region);
            int oldColor = board[0][0];
            int best = -1, max = -1;
            for (int c = 0; c < COLORS; c++) {
                if (c == oldColor) continue;
                if (counts[c] > max) { max = counts[c]; best = c; }
            }
            return (best == -1) ? (oldColor+1)%COLORS : best;
        }

        boolean[][] currentRegion(int[][] board) {
            boolean[][] reg = new boolean[SIZE][SIZE];
            int cur = board[0][0];
            Queue<int[]> q = new LinkedList<>();
            q.add(new int[]{0,0}); reg[0][0] = true;
            int[][] d = {{1,0},{-1,0},{0,1},{0,-1}};
            while (!q.isEmpty()) {
                int[] p = q.poll();
                for (int[] dir : d) {
                    int nx=p[0]+dir[0], ny=p[1]+dir[1];
                    if (nx>=0&&ny>=0&&nx<SIZE&&ny<SIZE&&!reg[nx][ny]&&board[nx][ny]==cur) {
                        reg[nx][ny]=true; q.add(new int[]{nx,ny});
                    }
                }
            }
            return reg;
        }

        int[] countBoundaryDC(int r1,int r2,int c1,int c2,int[][] board,boolean[][] reg) {
            int[] res = new int[COLORS];
            if ((r2-r1)<=2 && (c2-c1)<=2) {
                int[][] d = {{1,0},{-1,0},{0,1},{0,-1}};
                for (int i=r1;i<=r2;i++) for (int j=c1;j<=c2;j++) {
                    if (reg[i][j]) continue;
                    for (int[] dir:d) {
                        int ni=i+dir[0],nj=j+dir[1];
                        if (ni>=0&&nj>=0&&ni<SIZE&&nj<SIZE&&reg[ni][nj]) {
                            res[board[i][j]]++; break;
                        }
                    }
                }
                return res;
            }
            int mr=(r1+r2)/2, mc=(c1+c2)/2;
            int[] a=countBoundaryDC(r1,mr,c1,mc,board,reg);
            int[] b=countBoundaryDC(r1,mr,mc+1,c2,board,reg);
            int[] cc=countBoundaryDC(mr+1,r2,c1,mc,board,reg);
            int[] dd=countBoundaryDC(mr+1,r2,mc+1,c2,board,reg);
            for (int i=0;i<COLORS;i++) res[i]=a[i]+b[i]+cc[i]+dd[i];
            return res;
        }
    }

    // ════════════════════════════════════════════════════════
    //  GRID 3 — Backtracking  (logic from FloodItBt)
    // ════════════════════════════════════════════════════════
    class BTGrid extends BotGrid {

        // ── inner solver — exact copy of FloodSolverBT logic ──
        class Solver {

            int bestSolution;

            // Returns only the neighbor colors of the current flood region
            // (same as FloodSolverBT.neighborColors)
            Set<Integer> neighborColors(int[][] g) {
                boolean[][] vis = new boolean[SIZE][SIZE];
                Queue<int[]> q = new LinkedList<>();
                int start = g[0][0];
                q.add(new int[]{0, 0});
                vis[0][0] = true;
                int[][] d = {{1,0},{-1,0},{0,1},{0,-1}};
                Set<Integer> colors = new HashSet<>();
                while (!q.isEmpty()) {
                    int[] p = q.poll();
                    int x = p[0], y = p[1];
                    for (int[] dir : d) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];
                        if (nx < 0 || ny < 0 || nx >= SIZE || ny >= SIZE) continue;
                        if (g[nx][ny] == start && !vis[nx][ny]) {
                            vis[nx][ny] = true;
                            q.add(new int[]{nx, ny});
                        } else if (g[nx][ny] != start) {
                            colors.add(g[nx][ny]);
                        }
                    }
                }
                return colors;
            }

            // Recursive backtrack — no depth cap, pruned by bestSolution
            // (same as FloodSolverBT.backtrack)
            void backtrack(int[][] board, int depth, int prevColor) {
                if (isSolved(board, SIZE)) {
                    bestSolution = Math.min(bestSolution, depth);
                    return;
                }
                if (depth >= bestSolution) return;

                for (int c : neighborColors(board)) {
                    if (c == prevColor) continue;
                    int[][] next = copyOf(board);
                    floodFill(next, SIZE, c);
                    backtrack(next, depth + 1, c);
                }
            }

            // Pick the best first move — same as FloodSolverBT.bestMove
            int bestMove(int[][] board) {
                int bestColor = -1;
                int bestDepth = Integer.MAX_VALUE;

                for (int c : neighborColors(board)) {
                    int[][] next = copyOf(board);
                    floodFill(next, SIZE, c);

                    bestSolution = Integer.MAX_VALUE;
                    backtrack(next, 1, c);

                    if (bestSolution < bestDepth) {
                        bestDepth = bestSolution;
                        bestColor = c;
                    }
                }

                if (bestColor == -1)
                    bestColor = (board[0][0] + 1) % COLORS;

                return bestColor;
            }
        }

        @Override
        void solve(int[][] board, java.util.List<Integer> seq) {
            Solver solver = new Solver();
            int safety = SIZE * SIZE * COLORS + 50;
            while (!isSolved(board, SIZE) && safety-- > 0) {
                int best = solver.bestMove(board);
                seq.add(best);
                floodFill(board, SIZE, best);
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  GRID 4 — Dynamic Programming
    // ════════════════════════════════════════════════════════
    class DPGrid extends BotGrid {

        HashMap<Long, Integer> memo = new HashMap<>();

        @Override
        void solve(int[][] board, java.util.List<Integer> seq) {
            memo.clear();
            int safety = SIZE * SIZE * COLORS + 50;
            while (!isSolved(board, SIZE) && safety-- > 0) {
                int best = dpBestMove(board);
                seq.add(best);
                floodFill(board, SIZE, best);
            }
        }

        int dpBestMove(int[][] board) {
            int cur = board[0][0];
            Set<Integer> opts = neighborColors(board);
            int bestC = cur, bestCost = Integer.MAX_VALUE;
            for (int c : opts) {
                int[][] next = copyOf(board);
                floodFill(next, SIZE, c);
                int cost = 1 + dpSolve(next, 0);
                if (cost < bestCost) { bestCost = cost; bestC = c; }
            }
            return bestC;
        }

        int dpSolve(int[][] board, int depth) {
            if (isSolved(board, SIZE)) return 0;
            if (depth > SIZE * 3) return SIZE * SIZE;
            long key = encode(board);
            if (memo.containsKey(key)) return memo.get(key);
            Set<Integer> opts = neighborColors(board);
            int best = Integer.MAX_VALUE / 2;
            for (int c : opts) {
                int[][] next = copyOf(board);
                floodFill(next, SIZE, c);
                int v = 1 + dpSolve(next, depth+1);
                if (v < best) best = v;
            }
            memo.put(key, best);
            return best;
        }

        Set<Integer> neighborColors(int[][] g) {
            boolean[][] vis = new boolean[SIZE][SIZE];
            Queue<int[]> q = new LinkedList<>();
            int cur = g[0][0];
            q.add(new int[]{0,0}); vis[0][0]=true;
            int[][] d = {{1,0},{-1,0},{0,1},{0,-1}};
            Set<Integer> cols = new HashSet<>();
            while (!q.isEmpty()) {
                int[] p = q.poll();
                for (int[] dir:d) {
                    int nx=p[0]+dir[0],ny=p[1]+dir[1];
                    if (nx<0||ny<0||nx>=SIZE||ny>=SIZE) continue;
                    if (g[nx][ny]==cur && !vis[nx][ny]) { vis[nx][ny]=true; q.add(new int[]{nx,ny}); }
                    else if (g[nx][ny]!=cur) cols.add(g[nx][ny]);
                }
            }
            return cols;
        }

        long encode(int[][] g) {
            long key = 0;
            for (int[] row : g) for (int x : row) key = key * 7 + x;
            return key;
        }
    }

    // ════════════════════════════════════════════════════════
    //  GRID 5 — Human Player
    // ════════════════════════════════════════════════════════
    class HumanGrid extends JPanel {

        int[][] grid;
        int SIZE, COLORS;
        int moves = 0;
        boolean done = false;

        Stack<int[][]> undo = new Stack<>();
        Stack<int[][]> redo = new Stack<>();

        JLabel moveLabel;
        JLabel hintLabel;
        JButton undoBtn, redoBtn, hintBtn;

        HumanGrid() {
            setOpaque(false);
            setLayout(new BorderLayout(0, 4));

            JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 4));
            colorPanel.setOpaque(false);
            add(colorPanel, BorderLayout.NORTH);

            JPanel canvas = new GridCanvas();
            add(canvas, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new BorderLayout(0, 2));
            bottom.setOpaque(false);

            moveLabel = new JLabel("Moves: 0", JLabel.CENTER);
            moveLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
            moveLabel.setForeground(new Color(255, 230, 100));

            hintLabel = new JLabel(" ", JLabel.CENTER);
            hintLabel.setFont(new Font("Monospaced", Font.ITALIC, 11));
            hintLabel.setForeground(new Color(160, 220, 255));

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
            btnRow.setOpaque(false);

            undoBtn = humanBtn("↩ Undo", new Color(120, 120, 200));
            redoBtn = humanBtn("↪ Redo", new Color(120, 120, 200));
            hintBtn = humanBtn("💡 Hint", new Color(80, 200, 140));

            undoBtn.addActionListener(e -> undoMove());
            redoBtn.addActionListener(e -> redoMove());
            hintBtn.addActionListener(e -> showHint());

            btnRow.add(undoBtn);
            btnRow.add(redoBtn);
            btnRow.add(hintBtn);

            bottom.add(btnRow,    BorderLayout.NORTH);
            bottom.add(moveLabel, BorderLayout.CENTER);
            bottom.add(hintLabel, BorderLayout.SOUTH);

            add(bottom, BorderLayout.SOUTH);
        }

        void init(int[][] g, int sz, int colors, int dim) {
            grid   = g;
            SIZE   = sz;
            COLORS = colors;
            moves  = 0;
            done   = false;
            undo.clear(); redo.clear();

            moveLabel.setText("Moves: 0");
            moveLabel.setForeground(new Color(255, 230, 100));
            hintLabel.setText(" ");
            undoBtn.setEnabled(true);
            redoBtn.setEnabled(true);

            JPanel colorPanel = (JPanel) getComponent(0);
            colorPanel.removeAll();
            for (int c = 0; c < COLORS; c++) {
                final int col = c;
                JButton cb = new JButton();
                cb.setBackground(ALL_COLORS[c]);
                cb.setPreferredSize(new Dimension(32, 24));
                cb.setBorder(BorderFactory.createLineBorder(new Color(60,60,90), 2));
                cb.setFocusPainted(false);
                cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                cb.setToolTipText(COLOR_NAMES[c]);
                cb.addActionListener(e -> playerMove(col));
                colorPanel.add(cb);
            }
            colorPanel.revalidate();

            Dimension d = new Dimension(dim, dim + 24);
            getComponent(1).setPreferredSize(d);
            setPreferredSize(new Dimension(dim, dim + 90));
            repaint();
        }

        void playerMove(int color) {
            if (done) return;
            if (grid[0][0] == color) return;
            saveState();
            floodFill(grid, SIZE, color);
            moves++;
            hintLabel.setText(" ");
            moveLabel.setText("Moves: " + moves);
            repaint();
            if (isSolved(grid, SIZE)) {
                done = true;
                moveLabel.setText("✅ Done in " + moves + " moves!");
                moveLabel.setForeground(new Color(80, 240, 140));
            }
        }

        void showHint() {
            if (done) return;
            int best = -1, bestSz = -1;
            for (int c = 0; c < COLORS; c++) {
                if (c == grid[0][0]) continue;
                int[][] copy = copyOf(grid);
                floodFill(copy, SIZE, c);
                int sz = regionSize(copy);
                if (sz > bestSz) { bestSz = sz; best = c; }
            }
            hintLabel.setText("Hint: " + (best>=0 ? COLOR_NAMES[best] : "?"));
        }

        int regionSize(int[][] g) {
            boolean[][] vis = new boolean[SIZE][SIZE];
            Queue<int[]> q = new LinkedList<>();
            q.add(new int[]{0,0}); vis[0][0]=true;
            int cnt=0, col=g[0][0];
            int[][] d={{1,0},{-1,0},{0,1},{0,-1}};
            while (!q.isEmpty()) {
                int[] p=q.poll(); cnt++;
                for (int[] dir:d) {
                    int nx=p[0]+dir[0],ny=p[1]+dir[1];
                    if (nx>=0&&ny>=0&&nx<SIZE&&ny<SIZE&&!vis[nx][ny]&&g[nx][ny]==col) {
                        vis[nx][ny]=true; q.add(new int[]{nx,ny});
                    }
                }
            }
            return cnt;
        }

        void saveState()  { undo.push(copyOf(grid)); redo.clear(); }

        void undoMove() {
            if (!undo.empty() && !done) {
                redo.push(copyOf(grid)); grid=undo.pop(); moves--;
                moveLabel.setText("Moves: "+moves); hintLabel.setText(" "); repaint();
            }
        }

        void redoMove() {
            if (!redo.empty() && !done) {
                undo.push(copyOf(grid)); grid=redo.pop(); moves++;
                moveLabel.setText("Moves: "+moves); hintLabel.setText(" "); repaint();
            }
        }

        JButton humanBtn(String txt, Color bg) {
            JButton b = new JButton(txt);
            b.setFont(new Font("Monospaced", Font.BOLD, 11));
            b.setForeground(Color.WHITE);
            b.setBackground(bg);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        class GridCanvas extends JPanel {
            GridCanvas() {
                setOpaque(false);
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (done || grid == null) return;
                        int row = e.getY()/CELL;
                        int col = e.getX()/CELL;
                        if (row<0||col<0||row>=SIZE||col>=SIZE) return;
                        playerMove(grid[row][col]);
                    }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (grid == null) return;
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cur = grid[0][0];
                for (int i=0;i<SIZE;i++) for (int j=0;j<SIZE;j++) {
                    Color c = ALL_COLORS[grid[i][j]];
                    if (grid[i][j] == cur) {
                        g2.setColor(c.brighter());
                    } else {
                        g2.setColor(c);
                    }
                    g2.fillRoundRect(j*CELL+1,i*CELL+1,CELL-2,CELL-2,8,8);
                    g2.setColor(new Color(0,0,0,80));
                    g2.drawRoundRect(j*CELL+1,i*CELL+1,CELL-2,CELL-2,8,8);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════
    //  Rounded border helper
    // ════════════════════════════════════════════════════════
    static class RoundedBorder implements Border {
        private final Color color;
        private final int thick, arc;
        RoundedBorder(Color c, int t, int a) { color=c; thick=t; arc=a; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(thick+4,thick+4,thick+4,thick+4); }
        @Override public boolean isBorderOpaque() { return false; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thick));
            g2.drawRoundRect(x+thick/2,y+thick/2,w-thick,h-thick,arc,arc);
        }
    }

    // ════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new FloodItAll();
        });
    }
}