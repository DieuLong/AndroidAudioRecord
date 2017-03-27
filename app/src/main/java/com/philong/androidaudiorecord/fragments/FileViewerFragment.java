package com.philong.androidaudiorecord.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philong.androidaudiorecord.R;
import com.philong.androidaudiorecord.adapters.FileViewerAdapter;

/**
 * Created by Long on 08/03/2017.
 */

public class FileViewerFragment extends Fragment {

    private static final String ARG_POSITION  = "position";
    private static final String LOG_TAG = "FileViewerFragment";

    private int position;
    private FileViewerAdapter adapter;

    public static FileViewerFragment newInstance(int position){
        FileViewerFragment fileViewerFragment = new FileViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_POSITION, position);
        fileViewerFragment.setArguments(bundle);
        return fileViewerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        observer.startWatching();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_viewer, container, false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager lnn = new LinearLayoutManager(getActivity());
        lnn.setOrientation(LinearLayoutManager.VERTICAL);
        lnn.setReverseLayout(true);
        lnn.setStackFromEnd(true);
        recyclerView.setLayoutManager(lnn);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new FileViewerAdapter(getActivity(), lnn);
        recyclerView.setAdapter(adapter);
        return view;
    }

    FileObserver observer = new FileObserver(Environment.getExternalStorageDirectory().toString() + "SoundRecorder") {
        @Override
        public void onEvent(int event, String path) {
            if(event == FileObserver.DELETE){
                String filePath = Environment.getExternalStorageDirectory().toString() + "/SoundRecorder" + path + "]";
                adapter.removeOutOfApp(filePath);
            }
        }
    };
}
