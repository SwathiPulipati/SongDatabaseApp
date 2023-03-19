package com.example.finalprojecttake2.ui.player;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.finalprojecttake2.QueueFragment;
import com.example.finalprojecttake2.R;
import com.example.finalprojecttake2.WaveFragment;
import com.example.finalprojecttake2.databinding.FragmentPlayerBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PlayerFragment extends Fragment implements QueueFragment.ReceiveQueueFragmentValues{

    private PlayerViewModel playerViewModel;
    private FragmentPlayerBinding binding;

    FragmentTransaction fragmentTransaction;
    Fragment queueFragment, waveFragment;
    String audioUrl, documentName;

    MediaPlayer mp;
    boolean isPlaying = false, isPrepared = false, isLooping = false, showWave = false;

    FirebaseFirestore db;

    Timer timer;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        final TextView textView = binding.textPlayer;
        playerViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        binding = FragmentPlayerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        db = FirebaseFirestore.getInstance();

        queueFragment = new QueueFragment();
        waveFragment = new WaveFragment();
        fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.files_fragment_constraint, queueFragment);
        fragmentTransaction.commit();

        binding.switchFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showWave){
                    fragmentTransaction = getChildFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.files_fragment_constraint, queueFragment);
                    binding.switchFragmentButton.setImageResource(R.drawable.ic_baseline_waves_24);
                    showWave = false;
                    fragmentTransaction.commit();
                }else{
                    showWaveFragment();
                }
            }
        });

        binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_50);
        binding.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPrepared) {
                    if (isPlaying) {
                        binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_50);
                        mp.pause();
                        isPlaying = false;
                    } else {
                        binding.playButton.setImageResource(R.drawable.ic_baseline_pause_50);
                        mp.start();
                        isPlaying = true;
                    }
                }
            }
        });

        binding.forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPrepared){
                    mp.seekTo(mp.getCurrentPosition()+5000);
                    binding.currentTimestampDisplay.setText((mp.getCurrentPosition() / 60000) + ":" + returnSeconds(mp.getCurrentPosition()));
                }
            }
        });
        binding.rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPrepared){
                    mp.seekTo(mp.getCurrentPosition()-5000);
                    binding.currentTimestampDisplay.setText((mp.getCurrentPosition() / 60000) + ":" + returnSeconds(mp.getCurrentPosition()));
                }
            }
        });
        binding.loopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPrepared){
                    if (isLooping){
                        isLooping = false;
                        binding.loopButton.setImageResource(R.drawable.ic_baseline_text_rotation_none_24);
                        mp.setLooping(false);
                    }
                    else{
                        isLooping = true;
                        binding.loopButton.setImageResource(R.drawable.ic_baseline_repeat_one_24);
                        mp.setLooping(true);
                    }
                }
            }
        });

        binding.durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isPrepared && fromUser) {
                    System.out.println("ON PROGRESS CHANGED <=> ON PROGRESS CHANGED");
                    mp.seekTo(progress);
                    binding.currentTimestampDisplay.setText((mp.getCurrentPosition() / 60000) + ":" + returnSeconds(mp.getCurrentPosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return root;
    }

    private void showWaveFragment() {
        if (mp != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("audio session id", mp.getAudioSessionId());
            waveFragment.setArguments(bundle);
        }
        fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.files_fragment_constraint, waveFragment);
        binding.switchFragmentButton.setImageResource(R.drawable.ic_baseline_queue_music_24);
        showWave = true;
        fragmentTransaction.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.durationSeekBar.setProgress(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void receiveUrl(String audioUrl) {
        this.audioUrl = audioUrl;
        setUpMediaPlayer();
    }

    @Override
    public void receiveDocName(String docName) {
        documentName = docName;
    }

    private void setUpMediaPlayer() {
        if (mp != null ){
            binding.durationSeekBar.setProgress(0);
            if (mp.isPlaying())
                mp.stop();
            mp = null;
            isPlaying = false;
            isPrepared = false;
            isLooping = false;
            binding.loopButton.setImageResource(R.drawable.ic_baseline_text_rotation_none_24);
            if (timer != null)
                timer.cancel();
            binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_50);
            binding.currentTimestampDisplay.setText("0:00");
            binding.totalDurationDisplay.setText("0:00");
            if (showWave)
                showWaveFragment();
        }

        mp = new MediaPlayer();

        try {
            System.out.println("AUDIO URL => " +audioUrl);
            mp.setDataSource(audioUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        mp.prepareAsync();

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Toast.makeText(getActivity(), "File Prepared!", Toast.LENGTH_SHORT).show();
                isPrepared = true;
                if (showWave)
                    showWaveFragment();
                binding.durationSeekBar.setMax(mp.getDuration());
                binding.totalDurationDisplay.setText((mp.getDuration()/60000) +":"+ returnSeconds(mp.getDuration()));
                binding.currentTimestampDisplay.setText("0:00");
                playCycle();
            }
        });
    }

    private void playCycle() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                binding.currentTimestampDisplay.post(() -> {
                    binding.currentTimestampDisplay.setText((mp.getCurrentPosition() / 60000) + ":" + returnSeconds(mp.getCurrentPosition()));
                });
                binding.durationSeekBar.post(() -> {
                    binding.durationSeekBar.setProgress(mp.getCurrentPosition());
                });
            }
        },0,1000);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mp != null ){
            if (mp.isPlaying())
                mp.stop();
            mp = null;
            isPrepared = false;
            if (timer != null) {
                timer.cancel();
            }
            if (showWave)
                showWaveFragment();
        }
    }

    public String returnSeconds(int millis){
        int secs = 0;
        secs = millis % 60000 / 1000;
        if (secs < 10){
            return "0"+secs;
        }
        return String.valueOf(secs);
    }

}