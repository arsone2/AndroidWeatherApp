package com.example.arsone.weather;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.arsone.weather.data.Weather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


// https://www.javacodegeeks.com/2012/09/android-alarmmanager-tutorial.html

/*
We are going to define the BroadcastReciever which handles the intent registered with AlarmManager.
In the given class onReceive() method has been defined.
This method gets invoked as soon as intent is received.
Once we receive the intent we try to get the extra parameter associated with this intent.
This extra parameter is user-defined i.e ONE_TIME, basically indicates
whether this intent was associated with one-time timer or the repeating one.
Once the ONE_TIME parameter value has been extracted, Toast message is displayed accordingly.
Helper methods have also been defined, which can be used from other places with the help of objects
i.e setAlarm(), cancelAlarm() and onetimeTimer() methods.
These methods can also be defined somewhere else to do operation on the timer i.e set,
cancel, etc. To keep this tutorial simple, we have defined it in BroadcastReceiver.

        setAlarm(): This method sets the repeating alarm by use of setRepeating() method.
        setRepeating() method needs four arguments:

        type of alarm,
        trigger time: set it to the current time
        interval in milliseconds: in this example we are passing 5 seconds ( 1000 * 5 milliseconds)
        pending intent: It will get registered with this alarm.
        When the alarm gets triggered the pendingIntent will be broadcasted.

        cancelAlarm(): This method cancels the previously registered alarm by calling cancel() method.
         cancel() method takes pendingIntent as an argument.
         The pendingIntent should be matching one, only then the cancel() method
         can remove the alarm from the system.

        onetimeTimer(): This method creates an one-time alarm. This can be achieved by calling set() method.
        set() method takes three arguments:

        1.type of alarm
        2.trigger time
        3.pending intent
*/


public class CustomBroadcastReceiver extends BroadcastReceiver {

//    final public static String ONE_TIME = "onetime";

    // public final static String PARAM_TIME = "time";
    public final static String PARAM_TASK = "task";
    ///    public final static String PARAM_RESULT = "result";
    public final static String PARAM_STATUS = "status";
    public final static String PARAM_CITY_ID = "city_id";
    public final static String PARAM_ENTERED_CITY = "city_name";


    public final static int TASK_GET_WEATHER_ONE_CITY = 1;
    public final static int TASK_GET_WEATHER_ALL_CITIES = 2;
    // final int TASK3_CODE = 3;


    public final static int STATUS_GET_WEATHER_ONE_CITY_START = 101;
    public final static int STATUS_GET_WEATHER_ONE_CITY_FINISH = 102;

    public final static int STATUS_GET_WEATHER_ALL_CITIES_START = 103;
    public final static int STATUS_GET_WEATHER_ALL_CITIES_FINISH = 104;


    public static final int REQUEST_CODE = 12345;

    //   public static final String APP_ACTION = "com.codepath.example.servicesdemo.alarm";

    private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    //   private Context mContext;


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("AAAAA", "CustomBroadcastReceiver: onReceive()");


        int task = intent.getIntExtra(PARAM_TASK, 0);
        int status = intent.getIntExtra(PARAM_STATUS, 0);

        Log.d("AAAAA", "MainActivity: onReceive: task = " + task + ", status = " + status);

        // Ловим сообщения о старте задач
        //   if (status == STATUS_START) {

        switch (status) {

            case STATUS_GET_WEATHER_ONE_CITY_START:

                Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ONE_CITY_START");

                ///    showMessageBar(getString(R.string.message_wait_for_data), true);

                break;

            case STATUS_GET_WEATHER_ONE_CITY_FINISH:

                Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ONE_CITY_FINISH");

                //  refreshCityListView();
                ///     reInitCityListViewLoader();

                ///     hideMessageBar();

                break;

            case STATUS_GET_WEATHER_ALL_CITIES_START:

                Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ALL_CITIES_START");

                ///     showMessageBar(getString(R.string.message_wait_for_data), true);

                break;

            case STATUS_GET_WEATHER_ALL_CITIES_FINISH:

                Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ALL_CITIES_FINISH");

                // refreshCityListView();
                // reInitCityListViewLoader();
                ///    reInitCityListViewLoader();

                ///    hideMessageBar();

                break;
        }


