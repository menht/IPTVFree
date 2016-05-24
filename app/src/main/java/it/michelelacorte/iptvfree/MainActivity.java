package it.michelelacorte.iptvfree;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import it.michelelacorte.iptvfree.fragment.FragmentIPTV;
import it.michelelacorte.iptvfree.fragment.FragmentIPTVFavorite;
import it.michelelacorte.iptvfree.sd_reader.OnHandleFileListener;
import it.michelelacorte.iptvfree.util.BugReport;
import it.michelelacorte.iptvfree.util.FirstRun;
import it.michelelacorte.iptvfree.util.SharedPreference;
import it.michelelacorte.iptvfree.util.Utils;
import it.michelelacorte.iptvfree.util.ViewPagerAdapter;

@SuppressWarnings("all")
public class MainActivity extends AppCompatActivity  implements View.OnClickListener {

    /**
     * Floatin Action Button menù object
     */
    public static Boolean isFabOpen = false;
    public static FloatingActionButton fabMenu,fabAdd,fabDownload;
    public static Animation fab_open,fab_close,rotate_forward,rotate_backward;
    public static TextView labelAdd, labelDownload;

    /**
     * Layout object
     */
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private static Handler handler = new Handler();
    private static Runnable myrunnable;
    private ProgressDialog ringProgressDialog;
    private ProgressDialog mProgressDialog;
    private CoordinatorLayout coordinatorLayout;
    public static ViewPagerAdapter adapter;
    private Toolbar toolbar;
    private MaterialSearchView searchView;
    private MenuItem item;
    /**
     * List of data catched from m3u file
     */
    public static List<String> categoryName = new ArrayList<>();
    public static List<String> channelName = new ArrayList<>();
    public static List<String> channelLink = new ArrayList<>();
    public static List<Integer> categorySize = new ArrayList<>();
    //Favorite
    public static ArrayList<String> favoriteName = new ArrayList<>();
    public static ArrayList<String> favoriteLink = new ArrayList<>();
    //Personal List
    public static List<String> categoryNamePersonal = new ArrayList<>();
    public static List<String> channelNamePersonal = new ArrayList<>();
    public static List<String> channelLinkPersonal = new ArrayList<>();
    public static List<Integer> categorySizePersonal = new ArrayList<>();
    private static List<String> state;
    /**
     * Other variable
     */
    private String [] channelNameArray;
    private boolean isFavorite = false;
    public static int CATEGORY = 0;
    public static int CATEGORY_INDEX = 0;
    public static boolean DEFAULT = true;
    public static boolean isMyListLoadedFromPath = false;
    public static boolean isMyListLoadedFromURL = false;
    private String URL = null;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 100;
    private SharedPreference preferencesIPTV = new SharedPreference();


    /**
     * This class extends AsyncTask and implements code for getting data from URL
     */
    private class GetDataAsync extends AsyncTask<String, Void, Boolean> {
        ProgressDialog ringProgressDialog;
        Activity activity;

        public GetDataAsync(Activity activity)
        {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ringProgressDialog = ProgressDialog.show(activity, getResources().getString(R.string.please_wait), getResources().getString(R.string.getting_data), true);
            ringProgressDialog.setCancelable(false);
        }


