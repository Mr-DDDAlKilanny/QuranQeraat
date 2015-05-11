package kilanny.qeraatmushaf;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity {

    private int page;
    private static final int MAX_PAGE = 604;
    private boolean isLoadingPage;

    private void viewPage(int p) {
        if (p > 0 && p <= MAX_PAGE && !isLoadingPage) {
            isLoadingPage = true;
            String path = String.format(getString(R.string.downloadPage), p);
            ImageView v = (ImageView) findViewById(R.id.imageView);
            Bitmap b = null;
            try {
                b = new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        setProgressBarIndeterminateVisibility(true);
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        setProgressBarIndeterminateVisibility(false);
                    }

                    @Override
                    protected Bitmap doInBackground(String... params) {
                        try {
                            return getBitmapFromURL(params[0]);
                        } catch (IOException e) {
                            return null;
                        }
                    }
                }.execute(path).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (b == null) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
                dlgAlert.setMessage("لا يمكن تحميل الصورة. تأكد من اتصالك بالانترنت");
                dlgAlert.setTitle("خطأ");
                dlgAlert.setPositiveButton("موافق", null);
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
            } else {
                v.setImageBitmap(b);
                CharSequence text = "صفحة " + p;
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                page = p;
            }
            isLoadingPage = false;
        }
    }

    public static Bitmap getBitmapFromURL(String link) throws IOException {
        System.out.println(link);
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        BitmapFactory.Options o = new BitmapFactory.Options();
        return BitmapFactory.decodeStream(input, new Rect(0, 0, 0, 0), o);
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
        viewPage(page = 1);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
