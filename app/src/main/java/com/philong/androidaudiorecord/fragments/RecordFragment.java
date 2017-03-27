package com.philong.androidaudiorecord.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.philong.androidaudiorecord.R;
import com.philong.androidaudiorecord.services.RecordingService;

import java.io.File;

/**
 * Created by Long on 08/03/2017.
 */

public class RecordFragment extends Fragment{

    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();
    private int position;
    private FloatingActionButton fab;
    private Button btnPause;
    private TextView txtPrompt;
    private int recordPromptCount = 0;
    private boolean startRecording = true;
    private boolean pauseRecording = true;
    private Chronometer chronometer;
    private long timeWhenPause = 0;

    public static RecordFragment newInstance(int position){
        RecordFragment f = new RecordFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_POSITION, position);
        f.setArguments(bundle);
        return f;
    }

    public RecordFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);
        chronometer = (Chronometer)recordView.findViewById(R.id.chronometer);
        txtPrompt = (TextView)recordView.findViewById(R.id.recording_status_text);
        fab = (FloatingActionButton)recordView.findViewById(R.id.btnRecord);
        fab.setColorNormal(getResources().getColor(R.color.primary));
        fab.setColorPressed(getResources().getColor(R.color.primary_dark));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(startRecording);
                startRecording = !startRecording;
            }
        });
        btnPause = (Button)recordView.findViewById(R.id.btnPause);
        btnPause.setVisibility(View.GONE);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseRecord(pauseRecording);
                pauseRecording = !pauseRecording;
            }
        });
        return recordView;
    }

    private void onRecord(boolean start){
        Intent intent = new Intent(getActivity(), RecordingService.class);
        if(start){
            fab.setImageResource(R.drawable.ic_media_stop);
            Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                folder.mkdir();
            }
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if(recordPromptCount == 0){
                        txtPrompt.setText(getString(R.string.record_in_progress) + ".");
                    }else if(recordPromptCount == 1){
                        txtPrompt.setText(getString(R.string.record_in_progress) + "..");
                    }else if(recordPromptCount == 2){
                        txtPrompt.setText(getString(R.string.record_in_progress) + "...");
                        recordPromptCount = -1;
                    }
                    recordPromptCount++;
                }
            });
            getActivity().startService(intent);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            txtPrompt.setText(getString(R.string.record_in_progress) + ".");
            recordPromptCount++;
        }else{
            fab.setImageResource(R.drawable.ic_mic_white_36dp);
            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPause = 0;
            txtPrompt.setText(getString(R.string.record_prompt));
            getActivity().stopService(intent);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onPauseRecord(boolean pause){
        if(pause){
            fab.setImageResource(R.drawable.ic_media_play);
            txtPrompt.setText(getString(R.string.resume_recording_button).toUpperCase());
            timeWhenPause = chronometer.getBase() - SystemClock.elapsedRealtime();
            chronometer.stop();
        }else{
            fab.setImageResource(R.drawable.ic_media_stop);
            txtPrompt.setText(getString(R.string.pause_recording_button).toUpperCase());
            chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPause);
            chronometer.start();
        }
    }
}
