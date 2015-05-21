package kilanny.qeraatmushaf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by ibraheem on 05/11/2015.
 */
public class QuranImageView extends ImageView {

    private final int[] colors = { Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.RED, Color.BLUE };
    ArrayList<Selection> selections;
    Dimension currentPageSize;
    private Paint paint;

    private void init() {
        paint = new Paint();
        //paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
    }

    public QuranImageView(Context context) {
        super(context);
        init();
    }

    public QuranImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QuranImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
    }

    private RectF getActualRect(Rectangle r) {
        r = getScaledRectFromImageRect(
                 currentPageSize,
                r);
        RectF ret = new RectF();
        ret.set(r.x,
                r.y,
                r.x + r.width,
                r.y + r.height);
        return ret;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (selections != null) {
            for (Selection s : selections) {
                paint.setColor(colors[s.type.getValue() - 1]);
                paint.setAlpha(125);
                if (s.rect instanceof Rectangle)
                    canvas.drawRect(getActualRect((Rectangle) s.rect), paint);
                else {
                    Line l = getScaledLineFromImageLine(currentPageSize, (Line) s.rect);
                    canvas.drawLine(l.x1, l.y1, l.x2, l.y2, paint);
                }
            }
        }
    }

    public Rectangle getScaledRectFromImageRect(Dimension bmp, Rectangle r) {
        final Dimension d = new Dimension(getWidth(), getHeight());
        float w = d.width / (float) bmp.width;
        float h = d.height / (float) bmp.height;
        Rectangle rr = new Rectangle();
        rr.x = r.x * w;
        rr.y = r.y * h;
        rr.width = r.width * w;
        rr.height = r.height * h;
        return rr;
    }

    public Line getScaledLineFromImageLine(Dimension bmp, Line r) {
        final Dimension d = new Dimension(getWidth(), getHeight());
        float w = d.width / (float) bmp.width;
        float h = d.height / (float) bmp.height;
        Line rr = new Line();
        rr.x1 = r.x1 * w;
        rr.y1 = r.y1 * h;
        rr.x2 = r.x2 * w;
        rr.y2 = r.y2 * h;
        return rr;
    }
}
