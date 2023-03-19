package com.example.finalprojecttake2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditSongInfo extends AppCompatActivity {

    EditText editText;
    Button updateButton;

    String documentName, field, updatedText;
    FirebaseFirestore db;

    String name, ragam, talam, type, composer, years, notes, masterCopyUrl;
    ArrayList<String> classFileUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_song_info);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        getWindow().setLayout((int)(dm.widthPixels*.6), (int)(dm.heightPixels*.2));

        editText = findViewById(R.id.edit_song_value);
        updateButton = findViewById(R.id.update_button);

        documentName = getIntent().getStringExtra("document name");
        field = getIntent().getStringExtra("field");
        db = FirebaseFirestore.getInstance();

        if (field.equals("name") || field.equals("ragam")){
            setData(documentName);
        }

        editText.setHint("Enter " + field.substring(0,1).toUpperCase() + field.substring(1));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updatedText = charSequence.toString().trim();
                if (field.equals("name"))
                    name = updatedText;
                else if (field.equals("ragam"))
                    ragam = updatedText;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (field.equals("name") || field.equals("ragam")){
                    db.collection("songs").document(documentName).delete();
                    createNewDocument();
                }
                else{
                    db.collection("songs").document(documentName).update(field, updatedText).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                Toast.makeText(EditSongInfo.this, "Update Successful", Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(EditSongInfo.this, "Update Failed", Toast.LENGTH_SHORT).show();
                                System.out.println("UPDATE FAILED => " + task.getException());
                            }
                        }
                    });
                }

                sendDataBack();
            }
        });




    }

    private void createNewDocument() {
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
                    Toast.makeText(EditSongInfo.this, "Song Updated!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void setData(String documentName) {
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

                } else {
                    Toast.makeText(EditSongInfo.this, "Data Save Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendDataBack(){
        Intent intent = new Intent();
        intent.putExtra("name", name);
        intent.putExtra("ragam", ragam);
        setResult(RESULT_OK, intent);
        finish();
    }

}