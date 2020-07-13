// -----------------------------------------------------------------------------------
package com.bustracker.driver;
// -----------------------------------------------------------------------------------
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// -----------------------------------------------------------------------------------
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 100;
    // -------------------------------------------------------------------------------
    // Variables
    private GoogleMap Map;
    private FusedLocationProviderClient FusedClient;
    private SupportMapFragment MapFrag;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private LatLng startLocation;
    private Marker CurrentMarker;
    private Database database;
    private ArrayList<LatLng> Points;
    private ArrayList<LatLng> Home;
    private List<Student> StudentsList;

    private Polyline polyline_path;

    private Button OverallButton;
    private Button FollowButton;
    private Button StudentButton;
    private String MapZoom;

    private MediaPlayer Player;
    private Boolean Warn;

    private String BusID;
    private Run run;
    // -------------------------------------------------------------------------------



    // -------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Intent intent = getIntent();

        BusID = intent.getStringExtra("bus");
        run = GetRun(Integer.parseInt(intent.getStringExtra("run")));

        StudentsList = new ArrayList<>();
        database = new Database(BusID);
        Points = new ArrayList<LatLng>();
        Home = new ArrayList<LatLng>();

        GetStudents();

        TextView Bus = findViewById(R.id.busNum);
        TextView r = findViewById(R.id.run);

        Bus.setText(""+BusID);
        r.setText(run.toString() + "");

        database.UpdateBus(run);

        OverallButton = (Button) findViewById(R.id.overview);
        FollowButton = (Button) findViewById(R.id.follow);
        StudentButton = (Button) findViewById(R.id.button4);
        Button FinishButton = (Button) findViewById(R.id.finish);

        FollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapZoom = "Follow";
                Map.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
        });

        OverallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapZoom = "Over";
                Map.animateCamera(CameraUpdateFactory.zoomTo(14));
            }
        });

        StudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReadTag();
            }
        });

        MapZoom = "Follow";

        startLocation = null;
        Warn = false;

        FinishButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Finish();
            }});

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FusedClient = LocationServices.getFusedLocationProviderClient(this);
        MapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFrag.getMapAsync(this);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            finish();
        }

        Player = MediaPlayer.create(this, R.raw.bmw_new);
        Player.setLooping(true);
        Player.setVolume(0,0);
        if(!Player.isPlaying()) {
            Player.start();
        }


        LinearLayout constraintLayout = findViewById(R.id.root_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }
    // -------------------------------------------------------------------------------


    // -------------------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Map.setBuildingsEnabled(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                FusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                Map.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();
            }
        } else {
            FusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            Map.setMyLocationEnabled(true);
        }

        PolylineOptions routes = new PolylineOptions().width(13).color(Color.argb(94 ,91, 201, 212));
        polyline_path = Map.addPolyline(routes);
    }
    // -------------------------------------------------------------------------------
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                database.UpdateLocation(location);

                updateDisplay(location);
                lastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                Points.add(latLng);
                UpdatePoints(latLng);

                if(MapZoom == "Follow"){
                    CameraPosition cameraPosition = new CameraPosition.Builder().
                            target(latLng).
                            tilt(65).
                            zoom(20).
                            bearing(location.getBearing()).
                            build();
                    Map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }else{
                    CameraPosition cameraPosition2 = new CameraPosition.Builder().
                            target(latLng).
                            tilt(90).
                            zoom(15).
                            bearing(0).
                            build();
                    Map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition2));
                }
            }

        }

    };
    // -------------------------------------------------------------------------------
    private void UpdatePoints(LatLng newlatlng) {
        List<LatLng> points = polyline_path.getPoints();
        points.add(newlatlng);
        polyline_path.setPoints(points);
    }


    private void drawHome(){
        BitmapDrawable bitmapdraw1 = (BitmapDrawable)getResources().getDrawable(R.raw.school);
        Bitmap bi = bitmapdraw1.getBitmap();
        Bitmap sMarker = Bitmap.createScaledBitmap(bi, 90, 90, false);
        LatLng school = new LatLng(13.9066585, 100.5010501);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(school);
        markerOptions.title("School");
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(sMarker));
        CurrentMarker = Map.addMarker(markerOptions);


        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.raw.browser);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 90, 90, false);

        for (int i = 0; i < Home.size(); i++) {
            LatLng point = Home.get(i);
            if(point != null) {

                startLocation = point;
                markerOptions = new MarkerOptions();
                markerOptions.position(point);
                markerOptions.title("");
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                CurrentMarker = Map.addMarker(markerOptions);
            }

        }

    }
    // -------------------------------------------------------------------------------
    private void updateDisplay(Location l){
        updateSpeed(l);
    }
    // -------------------------------------------------------------------------------
    private void updateSpeed(Location L){
        double Speed = (L.getSpeed())*3.6;
        TextView speedDisplay = findViewById(R.id.speed);

        int S = (int) Speed;
        int Limit = 120;

        speedDisplay.setText(""+ S + "");

        if(S > Limit){
            Player.setVolume(1.0f, 1.0f);
            speedDisplay.setTextColor(Color.RED);
            if(!Warn){
                database.LogSpeed(L);
                Warn = true;
            }
        }else if (S <= Limit){
            speedDisplay.setTextColor(Color.GREEN);
            Player.setVolume(0,0);
            Warn = false;
        }

    }
    // -------------------------------------------------------------------------------
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this).setTitle("Location Service Required").setMessage("The Driver Requires Location Serivces To Be Able To Transmit The School Bus Location").setPositiveButton("Transmit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION );
                    }
                }).create().show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }
    // -------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        FusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        Map.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    // -------------------------------------------------------------------------------
    private LinearLayout CreateStudent(Student student){

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Create a LayoutParams for TextView
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView ID = new TextView(getApplicationContext());
        TextView FirstName = new TextView(getApplicationContext());
        //TextView LastName = new TextView(getApplicationContext());

        ID.setBackgroundColor(Color.parseColor("#00ffffff"));
        FirstName.setBackgroundColor(Color.parseColor("#00ffffff"));

        // Apply the layout parameters to TextView widget
        ID.setLayoutParams(lp);
        FirstName.setLayoutParams(lp);

        // Set Text
        ID.setText("Student ID    :  " + student.getID());
        FirstName.setText(student.getFirstName() + " " + student.getLastName());

        // Format
        ID.setTextSize(16);
        FirstName.setTextSize(19);
        FirstName.setTypeface(null, Typeface.BOLD);

        FirstName.setPadding(10, 5, 0, 15);
        ID.setPadding(10, 0, 0, 10);

        // ----------------------------------

        layout.addView(FirstName);
        layout.addView(ID);
        layout.setWeightSum(7.0f);
        layout.setGravity(Gravity.LEFT);
        layout.setPadding(2, 2, 2, 2);
        layout.setBackgroundColor(Color.parseColor("#20ffffff"));

        return layout;
    }
    // -------------------------------------------------------------------------------
    private void AddStudent(){
        LinearLayout studentList = findViewById(R.id.studentList);

        Collections.sort(StudentsList);

        for(Student student : StudentsList){
            Home.add(student.getHome());
            LinearLayout studentLayout = CreateStudent(student);
            studentList.addView(studentLayout);
            studentList.addView(CreateLine());

            database.updateStatus(student, StatusManager.firstState(run));
        }
        drawHome();

    }
    // -------------------------------------------------------------------------------
    private View CreateLine(){

        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 3));
        v.setBackgroundColor(Color.parseColor("#5BC9D4"));
        v.setPadding(20, 30, 20, 35);

        return v;
    }
    // -------------------------------------------------------------------------------
    private void Finish(){
        Intent intent = new Intent(getBaseContext(), SummaryActivity.class);
        Bundle args = new Bundle();
        Bundle args2 = new Bundle();

        args.putSerializable("ARRAYLIST",(Serializable)Points);

        args2.putSerializable("ARRAYLIST",(Serializable)StudentsList);

        intent.putExtra("BUNDLE",args);
        intent.putExtra("STUDENT",args2);

        intent.putExtra("Run", run.name());
        Player.stop();

        startActivity(intent);
    }
    // -------------------------------------------------------------------------------
    public void GetStudents() {
        FirebaseFirestore Data = FirebaseFirestore.getInstance();


        Query Ref = Data.collection("student").whereEqualTo("busID",(BusID));

        database.ReadData(Ref,run,new FirestoreCallBack(){
            @Override
            public void Callback(List<Student> List) {
                StudentsList.addAll(List);
                AddStudent();
            }

            @Override
            public void Callback(Student S) {

            }

            @Override
            public void Callback(GeoPoint geoPoint) {

            }

            @Override
            public void Callback(Boolean aBoolean) {

            }
        });

    }
    // -------------------------------------------------------------------------------


    // -------------------------------------------------------------------------------
    private Run GetRun(int id){
        Run E = Run.MORNING;
        if(id == 1){
            E = Run.MORNING;
        }else if(id == 2){
            E = Run.AFTERNOON;
        }
        return E;
    }
    // -------------------------------------------------------------------------------
    private void ReadTag(){
        Location TempLocation = lastLocation;

        Intent in = new Intent(getBaseContext(), TagTest.class);

        in.putExtra("Lat", (""+TempLocation.getLatitude()));
        in.putExtra("Long", (""+TempLocation.getLongitude()));

        startActivity(in);
    }
    // -------------------------------------------------------------------------------

}
// -------------------------------------------------------------------------------------------------