        //      mContext = context;


        Intent i = new Intent(context, GetDataService.class);

        /// i.putExtra("foo", "bar");

        context.startService(i);

        /// getData();
    }
}


/*

        String action = intent.getAction();

        if(action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {

                // Code to handle BOOT COMPLETED EVENT
                // TO-DO: I can start an service.. display a notification... start an activity

                // start AlarmManager action periodically after device boot up

                AlarmManager alarmManager=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);

              ///  Intent intent = new Intent(mContext, CustomBroadcastReceiver.class);

                //     intent.putExtra(ONE_TIME, Boolean.FALSE);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

                //After 5 seconds
                /// alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5 , pi);
                /// alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 5 , pi);

                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
            }
        }
*/


/*

//Toast.makeText(context, "Обнаружено сообщение: " +
        //                intent.getStringExtra("ru.alexanderklimov.broadcast.Message"),
        //        Toast.LENGTH_LONG).show();

        if (intent.getAction().equalsIgnoreCase("android.intent.action.ACTION_POWER_DISCONNECTED")) {
            String message = "Обнаружено сообщение " + intent.getAction();

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
*/



/*
        mContext = context;
        String action = intent.getAction();
        if (action.equalsIgnoreCase(BOOT_ACTION)) {
            // здесь ваш код
            // например, запускаем уведомление
            Intent intent = new Intent(context, ru.alexanderklimov.NotifyService.NotifyService.class);
            context.startService(intent);
            // в общем виде
            //для Activity
            Intent activivtyIntent = new Intent(context, MyActivity.class);
            activivtyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activivtyIntent);

            //для Service
            Intent serviceIntent = new Intent(context, MyService.class);
            context.startService(serviceIntent);
        }
        */


  /*      PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");

        //Acquire the lock
        wl.acquire();

        //You can do the processing here.
        Bundle extras = intent.getExtras();

        StringBuilder msgStr = new StringBuilder();

        if(extras != null && extras.getBoolean(ONE_TIME, Boolean.FALSE)){

            //Make sure this intent has been sent by the one-time timer button.
            msgStr.append("One time Timer : ");
        }

        Format formatter = new SimpleDateFormat("hh:mm:ss a");

        msgStr.append(formatter.format(new Date()));

        Toast.makeText(context, msgStr, Toast.LENGTH_LONG).show();

        //Release the lock
        wl.release();*/


/*
        Intent i = new Intent(context, MyTestService.class);
        i.putExtra("foo", "bar");
        context.startService(i);*/
    //}


/*    public void SetAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, CustomBroadcastReceiver.class);

        intent.putExtra(ONE_TIME, Boolean.FALSE);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        //After 5 seconds
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5, pi);
    }


    public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, CustomBroadcastReceiver.class);

        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(sender);
    }*/


    /*        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);*/

  /*  public void setOnetimeTimer(Context context) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, CustomBroadcastReceiver.class);

        intent.putExtra(ONE_TIME, Boolean.TRUE);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
    }
*/


/*
    // ------------------------------------------------------------
    // https://developer.android.com/training/scheduling/alarms.html#type
    //  Wake up the device to fire the alarm in 30 minutes, and every 30 minutes after that:
    // Hopefully your alarm will have a lower frequency than this!

            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
            AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
// ------------------------------------------------------------
// Wake up the device to fire a one-time (non-repeating) alarm in one minute:

private AlarmManager alarmMgr;
private PendingIntent alarmIntent;
...

alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
Intent intent = new Intent(context, AlarmReceiver.class);
alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() +
        60 * 1000, alarmIntent);
// ------------------------------------------------------------



    */





//}