        @Override
        protected Boolean doInBackground(String... url) {
            try{
                try {
                    DefaultHttpClient httpclient = new DefaultHttpClient();
                    HttpGet httppost = new HttpGet(url[0]);
                    HttpResponse response = null;
                    response = httpclient.execute(httppost);
                    HttpEntity ht = response.getEntity();
                    BufferedHttpEntity buf = new BufferedHttpEntity(ht);
                    InputStream is = buf.getContent();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String inputLine = null;
                    try {
                        inputLine = br.readLine();
                        boolean dummyLink = false;
                        int j = 0;
                        int line = 0;
                        while ((inputLine = br.readLine()) != null){
                            String st = inputLine.toString();
                            line++;
                            if(st.equalsIgnoreCase("<List>") || st.equalsIgnoreCase("</List>") || st.equalsIgnoreCase("#EXTM3U"))
                            {
                                //Not relevant line, decrease.
                                line--;
                            }
                            if(st.startsWith("http") || st.startsWith("rtmp") || st.startsWith("rtsp") || st.startsWith("mmsh")){
                                if(dummyLink) {
                                    dummyLink = false;
                                }else{
                                    j++;
                                    channelLink.add(st);
                                }
                            }
                            String dump = st;
                            for (int i=0; i<st.length()-1; i++){
                                if(st.charAt(i)==','){
                                    String afterComma = dump.replaceAll(".*,", "").trim();
                                    if(!Character.isLetterOrDigit(afterComma.charAt(0))){
                                        Pattern pattern = Pattern.compile("[^a-z A-Z]");
                                        Matcher matcher = pattern.matcher(afterComma);
                                        categoryName.add(matcher.replaceAll(""));
                                        dummyLink = true;
                                        if(line > 1) {
                                            categorySize.add(j);
                                        }
                                    }else if(Character.isLetterOrDigit(afterComma.charAt(0))){
                                        channelName.add(afterComma);
                                    }
                                }
                            }
                        }
                        categorySize.add(j);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }catch(Exception e) {
                Log.e(TAG, "Data Async: " + e);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            ringProgressDialog.dismiss();
        }
    }

    /**
     * This class extends AsyncTask and implements code for getting data from URL (specifically for Personal Data)
     */
    private class GetDataAsyncPersonal extends AsyncTask<String, Void, Boolean> {
        ProgressDialog ringProgressDialog;
        Activity activity;

        public GetDataAsyncPersonal(Activity activity)
        {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ringProgressDialog = ProgressDialog.show(activity, getResources().getString(R.string.please_wait), getResources().getString(R.string.getting_data), true);
            ringProgressDialog.setCancelable(false);
        }


        @Override
        protected Boolean doInBackground(String... url) {
            try{
                try {
                    DefaultHttpClient httpclient = new DefaultHttpClient();
                    HttpGet httppost = new HttpGet(url[0]);
                    HttpResponse response = null;
                    response = httpclient.execute(httppost);
                    HttpEntity ht = response.getEntity();
                    BufferedHttpEntity buf = new BufferedHttpEntity(ht);
                    InputStream is = buf.getContent();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String inputLine = null;
                    try {
                        inputLine = br.readLine();
                        boolean dummyLink = false;
                        int j = 0;
                        int line = 0;
                        while ((inputLine = br.readLine()) != null){
                            String st = inputLine.toString();
                            line++;
                            if(st.startsWith("http") || st.startsWith("rtmp") || st.startsWith("rtsp") || st.startsWith("mmsh")){
                                if(dummyLink) {
                                    dummyLink = false;
                                }else{
                                    j++;
                                    channelLinkPersonal.add(st);
                                }
                            }
                            String dump = st;
                            for (int i=0; i<st.length()-1; i++){
                                if(st.charAt(i)==','){
                                    String afterComma = dump.replaceAll(".*,", "").trim();
                                    if(!Character.isLetterOrDigit(afterComma.charAt(0))){
                                        Pattern pattern = Pattern.compile("[^a-z A-Z]");
                                        Matcher matcher = pattern.matcher(afterComma);
                                        categoryNamePersonal.add(matcher.replaceAll(""));
                                        dummyLink = true;
                                        if(line > 1) {
                                            categorySizePersonal.add(j);
                                        }
                                    }else if(Character.isLetterOrDigit(afterComma.charAt(0))){
                                        channelNamePersonal.add(afterComma);
                                    }
                                }
                            }
                        }
                        categorySizePersonal.add(j);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }catch(Exception e) {
                Log.e(TAG, "Data Async: " + e);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            ringProgressDialog.dismiss();
        }
    }

    /**
     * This class extends AsyncTask and implements code for getting playlist (.m3u format)
     */
    private class DownloadM3UList extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadM3UList(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                File folder = new File(Environment.getExternalStorageDirectory() + "/ListIPTV");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }
                if (success) {
                    output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/ListIPTV/listIPTV.m3u");
                } else {
                    Log.e(TAG, "Directory not created!");
                }
                DocumentBuilderFactory dbFactory
                        = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(URL);
                doc.getDocumentElement().normalize();
                String dataString = doc.getElementsByTagName("List").item(0).getTextContent();
                /*
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }

                    total += count;

                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));

                    output.write(data, 0, count);
                }
                */
                byte[] dataStringBytes = dataString.getBytes(StandardCharsets.UTF_8);
                output.write(dataStringBytes, 0, dataStringBytes.length);
                // flushing output
                output.flush();
            } catch (Exception e) {
                Log.e("DATA:", e.toString());
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Snackbar.make(coordinatorLayout, getResources().getString(R.string.download_error) + " " + result, Snackbar.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, getResources().getString(R.string.download_error) + " " + result, Toast.LENGTH_LONG).show();
                }
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Snackbar.make(coordinatorLayout, getResources().getString(R.string.download_success), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.snackbar_open), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/ListIPTV");
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(selectedUri, "resource/folder");

                                    if (intent.resolveActivityInfo(getPackageManager(), 0) != null)
                                    {
                                        startActivity(intent);
                                    }
                                }
                            })
                            .show();
                }else{
                    Toast.makeText(context, getResources().getString(R.string.download_success), Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * This class extends AsyncTask and get data from Shared Preferences
     */
    private class RestoreDataAsync extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog ringProgressDialog;
        Activity activity;

        public RestoreDataAsync(Activity activity)
        {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ringProgressDialog = ProgressDialog.show(activity, getResources().getString(R.string.please_wait), getResources().getString(R.string.getting_data), true);
            ringProgressDialog.setCancelable(false);
        }


        @Override
        protected Boolean doInBackground(Void... url) {
            //Restore Data from SharedPreferences
            restorePersonalList();
            restoreFavoriteList();
            restoreListState();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            ringProgressDialog.dismiss();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get locale language
        if(Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("English"))
        {
            //Set URL for English language
            URL = "https://raw.githubusercontent.com/michelelacorte/IPTVFree/master/English/iptvlistEnglish.xml";
        }
        if(Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("Italiano"))
        {
            //Set URL for Italian language
            URL = "https://raw.githubusercontent.com/michelelacorte/IPTVFree/master/Italiano/iptvlistItalian.xml";
        }
        //Check if connection is enabled, it is necessary!
        if(Utils.isOnline(getApplicationContext())) {
            PackageManager pm = getApplicationContext().getPackageManager();
            boolean isInstalledMXPlayer = Utils.isPackageInstalled("com.mxtech.videoplayer.ad", pm);
            boolean isInstalledXMTVPlayer = Utils.isPackageInstalled("com.xmtvplayer.watch.live.streams", pm);
            //Check if MXPlayer is installed, it is necessary!
            if(!isInstalledMXPlayer && !isInstalledXMTVPlayer) {
                    Spanned alertText = null;
                    if(Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("English"))
                    {
                        alertText = Html.fromHtml("Please install a media player to use IPTV Free. \n\n\nWe suggest you <a href=\"https://github.com/michelelacorte/IPTVFree/raw/master/xmtvplayer.apk\">XMTV Player</a> or <a href=\"https://play.google.com/store/apps/details?id=com.mxtech.videoplayer.ad&hl=it\">MX Player</a>");
                    }
                    if(Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("Italiano"))
                    {
                        alertText = Html.fromHtml("Per favore installa un lettore multimediale per utilizzare IPTV Free. \n\n\nTi consigliamo <a href=\"https://github.com/michelelacorte/IPTVFree/raw/master/xmtvplayer.apk\">XMTV Player</a> oppure <a href=\"https://play.google.com/store/apps/details?id=com.mxtech.videoplayer.ad&hl=it\">MX Player</a>");
                    }
                    ((TextView) new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                            .setTitle(getResources().getString(R.string.app_name))
                            .setMessage(alertText)
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.ok_dialog), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //No operation
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());
            }
                if(FirstRun.isFirstLaunch(getApplicationContext())) {
                    //Show disclaimer
                    disclaimerAlertDialog();
                }

                try {
                    new GetDataAsync(this).execute(URL).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


                //Request explicit permission (on android M)
                requestWriteExternalStoragePermission(this);


                //Other inizialize object
                toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                tabLayout = (TabLayout) findViewById(R.id.tabs);
                viewPager = (ViewPager) findViewById(R.id.viewpager);
                coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                        .coordinatorLayout);

                //Fab menù initialize object
                labelAdd = (TextView)findViewById(R.id.labeAdd);
                labelDownload = (TextView)findViewById(R.id.labelDownload);
                fabMenu = (FloatingActionButton)findViewById(R.id.fabMenu);
                fabAdd = (FloatingActionButton)findViewById(R.id.fabAdd);
                fabDownload = (FloatingActionButton)findViewById(R.id.fabDownload);
                fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
                fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
                rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
                rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
                fabMenu.setOnClickListener(this);
                fabAdd.setOnClickListener(this);
                fabDownload.setOnClickListener(this);

                searchView = (MaterialSearchView) findViewById(R.id.search_view);
                channelNameArray = channelName.toArray(new String[channelName.size()]);
                searchView.setSuggestions(channelNameArray);
                searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        int position = 0;
                        for(int i = 0; i < channelName.size(); i++)
                        {
                            if(channelName.get(i).equalsIgnoreCase(query))
                            {
                                position = i;
                            }
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(channelLink.get(position)));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        //Do some magic
                        return false;
                    }
                });

