package com.cfd.rahul.fotonn;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1;
    private TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void informationMenu() {
        //TODO Replace with your own

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (matches.contains("Photon") || matches.contains("Photon it") ||
                    matches.contains("select function") || matches.contains("open features")
                    || matches.contains("photon my image") || matches.contains("do something")
                    || matches.contains("photo on it") || matches.contains("photon net")) {
                t1.speak("Operation Successful. Now please select an image", TextToSpeech.QUEUE_FLUSH, null);
                Intent i = new Intent(this, FotoNN.class);
                i.putExtra("flag", true);
                i.putExtra("camera", false);
                startActivity(i);
            } else if (matches.contains("emotion") || matches.contains("emotions") || matches.contains("Emotion API") ||
                    matches.contains("detect face") || matches.contains("show face") || matches.contains("amazon API")) {
                t1.speak("Operation Successful. Now please select an image", TextToSpeech.QUEUE_FLUSH, null);
                Intent i = new Intent(this, DetectFace.class);
                i.putExtra("flag", true);
                i.putExtra("camera", false);
                startActivity(i);
            } else if (matches.contains("emotion camera") || matches.contains("emotions camera") ||
                    matches.contains("Emotion API camera") ||
                    matches.contains("detect face  camera") || matches.contains("show face  camera") ||
                    matches.contains("amazon API  camera")) {
                t1.speak("Operation Successful. Opening Camera.", TextToSpeech.QUEUE_FLUSH, null);
                Intent i = new Intent(this, DetectFace.class);
                i.putExtra("flag", true);
                i.putExtra("camera", true);
                startActivity(i);
            } else if (matches.contains("camera") || matches.contains("open camera") ||
                    matches.contains("photon camera") || matches.contains("open camera")) {
                t1.speak("Operation Successful. Opening camera.", TextToSpeech.QUEUE_FLUSH, null);
                Intent i = new Intent(this, FotoNN.class);
                i.putExtra("flag", true);
                i.putExtra("camera", true);
                startActivity(i);
            } else if (matches.contains("information")) {
                informationMenu();
            } else {
                t1.speak("Operation UnSuccessful. Please try again!", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public void fotoNNButtonClick(View View) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speech recognition demo");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            //TODO Add about us section here
            startActivity(new Intent(this, AboutUs.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.foton_it) {
            Intent i = new Intent(this, FotoNN.class);
            i.putExtra("flag", false);
            i.putExtra("camera", false);
            startActivity(i);
        } else if (id == R.id.emotions_api) {
            Intent i = new Intent(this, DetectFace.class);
            i.putExtra("flag", false);
            startActivity(i);
        } else if (id == R.id.get_text) {
            Toast.makeText(this, "Working on it...\nTo be implemented soon.", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onDestroy() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
    }
}
