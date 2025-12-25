import java.awt.*;
import java.util.ArrayList;

public class Board {
    int cnt = 4;                        // 石の数カウンタ
    ArrayList<Point> nextBlackGrids;    // 黒石を置く場合に相手の石を反転させることができる枡目の数カウンタ
    ArrayList<Point> nextWhiteGrids;    // 白石を置く場合に相手の石を反転させることができる枡目の数カウンタ
    Stone[][] stones = new Stone[9][9]; // 8x8の石配列
    boolean isBlackTurn;                // 黒の手番かどうか
    Player player1;             // コンピュータプレイヤー1(黒石)
    Player player2;             // コンピュータプレイヤー2(白石)

    // コンストラクタ(初期化)
    Board(int Player1Type, int Player2Type) {
        isBlackTurn = true;

        if (!Player.isHuman(Player1Type))
            player1 = new Player(Stone.BLACK, Player.type_computer, Player1Type); // 黒石を置くコンピュータプレイヤーを作成
        else player1 = new Player(Stone.BLACK, Player.type_human, Player1Type);
        if (!Player.isHuman(Player2Type))
            player2 = new Player(Stone.WHITE, Player.type_computer, Player2Type); // 白石を置くコンピュータプレイヤーを作成
        else player2 = new Player(Stone.WHITE, Player.type_human, Player2Type);

        stones[4][4] = new Stone(Stone.BLACK); // 黒は1, 白は2
        stones[5][5] = new Stone(Stone.BLACK);
        stones[4][5] = new Stone(Stone.WHITE);
        stones[5][4] = new Stone(Stone.WHITE);

        evaluateBoard();
    }
    Board copy() {
        // player の中身は使わないのでダミーで
        Board cp = new Board(0, 0);

        cp.cnt = this.cnt;
        cp.isBlackTurn = this.isBlackTurn;
        cp.stones = new Stone[9][9];
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                if (this.stones[r][c] != null) {
                    cp.stones[r][c] = new Stone(this.stones[r][c].obverse);
                }
            }
        }
        cp.nextBlackGrids = new ArrayList<>();
        cp.nextWhiteGrids = new ArrayList<>();
        cp.evaluateBoard();

        return cp;
    }

    Point centerPoint(int row, int col, int unit_size) {
        return new Point(col * unit_size + unit_size / 2, row * unit_size + unit_size / 2);
    }
    void paintStone(Graphics g, int unit_size, int row, int column) {
        stones[row][column].paint(g, centerPoint(row, column, unit_size), unit_size / 2 - 8);
    }

    void update(int row, int column, int color) {
        stones[row][column] = new Stone(color);
        // 石の反転処理
        int oppositeColor = (color == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;

        int[] dr = {-1, 1, 0, 0, -1, -1, 1, 1};
        int[] dc = {0, 0, -1, 1, -1, 1, -1, 1};
        for (int dir = 0; dir < 8; dir++) {
            int i = row + dr[dir];
            int j = column + dc[dir];
            boolean foundOpposite = false;

            while (i >= 1 && i <= 8 && j >= 1 && j <= 8 && stones[i][j] != null) {
                if (stones[i][j].obverse == oppositeColor) {
                    foundOpposite = true;
                    i += dr[dir];
                    j += dc[dir];
                } else {
                    if (foundOpposite && stones[i][j].obverse == color) {
                        // 石を反転させる
                        int x = row + dr[dir];
                        int y = column + dc[dir];
                        while (x != i || y != j) {
                            stones[x][y].setObverse(color);
                            x += dr[dir];
                            y += dc[dir];
                        }
                    }
                    break;
                }
            }
        }
    }
    boolean setStone(int row, int column, int color) {
        // 石を置く座標の妥当性がない場合
        if (row < 1 || row > 8 || column < 1 || column > 8) {
            System.out.println("[Warning] The position (" + row + ", " + column + ") is out of bounds.");
            return false;
        }
        // すでに石が置かれている場合
        if (stones[row][column] != null) {
            System.out.println("[Warning] There is already a stone at (" + row + ", " + column + ").");
            return false;
        }
        // オセロルールに従って石を置けない場合
        if (checkMove(row, column, color) == 0) {
            System.out.println("[Warning] The move is not valid at (" + row + ", " + column + ") according to Reversi rules.");
            return false;
        }
        // 石を置く
        System.out.println("[Control] Placing a new stone at (" + row + ", " + column + ") with color " + (color == Stone.BLACK ? "black" : "white") + ".");
        update(row, column, color);
        cnt++;
        return true;
    }

    int cntBlackStones() {
        int count = 0;
        for (int row = 1; row <= 8; row++)
            for (int column = 1; column <= 8; column++)
                if (stones[row][column] != null)
                    // 黒石のカウント
                    if (stones[row][column].isBlack())
                        count++;
        return count;
    }

    int checkMove(int row, int column, int color) {
        int ans = 0;
        int oppositeColor = (color == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;

        int[] dr = {-1, 1, 0, 0, -1, -1, 1, 1};
        int[] dc = {0, 0, -1, 1, -1, 1, -1, 1};

        for (int dir = 0; dir < 8; dir++) {
            int i = row + dr[dir];
            int j = column + dc[dir];
            boolean foundOpposite = false;
            int numflipped = 0;

            while (i >= 1 && i <= 8 && j >= 1 && j <= 8 && stones[i][j] != null) {
                if (stones[i][j].obverse == oppositeColor) {
                    foundOpposite = true;
                    i += dr[dir];
                    j += dc[dir];
                    numflipped++;
                }
                else {
                    if (foundOpposite && stones[i][j].obverse == color)
                        ans = Math.max(ans, numflipped);
                    break;
                }
            }
        }
        if (ans > 0)
            System.out.println("[Info] Move at (" + row + ", " + column + ") for color " + (color == Stone.BLACK ? "black" : "white") + " can flip " + ans + " stones.");
        return ans;
    }

    // オセロルールに従っていずれの石も配置できないか
    final void evaluateBoard() {
        nextBlackGrids = new ArrayList<>();
        nextWhiteGrids = new ArrayList<>();
        for (int row = 1; row <= 8; row++)
            for (int column = 1; column <= 8; column++)
                if (stones[row][column] == null) {
                    if (checkMove(row, column, Stone.BLACK) > 0)
                        nextBlackGrids.add(new Point(row, column));
                    if (checkMove(row, column, Stone.WHITE) > 0)
                        nextWhiteGrids.add(new Point(row, column));
                }

        if (nextBlackGrids.isEmpty()) System.out.println("[Info] No valid moves found for color black.");
        else System.out.println("[Info] Valid moves for color black: " + nextBlackGrids.size());
        if (nextWhiteGrids.isEmpty()) System.out.println("[Info] No valid moves found for color white.");
        else System.out.println("[Info] Valid moves for color white: " + nextWhiteGrids.size());
    }

    boolean nextTurn() {
        if (isBlackTurn) return nextWhiteGrids.isEmpty();
        else return !nextBlackGrids.isEmpty();
    }

    // 画面描画
    void paint(Graphics g, int unit_size) {
        // 背景
        g.setColor(Color.black);
        g.fillRect(0, 0, unit_size * 10, unit_size * 10);

        // 盤面
        g.setColor(new Color(0, 85, 0));
        g.fillRect(unit_size, unit_size, unit_size * 8, unit_size * 8);

        // 横線
        g.setColor(Color.black);
        for (int i = 0; i < 9; i++)
            g.drawLine(unit_size, (1 + i) * unit_size, unit_size * 9, (1 + i) * unit_size);

        // 縦線
        for (int i = 0; i < 9; i++)
            g.drawLine((1 + i) * unit_size, unit_size, (1 + i) * unit_size, unit_size * 9);

        // 目印
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++)
                g.fillRect((i * 4 + 3) * unit_size - 5, (j * 4 + 3) * unit_size - 5, 10, 10);

        // 石の描画
        for (int row = 1; row <= 8; row++)
            for (int column = 1; column <= 8; column++)
                if (stones[row][column] != null)
                    paintStone(g, unit_size, row, column);

        g.setFont(new Font("Arial", Font.BOLD, 18));

        // 石の数表示
        g.setColor(Color.white);
        String blackCountStr = "Black: " + cntBlackStones();
        String whiteCountStr = "White: " + (cnt - cntBlackStones());
        g.drawString("[" + blackCountStr + ", " + whiteCountStr + "]", unit_size / 2, (int)(unit_size * 9.5));

        // 手番を表示
        g.setColor(Color.white);
        String turnStr = isBlackTurn ? "Black's Turn" : "White's Turn";
        g.drawString(turnStr, unit_size / 2, unit_size / 2);
    }
}