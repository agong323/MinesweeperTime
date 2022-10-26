import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Game extends JPanel {
    private Board layout;

    public static final int WIDTH = 20;
    public static final int HEIGHT = 20;
    public static final int X = 24;
    public static final int Y = 20;

    static int uncover;
    public static final int BOMB_COUNT = 99;
    private boolean end = false;
    private boolean win = false;
    private boolean playAgain = false;
    private int timeLimit;

    private static final int PLAYAGAIN_W = 4 * WIDTH;
    private static final int PLAYAGAIN_H = 2 * HEIGHT;
    private static final int PLAYAGAIN_X = X * WIDTH / 2 - PLAYAGAIN_W / 2;
    private static final int PLAYAGAIN_Y = Y * HEIGHT/ 2 - PLAYAGAIN_H / 2;

    private static BufferedImage FACING_DOWN_IMAGE = null;
    private static BufferedImage FLAGGED_IMAGE = null;
    private static BufferedImage BOMB_IMAGE = null;

    static {
        try {
            FACING_DOWN_IMAGE = resizeImage(ImageIO.read(new File("src/img/" + Game.DRAW_FACING_DOWN + ".png")),WIDTH, HEIGHT);
            FLAGGED_IMAGE = resizeImage(ImageIO.read(new File("src/img/" + Game.DRAW_FLAGGED + ".png")), WIDTH, HEIGHT);
            BOMB_IMAGE = resizeImage(ImageIO.read(new File("src/img/" + Game.DRAW_BOMB + ".png")), WIDTH, HEIGHT);
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
        setPreferredSize(new Dimension(X * WIDTH, Y * HEIGHT));
        addMouseListener(new MyMouseAdapter());
        newGame();
    }

    private void newGame() {
        end = false;
        win = false;
        playAgain = false;
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
                        g.drawImage(BOMB_IMAGE, c * WIDTH, r * HEIGHT, this);
                    }else if (layout.board[r][c].isFlagged()) {
                        g.drawImage(FLAGGED_IMAGE, c * WIDTH, r * HEIGHT, this);
                    }else if (!layout.board[r][c].isCleared()) {
                        g.drawImage(FACING_DOWN_IMAGE, c * WIDTH, r * HEIGHT, this);

                    }else{
                        g.drawImage(layout.board[r][c].getImage(), c * WIDTH, r * HEIGHT, this);
                    }
                }
            }
        }

        else{
            for (int r = 0; r < layout.getLength(); r++){
                for (int c = 0; c < layout.getHeight(); c++) {
                    if (layout.board[r][c].isFlagged()) {
                        g.drawImage(FLAGGED_IMAGE, c * WIDTH, r * HEIGHT, this);
                    }else if (!layout.board[r][c].isCleared()) {
                        g.drawImage(FACING_DOWN_IMAGE, c * WIDTH, r * HEIGHT, this);
                    } else {
                        g.drawImage(layout.board[r][c].getImage(), c * WIDTH, r * HEIGHT, this);
                    }
                }
            }
        }
        if(playAgain){
            g.setColor(Color.red);
            g.fillRect(PLAYAGAIN_X, PLAYAGAIN_Y, PLAYAGAIN_W, PLAYAGAIN_H);
            g.setColor(Color.WHITE);
            g.setFont(new Font("TimesRoman", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("Play again?", X * WIDTH / 2 - fm.stringWidth("Play again?") / 2, Y * HEIGHT/ 2 + fm.getHeight()/2);
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
            int row = (y) / HEIGHT;
            boolean doRepaint = false;
            if(!end && !win){
                if (uncover == 0) {
                    Timer newTimer = new Timer();
                    Thread myTimer = new Thread(newTimer);
                    myTimer.start();
                }
                if (col < X && row < Y) {
                    if (e.getButton() == MouseEvent.BUTTON3 || (e.isControlDown() && e.getButton() == MouseEvent.BUTTON1)) {
                        if (layout.board[row][col].isCleared()) {
                        } else {
                            if (layout.board[row][col].isFlagged()) {
                                layout.board[row][col].setFlagged(false);
                            } else {
                                layout.board[row][col].setFlagged(true);
                            }
                            doRepaint = true;
                        }
                    } else if (e.getButton() == MouseEvent.BUTTON1) {
                        if (layout.board[row][col].isCleared() || layout.board[row][col].isFlagged()) {
                        } else if (layout.board[row][col].isBomb()) {
                            end = true;
                            doRepaint = true;
                        } else {
                            if (layout.board[row][col].getBombNearby() == 0) {
                                clearZeros(row, col);
                            }
                            layout.board[row][col].clear();
                            doRepaint = true;
                        }
                    }
                }
                if (doRepaint) {
                    repaint();
                }
            }
            if(playAgain && x >= PLAYAGAIN_X && x <= PLAYAGAIN_X + PLAYAGAIN_W && y >= PLAYAGAIN_Y && y <= PLAYAGAIN_Y + PLAYAGAIN_H){
                newGame();
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
            } catch (InterruptedException e) {}
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
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                playAgain = true;
                repaint();
            }
            if(win){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                playAgain = true;
                repaint();
            }
        }
    }
}
