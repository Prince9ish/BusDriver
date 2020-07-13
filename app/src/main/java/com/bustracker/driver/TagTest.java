// -----------------------------------------------------------------------------------
package com.bustracker.driver;
// -----------------------------------------------------------------------------------
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
// -----------------------------------------------------------------------------------
public class TagTest extends AppCompatActivity {
    // --------------------------------------------------------------------------------
    // Variables
    private TextView text;
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;

    private MediaPlayer Success;
    private MediaPlayer Fail;

    private ImageView Image;

    private Student CurrentStudent;
    private Database database;
    private Events events;

    private GeoPoint location;
    // --------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_test);

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        database = new Database();

        Intent intent = getIntent();

        double Lat= Double.parseDouble(intent.getStringExtra("Lat"));
        double Long = Double.parseDouble(intent.getStringExtra("Long"));

        location = new GeoPoint(Lat, Long);

        getSupportActionBar().hide();

        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();



        Success = MediaPlayer.create(this, R.raw.payment_success);
        Fail = MediaPlayer.create(this, R.raw.payment_failure);

        Button OverallButton = findViewById(R.id.button);
        Button BackButton = findViewById(R.id.button3);
        Button Confirm = findViewById(R.id.button2);

        text = findViewById(R.id.edit);
        text1 = findViewById(R.id.textView5);
        text2 = findViewById(R.id.textView6);
        text3 = findViewById(R.id.textView7);

        Image = findViewById(R.id.imageView);
        Image.setImageResource(R.raw.image);

        final EditText edit = (EditText)findViewById(R.id.tag);
        edit.setInputType(InputType.TYPE_NULL);


        OverallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text.setText("Please Tap Tag On Reader");
                EditText t = (EditText) findViewById(R.id.tag);
                t.setText("", TextView.BufferType.EDITABLE);
                Image.setImageResource(R.raw.image);
                text1.setText("ID      :  ");
                text2.setText("Name    :  ");
                text3.setText("Current --> Next");
            }
        });

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Confirm();
            }
        });

        edit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    GetStudents(edit.getText().toString());

                    return true;
                }
                return false;
            }
        });

    }


    private void back(){
        super.onBackPressed();
    }

    private void Confirm(){

        database.UpdateStudent(CurrentStudent, events,location);
        database.updateStatus(CurrentStudent, StatusManager.nextState(events));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lat", String.valueOf(location.getLatitude()));
        editor.putString("long", String.valueOf(location.getLongitude()));

        editor.commit();

        super.onBackPressed();
    }

    // --------------------------------------------------------------------------------
    public void GetStudents(String ID) {

        FirebaseFirestore Data = FirebaseFirestore.getInstance();
        Query Ref = Data.collection("student").whereEqualTo("tagID", ID);

        ReadData(Ref,new FirestoreCallBack() {
            @Override
            public void Callback(List<Student> List) {

            }

            @Override
            public void Callback(Student S) {
                if(S != null) {
                    text.setText("READ SUCESSFULL");
                    text1.setText("ID        :  " + S.getID());
                    text2.setText("Name    :  " + S.getFirstName() +" " + S.getLastName());
                    text3.setText("" + StatusManager.GetStringStatus(events));

                    if(S.getID()==4){
                        Image.setImageResource(R.raw.crowley);
                    }else if(S.getID() == 3){
                        Image.setImageResource(R.raw.wonder);
                    }else if (S.getID() == 2){
                        Image.setImageResource(R.raw.img);
                    }

                    Success.start();
                    CurrentStudent = S;

                }else{
                    text.setText("This Tag Not Assign To Any Students Yet");
                    text.setTextColor(Color.RED);
                    Fail.start();
                }
            }

            @Override
            public void Callback(GeoPoint geoPoint) {

            }

            @Override
            public void Callback(Boolean aBoolean) {

            }
        });
    }
    // --------------------------------------------------------------------------------
    private void ReadData(final Query Ref, final FirestoreCallBack CallBack){
        final Student[] Temp = new Student[1];
        Ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String FirstName = document.getString("firstName");
                        String LastName = document.getString("lastName");
                        int ID = Integer.parseInt(document.getString("ID"));
                        events = Events.valueOf(document.getString("status"));

                        Temp[0] = new Student(ID, FirstName, LastName);

                        CallBack.Callback(Temp[0]);
                    }
                }
            }
        });
    }
    // --------------------------------------------------------------------------------


}
// ----------------------------------------------------------------------------------------