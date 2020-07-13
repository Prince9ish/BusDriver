package com.bustracker.driver;

public class StatusManager {

    public static String GetStrStatus(Events event) {
        String Result = "";
        if(event == Events.LEAVEHOME){
            Result = "Home Pick Up";
        }else if(event == Events.ARRIVESCHOOL){
            Result = "On Board";
        }else if(event == Events.DONE){
            Result = "Arrived";
        }
        else if(event == Events.LEAVESCHOOL){
            Result = "Leaving School";
        }else if(event == Events.ARRIVEHOME) {
            Result = "On Board";
        }
        return Result;
    }

    public static String GetStringStatus(Events event) {
        String Result = "";
        if(event == Events.LEAVEHOME){
            Result = "Home Pick Up" + " --> " +GetStrStatus(nextState(event));
        }else if(event == Events.ARRIVESCHOOL){
            Result = "On Board"  +" --> " +GetStrStatus(nextState(event));
        }else if(event == Events.DONE){
            Result = "Arrived";
        }
        else if(event == Events.LEAVESCHOOL){
            Result = "Leaving School";
        }else if(event == Events.ARRIVEHOME) {
            Result = "On Board";
        }
        return Result;
    }



    public static Events firstState(Run R){

        if(R.equals(Run.MORNING)){
            return Events.LEAVEHOME;
        }else if (R.equals(Run.AFTERNOON)){
            return Events.LEAVESCHOOL;
        }

        return Events.LEAVEHOME;
    }

    public static Events locationState(Run R){

        if(R.equals(Run.MORNING)){
            return Events.LEAVEHOME;
        }else if (R.equals(Run.AFTERNOON)){
            return Events.ARRIVEHOME;
        }

        return Events.LEAVEHOME;
    }

    public static void nextState(Run R,Events event){
        if(R.equals(Run.MORNING)){
            morningState(event);
        }else if (R.equals(Run.AFTERNOON)){
            afternoonState(event);
        }
    }

    public static Events nextState(Events event){
        if(event.equals(Events.DONE)){
            return Events.DONE;
        } else if(event.equals(Events.LEAVEHOME) || event.equals(Events.ARRIVESCHOOL)){
            return morningState(event);
        }else if (event.equals(Events.LEAVESCHOOL) || event.equals(Events.ARRIVEHOME)){
            return afternoonState(event);
        }
        return morningState(event);
    }

    private static void getRun(){

    }

    public static Boolean OnBoard(Events event){
        if(!event.equals(Events.DONE)){
            return true;
        }
        return false;
    }

    private static Events morningState(Events event){
        if(event.equals(Events.LEAVEHOME)){
           return Events.ARRIVESCHOOL;
        } else if(event.equals(Events.ARRIVESCHOOL)){
            return Events.DONE;
        }
        return Events.ARRIVESCHOOL;
    }

    private static Events afternoonState(Events event){
        if(event.equals(Events.LEAVESCHOOL)){
            return Events.ARRIVEHOME;
        }else if(event.equals(Events.ARRIVEHOME)){
            return Events.DONE;
        }
        return Events.ARRIVEHOME;
    }

}
