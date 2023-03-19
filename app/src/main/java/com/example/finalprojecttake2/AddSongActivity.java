package com.example.finalprojecttake2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.finalprojecttake2.databinding.ActivityAddSongBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddSongActivity extends AppCompatActivity {

    ActivityAddSongBinding binding;
    String name, ragam, talam, type, composer, years, notes, masterCopyUrl;
    ArrayList<String> classFileUrls;
    EditText nameInput, ragamInput, talamInput, typeInput, composerInput, yearInput, notesInput;
    Button submitSongDataButton, addClassFilesButton, addMasterCopyButton;
    private static final int AUDIO_REQUEST = 2;
    boolean toggleFileCategory;    // false is class files, true is master copy

    Uri audioUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        binding = ActivityAddSongBinding.inflate(getLayoutInflater());

        nameInput = findViewById(R.id.name_input);
        ragamInput = findViewById(R.id.ragam_input);
        talamInput = findViewById(R.id.talam_input);
        typeInput = findViewById(R.id.type_input);
        composerInput = findViewById(R.id.composer_input);
        yearInput = findViewById(R.id.year_input);
        notesInput = findViewById(R.id.notes_input);
        submitSongDataButton = findViewById(R.id.submit_song_data_button);
        addClassFilesButton = findViewById(R.id.add_class_files_button);
        addMasterCopyButton = findViewById(R.id.add_master_copy_button);

        classFileUrls = new ArrayList<String>();

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                name = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ragamInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ragam = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        talamInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                talam = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        typeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                type = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        composerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                composer = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        yearInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                years = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        notesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                notes = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        addClassFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFileCategory = false;
                openAudioFile();
            }
        });

        addMasterCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFileCategory = true;
                openAudioFile();
            }
        });

        submitSongDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
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

                db.collection("songs").document(name+ " - " +ragam).set(songInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            Toast.makeText(AddSongActivity.this, "Song Added!", Toast.LENGTH_SHORT).show();

                    }
                });

                finish();

            }
        });

    }

    private void openAudioFile() {
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
            uploadAudioFile();
        }
    }

    private void uploadAudioFile() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (audioUri != null){
            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("master-copies").child(System.currentTimeMillis() +"."+ getFileExtension(audioUri));
            fileRef.putFile(audioUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            System.out.println(url);

                            if (toggleFileCategory){
                                masterCopyUrl = url;
                            }
                            else{
                                classFileUrls.add(url);
                            }

                            pd.dismiss();
                            Toast.makeText(AddSongActivity.this, "File Uploaded!", Toast.LENGTH_SHORT).show();
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
}