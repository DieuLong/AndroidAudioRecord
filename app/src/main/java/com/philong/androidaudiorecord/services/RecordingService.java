package com.philong.androidaudiorecord.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.philong.androidaudiorecord.R;
import com.philong.androidaudiorecord.database.DBHelper;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Long on 08/03/2017.
 */


public class RecordingService extends Service {

    private static final String LOG_TAG = RecordingService.class.getSimpleName();
    private String fileName;
    private String filePath;
    private MediaRecorder mediaRecorder;
    private DBHelper db;
    private long startTime = 0;
    private long elapsedMillis = 0;
    private int elapsedSeconds = 0;


    private Timer timer;
    private TimerTask timerTask;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    public void startRecording(){
        setFileNameAndPath();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            startTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFileNameAndPath(){
        int count = 0;
        File f;
        do{
            count++;
            fileName = getString(R.string.default_file_name) + " #" + (db.getCount() + count) + ".mp4";
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            filePath += "/SoundRecorder/" + fileName;
            f = new File(filePath);

        }while(f.exists() && !f.isDirectory());
    }

    public void stopRecording(){
        mediaRecorder.stop();
        elapsedMillis = System.currentTimeMillis() - startTime;
        mediaRecorder.release();
        Toast.makeText(this, getString(R.string.toast_recording_finish) +  " " + fileName, Toast.LENGTH_SHORT).show();
        if(timerTask != null){
            timerTask.cancel();
            timerTask = null;
        }
        mediaRecorder = null;
        try{
            db.addRecording(fileName, filePath, elapsedMillis);
        }catch(Exception ex){
            ex.printStackTrace();;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaRecorder != null){
            stopRecording();
        }
    }


}
