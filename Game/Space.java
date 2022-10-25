import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Space {
    private boolean bomb;
    private boolean flagged;
    private boolean cleared;
    private int bombNearby;
    private BufferedImage image;

    public Space(boolean bomb) throws IOException {
        this.bomb = bomb;
        flagged = false;
        cleared = false;
        bombNearby = 0;

        BufferedImage temp = ImageIO.read(new File("src/img/" + Game.DRAW_FACING_DOWN + ".png"));
        image = Game.resizeImage(temp, Game.WIDTH, Game.HEIGHT);
    }

    public void setFlagged(boolean flagged){
        this.flagged = flagged;
    }
    public void setBomb(boolean bomb){ this.bomb = bomb; }
    public void clear(){
        boolean prevClear = cleared;
        cleared = true;
        if(!prevClear)
            Game.uncover ++;
    }
    public boolean isBomb(){
        return bomb;
    }
    public boolean isCleared() {
        return cleared;
    }
    public boolean isFlagged(){
        return flagged;
    }
    public void setBombNearby(int bombNearby){
        this.bombNearby = bombNearby;
    }
    public int getBombNearby(){ return bombNearby; }

    public void setImage(String filename) throws IOException{
        BufferedImage temp = ImageIO.read(new File(filename));
        image = Game.resizeImage(temp, Game.WIDTH, Game.HEIGHT);
    }
    public BufferedImage getImage(){
        return image;
    }
}
