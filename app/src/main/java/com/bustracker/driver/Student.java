// ----------------------------------------------------
package com.bustracker.driver;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

// ----------------------------------------------------
public class Student implements Serializable, Comparable {
    // -----------------------------------------------
    // Variables
    private int ID;
    private String FirstName;
    private String LastName;

    private transient GeoPoint Home;
    private Events event;
    // -----------------------------------------------


    // -----------------------------------------------
    public Student(int ID, String firstName, String lastName) {
        this.ID = ID;
        FirstName = firstName;
        LastName = lastName;


    }

    public Student(int ID, String firstName, String lastName, GeoPoint Home) {
        this.ID = ID;
        FirstName = firstName;
        LastName = lastName;

        this.Home = Home;
    }
    // -----------------------------------------------

    private void updateStatus(){
        Database database = new Database();
        database.UpdateStudent(this, event);
    }

    // -----------------------------------------------
    // Getter
    // -----------------------------------
    public int getID() {
        return ID;
    }
    // -----------------------------------
    public String getFirstName() {
        return FirstName;
    }

    public String getFullName() {
        return FirstName + " " + LastName;
    }
    // -----------------------------------
    public String getLastName() {
        return LastName;
    }
    // -----------------------------------------------

    @Override
    public String toString() {
        return "ID : " + ID +
                "\nName : '" + FirstName + " " + LastName +
                "\nStatus : " + event;
    }

    public LatLng getHome() {
        LatLng H = null;
        if(Home !=null) {
            H = new LatLng(Home.getLatitude(), Home.getLongitude());
        }
        return H;
    }

    @Override
    public int compareTo(Object o) {
        Student S1 = this;
        Student S2 = (Student) o;

        String F1 = S1.getFirstName();
        String F2 = S2.getFirstName();

        return F1.compareTo(F2);
    }
}
// ----------------------------------------------------


