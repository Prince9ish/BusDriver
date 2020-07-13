// -------------------------------------------------------------------------------------------------
package com.bustracker.driver;
// -------------------------------------------------------------------------------------------------

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

// -------------------------------------------------------------------------------------------------
public class StartActivity extends AppCompatActivity {
    // ---------------------------------------------------------------------------------------------
    // Variable
    private int run;
    private Button startButton;

    Spinner spin;

    private ArrayList<String> Bus;
    private String[] list;

    private Button Morning;
    private Button Afternoon;

    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        startButton = findViewById(R.id.start);

        Bus = new ArrayList<>();
        spin = (Spinner) findViewById(R.id.spinner);


        Morning = findViewById(R.id.moring);
        Afternoon = findViewById(R.id.afternoon);

        Morning.setText("Morning \n SELCETED");

        run = 1;

        GetBuses();

        getSupportActionBar().hide();

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Confirm();
            }
        });

        Morning.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                clearRun();
                Morning.setText("Morning \n SELCETED");
                run = 1;
            }

        });

        Afternoon.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                clearRun();
                Afternoon.setText("Afternoon \n SELCETED");
                run = 2;
            }

        });

    }

    private void GetBuses(){
        Database database = new Database();
        FirebaseFirestore Data = FirebaseFirestore.getInstance();

        Query Ref = Data.collection("bus");

        database.ReadBus(Ref, new FSCallBack() {
            @Override
            public void Callback(List<String> List) {
                Bus.addAll(List);
                AddBus();
            }
        });

    }

    private void AddBus(){
        list = Bus.toArray(new String[0]);
        ArrayAdapter aa = new ArrayAdapter(this,R.layout.spinner,list);
        spin.setAdapter(aa);
    }
    // ---------------------------------------------------------------------------------------------
    private void Confirm(){

        String BusID = "B001";
        BusID = spin.getSelectedItem().toString();

        Intent intent = new Intent(getBaseContext(), MapsActivity.class);

        intent.putExtra("bus", (BusID));
        intent.putExtra("run", (""+run));

        startActivity(intent);
    }
    // ---------------------------------------------------------------------------------------------

    private void clearRun(){
        Morning.setText("Morning");
        Afternoon.setText("Afternoon");
    }

}
// -------------------------------------------------------------------------------------------------
