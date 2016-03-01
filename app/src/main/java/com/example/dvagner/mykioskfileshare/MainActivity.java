package com.example.dvagner.mykioskfileshare;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sPref;
    final String URL = "saved_url";
    final String Domain = "saved_domain";
    final String User_Name = "saved_username";
    final String Pass = "saved_password";
    final String Timmer = "saved_timer";

    private Integer timer_settings;
    private WebView mWebView;
    protected PowerManager.WakeLock mWakeLock;
    private Timer autoUpdate;
    private Timer autoUpdateScreen;
    private  Long seconds;
    ArrayList<String> names = new ArrayList<String>();
    private String current_path="";
    private String domain;
    private String user_name;
    private String pass;
    private String url_settings;
    private String url1_settings1;
    private ImageView imgView;
    private VideoView vidView;
    private String ffile;
    private Integer counter=0;
    private ContentResolver cResolver;
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window = getWindow();
        cResolver = getContentResolver();
        autoUpdate = new Timer();
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        timer_settings =  Integer.parseInt(sPref.getString(Timmer, "0"));
        mWebView =(WebView) findViewById(R.id.webView);
        imgView = (ImageView) findViewById(R.id.imageView);
        vidView = (VideoView) findViewById(R.id.videoView);
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
        imgView.setAnimation(myFadeInAnimation);
        imgView.startAnimation(myFadeInAnimation);
        autoUpdateScreen = new Timer();
        autoUpdateScreen.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        screenSettings();

                    }
                });
            }
        }, 0, 10000); // updates each 40 secs
    }

    class LoadSMBFile extends AsyncTask<String, String, String> {
        private Exception exception=null;
        private ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Loading files from share, please wait.");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... f_url) {
                try {
                    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user_name, pass);
                    String path = "smb://"+url1_settings1+"";
                     SmbFile sFile = new SmbFile(path, auth);
                    SmbFile[] list = sFile.listFiles();
                    names.clear();
                    for (int i=0; i<list.length; i++) {
                        if (list[i].isFile()) {
                            SmbFileInputStream sfos = new SmbFileInputStream(list[i]);
                            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ "/" + list[i].getName());
                            //File outputFile = new File(getApplication().getFilesDir().getAbsolutePath()+ "/" + list[i].getName());
                            names.add(list[i].getName());
                                byte[] buffer = new byte[8192];
                                OutputStream outStream = new FileOutputStream(outputFile);
                                int n;
                                while ((n = sfos.read(buffer)) > 0) {
                                    outStream.write(buffer, 0, n);
                                }
                                outStream.close();
                         }
                    }
                } catch (SmbAuthException e) {
                    exception = e;
                } catch (SmbException e) {
                    exception = e;
                } catch (MalformedURLException e) {
                    exception = e;
                } catch (UnknownHostException e) {
                    exception = e;
                } catch (IOException e) {
                    exception = e;
                }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (exception!=null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Error!")
                        .setMessage(exception.getMessage())
                        .setCancelable(false)
                        .setNegativeButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ "/");
                for (File file : directory.listFiles()) {
                    if (file.isFile()) { names.add(file.getName()); }
                }
            }
            super.onPostExecute(s);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_reload)
        {
            new LoadSMBFile().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            webview.loadUrl(url);
            return true;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode,event);
    }

    @Override
    protected void onResume() {
        screenSettings();
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        url_settings = sPref.getString(URL, null);
        timer_settings = Integer.parseInt(sPref.getString(Timmer, "0"));
        url1_settings1 = sPref.getString(URL, null);
        domain = sPref.getString(Domain, null);
        user_name = sPref.getString(User_Name, null);
        pass = sPref.getString(Pass, null);
        new LoadSMBFile().execute();
        super.onResume();
        if (timer_settings>0) {
            autoUpdate.cancel();
            autoUpdate.purge();
            seconds = new Long(timer_settings * 1000);
            autoUpdate = new Timer();
            autoUpdate.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            updateHTML();
                        }
                    });
                }
            }, 0, seconds.longValue()); // updates each 40 secs
        }
        else
        {
            //Toast.makeText(this, "Timer  canceled ", Toast.LENGTH_SHORT).show();
            autoUpdate.cancel();
            autoUpdate.purge();
        }

    }

    private void screenSettings() {
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 255);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = 1;
        window.setAttributes(lp);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    private void updateHTML(){
        String type = null;
        screenSettings();
if (names.size()>0) {
    System.out.println(names.get(counter));
    //Toast.makeText(this, "Loaded file: "+names.get(counter), Toast.LENGTH_SHORT).show();
    File loadFile = new  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + names.get(counter));

    if(loadFile.exists()){
        String extension = MimeTypeMap.getFileExtensionFromUrl(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + names.get(counter));
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            Toast.makeText(this, "File type: "+type, Toast.LENGTH_SHORT).show();
             if (type.contains("image")) {
                 Bitmap myBitmap = BitmapFactory.decodeFile(loadFile.getAbsolutePath());
                 ImageView myImage = (ImageView) findViewById(R.id.imageView);
                 myImage.setImageBitmap(myBitmap);
             }
            if (type.contains("video")) {
                vidView.setVisibility(View.VISIBLE);
                vidView.setVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + names.get(counter));
                vidView.start();
            }
        }

    }
    counter++;
    if (counter==names.size()) { counter=0; }

}
    }

}
