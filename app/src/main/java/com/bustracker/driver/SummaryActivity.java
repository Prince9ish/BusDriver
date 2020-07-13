// ----------------------------------------------------------------------------------------
package com.bustracker.driver;
// ----------------------------------------------------------------------------------------

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// ----------------------------------------------------------------------------------------
public class SummaryActivity extends FragmentActivity implements OnMapReadyCallback {
    // ------------------------------------------------------------------------------------
    // Variable
    private GoogleMap Map;
    private ArrayList<LatLng> Points;
    private TextView distance;
    private ArrayList<Student> StudentsList;
    private ArrayList<GeoPoint> studentLocation;
    private Boolean OnBoard;

    private MediaPlayer Player;


    private Run run;

    // ------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        Points = (ArrayList<LatLng>) args.getSerializable("ARRAYLIST");

        Bundle args2 = intent.getBundleExtra("STUDENT");
        StudentsList = (ArrayList<Student>) args2.getSerializable("ARRAYLIST");

        run = Run.valueOf(intent.getStringExtra("Run"));

        OnBoard = false;
        findViewById(R.id.Warning).setVisibility(View.INVISIBLE);

        studentLocation = new ArrayList<>();

        distance = findViewById(R.id.distance);

        Player = MediaPlayer.create(this, R.raw.mercedes_benz_chime);
        Player.setLooping(true);
        Player.setVolume(0,0);
        if(!Player.isPlaying()) {
            Player.start();
        }
    }

    // ------------------------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;

        DrawLine();

        LatLng start = Points.get(0);
        LatLng end = Points.get(Points.size() - 1);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start);
        builder.include(end);
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);

        Map.animateCamera(cu);
    }

    // ------------------------------------------------------------------------------------
    private void DrawLine() {

        Map.clear();  //clears all Markers and Polylines

        Marker CurrentMarker;

        PolylineOptions options = new PolylineOptions().width(8).color(Color.rgb(91, 201, 212)).geodesic(true);

        for (int i = 0; i < Points.size(); i++) {
            LatLng point = Points.get(i);
            //options = new PolylineOptions().width(8).color(Po.get(i).getColour()).geodesic(true);

            if (i == 0) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                markerOptions.title("Start Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(185));
                CurrentMarker = Map.addMarker(markerOptions);
            }

            if (i == Points.size() - 1) {
                BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.raw.finish);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 70, 70, false);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                markerOptions.title("Finish Position");
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                CurrentMarker = Map.addMarker(markerOptions);
            }

            options.add(point);
        }
        Polyline line = Map.addPolyline(options); //add Polyline
        distance.setText("DISTANCE : "+CalculateDistance()+" KILOMETERS");
        GetLocation();
    }
    // ------------------------------------------------------------------------------------
    private double CalculateDistance(){

        LatLng LastPoint = Points.get(0);
        float Distance = 0;

        for (int i = 1; i < Points.size(); i++) {
            LatLng point = Points.get(i);
            float[] dist = new float[3];
            Location.distanceBetween(LastPoint.latitude, LastPoint.longitude, point.latitude, point.longitude, dist);
            LastPoint = point;
            Distance += dist[0];
        }

        Distance = Distance/1000;

        double roundOff = Math.round(Distance * 100.0) / 100.0;

        return roundOff;
    }

    private void GetLocation(){
        for(Student student : StudentsList){
            GetStudents(String.valueOf(student.getID()),student.getFullName());
            CheckOnboardStudents(String.valueOf(student.getID()));
        }
    }

    private void CreateMarker(GeoPoint geopoint, String Name){

        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.raw.pick);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 90, 90, false);


        LatLng point = new LatLng(geopoint.getLatitude(),geopoint.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.title(Name);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        Map.addMarker(markerOptions).showInfoWindow();
    }

    public void CheckOnboardStudents(String ID) {

        Database database = new Database();
        FirebaseFirestore Data = FirebaseFirestore.getInstance();

        DocumentReference Ref = Data.collection("student").document(ID);

        Read(Ref,new FirestoreCallBack() {
            @Override
            public void Callback(List<Student> List) {

            }

            @Override
            public void Callback(Student S) {
                if(S != null) {

                }
            }

            public void Callback(GeoPoint M) {
            }

            public void Callback(Boolean M) {
                if(M) {
                    OnBoard = true;
                    Warning();
                }
            }

        });
    }

    public void Warning(){
        Player.setVolume(1.0f, 1.0f);

        findViewById(R.id.Warning).setVisibility(View.VISIBLE);
    }



    public void GetStudents(String ID, final String Name) {

        Database database = new Database();
        FirebaseFirestore Data = FirebaseFirestore.getInstance();

        String Date = database.GetDate();
        String Event = StatusManager.locationState(run).name();

        DocumentReference Ref = Data.collection("student").document(ID).collection("Event").document(Date);

        ReadData(Ref,new FirestoreCallBack() {
            @Override
            public void Callback(List<Student> List) {

            }

            @Override
            public void Callback(Student S) {
                if(S != null) {

                }
            }

            public void Callback(GeoPoint M) {
                if(M != null) {
                    CreateMarker(M,Name);
                }
            }

            @Override
            public void Callback(Boolean aBoolean) {

            }
        });
    }

    // --------------------------------------------------------------------------------
    private void ReadData(final DocumentReference Ref, final FirestoreCallBack CallBack){
        final GeoPoint[] Temp = new GeoPoint[1];
        Ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String Event = StatusManager.locationState(run).name();
                        Map<String,Object> t = document.getData();

                        Map<String,Object> te = (java.util.Map<String, Object>) t.get(Event);

                        if(te != null) {
                            GeoPoint temp = (GeoPoint) te.get("LOCATION");
                            Temp[0] = temp;
                            CallBack.Callback(Temp[0]);
                        }

                    }
                }
            }
            });
    }


    private void Read(final DocumentReference Ref, final FirestoreCallBack CallBack){
        final Boolean[] Temp = new Boolean[1];
        Ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String current = (String) document.get("status");
                        Events Event = Events.valueOf(current);

                        Temp[0] = StatusManager.OnBoard(Event);

                        CallBack.Callback(Temp[0]);

                    }
                }
            }
        });
    }


    private Run GetRun(int id){
        Run E = Run.MORNING;
        if(id == 1){
            E = Run.MORNING;
        }else if(id == 2){
            E = Run.AFTERNOON;
        }
        return E;
    }

}
// ----------------------------------------------------------------------------------------