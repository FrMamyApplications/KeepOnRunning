package com.example.keeponrunning;

//https://stackoverflow.com/questions/40103742/update-textview-every-second-in-android

//https://stackoverflow.com/questions/1965784/streaming-audio-from-a-url-in-android-using-mediaplayer

//https://cs.android.com/android/platform/superproject/+/master:frameworks/base/media/java/android/media/MediaPlayer.java;l=4261

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

Button Start;
Button Stop;
ProgressBar ProgressBar;
TextView InfoAnzeige;

private WifiManager wifimanager;

Integer TimeStep=1000;
Integer TimerWert=0;

Boolean isPlaying_onSaveInstanceState;
Boolean maxWarteZeit_Abgelaufen=false;

Handler handler = new Handler();

MediaPlayer player=new MediaPlayer ();





public void check_If_WebAdress_is_Valid()   {


  final int maxWarteZeit = 10;

  TimerWert = TimerWert + 1;

  if (TimerWert>maxWarteZeit)
  {
    maxWarteZeit_Abgelaufen=true;
    TimerWert=0;
    player.reset();
    player.stop();
    ProgressBar.setVisibility(View.GONE);

    Toast toast = Toast.makeText(getApplicationContext(), "Web-Adresse nicht aufrufbar", Toast.LENGTH_LONG);toast.show();

  }
}

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            check_If_WebAdress_is_Valid();

            handler.postDelayed(this, TimeStep);

            //Log.d("aaa",String.valueOf (TimerWert));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate (savedInstanceState);


        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if(savedInstanceState == null)
        {
            isPlaying_onSaveInstanceState=false;
        }
        else
        {
            isPlaying_onSaveInstanceState = savedInstanceState.getBoolean ("isPlaying");

            Log.d("check","After onCreate:  "+String.valueOf ("isPlaying_onSaveInstanceState  "+isPlaying_onSaveInstanceState));
        }


        if (getResources ().getConfiguration ().orientation== Configuration.ORIENTATION_PORTRAIT)
        {
            setContentView (R.layout.activity_main);
        }

        if (getResources ().getConfiguration ().orientation==Configuration.ORIENTATION_LANDSCAPE)
        {
            setContentView (R.layout.activity_main_landscape);
        }

        Start = (Button) findViewById (R.id.buttonStart);
        Stop = (Button) findViewById (R.id.buttonStop);
        InfoAnzeige= (TextView) findViewById (R.id.counter);


        ProgressBar= (ProgressBar) findViewById (R.id.progressBar);
        ProgressBar.setVisibility(View.INVISIBLE);

        final WifiManager wifi = (WifiManager) getSystemService (Context.WIFI_SERVICE);
        wifimanager=(WifiManager) getApplication ().getSystemService (Context.WIFI_SERVICE);


        Start.setOnClickListener(new View.OnClickListener ()
        {

            @Override
            public void onClick(View v)
            {
                Integer WifiStatus=wifi.getConnectionInfo().getNetworkId();
                Log.d("12","qqq  "+String.valueOf (WifiStatus));


Log.d("check", "Start_button:  isPlaying "+String.valueOf (player.isPlaying ())+"  isPlayin_onSave.. "+String.valueOf (isPlaying_onSaveInstanceState));
                if (WifiStatus != -1) /* connected */
                {
                    if (player.isPlaying () || isPlaying_onSaveInstanceState)
                    {}
                    else
                    {
                        PlayMusic();
                    }

                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Kein WLAN", Toast.LENGTH_LONG);toast.show();
                }
            }
        });



        Stop.setOnClickListener(new View.OnClickListener ()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v)
            {

                if (isPlaying_onSaveInstanceState==true)
                {
                    //player.reset ();
                    StopPlayer();
                    Log.d("check","STOP!");
                }
                else
                {
                    StopPlayer();
                }


            }
        });

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer player) {
                ProgressBar.setVisibility(View.GONE);

                player.start();
                TimerWert=0;
                handler.removeCallbacks(runnable);
            }
        });


    }

    public void PlayMusic()
    {
        player.reset ();
        player.stop ();

        String url = "http://stream.radioluebeck.de/radioluebeck-192.mp3";
        try {
            player.setDataSource (url);
        } catch (IOException e) {
            e.printStackTrace ();
        }
        player.prepareAsync ();
        ProgressBar.setVisibility (View.VISIBLE);

        runnable.run ();
    }

    public void StopPlayer()
    {
        player.reset ();
        player.stop ();
        isPlaying_onSaveInstanceState=false;
        Log.d("check", "Stop_button:  isPlaying "+String.valueOf (player.isPlaying ())+"  isPlayin_onSave.. "+String.valueOf (isPlaying_onSaveInstanceState));

    }


    @Override
    protected void onStart()
    {
        super.onStart ();
        IntentFilter intentFilter = new IntentFilter (WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver (wifiStateReceiver, intentFilter);

    }

    @Override
    protected void onStop()
    {
        super.onStop ();
        unregisterReceiver (wifiStateReceiver);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //player.pause ();
        //player.reset ();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {


        if (player.isPlaying())
        {
            isPlaying_onSaveInstanceState=true;
        }
        else
        {
            isPlaying_onSaveInstanceState=false;
        }

        outState.putBoolean("isPlaying", isPlaying_onSaveInstanceState);

        super.onSaveInstanceState(outState);
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver () {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra (WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.d("checkWLan","Enable");
                    InfoAnzeige.setText("WALN Enable");

                    /*Log.d("checkWLan", String.valueOf (player.isPlaying ())+" Broad "+String.valueOf (isPlaying_onSaveInstanceState));

                    if (player.isPlaying () || isPlaying_onSaveInstanceState)
                    {}
                    else
                    {
                        StopPlayer();
                    }*/
                break;

                case WifiManager.WIFI_STATE_DISABLED:
                    Log.d("checkWlan","Disable");
                    InfoAnzeige.setText("WALN Disable");
                    StopPlayer();
                    break;
            }
        }
    };



}
