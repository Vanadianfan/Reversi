import java.awt.*;
import java.util.ArrayList;

public class Player {
    public final static int type_human = 0;
    public final static int type_computer = 1;
    public int strategy; // コンピュータの戦略: 1=ランダム, 2=貪欲, 3=盤⾯考慮
    private int color;    // Stone.BLACK or Stone.WHITE
    private int type;     // type_human or type_computer

    Player (int c, int t, int strat) {
        if (c == Stone.BLACK || c == Stone.WHITE)
            color = c;
        else {
            System.out.println("プレイヤーの石は黒か白でなければいけません:" + c);
            System.exit(0);
        }
        if (t == type_human || t == type_computer)
            type = t;
        else {
            System.out.println("プレイヤーは人間かコンピュータでなければいけません:" + t);
            System.exit(0);
        }
        strategy = strat;
    }

    int getColor() { return color; }
    int getType() { return type; }

    static boolean isHuman(int strategy) { return strategy == 0; }
    static boolean isComputer(int strategy) { return strategy != 0; }
    boolean isHumanPlayer() { return type == type_human; }
    boolean isComputerPlayer() { return type == type_computer; }

    Point oneRandomPonitOf(ArrayList<Point> points) {
        if (points.isEmpty()) return new Point(-1, -1);
        int idx = (int)(Math.random() * points.size());
        return points.get(idx);
    }

    // ランダムに次の手を決定
    Point randomTactics(Board bd) {
        ArrayList<Point> nextGrids = (color == Stone.BLACK) ? bd.nextBlackGrids : bd.nextWhiteGrids;
        return oneRandomPonitOf(nextGrids);
    }
    // 貪欲によって次の手を決定
    Point greedyTactics(Board bd) {
        ArrayList<Point> nextGrids = (color == Stone.BLACK) ? bd.nextBlackGrids : bd.nextWhiteGrids;
        ArrayList<Point> candidates = new ArrayList<>();
        if (nextGrids.isEmpty()) return new Point(-1, -1); // 置ける場所がない場合

        int maxFlipped = -1;

        for (Point p: nextGrids) {
            int flippedCount = bd.checkMove(p.x, p.y, color);
            if (flippedCount > maxFlipped) {
                maxFlipped = flippedCount;
                candidates.add(p);
            }
            else if (flippedCount == maxFlipped) {
                candidates.add(p);
            }
        }

        return oneRandomPonitOf(candidates);
    }
    // weight + flippedCount * 10の最大値を与える手を選択, 複数ある場合にはランダムに選択
    Point BoardTactics(Board bd) {
        ArrayList<Point> nextGrids = (color == Stone.BLACK) ? bd.nextBlackGrids : bd.nextWhiteGrids;
        if (nextGrids.isEmpty()) return new Point(-1, -1); // 置ける場所がない場合

        int weight[][] = {
            {120, -20, 20, 5, 5, 20, -20, 120},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {5, -5, 3, 3, 3, 3, -5, 5},
            {5, -5, 3, 3, 3, 3, -5, 5},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {120, -20, 20, 5, 5, 20, -20, 120}
        };

        int maxScore = Integer.MIN_VALUE;
        ArrayList<Point> candidates = new ArrayList<>();
        for (Point p: nextGrids) {
            int flippedCount = bd.checkMove(p.x, p.y, color);
            int score = weight[p.x - 1][p.y - 1] + flippedCount * 10; // 重み付けスコア計算
            if (score > maxScore) {
                maxScore = score;
                candidates.clear();
                candidates.add(p);
            } else if (score == maxScore) {
                candidates.add(p);
            }
        }

        return oneRandomPonitOf(candidates);
    }

    // 角が空いている状態で，角の隣（C/Xマス）に打つのは危険，という判定
    boolean isDanger(Point p, Board bd) {
        int r = p.x, c = p.y;

        // 左上角 (1,1) が空いているとき： (1,2),(2,1),(2,2) は危険
        if (bd.stones[1][1] == null) {
            if ((r == 1 && c == 2) || (r == 2 && c == 1) || (r == 2 && c == 2)) return true;
        }
        // 右上角 (1,8)
        if (bd.stones[1][8] == null) {
            if ((r == 1 && c == 7) || (r == 2 && c == 8) || (r == 2 && c == 7)) return true;
        }
        // 左下角 (8,1)
        if (bd.stones[8][1] == null) {
            if ((r == 7 && c == 1) || (r == 8 && c == 2) || (r == 7 && c == 2)) return true;
        }
        // 右下角 (8,8)
        if (bd.stones[8][8] == null) {
            if ((r == 7 && c == 8) || (r == 8 && c == 7) || (r == 7 && c == 7)) return true;
        }

        return false;
    }
    Point MyTactics(Board bd) {
        ArrayList<Point> nextGrids = (color == Stone.BLACK) ? bd.nextBlackGrids : bd.nextWhiteGrids;
        if (nextGrids.isEmpty()) return new Point(-1, -1);

        int[][] weight = {
            {120, -20, 20, 5, 5, 20, -20, 120},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {5, -5, 3, 3, 3, 3, -5, 5},
            {5, -5, 3, 3, 3, 3, -5, 5},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {120, -20, 20, 5, 5, 20, -20, 120}
        };

        int maxScore = Integer.MIN_VALUE;
        ArrayList<Point> candidates = new ArrayList<>();

        for (Point p : nextGrids) {
            int flipped = bd.checkMove(p.x, p.y, color);
            int boardScore = weight[p.x - 1][p.y - 1];
            int flipScore  = flipped * 5;

            // 危険マス（角が空いている時の X / C）
            int dangerPenalty = 0;
            if (isDanger(p, bd)) dangerPenalty = 80;

            // 相手のモビリティ
            Board copy = bd.copy();
            copy.setStone(p.x, p.y, color);
            int oppMoves = (color == Stone.BLACK) ? copy.nextWhiteGrids.size() : copy.nextBlackGrids.size();
            int mobilityPenalty = oppMoves * 10;

            int score = boardScore + flipScore - dangerPenalty - mobilityPenalty;

            if (score > maxScore) {
                maxScore = score;
                candidates.clear();
                candidates.add(p);
            } else if (score == maxScore) {
                candidates.add(p);
            }
        }

        return oneRandomPonitOf(candidates);
    }

    Point nextMove(Board bd) {
        if (type != type_computer) {
            System.err.println("[Error] Human players cannot make automated moves.");
            return new Point(-1, -1);
        }
        switch (strategy) {
            case 1 -> {
                return randomTactics(bd);
            }
            case 2 -> {
                return greedyTactics(bd);
            }
            case 3 -> {
                return BoardTactics(bd);
            }
            case 4 -> {
                return MyTactics(bd);
            }
            default -> throw new AssertionError();
        }
    }
}