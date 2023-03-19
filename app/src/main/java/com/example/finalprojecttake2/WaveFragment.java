package com.example.finalprojecttake2;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chibde.visualizer.LineBarVisualizer;
import com.example.finalprojecttake2.ui.player.PlayerFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WaveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WaveFragment extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    LineBarVisualizer lineBarVisualizer;
    int audioSessionId;

    public WaveFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WaveFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WaveFragment newInstance(int audioSessionId) {
        WaveFragment fragment = new WaveFragment();
        Bundle args = new Bundle();
        args.putInt("audio session id", audioSessionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            audioSessionId = getArguments().getInt("audio session id");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("ON CREATE VIEW OF WAVE FRAGMENT => " +audioSessionId);
        View waveFragmentView =  inflater.inflate(R.layout.fragment_wave, container, false);
        if (getArguments() != null) {
            lineBarVisualization(waveFragmentView);
        }
        return waveFragmentView;
    }

    public void lineBarVisualization(View view) {
        lineBarVisualizer = view.findViewById(R.id.visualizerLineBar);
        lineBarVisualizer.setColor(ContextCompat.getColor(getContext(), R.color.white));
        lineBarVisualizer.setDensity(60);
        lineBarVisualizer.setPlayer(audioSessionId);
    }


}