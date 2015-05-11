package kilanny.qeraatmushaf;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private final String settingFilename = "myfile";
    private int page;
    private static final int MAX_PAGE = 604;
    private boolean isLoadingPage;
    private final ArrayList<Selection>[] sel = new ArrayList[MAX_PAGE];

    private void readSettings() {
        page = 1;
        FileInputStream inputStream;
        byte[] buff = new byte[1024];
        try {
            inputStream = openFileInput(settingFilename);
            int len = inputStream.read(buff, 0, buff.length);
            page = Integer.parseInt(new String(buff, 0, len));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveSettings() {
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(settingFilename, Context.MODE_PRIVATE);
            outputStream.write(("" + page).getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void viewPage(final int p) {
        if (p > 0 && p <= MAX_PAGE && !isLoadingPage) {
            isLoadingPage = true;
            final QuranImageView v = (QuranImageView) findViewById(R.id.imageView);
            v.selections = null;
            v.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ajax_loader));
            v.invalidate();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    setProgressBarIndeterminateVisibility(true);
                    String path = String.format(getString(R.string.downloadPage), p);
                    final Bitmap b = getBitmapFromURL(path);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (b == null) {
                                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
                                dlgAlert.setMessage("لا يمكن تحميل الصورة. تأكد من اتصالك بالانترنت");
                                dlgAlert.setTitle("خطأ");
                                dlgAlert.setPositiveButton("موافق", null);
                                dlgAlert.setCancelable(false);
                                dlgAlert.create().show();
                            } else {
                                v.setImageBitmap(b);
                                v.selections = sel[p - 1];
                                CharSequence text = "صفحة " + p;
                                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                                page = p;
                                saveSettings();
                            }
                            setProgressBarIndeterminateVisibility(false);
                            isLoadingPage = false;
                        }
                    });
                }
            }).start();
        }
    }

    public static Bitmap getBitmapFromURL(String link) {
        System.out.println(link);
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.Options o = new BitmapFactory.Options();
            return BitmapFactory.decodeStream(input, new Rect(0, 0, 0, 0), o);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void readSelections() {
        for (int i = 0; i < MAX_PAGE; ++i) {
            sel[i] = new ArrayList<>();
        }
        XmlResourceParser parser = getResources().getXml(R.xml.data);
        try {
            int eventType = parser.getEventType();
            Selection currentProduct = null;
            while (eventType != XmlPullParser.END_DOCUMENT){
                String name = null;
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equals("selection")){
                            currentProduct = new Selection();
                            currentProduct.page = parser.getAttributeIntValue(null, "page", 1);
                            Rectangle r = new Rectangle();
                            String[] tmp = parser.getAttributeValue(null, "rect").split(",");
                            r.x = Integer.parseInt(tmp[0]);
                            r.y = Integer.parseInt(tmp[1]);
                            r.width = Integer.parseInt(tmp[2]);
                            r.height = Integer.parseInt(tmp[3]);
                            currentProduct.rect = r;
                            currentProduct.type = SelectionType.fromValue(
                                    parser.getAttributeIntValue(null, "type", 1));
                        } else if (currentProduct != null){
                            if (name.equals("descr")){
                                currentProduct.descr = parser.nextText();
                            } else if (name.equals("shahed")){
                                currentProduct.shahed = parser.nextText();
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equalsIgnoreCase("selection") && currentProduct != null){
                            sel[currentProduct.page - 1].add(currentProduct);
                        }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    private Surah getSurah(int idx) {
        XmlResourceParser parser = getResources().getXml(R.xml.qurandata);
        Surah s = new Surah();
        s.index = idx;
        s.page = MAX_PAGE * 2;
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                String name = null;
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equals("sura") &&
                                parser.getAttributeIntValue(null, "index", 0) == idx){
                            s.name = parser.getAttributeValue(null, "name");
                        } else if (name.equals("page") &&
                                parser.getAttributeIntValue(null, "sura", 0) == idx){
                            s.page = Math.min(s.page, parser.getAttributeIntValue(null, "index", 0));
                        }
                        break;
                }
                eventType = parser.next();
            }
            return s;
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView v = (ImageView) findViewById(R.id.imageView);
        v.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                viewPage(page - 1);
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                viewPage(page + 1);
            }
        });
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                isLoadingPage = true;
                readSelections();
                isLoadingPage = false;
                readSettings();
                viewPage(page);
                return null;
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_goto) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.fragment_goto_dlg);
            dialog.setTitle("ذهاب إلى الصفحة");
            final ListView l = (ListView) dialog.findViewById(R.id.listViewSurah);
            Surah[] values = new Surah[114];
            for (int i = 0; i < 114; ++i) {
                values[i] = getSurah(i + 1);
            }
            l.setAdapter(new ArrayAdapter<Surah>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, values));
            l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Surah itemValue = (Surah) l.getItemAtPosition(position);
                    EditText txt = (EditText) dialog.findViewById(R.id.editTextPageNum);
                    txt.setText("" + itemValue.page);
                }
            });
            Button b = (Button) dialog.findViewById(R.id.buttonGoto);
            b.setOnClickListener(new View.OnClickListener() {
                private Runnable err = new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
                        dlgAlert.setMessage(String.format("أدخل رقم صفحة صحيح في المدى (1-%d)", MAX_PAGE));
                        dlgAlert.setTitle("خطأ");
                        dlgAlert.setPositiveButton("موافق", null);
                        dlgAlert.setCancelable(false);
                        dlgAlert.create().show();
                    }
                };

                @Override
                public void onClick(View v) {
                    try {
                        EditText txt = (EditText) dialog.findViewById(R.id.editTextPageNum);
                        int num = Integer.parseInt(txt.getText().toString());
                        if (num > 0 && num <= MAX_PAGE) {
                            dialog.dismiss();
                            viewPage(num);
                        } else {
                            err.run();
                        }
                    } catch (Exception ex) {
                        err.run();
                    }
                }
            });
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

enum SelectionType {
    Farsh(1),
    Hamz(2),
    Edgham(3),
    Emalah(4);

    private final int value;
    private SelectionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SelectionType fromValue(int t) {
        switch (t) {
            case 1:
                return Farsh;
            case 2:
                return Hamz;
            case 3:
                return Edgham;
            case 4:
                return Emalah;
            default:
                throw new IllegalArgumentException();
        }
    }
}

class Rectangle {
    float x, y, width, height;
}

class Selection {
    Rectangle rect;
    int page;
    String shahed;
    String descr;
    SelectionType type;
}
class Surah {
    String name;
    int page;
    int index;

    @Override
    public String toString() {
        return "سورة " + name;
    }
}