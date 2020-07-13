package com.bustracker.driver;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public interface FirestoreCallBack {
    void Callback(List<Student> List);
    void Callback(Student S);
    void Callback(GeoPoint geoPoint);
    void Callback(Boolean aBoolean);
}
