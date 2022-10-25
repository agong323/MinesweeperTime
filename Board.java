
import java.io.IOException;

public class Board{
    Space[][] board = new Space[Game.Y][Game.X];

    public Board() throws IOException {
        int remainder = Game.BOMB_COUNT;

        double probability = (double) Game.BOMB_COUNT / (Game.X * Game.Y);

        try{
            for(int r = 0; r < Game.Y; r ++){
                for(int c = 0; c < Game.X; c ++){
                    if(Math.random() < probability && remainder > 0){
                        board[r][c] = new Space(true);
                        board[r][c].setImage("src/img/" + Game.DRAW_BOMB + ".png");
                        remainder --;
                    }
                    else{
                        board[r][c] = new Space(false);
                    }
                }
            }
            while(remainder > 0){
                int x = (int)(Math.random() * Game.X);
                int y = (int)(Math.random() * Game.Y);
                if(!board[y][x].isBomb()){
                    board[y][x].setBomb(true);
                    board[y][x].setImage("src/img/" + Game.DRAW_BOMB + ".png");
                    remainder --;
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        calculate();
    }
    public void calculate() throws IOException {
        for(int y = 0; y < Game.Y; y ++){
            for(int x = 0; x < Game.X; x ++){
                if(!board[y][x].isBomb()){
                    int count = 0;
                    if(y > 0 && board[y - 1][x].isBomb())
                        count ++;
                    if(y < Game.Y - 1 && board[y + 1][x].isBomb())
                        count ++;
                    if(x > 0 && board[y][x - 1].isBomb())
                        count ++;
                    if(x < Game.X-1 && board[y][x + 1].isBomb())
                        count ++;
                    if(x > 0 && y > 0 && board[y - 1][x - 1].isBomb())
                        count ++;
                    if(x < Game.X - 1 && y > 0 && board[y - 1][x + 1].isBomb())
                        count ++;
                    if(x > 0 && y < Game.Y - 1 && board[y + 1][x - 1].isBomb())
                        count ++;
                    if(x < Game.X - 1 && y < Game.Y - 1 && board[y + 1][x + 1].isBomb())
                        count ++;
                    board[y][x].setBombNearby(count);
                    board[y][x].setImage("src/img/" + count + ".png");
                }
            }
        }
    }
    public int getLength(){
        return board.length;
    }
    public int getHeight(){
        return board[0].length;
    }

}
