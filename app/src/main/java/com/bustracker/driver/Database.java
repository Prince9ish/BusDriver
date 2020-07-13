// --------------------------------------------------------------
package com.bustracker.driver;
// --------------------------------------------------------------

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// --------------------------------------------------------------
public class Database {
    // ----------------------------------------------------------
    // Variables
    FirebaseFirestore Data;
    private String Email;
    private String Password;
    private String Bus;
    private int TopSpeed;
    // ----------------------------------------------------------

    // ----------------------------------------------------------
    public Database() {
        Data = FirebaseFirestore.getInstance();
    }
    // ----------------------------------------------------------
    public Database(String busID) {
        Data = FirebaseFirestore.getInstance();
        Bus = busID;
        TopSpeed = 0;
    }
    // ----------------------------------------------------------

    public void LogSpeed(Location L){

        double Speed = (L.getSpeed())*3.6;
        double Latitude = L.getLatitude();
        double Longitude = L.getLongitude();

        String Timestamp = GetTimestamp();
        String Date = GetDate();

        GeoPoint Point = new GeoPoint(Latitude, Longitude);

        Map<String, Object> data = new HashMap<>();
        data.put("Speed", Speed);
        data.put("Location", Point);
        data.put("Timestamp", Timestamp);

        Map<String, Object> data1 = new HashMap<>();

        DocumentReference Ref = Data.collection("bus").document(Bus).collection("SpeedWarning").document(Date);
        Ref.set(data1,SetOptions.merge());

        Ref.update("SpeedWarningList", FieldValue.arrayUnion(data));

    }
    // ----------------------------------------------------------
    public void UpdateLocation(Location L){

        double Speed = (L.getSpeed())*3.6;
        double Latitude = L.getLatitude();
        double Longitude = L.getLongitude();

        GeoPoint Point = new GeoPoint(Latitude, Longitude);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("speed", Speed);
        data1.put("location",Point);
        int s = (int) Speed;

        if(s > TopSpeed){
            data1.put("MaxSpeed", Speed);
            TopSpeed = s;
        }

        Data.collection("bus").document(Bus).set(data1,SetOptions.merge());

    }
    // ----------------------------------------------------------
    private String GetTimestamp(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String result  = dateFormat.format(new Date());

        return result;
    }

    public String GetDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String result  = dateFormat.format(new Date());

        return result;
    }
    // ----------------------------------------------------------
    public void UpdateStudent(Student S, Events E,GeoPoint location){
        String ID = String.valueOf(S.getID());

        String Timestamp = GetTimestamp();
        String Date = GetDate();
        String event = E.name();
        Boolean arrived = false;

        Map<String, Object> data = new HashMap<>();
        data.put("TIME",Timestamp);
        data.put("LOCATION", location);
        data.put("NOTIFIED", false);

        Map<String, Object> data1 = new HashMap<>();
        data1.put(event, data);

        Data.collection("student").document(ID).collection("Event").document(Date).set(data1, SetOptions.merge());
    }


    public void UpdateStudent(Student S, Events E){
        String ID = String.valueOf(S.getID());

        String Timestamp = GetTimestamp();
        String Date = GetDate();
        String event = E.name();

        Map<String, Object> data = new HashMap<>();
        data.put("TIME",Timestamp);

        Map<String, Object> data1 = new HashMap<>();
        data1.put(event, data);


        Data.collection("student").document(ID).collection("Event").document(Date).set(data1, SetOptions.merge());
    }

    public void UpdateBus(Run R){
        String event = R.name();

        Map<String, Object> data = new HashMap<>();
        data.put("Run",R.name());

        Data.collection("bus").document(Bus).set(data, SetOptions.merge());
    }


    public void updateStatus(Student S, Events E){
        String ID = String.valueOf(S.getID());

        Map<String, Object> data1 = new HashMap<>();
        data1.put("status", E.toString());

        DocumentReference Ref =Data.collection("student").document(ID);

        Ref.update(data1);

    }
    // ----------------------------------------------------------
    public void ReadData(final Query Ref, Run run, final FirestoreCallBack CallBack){

        final List<Student> Temp = new ArrayList<>();

        String Date = GetDate();
        String R = run.toString();

        final String search = Date+","+R;

        Ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String FirstName = document.getString("firstName");
                        String LastName  = document.getString("lastName");


                        int ID = Integer.parseInt(document.getString("ID"));

                        List<String> list = (List<String>)document.get("Absent");

                        if(!list.contains(search)){
                            Student s = new Student(ID, FirstName, LastName);

                            if(document.getGeoPoint("GeoPoint") != null) {
                                GeoPoint Home = (GeoPoint) document.getGeoPoint("GeoPoint");
                                s = new Student(ID, FirstName, LastName,Home);
                            }

                            Temp.add(s);
                        }

                    }
                     CallBack.Callback(Temp);
                }
            }
        });

    }
    // -----------------------------------------------------------------------------------
    public void ReadBus(final Query Ref, final FSCallBack CallBack){

        final List<String> Temp = new ArrayList<>();

        Ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Temp.add(document.getId());
                        //Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                }
                CallBack.Callback(Temp);
            }
        });

    }
    // -----------------------------------------------------------------------------------


}