                if(!FirstRun.isFirstLaunch(getApplicationContext())) {
                    //Restore Data from SharedPreferences asynchronous
                    new RestoreDataAsync(this).execute();
                }

                NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
                navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.About:
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogCustom);
                                builder.setTitle(getResources().getString(R.string.app_name))
                                        .setMessage(R.string.dialog_message)
                                        .setCancelable(false)
                                        .setNegativeButton(getResources().getString(R.string.ok_dialog), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });

                                AlertDialog about = builder.create();
                                about.show();
                                ((TextView) about.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.Donate:
                                AlertDialog.Builder donate = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogCustom);
                                donate.setTitle(getResources().getString(R.string.donate))
                                        .setMessage(R.string.donate_message)
                                        .setCancelable(false)
                                        .setNegativeButton(getResources().getString(R.string.ok_dialog), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });

                                AlertDialog donation = donate.create();
                                donation.show();
                                ((TextView) donation.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.Share:
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("text/plain");
                                share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_message));
                                startActivity(Intent.createChooser(share, getResources().getString(R.string.app_name)));
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.italian:
                                MainActivity.this.setTitle(getResources().getString(R.string.italian));
                                item.setVisible(true);
                                ringProgressDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.please_wait), getResources().getString(R.string.getting_data), true);
                                ringProgressDialog.setCancelable(false);
                                Utils.clearIfNotEmpty(categoryName, categorySize, channelName, channelLink);
                                URL = "https://raw.githubusercontent.com/michelelacorte/IPTVFree/master/Italiano/iptvlistItalian.xml";
                                try {
                                    new GetDataAsync(MainActivity.this).execute(URL).get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                                myrunnable = new Runnable() {
                                    public void run() {
                                        setupViewPager(viewPager);
                                        tabLayout.setupWithViewPager(viewPager);
                                        ringProgressDialog.dismiss();
                                    }
                                };
                                handler.postDelayed(myrunnable, 1000);
                                channelNameArray = channelName.toArray(new String[channelName.size()]);
                                searchView.setSuggestions(channelNameArray);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.english:
                                MainActivity.this.setTitle(getResources().getString(R.string.english));
                                item.setVisible(true);
                                ringProgressDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.please_wait), getResources().getString(R.string.getting_data), true);
                                ringProgressDialog.setCancelable(false);
                                Utils.clearIfNotEmpty(categoryName, categorySize, channelName, channelLink);
                                URL = "https://raw.githubusercontent.com/michelelacorte/IPTVFree/master/English/iptvlistEnglish.xml";
                                try {
                                    new GetDataAsync(MainActivity.this).execute(URL).get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                                myrunnable = new Runnable() {
                                    public void run() {
                                        setupViewPager(viewPager);
                                        tabLayout.setupWithViewPager(viewPager);
                                        ringProgressDialog.dismiss();
                                    }
                                    };
                                handler.postDelayed(myrunnable, 1000);
                                channelNameArray = channelName.toArray(new String[channelName.size()]);
                                searchView.setSuggestions(channelNameArray);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.mixed:
                                MainActivity.this.setTitle(getResources().getString(R.string.mixed));
                                item.setVisible(true);
                                ringProgressDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.please_wait), getResources().getString(R.string.getting_data), true);
                                ringProgressDialog.setCancelable(false);
                                Utils.clearIfNotEmpty(categoryName, categorySize, channelName, channelLink);
                                URL = "https://raw.githubusercontent.com/michelelacorte/IPTVFree/master/Mixed/iptvlistMixed.xml";
                                try {
                                    new GetDataAsync(MainActivity.this).execute(URL).get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                                myrunnable = new Runnable() {
                                    public void run() {
                                        setupViewPager(viewPager);
                                        tabLayout.setupWithViewPager(viewPager);
                                        ringProgressDialog.dismiss();
                                    }
                                };
                                handler.postDelayed(myrunnable, 1000);
                                channelNameArray = channelName.toArray(new String[channelName.size()]);
                                searchView.setSuggestions(channelNameArray);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.favorite:
                                if(favoriteLink != null && favoriteName != null
                                        && favoriteLink.size() > 0 && favoriteName.size() > 0) {
                                    MainActivity.this.setTitle(getResources().getString(R.string.favorite));
                                    item.setVisible(false);
                                    ringProgressDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.please_wait), getResources().getString(R.string.getting_data), true);
                                    ringProgressDialog.setCancelable(false);
                                    isFavorite = true;
                                    myrunnable = new Runnable() {
                                        public void run() {
                                            setupViewPagerFavorite(viewPager);
                                            tabLayout.setupWithViewPager(viewPager);
                                            ringProgressDialog.dismiss();
                                        }
                                    };
                                    handler.postDelayed(myrunnable, 1000);
                                }else{
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogCustom);
                                    dialog.setTitle(getResources().getString(R.string.dialog_loader_list_favorite));
                                    dialog.setMessage(getResources().getString(R.string.dialog_loader_list_favorite_message));
                                    dialog.setCancelable(true);
                                    dialog.setNegativeButton(getResources().getString(R.string.dialog_loader_cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    dialog.create();
                                    dialog.show();
                                }
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.Bug:
                                BugReport.reportBug(MainActivity.this);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.mylist:
                                if(isMyListLoadedFromPath) {
                                    MainActivity.this.setTitle(getResources().getString(R.string.mylist));
                                    item.setVisible(false);
                                    ringProgressDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.please_wait), getResources().getString(R.string.getting_data), true);
                                    ringProgressDialog.setCancelable(false);
                                    myrunnable = new Runnable() {
                                        public void run() {
                                            setupViewPagerPersonal(viewPager);
                                            tabLayout.setupWithViewPager(viewPager);
                                            ringProgressDialog.dismiss();
                                        }
                                    };
                                    handler.postDelayed(myrunnable, 1000);
                                    drawerLayout.closeDrawers();
                                }else if(isMyListLoadedFromURL) {
                                    MainActivity.this.setTitle(getResources().getString(R.string.mylist));
                                    item.setVisible(false);
                                    ringProgressDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.please_wait), getResources().getString(R.string.getting_data), true);
                                    ringProgressDialog.setCancelable(false);
                                    Utils.clearIfNotEmpty(categoryNamePersonal, categorySizePersonal, channelNamePersonal, channelLinkPersonal);
                                    URL = Utils.getLoadedFromURLString();
                                    if(FirstRun.isFirstLaunch(getApplicationContext()))
                                    {
                                        preferencesIPTV.savePreferencesURL(getApplicationContext(), "PersonalURL", URL);
                                    }else if(URL == null)
                                    {
                                        URL = preferencesIPTV.loadPreferencesURL(getApplicationContext(), "PersonalURL");
                                    }
                                    preferencesIPTV.savePreferencesURL(getApplicationContext(), "PersonalURL", URL);
                                    try {
                                        new GetDataAsyncPersonal(MainActivity.this).execute(URL).get();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                    myrunnable = new Runnable() {
                                        public void run() {
                                            setupViewPagerPersonal(viewPager);
                                            tabLayout.setupWithViewPager(viewPager);
                                            ringProgressDialog.dismiss();
                                        }
                                    };
                                    handler.postDelayed(myrunnable, 10000);
                                    drawerLayout.closeDrawers();
                                }else{
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogCustom);
                                    dialog.setTitle(getResources().getString(R.string.dialog_loader_list_title));
                                    dialog.setMessage(getResources().getString(R.string.dialog_loader_list_message));
                                    dialog.setCancelable(true);
                                    dialog.setNegativeButton(getResources().getString(R.string.dialog_loader_cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    dialog.create();
                                    dialog.show();
                                }
                                drawerLayout.closeDrawers();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

                drawerLayout = (DrawerLayout) findViewById(R.id.DrawerLayout);
                mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                    }
                };
                drawerLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerToggle.syncState();
                    }
                });
                drawerLayout.setDrawerListener(mDrawerToggle);
                mDrawerToggle.syncState();
                //Set-up Tab
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);

                Utils.tutorialView(this, "IPTVFreeIntroView", toolbar, fabMenu, tabLayout, viewPager, searchView);
        }else{
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this, R.style.AlertDialogCustom);
            builder.setTitle(getResources().getString(R.string.app_name));
            builder.setCancelable(false);
            builder.setMessage(getResources().getString(R.string.connection));
            builder.setPositiveButton(getResources().getString(R.string.ok_dialog), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    /**
     * Hanlde request permission result
     * @param requestCode int
     * @param permissions String[]
     * @param grantResults int[]
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSIONS: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    /**
     * On click interface implemented
     * @param v View
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fabMenu:
                animateFAB();
                break;
            case R.id.fabAdd:
                Utils.addFromURLOrFolder(MainActivity.this);
                closeFAB();
                break;
            case R.id.fabDownload:
                closeFAB();
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage(getResources().getString(R.string.download_message));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);
                final DownloadM3UList downloadTask = new DownloadM3UList(MainActivity.this);
                downloadTask.execute(URL);

                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });
                break;
        }
    }

    /**
     * On stop application
     */
    @Override
    public void onStop()
    {
        super.onStop();
        savePersonalList();
        saveFavoriteList();
        saveListState();
    }

    /**
     * On destroy application
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        savePersonalList();
        saveFavoriteList();
        saveListState();
    }

    /**
     * On pause application
     */
    @Override
    public void onPause()
    {
        super.onPause();
        savePersonalList();
        saveFavoriteList();
        saveListState();
    }

    /**
     * Series of option for close FAB
     */
    private void closeFAB()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            labelAdd.startAnimation(fab_close);
            labelDownload.startAnimation(fab_close);
        }
        fabMenu.startAnimation(rotate_backward);
        fabAdd.startAnimation(fab_close);
        fabDownload.startAnimation(fab_close);
        fabAdd.setClickable(false);
        fabDownload.setClickable(false);
        isFabOpen = false;
    }

    /**
     * Series of option for open FAB
     */
    private void openFAB()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            labelAdd.startAnimation(fab_open);
            labelDownload.startAnimation(fab_open);
        }
        fabMenu.startAnimation(rotate_forward);
        fabAdd.startAnimation(fab_open);
        fabDownload.startAnimation(fab_open);
        fabAdd.setClickable(true);
        fabDownload.setClickable(true);
        isFabOpen = true;
    }

    /**
     * Animation of Floating Action Button
     */
    public void animateFAB(){
        if(isFabOpen){
            closeFAB();
        } else {
            openFAB();
        }
    }

    /**
     * Set up view pager of other category
     * @param viewPager ViewPager
     */
    private void setupViewPager(final ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        CATEGORY = 0;
        CATEGORY_INDEX = 0;
        DEFAULT = true;
        if(Utils.isOnline(getApplicationContext())) {
            for (int i = 0; i < categoryName.size(); i++) {
                try {
                    if (MainActivity.categorySize.get(MainActivity.CATEGORY_INDEX) != null) {
                        ArrayList<String> channels = new ArrayList<String>(channelName.subList(CATEGORY, categorySize.get(CATEGORY_INDEX)));
                        ArrayList<String> links = new ArrayList<String>(channelLink.subList(CATEGORY, categorySize.get(CATEGORY_INDEX)));
                        CATEGORY = categorySize.get(CATEGORY_INDEX);
                        CATEGORY_INDEX++;
                        adapter.addFragment(FragmentIPTV.newInstance(channels, links), categoryName.get(i));
                        DEFAULT = false;
                    }
                } catch (Exception e) {
                        ArrayList<String> channels = new ArrayList<String>(channelName.subList(CATEGORY, channelName.size()));
                        ArrayList<String> links = new ArrayList<String>(channelLink.subList(CATEGORY, channelLink.size()));
                        adapter.addFragment(FragmentIPTV.newInstance(channels, links), categoryName.get(i));

                }
            }
            viewPager.setAdapter(adapter);
        }

    }

    /**
     * Setup View pager with personal data
     * @param viewPager ViewPager
     */
    private void setupViewPagerPersonal(final ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        CATEGORY = 0;
        CATEGORY_INDEX = 0;
        DEFAULT = true;
        if(Utils.isOnline(getApplicationContext())) {
            for (int i = 0; i < categoryNamePersonal.size(); i++) {
                try {
                    if (MainActivity.categorySizePersonal.get(MainActivity.CATEGORY_INDEX) != null) {
                        ArrayList<String> channels = new ArrayList<String>(channelNamePersonal.subList(CATEGORY, categorySizePersonal.get(CATEGORY_INDEX)));
                        ArrayList<String> links = new ArrayList<String>(channelLinkPersonal.subList(CATEGORY, categorySizePersonal.get(CATEGORY_INDEX)));
                        CATEGORY = categorySizePersonal.get(CATEGORY_INDEX);
                        CATEGORY_INDEX++;
                        adapter.addFragment(FragmentIPTV.newInstance(channels, links), categoryNamePersonal.get(i));
                        DEFAULT = false;
                    }
                } catch (Exception e) {
                    ArrayList<String> channels = new ArrayList<String>(channelNamePersonal.subList(CATEGORY, channelNamePersonal.size()));
                    ArrayList<String> links = new ArrayList<String>(channelLinkPersonal.subList(CATEGORY, channelLinkPersonal.size()));
                    adapter.addFragment(FragmentIPTV.newInstance(channels, links), categoryNamePersonal.get(i));

                }
            }
            viewPager.setAdapter(adapter);
        }

    }

    /**
     * Set up view pager of Favorite category
     * @param viewPager ViewPager
     */
    private void setupViewPagerFavorite(final ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if(Utils.isOnline(getApplicationContext())) {
            if(isFavorite) {
                adapter.addFragment(FragmentIPTVFavorite.newInstance(favoriteName, favoriteLink), getResources().getString(R.string.favorite));
            }
            viewPager.setAdapter(adapter);
        }

    }


    /**
     * Handle file listener response
     * @param activity Activity
     * @return
     */
    public static OnHandleFileListener onHandleFileListener(final Activity activity) {
        return new OnHandleFileListener() {
            @Override
            public void handleFile(final String filePath) {
                try{
                    try {
                        Utils.clearIfNotEmpty(categoryNamePersonal, categorySizePersonal, channelNamePersonal, channelLinkPersonal);
                        File file = new File(filePath);
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String inputLine = null;
                        try {
                            inputLine = br.readLine();
                            boolean dummyLink = false;
                            int j = 0;
                            int line = 0;
                            while ((inputLine = br.readLine()) != null){
                                String st = inputLine.toString();
                                line++;
                                if(st.equalsIgnoreCase("#EXTM3U"))
                                {
                                    //Not relevant line, decrease.
                                    line--;
                                }
                                if(st.startsWith("http") || st.startsWith("rtmp") || st.startsWith("rtsp") || st.startsWith("mmsh")){
                                    if(dummyLink) {
                                        dummyLink = false;
                                    }else{
                                        j++;
                                        channelLinkPersonal.add(st);
                                    }
                                }
                                String dump = st;
                                for (int i=0; i<st.length()-1; i++){
                                    if(st.charAt(i)==','){
                                        String afterComma = dump.replaceAll(".*,", "").trim();
                                        if(!Character.isLetterOrDigit(afterComma.charAt(0))){
                                            Pattern pattern = Pattern.compile("[^a-z A-Z]");
                                            Matcher matcher = pattern.matcher(afterComma);
                                            categoryNamePersonal.add(matcher.replaceAll(""));
                                            dummyLink = true;
                                            if(line > 1) {
                                                categorySizePersonal.add(j);
                                            }
                                        }else if(Character.isLetterOrDigit(afterComma.charAt(0))){
                                            channelNamePersonal.add(afterComma);
                                        }
                                    }
                                }
                            }
                            categorySizePersonal.add(j);
                            isMyListLoadedFromPath = true;
                            isMyListLoadedFromURL = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }catch(Exception e) {
                    Log.e(TAG, "Data Async: " + e);
                }
                Toast.makeText(activity, activity.getResources().getString(R.string.dialog_loader_load) + ": " + filePath, Toast.LENGTH_LONG).show();
            }
        };
    }

    /**
     * Request permission on android M
     * @param activity Activity
     */
    public void requestWriteExternalStoragePermission(Activity activity)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasWriteExternalStoragePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            List<String> permissions = new ArrayList<String>();
            if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissions.isEmpty()) {
                activity.requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSIONS);
            }
        }
    }

    /**
     * Save personal list on Shared Preferences
     */
    private void savePersonalList()
    {
        preferencesIPTV.savePreferencesData(getApplicationContext(),  "channelNamePersonal", channelNamePersonal);
        preferencesIPTV.savePreferencesData(getApplicationContext(), "channelLinkPersonal", channelLinkPersonal);
        preferencesIPTV.savePreferencesData(getApplicationContext(), "categoryNamePersonal", categoryNamePersonal);
        List<String> convertedCategorySizePersonal = new ArrayList<String>(categorySizePersonal.size());
        for (Integer myInt : categorySizePersonal) {
            convertedCategorySizePersonal.add(String.valueOf(myInt));
        }
        preferencesIPTV.savePreferencesData(getApplicationContext(), "categorySizePersonal", convertedCategorySizePersonal);
    }

    /**
     * Restore personal list from Shared Preferences
     */
    private void restorePersonalList()
    {
        try {
            channelLinkPersonal = preferencesIPTV.loadPreferencesData(getApplicationContext(), "channelLinkPersonal");
            channelNamePersonal = preferencesIPTV.loadPreferencesData(getApplicationContext(), "channelNamePersonal");
            categoryNamePersonal = preferencesIPTV.loadPreferencesData(getApplicationContext(), "categoryNamePersonal");
            List<String> convertedCategorySizePersonal = preferencesIPTV.loadPreferencesData(getApplicationContext(), "categorySizePersonal");
            for (String toConvert : convertedCategorySizePersonal) {
                categorySizePersonal.add(Integer.valueOf(toConvert));
            }
            Collections.sort(categorySizePersonal);
        }catch(Exception e)
        {
            Log.e(TAG, "Some list are empty! \n" + e);
        }
    }

    /**
     * Save favorite list on Shared Preferences
     */
    private void saveFavoriteList()
    {
        preferencesIPTV.savePreferencesData(getApplicationContext(), "favoriteName", favoriteName);
        preferencesIPTV.savePreferencesData(getApplicationContext(), "favoriteLink", favoriteLink);
    }

    /**
     * Restore favorite list from Shared Preferences
     */
    private void restoreFavoriteList()
    {
        try {
            favoriteName = preferencesIPTV.loadPreferencesData(getApplicationContext(), "favoriteName");
            favoriteLink = preferencesIPTV.loadPreferencesData(getApplicationContext(), "favoriteLink");
        }catch(Exception e){
            Log.e(TAG, "Some list are empty! \n" + e);
        }
    }

    /**
     * Save list state on Shared Preferences
     */
    private void saveListState()
    {
        state = new ArrayList<>();
        state.add(String.valueOf(isMyListLoadedFromPath));
        state.add(String.valueOf(isMyListLoadedFromURL));
        preferencesIPTV.savePreferencesData(getApplicationContext(), "stateList", state);
    }

    /**
     * Restore list state on Shared Preferences
     */
    private void restoreListState()
    {
        try {
            state = preferencesIPTV.loadPreferencesData(getApplicationContext(), "stateList");
            isMyListLoadedFromPath = Boolean.valueOf(state.get(0));
            isMyListLoadedFromURL = Boolean.valueOf(state.get(1));
        }catch(Exception e){
            Log.e(TAG, "Some list are empty! \n" + e);
        }
    }

    private void disclaimerAlertDialog()
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(R.string.disclaimer_dialog_message));
        builder.setNegativeButton(getResources().getString(R.string.disclaimer_dialog_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.disclaimer_dialog_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               //no operation
            }
        });
        builder.show();
    }
}