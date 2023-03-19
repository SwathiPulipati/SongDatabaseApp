package com.example.finalprojecttake2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SongDetails extends AppCompatActivity {

    TextView nameDisplay, ragamDisplay, talamDisplay, typeDisplay, composerDisplay, yearDisplay, notesDisplay, classFilesDisplay, masterCopyDisplay, songDetailsHeading;
    Button addClassFilesButton, removeClassFilesButton, replaceMasterCopyButton, fillQueueButton;
    ImageButton nameEditButton, ragamEditButton, talamEditButton, typeEditButton, composerEditButton, yearEditButton, notesEditButton, favoriteButton;
    FirebaseFirestore db;
    String name, ragam, talam, type, composer, years, notes, masterCopyUrl, documentName;
    ArrayList<String> classFileUrls;
    private static final int AUDIO_REQUEST = 3, EDIT_REQUEST = 4;
    String toggleFileCategory;
    boolean isFavorite;

    Uri audioUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_details);

        System.out.println("ON CREATE METHOD <=> ON CREATE METHOD");

        songDetailsHeading = findViewById(R.id.song_details_heading);
        nameDisplay = findViewById(R.id.name_display);
        ragamDisplay = findViewById(R.id.ragam_display);
        talamDisplay = findViewById(R.id.talam_display);
        typeDisplay = findViewById(R.id.type_display);
        composerDisplay = findViewById(R.id.composer_display);
        yearDisplay = findViewById(R.id.year_display);
        notesDisplay = findViewById(R.id.notes_display);
        classFilesDisplay = findViewById(R.id.class_files_display);
        masterCopyDisplay = findViewById(R.id.master_copy_display);

        fillQueueButton = findViewById(R.id.fill_queue_button);
        addClassFilesButton = findViewById(R.id.add_more_class_files_button);
        removeClassFilesButton = findViewById(R.id.remove_class_files_button);
        replaceMasterCopyButton = findViewById(R.id.replace_master_copy_button);

        nameEditButton = findViewById(R.id.name_edit_button);
        ragamEditButton = findViewById(R.id.ragam_edit_button);
        talamEditButton = findViewById(R.id.talam_edit_button);
        typeEditButton = findViewById(R.id.type_edit_button);
        composerEditButton = findViewById(R.id.composer_edit_button);
        yearEditButton = findViewById(R.id.year_edit_button);
        notesEditButton = findViewById(R.id.notes_edit_button);
        favoriteButton = findViewById(R.id.favorite_button);

        db = FirebaseFirestore.getInstance();
        classFileUrls = new ArrayList<>();
        documentName = getIntent().getStringExtra("name") + " - " + getIntent().getStringExtra("ragam");


        addClassFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFileCategory = "class-recordings";
                openAudioFile();
            }
        });
        removeClassFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classFileUrls.clear();
                db.collection("songs").document(documentName).update("class recordings", classFileUrls).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callData(documentName);
                        }
                    }
                });
            }
        });
        replaceMasterCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFileCategory = "master-copies";
                openAudioFile();
            }
        });

        nameEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEditActivity("name");
            }
        });
        ragamEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEditActivity("ragam");
            }
        });

        talamEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEditActivity("talam");
            }
        });
        typeEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEditActivity("type");
            }
        });
        composerEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEditActivity("composer");
            }
        });
        yearEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEditActivity("years");
            }
        });
        notesEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEditActivity("notes");
            }
        });

        fillQueueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearQueue();
            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFavorite){
                    favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    isFavorite = false;
                    db.collection("favorites").document(name +" - "+ ragam).delete();
                }
                else{
                    favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_24);
                    isFavorite = true;
                    makeFavorite();
                }
            }
        });

    }

    private void makeFavorite() {
        Map<String, Object> songInfo = new HashMap<>();
        songInfo.put("name", name);
        songInfo.put("ragam", ragam);
        songInfo.put("talam", talam);
        songInfo.put("type", type);
        songInfo.put("composer", composer);
        songInfo.put("years", years);
        songInfo.put("notes", notes);
        songInfo.put("class recordings", classFileUrls);
        songInfo.put("master copy", masterCopyUrl);

        System.out.println(songInfo);

        db.collection("favorites").document(name+ " - " +ragam).set(songInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(SongDetails.this, "Added to Favorites!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFavorite() {
        isFavorite = false;
        db.collection("favorites").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().isEmpty())
                    isFavorite = false;
                else{
                    for (QueryDocumentSnapshot doc: task.getResult()){
                        if(doc.getId().equals(name +" - "+ ragam)) {
                            isFavorite = true;
                            favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_24);

                        }
                    }
                }
            }
        });
    }

    private void clearQueue() {
        db.collection("queue").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot doc: task.getResult()) {
                        db.collection("queue").document(doc.getId()).delete();
                    }
                    addToQueue();
                }
            }
        });
    }

    private void addToQueue() {
        Map<String, Object> songInfo = new HashMap<>();
        songInfo.put("name", name);
        songInfo.put("ragam", ragam);
        songInfo.put("talam", talam);
        songInfo.put("type", type);
        songInfo.put("composer", composer);
        songInfo.put("years", years);
        songInfo.put("notes", notes);
        songInfo.put("class recordings", classFileUrls);
        songInfo.put("master copy", masterCopyUrl);

        System.out.println(songInfo);

        db.collection("queue").document(name+ " - " +ragam).set(songInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(SongDetails.this, "Added To Queue!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void callEditActivity(String field) {
        Intent intent = new Intent(SongDetails.this, EditSongInfo.class);
        intent.putExtra("document name", name + " - " + ragam);
        intent.putExtra("field", field);
        startActivityForResult(intent, EDIT_REQUEST);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("ON START METHOD <=> ON START METHOD");


    }

    private void setUI() {
        setCorrectText(songDetailsHeading, name);
        setCorrectText(nameDisplay, name);
        setCorrectText(ragamDisplay, ragam);
        setCorrectText(talamDisplay, talam);
        setCorrectText(typeDisplay, type);
        setCorrectText(composerDisplay, composer);
        setCorrectText(yearDisplay, years);
        setCorrectText(notesDisplay, notes);

        if (masterCopyUrl == null) {
            masterCopyDisplay.setText("No Master Copy Uploaded");
            replaceMasterCopyButton.setText("Add");
        }
        else {
            masterCopyDisplay.setText("Master Copy Uploaded");
            replaceMasterCopyButton.setText("Replace");
        }

        if (classFileUrls == null)
            classFileUrls = new ArrayList<>();
        classFilesDisplay.setText(classFileUrls.size()+ " Class Files Uploaded");
    }

    private void setCorrectText(TextView display, String data) {
        if (data == null)
            display.setText("");
        else if (data.equals("notes")){

        }else{
            display.setText(data);
        }
    }

    private void callData(String documentName) {
        db.collection("songs").document(documentName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    name = (String) task.getResult().get("name");
                    ragam = (String) task.getResult().get("ragam");
                    talam = (String) task.getResult().get("talam");
                    type = (String) task.getResult().get("type");
                    composer = (String) task.getResult().get("composer");
                    years = (String) task.getResult().get("years");
                    notes = (String) task.getResult().get("notes");
                    masterCopyUrl = (String) task.getResult().get("master copy");
                    classFileUrls = new ArrayList<>();
                    classFileUrls = (ArrayList<String>) task.getResult().get("class recordings");

                    checkFavorite();

                    setUI();

                } else {
                    Toast.makeText(SongDetails.this, "Data Call Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openAudioFile() {
        System.out.println("SIZE OF CLASS RECORDING LIST (OPEN AUDIO FILE) => "+classFileUrls.size());
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUDIO_REQUEST && resultCode == RESULT_OK){
            audioUri = data.getData();
            System.out.println("SIZE OF CLASS RECORDING LIST (ON ACTIVITY RESULT) => "+classFileUrls.size());
            uploadAudioFile();
        }
        if (requestCode == EDIT_REQUEST && resultCode == RESULT_OK){
            System.out.println("ON ACTIVITY RESULT METHOD <=> ON ACTIVITY RESULT METHOD");
            documentName = data.getStringExtra("name") + " - " + data.getStringExtra("ragam");
        }
    }

    private void uploadAudioFile() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (audioUri != null){
            System.out.println("SIZE OF CLASS RECORDING LIST (UPLOAD AUDIO FILE) => "+classFileUrls.size());
            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(toggleFileCategory).child(System.currentTimeMillis() +"."+ getFileExtension(audioUri));
            fileRef.putFile(audioUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            Map<String, Object> urlInfo = new HashMap<>();

                            if (toggleFileCategory.equals("master-copies")){
                                masterCopyUrl = url;
                                urlInfo.put("master copy", masterCopyUrl);
                            }
                            else{
                                System.out.println("SIZE OF CLASS RECORDING LIST => "+classFileUrls.size());
                                classFileUrls.add(url);
                                urlInfo.put("class recordings", classFileUrls);
                                System.out.println("SIZE OF CLASS RECORDING LIST => "+classFileUrls.size());
                            }
                            
                            db.collection("songs").document(documentName).update(urlInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SongDetails.this, "File Uploaded!", Toast.LENGTH_SHORT).show();
                                        callData(documentName);
                                    }
                                }
                            });

                            pd.dismiss();
                            Toast.makeText(SongDetails.this, "File Uploaded!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("ON RESUME METHOD <=> ON RESUME METHOD");

        db.collection("songs").document(documentName).addSnapshotListener(SongDetails.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    System.out.println("FIREBASE ERROR => " +error);
                    return;
                }

                System.out.println("DOCUMENT NAME => " +documentName);
                callData(documentName);
                documentName = name +" - "+ ragam;
            }
        });
    }
}