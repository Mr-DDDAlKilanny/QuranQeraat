package kilanny.qeraatmushaf;

/**
 * Created by ibraheem on 05/13/2015.
 */
public class Dimension {
    public int width, height;

    public Dimension() {
    }

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return String.format("Dimension[%d, %d]", width, height);
    }
}
