import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

public class Game extends JPanel {
    private Board layout;

    public static final int WIDTH = 20;
    public static final int HEIGHT = 20;
    public static final int X = 24;
    public static final int Y = 20;
    protected JFrame window;

    static int uncover;
    public static final int BOMB_COUNT = 99;
    private int score_board_height = 0;
    private boolean end = false;
    private boolean win = false;
    private int flags;
    private int timeLimit;

    private static BufferedImage FACING_DOWN_IMAGE = null;
    private static BufferedImage FLAGGED_IMAGE = null;
    private static BufferedImage BOMB_IMAGE = null;

    static {
        try {
            FACING_DOWN_IMAGE = ImageIO.read(new File("src/img/" + Game.DRAW_FACING_DOWN + ".png"));
            FLAGGED_IMAGE = ImageIO.read(new File("src/img/" + Game.DRAW_FLAGGED + ".png"));
            BOMB_IMAGE = ImageIO.read(new File("src/img/" + Game.DRAW_BOMB + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final int DRAW_BOMB = 9;
    public static final int DRAW_FACING_DOWN = 10;
    public static final int DRAW_FLAGGED = 11;


    private final JLabel status;

    public Game(JLabel status) {
        this.status = status;
        initGame();
    }

    public void initGame() {
        setPreferredSize(new Dimension(X * WIDTH, Y * HEIGHT + score_board_height));
        addMouseListener(new MyMouseAdapter());
        newGame();
    }

    private void newGame() {
        end = false;
        win = false;
        uncover = 0;
        timeLimit = 211;
        status.setText("TIME LEFT: " + timeLimit);
        try {
            layout = new Board();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void paintComponent(Graphics g) {
        if(end){
            for(int r = 0; r < layout.getLength(); r ++){
                for(int c = 0; c < layout.getHeight(); c ++){
                    if (layout.board[r][c].isBomb()) {
                        BufferedImage temp = null;
                        try {
                            g.drawImage(resizeImage(BOMB_IMAGE, WIDTH, HEIGHT), c * WIDTH, r * HEIGHT + score_board_height, this);
                        } catch (IOException e) {
                        }
                    }else if (layout.board[r][c].isFlagged()) {
                        BufferedImage temp = null;
                        try {
                            g.drawImage(resizeImage(FLAGGED_IMAGE, WIDTH, HEIGHT), c * WIDTH, r * HEIGHT + score_board_height, this);
                        } catch (IOException e) { }
                    }else if (!layout.board[r][c].isCleared()) {
                        BufferedImage temp = null;
                        try {
                            g.drawImage(resizeImage(FACING_DOWN_IMAGE, WIDTH, HEIGHT), c * WIDTH, r * HEIGHT + score_board_height, this);
                        } catch (IOException e) { }
                    }else{
                        g.drawImage(layout.board[r][c].getImage(), c * WIDTH, r * HEIGHT + score_board_height, this);
                    }
                }
            }
        }

        else{
            for (int r = 0; r < layout.getLength(); r++){
                for (int c = 0; c < layout.getHeight(); c++) {
                    if (layout.board[r][c].isFlagged()) {
                        BufferedImage temp = null;
                        try {
                            g.drawImage(resizeImage(FLAGGED_IMAGE, WIDTH, HEIGHT), c * WIDTH, r * HEIGHT + score_board_height, this);
                        } catch (IOException e) { }
                    }else if (!layout.board[r][c].isCleared()) {
                        BufferedImage temp = null;
                        try {
                            g.drawImage(resizeImage(FACING_DOWN_IMAGE, WIDTH, HEIGHT), c * WIDTH, r * HEIGHT + score_board_height, this);
                        } catch (IOException e) { }
                    } else {
                        g.drawImage(layout.board[r][c].getImage(), c * WIDTH, r * HEIGHT + score_board_height, this);
                    }
                }
            }
        }
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) throws IOException {
        Image resultingImage = originalImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    private class MyMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {

            int x = e.getX();
            int y = e.getY();
            int col = x / WIDTH;
            int row = (y - score_board_height) / HEIGHT;
            boolean doRepaint = false;
            if(uncover == 0){
                Timer newTimer = new Timer();
                Thread myTimer = new Thread(newTimer);
                myTimer.start();
            }
            if (col < X && row < Y) {
                if (e.getButton() == MouseEvent.BUTTON3 || (e.isControlDown() && e.getButton() == MouseEvent.BUTTON1)) {
                    if (layout.board[row][col].isCleared()) {
                    } else{
                        if(layout.board[row][col].isFlagged()) {
                            layout.board[row][col].setFlagged(false);
                            flags--;
                        }
                        else{
                            layout.board[row][col].setFlagged(true);
                            flags ++;
                        }
                        doRepaint = true;
                    }
                }
                else if(e.getButton() == MouseEvent.BUTTON1){
                    if (layout.board[row][col].isCleared() || layout.board[row][col].isFlagged()) {
                    } else if (layout.board[row][col].isBomb()) {
                        end = true;
                        doRepaint = true;
                    }else{
                        if(layout.board[row][col].getBombNearby() == 0){
                            clearZeros(row, col);
                        }
                        layout.board[row][col].clear();
                        doRepaint = true;
                    }
                }
            }
            if(doRepaint){
                repaint();
            }
        }
        private void clearZeros(int r, int c){
            //clear itself
            if(r < Y && r >=0 && c < X && c >= 0)
                layout.board[r][c].clear();

            if(r + 1 < Y && !layout.board[r + 1][c].isCleared()) {
                layout.board[r + 1][c].clear();
                if(layout.board[r + 1][c].getBombNearby() == 0)
                    clearZeros(r + 1, c);
            }
            if(c + 1 < X && r + 1 < Y && !layout.board[r + 1][c + 1].isCleared()) {
                layout.board[r + 1][c + 1].clear();
                if(layout.board[r + 1][c + 1].getBombNearby() == 0)
                    clearZeros(r + 1, c + 1);
            }
            if(c - 1 >= 0 && r + 1 < Y && !layout.board[r + 1][c - 1].isCleared()) {
                layout.board[r + 1][c - 1].clear();
                if(layout.board[r + 1][c - 1].getBombNearby() == 0)
                    clearZeros(r + 1, c - 1);
            }
            if(r - 1 >= 0 && !layout.board[r - 1][c].isCleared()) {
                layout.board[r - 1][c].clear();
                if(layout.board[r - 1][c].getBombNearby() == 0)
                    clearZeros(r - 1, c);
            }
            if(c + 1 < X && r - 1 >= 0 && !layout.board[r - 1][c + 1].isCleared()) {
                layout.board[r - 1][c + 1].clear();
                if(layout.board[r - 1][c + 1].getBombNearby() == 0)
                    clearZeros(r - 1, c + 1);
            }
            if(c - 1 >= 0 && r - 1 >= 0 && !layout.board[r - 1][c - 1].isCleared()) {
                layout.board[r - 1][c - 1].clear();
                if(layout.board[r - 1][c - 1].getBombNearby() == 0)
                    clearZeros(r - 1, c - 1);
            }
            if(c + 1 < X && !layout.board[r][c + 1].isCleared()) {
                layout.board[r][c + 1].clear();
                if(layout.board[r][c + 1].getBombNearby() == 0)
                    clearZeros(r, c + 1);
            }
            if(c - 1 >=0 && !layout.board[r][c - 1].isCleared()) {
                layout.board[r][c - 1].clear();
                if(layout.board[r][c - 1].getBombNearby() == 0)
                    clearZeros(r, c - 1);
            }

        }
    }
    private class Timer implements Runnable{
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            while(!(uncover == (X)*(Y) - BOMB_COUNT) && timeLimit >= 1 && !end && !win){
                try {
                    timeLimit --;
                    status.setText("TIME LEFT: " + timeLimit);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            if(uncover == (X)*(Y) - BOMB_COUNT){
                status.setText("You Win!");
                win = true;
            }
            else if(timeLimit <= 0){
                end = true;
                repaint();
            }
            if(end){
                status.setText("Game Over! Score: " + (int)((double)uncover / (X * Y - BOMB_COUNT) * 100) + "%");
            }
        }
    }
}
