import java.awt.*;

public class Stone {
    public final static int BLACK = 1;  // 黒=1
    public final static int WHITE = 2;  // 白=2
    int obverse;                // 表面の色

    Stone(int color) {
        if (color == BLACK || color == WHITE)
            obverse = color;
        else
            System.out.println("[Warning] 黒か白でなければいけません");
    }

    boolean isBlack() { return obverse == BLACK;}
    boolean isWhite() { return obverse == WHITE;}

    // 表面の色を設定
    void setObverse(int color) {
        if (color == BLACK || color == WHITE)
            obverse = color;
        else
            System.out.println("[Warning] 黒か白でなければいけません");
    }

    // 表面の色で中心p、半径radの円を塗りつぶす
    void paint(Graphics g, Point p, int rad) {
        if (obverse == BLACK) {
            g.setColor(Color.black);                            // ペンを黒に設定
            g.fillOval(p.x - rad, p.y - rad, rad * 2, rad * 2); // 円を描画
        }
        else if (obverse == WHITE) {
            g.setColor(Color.white);                            // ペンを白に設定
            g.fillOval(p.x - rad, p.y - rad, rad * 2, rad * 2); // 円を描画
        }
    }
}