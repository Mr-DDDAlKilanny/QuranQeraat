package kilanny.qeraatmushaf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by ibraheem on 05/11/2015.
 */
public class QuranImageView extends ImageView {

    private final int[] colors = { Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.RED, Color.BLUE };
    ArrayList<Selection> selections;
    private int lastImageWidth, lastImageHeight;

    public QuranImageView(Context context) {
        super(context);
    }

    public QuranImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuranImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        lastImageWidth = bm.getWidth();
        lastImageHeight = bm.getHeight();
    }

    private Rectangle getActualRect(Rectangle r) {
        Rectangle ret = new Rectangle();
        float w = (float) getWidth() / lastImageWidth;
        float h = (float) getHeight() / lastImageHeight;
        ret.x = r.x * w;
        ret.y = r.y * h;
        ret.width = r.width * w;
        ret.height = r.height * h;
        return ret;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (selections != null) {
            Paint paint = new Paint();
            paint.setStrokeWidth(2);
            for (Selection s : selections) {
                Rectangle d = getActualRect(s.rect);
                paint.setColor(colors[s.type.getValue() - 1]);
                paint.setAlpha(10);
                canvas.drawRect(d.x, d.y, d.width, d.height, paint);
            }
        }
    }
}
