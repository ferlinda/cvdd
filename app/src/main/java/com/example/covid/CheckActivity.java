package com.example.covid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CheckActivity extends AppCompatActivity {

    private TextView questionText;
    private String profileName;
    private String levelResult;
    private Date currentTime;

    private int tapCount = 0;
    public final ArrayList<Integer> point = new ArrayList<Integer>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private DocumentReference questionRef = db.collection("question").document("q_one");
    private DocumentReference profile = db.collection("UsersData").document(userID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //transparent statusBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //hide actionBar
        try
        {
            Objects.requireNonNull(this.getSupportActionBar()).hide();
        }
        catch (NullPointerException ignored){}

        setContentView(R.layout.activity_check);
        questionText = findViewById(R.id.question);

        currentTime = Calendar.getInstance().getTime();
        Button buttonCount = findViewById(R.id.button_yes);
        Button buttonNotCount = findViewById(R.id.button_no);


        buttonCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tapCount = tapCount+1;
                point.add(tapCount);

                if (tapCount >= 7){
                    if((point.get(0) == 1) && (point.get(1) == 2) && (point.get(2) == 3)){
                        levelResult = "tinggi";
                    }else{
                        levelResult = "rendah";
                    }
                    saveResult();
                    goToMainActivity();
                }
                profile.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()){
                                    profileName = documentSnapshot.getString("Name");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });

                questionRef.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()){
                                    String questionString = documentSnapshot.getString(Integer.toString(tapCount));
                                    questionText.setText(questionString);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CheckActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        buttonNotCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tapCount = tapCount+1;
                point.add(tapCount);
                if (tapCount >= 7){
                    if((point.get(0) == 1) && (point.get(1) == 2) && (point.get(2) == 3)){
                        levelResult = "tinggi";
                    }else{
                        levelResult = "rendah";
                    }
                    loadProfile();
                    saveResult();
                    goToMainActivity();
                }
                profile.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()){
                                    profileName = documentSnapshot.getString("1");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });

                questionRef.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()){
                                    String questionString = documentSnapshot.getString(Integer.toString(tapCount));
                                    questionText.setText(questionString);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CheckActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

    }

    public void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void loadProfile(){

    }

    public void saveResult(){

        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("level", levelResult);
        finalResult.put("lastCheck", currentTime);

        db.collection("result").document(profileName).set(finalResult)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(CheckActivity.this, profileName, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CheckActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}
