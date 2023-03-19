package com.example.finalprojecttake2.ui.favorites;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojecttake2.R;
import com.example.finalprojecttake2.Song;
import com.example.finalprojecttake2.SongDetails;
import com.example.finalprojecttake2.databinding.FragmentFavoritesBinding;
import com.example.finalprojecttake2.ui.search.SearchFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment {

    private FavoritesViewModel favoritesViewModel;
    private FragmentFavoritesBinding binding;

    FirebaseFirestore db;
    FavListAdapter favListAdapter;
    ArrayList<Song> favs;
    ListenerRegistration listenerRegistration;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*favoritesViewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
        final TextView textView = binding.textFavorites;
        favoritesViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        favs = new ArrayList<>();
        favListAdapter = new FavoritesFragment.FavListAdapter(favs);
        binding.favoritesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(), 2));
        binding.favoritesRecyclerView.setAdapter(favListAdapter);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        listenerRegistration = db.collection("songs").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    return;
                }

                fillFavList();

                favListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        listenerRegistration.remove();
    }

    private void fillFavList() {
        if (favs != null) {
            favs.clear();
        }
        else {
            favs = new ArrayList<>();
        }
        db.collection("favorites").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        favs.add(new Song(doc.get("name"), doc.get("ragam"), doc.get("type")));
                    }

                    favListAdapter.notifyDataSetChanged();
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

    public class FavListAdapter extends RecyclerView.Adapter<FavoritesFragment.FavListAdapter.ViewHolder>{

        private ArrayList<Song> list;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView nameDisplay, ragamDisplay;
            public CardView cardDisplayView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameDisplay = itemView.findViewById(R.id.display_fav_song_name);
                ragamDisplay = itemView.findViewById(R.id.display_fav_ragam_name);
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

        public FavListAdapter(ArrayList<Song> list){
            this.list = list;
        }

        @NonNull
        @Override
        public FavoritesFragment.FavListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View songView = inflater.inflate(R.layout.favorites_list_layout, parent, false);
            FavoritesFragment.FavListAdapter.ViewHolder viewHolder = new FavoritesFragment.FavListAdapter.ViewHolder(songView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull FavoritesFragment.FavListAdapter.ViewHolder holder, int position) {
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