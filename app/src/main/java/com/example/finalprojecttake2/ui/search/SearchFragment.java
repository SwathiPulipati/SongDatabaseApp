package com.example.finalprojecttake2.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojecttake2.AddSongActivity;
import com.example.finalprojecttake2.R;
import com.example.finalprojecttake2.Song;
import com.example.finalprojecttake2.SongDetails;
import com.example.finalprojecttake2.databinding.FragmentSearchBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class SearchFragment extends Fragment {

    //private SearchViewModel searchViewModel;
    private FragmentSearchBinding binding;
    ArrayList<Song> songs;
    SongListAdapter songListAdapter;
    FirebaseFirestore db;
    String spinnerSelection;
    //private static final int ADD_BUTTON_RESULT_CODE = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        searchViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;*/

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        songs = new ArrayList<>();
        songListAdapter = new SongListAdapter(songs);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        binding.recyclerView.setAdapter(songListAdapter);

        binding.addActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddSongActivity.class));
            }
        });

    //spinner adapter
        ArrayList<String> spinnerList = new ArrayList<>();
        spinnerList.add("Name");
        spinnerList.add("Ragam");
        spinnerList.add("Talam");
        spinnerList.add("Type");
        spinnerList.add("Composer");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, spinnerList);
        binding.spinner.setAdapter(spinnerAdapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinnerSelection = spinnerList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    //search view
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.equals("")){
                    fillSongList();
                }else {
                    filter(s);
                }
                return false;
            }
        });



        return root;
    }

    private String setTitleCase(String s) {
        String[] strings = s.trim().split(" ");
        s = "";
        for (String str: strings) {
            str = str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
            s += str+ " ";
        }
        return s.trim();
    }

    private void filter(String s) {
        s = setTitleCase(s);

        System.out.println("FILTER WORD => " +s);
        if (songs != null)
            songs.clear();
        db.collection("songs").whereEqualTo(spinnerSelection.toLowerCase().trim(), s).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        songs.add(new Song(doc.get("name"), doc.get("ragam"), doc.get("type")));
                    }

                    Toast.makeText(getActivity(), "Document Call Success", Toast.LENGTH_SHORT).show();
                    songListAdapter.notifyDataSetChanged();
                            /*for (Song s: songs) {
                                System.out.println("DOCUMENT   => " +s.getName());
                            }*/

                } else {
                    Toast.makeText(getActivity(), "Document Call Failed", Toast.LENGTH_SHORT).show();
                    System.out.println("Failure");
                }
            }
        });
    }



    @Override
    public void onStart() {
        super.onStart();
        db.collection("songs").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    return;
                }

                fillSongList();

                songListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fillSongList() {
        if (songs != null) {
            songs.clear();
        }
        else {
            songs = new ArrayList<>();
        }
        db.collection("songs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                songs.add(new Song(doc.get("name"), doc.get("ragam"), doc.get("type")));
                            }

                            songListAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(), "Document Call Success", Toast.LENGTH_SHORT).show();
                            /*for (Song s: songs) {
                                System.out.println("DOCUMENT   => " +s.getName());
                            }*/

                        } else {
                            Toast.makeText(getActivity(), "Document Call Failed", Toast.LENGTH_SHORT).show();
                            System.out.println("Failure");
                        }
                    }
                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder>{

        private ArrayList<Song> list;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView nameDisplay, ragamDisplay;
            public CardView cardDisplayView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameDisplay = itemView.findViewById(R.id.display_song_name);
                ragamDisplay = itemView.findViewById(R.id.display_ragam_name);
                cardDisplayView = itemView.findViewById(R.id.cardview_display);

                itemView.setOnClickListener(this::onClick);
            }

            public void onClick(View view) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Song item = list.get(position);
                    Intent intent = new Intent(getActivity(), SongDetails.class);
                    intent.putExtra("name", item.getName());
                    intent.putExtra("ragam", item.getRagam());
                    startActivity(intent);

                }
            }
        }

        public SongListAdapter(ArrayList<Song> list){
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View songView = inflater.inflate(R.layout.song_list_layout, parent, false);
            ViewHolder viewHolder = new ViewHolder(songView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Song item = list.get(position);

            holder.nameDisplay.setText(item.getName());
            holder.ragamDisplay.setText(item.getRagam());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


    }
}