import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class FloodItDP extends JFrame {

    Color[] ALL_COLORS = {
            Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.MAGENTA, Color.ORANGE
    };

    String[] COLOR_NAMES = {
            "Red","Green","Blue","Yellow","Magenta","Orange"
    };

    int SIZE = 6;
    int COLOR_COUNT = 6;
    int CELL = 40;

    int[][] grid;
    int moves = 0;

    boolean vsBot = false;
    boolean gameOver = false;

    Stack<int[][]> undo = new Stack<>();
    Stack<int[][]> redo = new Stack<>();

    JLabel movesLabel;
    JLabel hintLabel;

    JComboBox<String> modeBox;
    JComboBox<Integer> sizeBox;
    JComboBox<Integer> colorBox;

    Solver solver;
    Board board;

    public FloodItDP() {

        setTitle("Flood-It (DP Solver)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createToolbar();
        createBoard();
        createBottomPanel();

        newGame();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void createToolbar() {

        JPanel bar = new JPanel();

        modeBox = new JComboBox<>(new String[]{
                "Single Player",
                "Player vs Bot"
        });

        sizeBox = new JComboBox<>();
        for(int i=4;i<=10;i++) sizeBox.addItem(i);
        sizeBox.setSelectedItem(6);

        colorBox = new JComboBox<>();
        for(int i=3;i<=6;i++) colorBox.addItem(i);
        colorBox.setSelectedItem(6);

        JButton newBtn = new JButton("New Game");
        JButton undoBtn = new JButton("Undo");
        JButton redoBtn = new JButton("Redo");
        JButton hintBtn = new JButton("Hint");

        newBtn.addActionListener(e->newGame());
        undoBtn.addActionListener(e->undoMove());
        redoBtn.addActionListener(e->redoMove());
        hintBtn.addActionListener(e->showHint());

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

        add(bar,BorderLayout.NORTH);
    }

    void createBoard() {

        board = new Board();

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(board);

        add(wrapper,BorderLayout.CENTER);
    }

    void createBottomPanel() {

        JPanel bottom = new JPanel(new BorderLayout());

        movesLabel = new JLabel("",JLabel.CENTER);
        movesLabel.setFont(new Font("Arial",Font.BOLD,16));

        hintLabel = new JLabel(" ",JLabel.CENTER);

        bottom.add(movesLabel,BorderLayout.CENTER);
        bottom.add(hintLabel,BorderLayout.SOUTH);

        add(bottom,BorderLayout.SOUTH);
    }

    void newGame() {

        SIZE = (int)sizeBox.getSelectedItem();
        COLOR_COUNT = (int)colorBox.getSelectedItem();
        vsBot = modeBox.getSelectedIndex()==1;

        grid = new int[SIZE][SIZE];

        Random r = new Random();

        for(int i=0;i<SIZE;i++)
            for(int j=0;j<SIZE;j++)
                grid[i][j] = r.nextInt(COLOR_COUNT);

        solver = new Solver(SIZE,COLOR_COUNT);

        undo.clear();
        redo.clear();

        moves = 0;
        gameOver = false;

        board.setPreferredSize(new Dimension(SIZE*CELL,SIZE*CELL));

        pack();
        updateMoves();
        repaint();
    }

    void showHint(){

        if(gameOver) return;

        int best = solver.bestMove(grid);

        hintLabel.setText("Hint: Try "+COLOR_NAMES[best]);
    }

    void floodFill(int color){

        int old = grid[0][0];
        if(old==color) return;

        Queue<Point> q = new LinkedList<>();
        boolean[][] vis = new boolean[SIZE][SIZE];

        q.add(new Point(0,0));
        vis[0][0]=true;

        int[][] d={{1,0},{-1,0},{0,1},{0,-1}};

        while(!q.isEmpty()){

            Point p=q.poll();
            grid[p.x][p.y]=color;

            for(int[] dir:d){

                int nx=p.x+dir[0];
                int ny=p.y+dir[1];

                if(nx>=0 && ny>=0 && nx<SIZE && ny<SIZE
                        && !vis[nx][ny] && grid[nx][ny]==old){

                    vis[nx][ny]=true;
                    q.add(new Point(nx,ny));
                }
            }
        }
    }

    void botMove(){

        int color = solver.bestMove(grid);

        floodFill(color);
        moves++;

        checkEnd();
        repaint();
    }

    void saveState(){
        undo.push(copyGrid());
        redo.clear();
    }

    void undoMove(){

        if(!undo.empty() && !gameOver){

            redo.push(copyGrid());
            grid=undo.pop();
            moves--;

            updateMoves();
            repaint();
        }
    }

    void redoMove(){

        if(!redo.empty() && !gameOver){

            undo.push(copyGrid());
            grid=redo.pop();
            moves++;

            updateMoves();
            repaint();
        }
    }

    int[][] copyGrid(){

        int[][] c=new int[SIZE][SIZE];

        for(int i=0;i<SIZE;i++)
            c[i]=Arrays.copyOf(grid[i],SIZE);

        return c;
    }

    void checkEnd(){

        updateMoves();

        if(isSolved()){

            gameOver=true;

            JOptionPane.showMessageDialog(this,
                    "Solved in "+moves+" moves!");
        }
    }

    boolean isSolved(){

        int c=grid[0][0];

        for(int[] r:grid)
            for(int x:r)
                if(x!=c) return false;

        return true;
    }

    void updateMoves(){
        movesLabel.setText("Moves: "+moves);
    }

    class Board extends JPanel{

        Board(){

            addMouseListener(new MouseAdapter(){

                public void mouseClicked(MouseEvent e){

                    if(gameOver) return;

                    int x=e.getY()/CELL;
                    int y=e.getX()/CELL;

                    if(x<0||y<0||x>=SIZE||y>=SIZE) return;

                    saveState();

                    floodFill(grid[x][y]);
                    moves++;

                    checkEnd();
                    repaint();

                    if(vsBot && !gameOver)
                        botMove();
                }
            });
        }

        protected void paintComponent(Graphics g){

            super.paintComponent(g);

            for(int i=0;i<SIZE;i++)
                for(int j=0;j<SIZE;j++){

                    g.setColor(ALL_COLORS[grid[i][j]]);
                    g.fillRect(j*CELL,i*CELL,CELL,CELL);

                    g.setColor(Color.BLACK);
                    g.drawRect(j*CELL,i*CELL,CELL,CELL);
                }
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new FloodItDP());
    }
}

class Solver{

    int SIZE;
    int COLORS;

    HashMap<Long,Integer> dp = new HashMap<>();

    Solver(int s,int c){
        SIZE=s;
        COLORS=c;
    }

    int bestMove(int[][] board){

        int bestColor = board[0][0];
        int bestCost = Integer.MAX_VALUE;

        Set<Integer> options = neighborColors(board);

        for(int c:options){

            int[][] next = copy(board);
            flood(next,c);

            int cost = 1 + solve(next);

            if(cost < bestCost){
                bestCost = cost;
                bestColor = c;
            }
        }

        return bestColor;
    }

    int solve(int[][] board){

        if(solved(board)) return 0;

        long key = encode(board);

        if(dp.containsKey(key))
            return dp.get(key);

        int best=Integer.MAX_VALUE;

        Set<Integer> options = neighborColors(board);

        for(int c:options){

            int[][] next = copy(board);
            flood(next,c);

            int cost = 1 + solve(next);

            best=Math.min(best,cost);
        }

        dp.put(key,best);

        return best;
    }

    Set<Integer> neighborColors(int[][] g){

        boolean[][] vis=new boolean[SIZE][SIZE];
        Queue<int[]> q=new LinkedList<>();

        int start=g[0][0];

        q.add(new int[]{0,0});
        vis[0][0]=true;

        int[][] d={{1,0},{-1,0},{0,1},{0,-1}};

        Set<Integer> colors=new HashSet<>();

        while(!q.isEmpty()){

            int[] p=q.poll();
            int x=p[0],y=p[1];

            for(int[] dir:d){

                int nx=x+dir[0];
                int ny=y+dir[1];

                if(nx<0||ny<0||nx>=SIZE||ny>=SIZE) continue;

                if(g[nx][ny]==start && !vis[nx][ny]){
                    vis[nx][ny]=true;
                    q.add(new int[]{nx,ny});
                }
                else if(g[nx][ny]!=start){
                    colors.add(g[nx][ny]);
                }
            }
        }

        return colors;
    }

    boolean solved(int[][] g){

        int c=g[0][0];

        for(int[] r:g)
            for(int x:r)
                if(x!=c) return false;

        return true;
    }

    long encode(int[][] g){

        long key=0;

        for(int i=0;i<SIZE;i++)
            for(int j=0;j<SIZE;j++)
                key=key*7+g[i][j];

        return key;
    }

    int[][] copy(int[][] g){

        int[][] c=new int[SIZE][SIZE];

        for(int i=0;i<SIZE;i++)
            System.arraycopy(g[i],0,c[i],0,SIZE);

        return c;
    }

    void flood(int[][] g,int color){

        int old=g[0][0];
        if(old==color) return;

        Queue<int[]> q=new LinkedList<>();
        boolean[][] vis=new boolean[SIZE][SIZE];

        q.add(new int[]{0,0});
        vis[0][0]=true;

        int[][] d={{1,0},{-1,0},{0,1},{0,-1}};

        while(!q.isEmpty()){

            int[] p=q.poll();
            int x=p[0],y=p[1];

            g[x][y]=color;

            for(int[] dir:d){

                int nx=x+dir[0];
                int ny=y+dir[1];

                if(nx>=0 && ny>=0 && nx<SIZE && ny<SIZE
                        && !vis[nx][ny] && g[nx][ny]==old){

                    vis[nx][ny]=true;
                    q.add(new int[]{nx,ny});
                }
            }
        }
    }
}