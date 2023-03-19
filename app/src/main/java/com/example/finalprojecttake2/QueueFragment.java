package com.example.finalprojecttake2;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.finalprojecttake2.ui.player.PlayerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QueueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QueueFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ReceiveQueueFragmentValues receiveValues;
    FirebaseFirestore db;
    DocumentSnapshot document;
    ArrayList<String> masterCopies, classRecordings, classFiles;

    ArrayAdapter<String> masterCopyAdapter, classFilesAdapter;

    TextView songQueueHeading;
    ListView masterCopyDisplayQueue, classFilesDisplayQueue;
    ImageButton clearQueueButton;

    public QueueFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QueueFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QueueFragment newInstance(String param1, String param2) {
        QueueFragment fragment = new QueueFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        onAttachToParent(getParentFragment());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View queueFragmentView =  inflater.inflate(R.layout.fragment_queue, container, false);

        songQueueHeading = queueFragmentView.findViewById(R.id.song_queue_heading);
        masterCopyDisplayQueue = queueFragmentView.findViewById(R.id.master_copy_display_queue);
        classFilesDisplayQueue = queueFragmentView.findViewById(R.id.class_files_display_queue);
        clearQueueButton = getParentFragment().getView().findViewById(R.id.clear_queue_button);

        clearQueueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("CLEAR QUEUE BUTTON <=> CLEAR QUEUE BUTTON");
                db.collection("queue").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty())
                            db.collection("queue").document(document.getId()).delete();
                    }
                });
                updateView();
            }
        });

        masterCopies = new ArrayList<>();
        classRecordings = new ArrayList<>();
        classFiles = new ArrayList<>();

        masterCopyAdapter = new ArrayAdapter<String>(getContext(), R.layout.queue_list_view_layout, masterCopies);
        masterCopyDisplayQueue.setAdapter(masterCopyAdapter);
        classFilesAdapter = new ArrayAdapter<String>(getContext(), R.layout.queue_list_view_layout, classRecordings);
        classFilesDisplayQueue.setDivider(null);
        classFilesDisplayQueue.setAdapter(classFilesAdapter);

        db = FirebaseFirestore.getInstance();
        updateView();

        masterCopyDisplayQueue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String mc = document.get("master copy").toString();
                receiveValues.receiveUrl(mc);
            }
        });

        classFilesDisplayQueue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                receiveValues.receiveUrl(classFiles.get(i));
            }
        });

        return queueFragmentView;
    }

    private void updateView() {
        db.collection("queue").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && !task.getResult().isEmpty()){
                    for (QueryDocumentSnapshot doc: task.getResult()) {
                        document = doc;
                        receiveValues.receiveDocName(doc.getId());
                    }
                    songQueueHeading.setText(document.getId());

                    masterCopies.clear();
                    if (document.get("master copy") != null){
                        masterCopies.add("Master Copy");
                        masterCopyAdapter.notifyDataSetChanged();
                    }
                    classRecordings.clear();
                    classFiles.clear();
                    if (document.get("class recordings") != null){
                        classFiles = (ArrayList<String>) document.get("class recordings");
                        for (int i=0; i<classFiles.size(); i++){
                            classRecordings.add("Class Recording " +(i+1));
                        }
                        classFilesAdapter.notifyDataSetChanged();
                    }
                }
                else if(task.isSuccessful() && task.getResult().isEmpty()){
                    songQueueHeading.setText("Choose A Song");

                    masterCopies.clear();
                    masterCopyAdapter.notifyDataSetChanged();

                    classRecordings.clear();
                    classFiles.clear();
                    classFilesAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    public void onAttachToParent(Fragment parentFragment) {
        try {
            receiveValues = (ReceiveQueueFragmentValues) parentFragment;
        }catch (ClassCastException e){
            throw new ClassCastException(
                    parentFragment.toString() + " must implement OnPlayerSelectionSetListener");
        }
    }

    public interface ReceiveQueueFragmentValues{
        void receiveUrl(String audioUrl);  //true for class recordings, false for master copy
        void receiveDocName(String docName);
    }


}