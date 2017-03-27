package com.philong.androidaudiorecord.fragments;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.philong.androidaudiorecord.R;
import com.philong.androidaudiorecord.model.RecordingItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Long on 08/03/2017.
 */

public class PlaybackFragment extends DialogFragment {

    private static final String LOG_TAG = "PlaybackFragment";
    private static final String ARG_ITEM = "recording_item";
    private RecordingItem item;

    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer = null;
    private SeekBar seekBar;
    private FloatingActionButton fab;
    private TextView txtTimeStart;
    private TextView txtTimeEnd;
    private TextView txtNameFile;

    private boolean isPlaying = false;

    long minutes = 0;
    long seconds = 0;

    public static PlaybackFragment newInstance(RecordingItem item){
        PlaybackFragment playbackFragment = new PlaybackFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_ITEM, item);
        playbackFragment.setArguments(bundle);
        return playbackFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getArguments().getParcelable(ARG_ITEM);
        long itemDuration = item.getLength();
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_media_playback, null);
        txtNameFile = (TextView)view.findViewById(R.id.file_name_text_view);
        txtTimeEnd = (TextView)view.findViewById(R.id.file_length_text_view);
        txtTimeStart = (TextView)view.findViewById(R.id.current_progress_text_view);
        seekBar = (SeekBar)view.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress);
                    handler.removeCallbacks(runnable);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration()) - TimeUnit.MINUTES.toSeconds(minutes);
                    txtTimeStart.setText(String.format("%02d:%02d", minutes, seconds));
                    updateSeekBar();
                }else if(mediaPlayer == null && fromUser){
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer != null){
                    handler.removeCallbacks(runnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer != null){
                    handler.removeCallbacks(runnable);
                    mediaPlayer.seekTo(seekBar.getProgress());
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration()) - TimeUnit.MINUTES.toSeconds(minutes);
                    txtTimeStart.setText(String.format("%02d:%02d", minutes, seconds));
                    updateSeekBar();
                }
            }
        });
        alertDialog.setView(view);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return alertDialog.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        AlertDialog alertDialog = (AlertDialog)getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mediaPlayer != null){
            stopPlaying();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            stopPlaying();
        }
    }

    private void startPlaying(){
        fab.setImageResource(R.drawable.ic_media_pause);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(item.getFilePath());
            mediaPlayer.prepare();;
            seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });
        updateSeekBar();
    }

    private void prepareMediaPlayerFromPoint(int progress){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(item.getFilePath());
            mediaPlayer.prepare();
            seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pausePlaying(){
        fab.setImageResource(R.drawable.ic_media_play);
        handler.removeCallbacks(runnable);
        mediaPlayer.pause();
    }

    private void resumePLaying(){
        fab.setImageResource(R.drawable.ic_media_pause);
        handler.removeCallbacks(runnable);
        mediaPlayer.start();
        updateSeekBar();
    }

    private void stopPlaying(){
        fab.setImageResource(R.drawable.ic_media_play);
        handler.removeCallbacks(runnable);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        seekBar.setProgress(seekBar.getMax());
        isPlaying = !isPlaying;
        txtTimeStart.setText(txtTimeEnd.getText());
        seekBar.setProgress(seekBar.getMax());
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null){
                int current = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(current);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(current);
                long seconds = TimeUnit.MICROSECONDS.toSeconds(current) - TimeUnit.MINUTES.toSeconds(minutes);
                txtTimeStart.setText(String.format("%02d:02d", minutes, seconds));
                updateSeekBar();
            }
        }
    };

    private void updateSeekBar(){
        handler.postDelayed(runnable, 1000);
    }
}
