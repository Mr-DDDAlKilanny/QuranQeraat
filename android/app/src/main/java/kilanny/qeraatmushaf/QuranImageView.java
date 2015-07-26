package kilanny.qeraatmushaf;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ibraheem on 05/11/2015.
 */
public class QuranImageView extends ImageView {

    private final int[] colors = new int[7];
    private static final int LINE_BORDER = 10;
    ArrayList<Selection> selections;
    private ArrayList<Boolean> lastTypes = new ArrayList<>();
    private ArrayList<RectF> lastRects = new ArrayList<>();
    private ArrayList<Line> lastLines = new ArrayList<>();
    Dimension currentPageSize;
    SharedPreferences pref;
    private Paint rectPaint, linePaint;
    private Resources res = getResources();

    private void init() {
        rectPaint = new Paint();
        linePaint = new Paint();
        linePaint.setStrokeWidth(LINE_BORDER / 2);
        rectPaint.setStyle(Paint.Style.FILL);
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

    private void initPrefs() {
        String tmp = pref.getString("listFarshColor", res.getString(R.string.yellow));
        colors[0] = tmp.equals("-1") ? -1 : Color.parseColor(tmp);
        tmp = pref.getString("listHamzColor", res.getString(R.string.magenta));
        colors[1] = tmp.equals("-1") ? -1 : Color.parseColor(tmp);
        tmp = pref.getString("listEdghamColor", res.getString(R.string.cyan));
        colors[2] = tmp.equals("-1") ? -1 : Color.parseColor(tmp);
        tmp = pref.getString("listEmalahColor", res.getString(R.string.red));
        colors[3] = tmp.equals("-1") ? -1 : Color.parseColor(tmp);
        tmp = pref.getString("listNaqlColor", res.getString(R.string.blue));
        colors[4] = tmp.equals("-1") ? -1 : Color.parseColor(tmp);
        tmp = pref.getString("listMadColor", res.getString(R.string.white));
        colors[5] = tmp.equals("-1") ? -1 : Color.parseColor(tmp);
        tmp = pref.getString("listSaktColor", res.getString(R.string.gray));
        colors[6] = tmp.equals("-1") ? -1 : Color.parseColor(tmp);
    }

    private boolean selectionShouldBeViewed(Selection s) {
        Set<String> lastRewayaat = pref.getStringSet("listSelectedRewayaat", new HashSet<>(
                Arrays.asList(res.getStringArray(R.array.rewayaat_vales))));
        for (String t : lastRewayaat) {
            Rewayah rew = Rewayah.getByCombinedCode(t);
            for (RewayahSelectionGroup g : s.khelafat) {
                for (RewayahSelection r : g.rewayaat) {
                    if (rew == r.r) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (selections != null) {
            initPrefs();
            lastRects.clear();
            lastLines.clear();
            lastTypes.clear();
            for (Selection s : selections) {
                if (colors[s.type.getValue() - 1] != -1 && selectionShouldBeViewed(s)) {
                    if (s.rect instanceof Rectangle) {
                        lastTypes.add(Boolean.TRUE);
                        RectF r = getActualRect((Rectangle) s.rect);
                        lastRects.add(r);
                        rectPaint.setColor(colors[s.type.getValue() - 1]);
                        rectPaint.setAlpha(125);
                        canvas.drawRect(r, rectPaint);
                    } else {
                        lastTypes.add(Boolean.FALSE);
                        Line l = getScaledLineFromImageLine(currentPageSize, (Line) s.rect);
                        lastLines.add(l);
                        linePaint.setColor(colors[s.type.getValue() - 1]);
                        linePaint.setAlpha(125);
                        canvas.drawLine(l.x1, l.y1, l.x2, l.y2, linePaint);
                    }
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

    public void getSelectionAtPos(float x, float y, ArrayList<Selection> matches) {
        int[] ty = new int[2];
        for (int i = 0; i < lastTypes.size(); ++i) {
            if (lastTypes.get(i).booleanValue()) {
                RectF r = lastRects.get(ty[0]++);
                if (r.contains(x, y)) matches.add(selections.get(i));
            } else {
                Line l = lastLines.get(ty[1]++);
                RectF r = new RectF();
                r.set(l.x1,
                        l.y1 - LINE_BORDER,
                        l.x2,
                        l.y2 + LINE_BORDER);
                if (r.contains(x, y)) matches.add(selections.get(i));
            }
        }
    }
}
