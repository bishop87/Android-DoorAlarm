package it.bishop87.dooralarm.activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

public class RingtonePlayingService extends Service
{
    private Ringtone ringtone;

    private AudioManager audioManager;
    private Vibrator vibrator;
    private int currentVolumeLevel;
    private int currentRingerMode;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        try{
            currentVolumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            currentRingerMode = audioManager.getRingerMode();
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(
                    AudioManager.STREAM_RING,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
                    AudioManager.FLAG_VIBRATE);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Errore Gestione Audio", Toast.LENGTH_LONG).show();
        }

        Uri ringtoneUri = Uri.parse(intent.getExtras().getString("ringtone-uri"));
        //Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        this.ringtone = RingtoneManager.getRingtone(this, ringtoneUri);

        ringtone.play();

        //---vibrazione
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.cancel();
            long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                //deprecated in API 26
                vibrator.vibrate(pattern, 0);
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Errore Gestione Vibrazione", Toast.LENGTH_LONG).show();
        }

        //avvio alarm activity
        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alarmIntent);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        ringtone.stop();
        vibrator.cancel();

        try{
            audioManager.setRingerMode(currentRingerMode);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, currentVolumeLevel, AudioManager.FLAG_VIBRATE);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Errore", Toast.LENGTH_LONG).show();
        }
    }

}
