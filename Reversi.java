import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Reversi extends JPanel {
    public final static int UNIT_SIZE = 80;
    Board board;
    Player currentPlayer;  // 現在の手番のプレイヤー(保守なし、スレッドに送る用)
    final static String DROP_AUDIO_PATH = "content/audio/drop.wav";

    // コンストラクタ(初期化)
    public Reversi() {
        setPreferredSize(new Dimension(UNIT_SIZE * 10, UNIT_SIZE * 10));
        addMouseListener(new MouseProc());
    }

    // 画面描画
    @Override
    public void paintComponent(Graphics g) {
        board.paint(g, UNIT_SIZE);  // Boardクラスのpaintメソッドを呼び出す
    }

    // 起動
    public static void main(String[] args) {
        if (args.length != 0 && args.length != 2) {
            System.out.println("Usage: java Reversi [<Player1Strategy> <Player2Strategy>]");
            System.out.println("Player Strategy: 0 = human, 1 = random, 2 = greedy, 3 = board consideration, 4 = my tactics");
            System.exit(0);
        }

        JFrame f = new JFrame();
        Reversi app = new Reversi();
        app.board = new Board(Integer.parseInt(args.length == 2 ? args[0] : "0"), Integer.parseInt(args.length == 2 ? args[1] : "0"));
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(app);
        f.pack();
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        f.setTitle("Reversi");

        if (app.board.player1.isComputerPlayer()) {
            app.currentPlayer = app.board.player1;
            app.startComputerTurn();
        }
    }

    void end() {
        int blackCount = board.cntBlackStones();
        int whiteCount = board.cnt - blackCount;
        System.out.println("[Control] Game Over. Black: " + blackCount + ", White: " + whiteCount);
        String msg = "[黒:" + blackCount + "," + "白:" + whiteCount + "]で" + (blackCount > whiteCount ? "黒" : "白") + "の勝ち";
        JOptionPane.showMessageDialog(this, msg, "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
        System.out.println("[Info] The game has ended with black using strategy " + board.player1.strategy + " and white using strategy " + board.player2.strategy + ".");
        System.exit(0);
    }

    void startComputerTurn() {
        removeMouseListener(new MouseProc());
        Thread ct = new ComputerThread();
        ct.start();
    }

    // コンピュータの手番スレッド
    class ComputerThread extends Thread {
        @Override
        @SuppressWarnings("CallToPrintStackTrace")
        public void run() {
            try {
                Thread.sleep(1000);  // 1秒間待つ
                Point p = currentPlayer.nextMove(board);
                if (p.x != -1 && p.y != -1) {
                    if (!board.setStone(p.x, p.y, currentPlayer.getColor())) return;
                    AudioPlayer.playSound(DROP_AUDIO_PATH);
                    System.out.println("[Info] Computer chosed position (" + p.x + ", " + p.y + ").");
                    update();
                }
                else System.out.println("[Info] Computer has no valid move.");
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            if (board.isBlackTurn) {
                if (board.player1.isComputerPlayer()) {
                    currentPlayer = board.player1;
                    startComputerTurn();
                }
            }
            else {
                if (board.player2.isComputerPlayer()) {
                    currentPlayer = board.player2;
                    startComputerTurn();
                }
            }
            addMouseListener(new MouseProc());
        }
    }

    // 画面更新と手番管理
    private void update() {
        repaint();

        if (board.cnt >= 64) end();

        board.evaluateBoard();
        if (board.nextBlackGrids.isEmpty() && board.nextWhiteGrids.isEmpty()) end();

        boolean nextTurn = board.nextTurn();
        if (board.isBlackTurn == nextTurn) {
            System.out.println("[Info] " + (board.isBlackTurn ? "White": "Black") + " has no valid moves and must pass.");
            JOptionPane.showMessageDialog(Reversi.this, (board.isBlackTurn ? "White": "Black") + " has no valid moves and must pass.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
        else board.isBlackTurn = nextTurn;
        System.out.println("[Control] Next turn: " + (board.isBlackTurn ? "Black" : "White"));
    }

    // クリックされた時の処理用のクラス
    class MouseProc extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent me) {
            Point point = me.getPoint();
            int btn = me.getButton();     // ↓ 確認のための出力
            System.out.println("[Info] Clicked on (" + point.x + ", " + point.y + ")");
            if (btn == MouseEvent.BUTTON1 && board.player1.isHumanPlayer()) {   // 左クリックだったら黒石
                if (!board.isBlackTurn) {
                    System.out.println("[Warning] It's not black's turn.");
                    return;
                }
                int col = point.x / UNIT_SIZE;
                int row = point.y / UNIT_SIZE;
                if (board.setStone(row, col, Stone.BLACK)){
                    AudioPlayer.playSound(DROP_AUDIO_PATH);
                    update();
                    if (board.player2.isComputerPlayer()) {
                        currentPlayer = board.player2;
                        startComputerTurn();
                    }
                }
            }
            if (btn == MouseEvent.BUTTON3 && board.player2.isHumanPlayer()) {   // 左クリックだったら黒石
                if (board.isBlackTurn) {
                    System.out.println("[Warning] It's not white's turn.");
                    return;
                }
                int col = point.x / UNIT_SIZE;
                int row = point.y / UNIT_SIZE;
                if (board.setStone(row, col, Stone.WHITE)){
                    AudioPlayer.playSound(DROP_AUDIO_PATH);
                    update();
                    if (board.player1.isComputerPlayer()) {
                        currentPlayer = board.player1;
                        startComputerTurn();
                    }
                }
            }
        }
    }
}