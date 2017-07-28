package com.example.arsone.weather;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


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


public class StaticBroadcastReceiver extends BroadcastReceiver {

//    final public static String ONE_TIME = "onetime";

/*    // public final static String PARAM_TIME = "time";
    public final static String PARAM_TASK = "task";
    ///    public final static String PARAM_RESULT = "result";
    public final static String PARAM_STATUS = "status";
    public final static String PARAM_CITY_ID = "city_id";
    public final static String PARAM_ENTERED_CITY = "city_name";*/

    private static final String BROADCAST_STATIC_ACTION = "com.example.arsone.weather.static.broadcast";
    private static final String BROADCAST_DYNAMIC_ACTION = "com.example.arsone.weather.dynamic.broadcast";
    /// public final static String BROADCAST_ACTION = "com.example.arsone.weather.broadcast";

    public final static String PARAM_TASK = "task";
    ///    public final static String PARAM_RESULT = "result";
/*    public final static String PARAM_STATUS = "status";
    public final static String PARAM_CITY_ID = "city_id";
    public final static String PARAM_ENTERED_CITY = "city_name";
    public final static String PARAM_LANG_CODE = "language_index";
    public final static String PARAM_ACTION = "action";*/


    /*    public final static int TASK_GET_WEATHER_ONE_CITY = 1;
        public final static int TASK_GET_WEATHER_ALL_CITIES = 2;*/
    private final static int TASK_GET_DATA_PERIODICALLY = 3;


/*    public final static int STATUS_GET_WEATHER_ONE_CITY_START = 101;
    public final static int STATUS_GET_WEATHER_ONE_CITY_FINISH = 102;

    public final static int STATUS_GET_WEATHER_ALL_CITIES_START = 103;
    public final static int STATUS_GET_WEATHER_ALL_CITIES_FINISH = 104;

    public final static int STATUS_RUN_ALL_CITIES = 106;*/

    //  public static final int REQUEST_CODE = 12345;

    private final static int RQS_2 = 2;

    public static final int REQUEST_CODE = 12345;

    //   public static final String APP_ACTION = "com.codepath.example.servicesdemo.alarm";

    // private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    //  private Context mContext;


    // действия при получении сообщений
    @Override
    public void onReceive(Context context, Intent intent) {

        //  String action = intent.getAction();

/*        if (action == null)
            return;*/

        //   mContext = context;
        String action = null;


    //    Log.d("AAAAA", "StaticBroadcastReceiver: onReceive()");

        if ((intent != null) && ((action = intent.getAction()) != null)) {

            if (action.equalsIgnoreCase(BROADCAST_STATIC_ACTION)) {

                // -----------------------------------------------------------
                // get detailed data for ALL cities
                Intent i = new Intent(context, GetDataService.class)
                        .putExtra(PARAM_TASK, TASK_GET_DATA_PERIODICALLY); // get weather data for all cities
                //      .putExtra(PARAM_LANG_CODE, mMapLanguageIndex) // language index
                //    .setAction(BROADCAST_DYNAMIC_ACTION);

                // start service for added a city details and weather data
                context.startService(i);
                // -----------------------------------------------------------
            }

            else { // different boot actions: run service periodically

                Log.d("AAAAA", "StaticBroadcastReceiver: onReceive() - after BOOT actions");

                // Construct an intent that will execute the AlarmReceiver
                Intent alarmIntent = new Intent(context, GetDataService.class);

                // alarmIntent.setAction(BROADCAST_STATIC_ACTION);
                alarmIntent.putExtra(PARAM_TASK, TASK_GET_DATA_PERIODICALLY);

                // intent.setAction(BROADCAST_STATIC_ACTION);

                // Create a PendingIntent to be triggered when the alarm goes off
                final PendingIntent pIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                        alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Setup periodic alarm every every half hour from this point onwards
                long firstMillis = System.currentTimeMillis(); // alarm is set right away

                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
                // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
                alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);

            }

        }

 /*       // this.context = this;
        Intent alarm = new Intent(mContext, GetDataService.class);

        /// alarm.setAction(BROADCAST_DYNAMIC_ACTION);
        alarm.putExtra(PARAM_TASK, TASK_GET_DATA_PERIODICALLY);

      //  boolean alarmRunning = (PendingIntent.getBroadcast(mContext, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);

        // if it is already running, do nothing because we don’t want to register it multiple times
      //  if (!alarmRunning) {

            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, alarm, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    SystemClock.elapsedRealtime() + 1000,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent);*/
        //    }
    }
}



/*        if (action.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {

            Log.d("AAAAA", "StaticBroadcastReceiver: ACTION_BOOT_COMPLETED");

            setAlarmManager();*/

/*            // read settings from DB
            Cursor cursor = context.getContentResolver().query(DataContentProvider.SETTINGS_CONTENT_URI,
                    new String[]{DataContract.SettingsEntry.COLUMN_UNITS_FORMAT,
                            DataContract.SettingsEntry.COLUMN_SORT_CITIES,
                            DataContract.SettingsEntry.COLUMN_MAP_LANGUAGE},
                    null, // DataContract.CityEntry.COLUMN_ENTERED_CITY + "=?",
                    null, // new String[]{enteredCity},
                    null);*/
/*
            if (cursor != null && cursor.getCount() > 0) {

                cursor.moveToFirst();

                // 0 = English, 1 = Russian
                mMapLanguageIndex = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_MAP_LANGUAGE));

                cursor.close();
            }

            // Code to handle BOOT COMPLETED EVENT
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent runIntent = new Intent(context, GetDataService.class)
                    .putExtra(PARAM_TASK, TASK_GET_WEATHER_ALL_CITIES) // get weather data for all cities
                    // .putExtra(PARAM_STATUS, STATUS_RUN_ALL_CITIES)
                    .putExtra(PARAM_LANG_CODE, mMapLanguageIndex); // language index

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    RQS_1, runIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            // FLAG_UPDATE_CURRENT // FLAG_CANCEL_CURRENT

            // РќР° СЃР»СѓС‡Р°Р№, РµСЃР»Рё РјС‹ СЂР°РЅРµРµ Р·Р°РїСѓСЃРєР°Р»Рё Р°РєС‚РёРІРёС‚Рё, Р° РїРѕС‚РѕРј РїРѕРјРµРЅСЏР»Рё РІСЂРµРјСЏ,
            // РѕС‚РєР°Р¶РµРјСЃСЏ РѕС‚ СѓРІРµРґРѕРјР»РµРЅРёСЏ
            // alarmManager.cancel(pendingIntent);

            alarmManager.setRepeating(AlarmManager.RTC,
                    System.currentTimeMillis(),
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent);*/

//      }
/*
        else if (action.equalsIgnoreCase(Intent.QUICKBOOT_POWERON)) {

        }
*/
/*        else if (action.equalsIgnoreCase(BROADCAST_STATIC_ACTION)) {

            Log.d("AAAAA", "StaticBroadcastReceiver: BROADCAST_STATIC_ACTION");*/

 /*           PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");

            // lock
            wl.acquire();

            int status = intent.getIntExtra(PARAM_STATUS, 0);
            int mTask = intent.getIntExtra(PARAM_TASK, 0);
            int languageIndex = intent.getIntExtra(PARAM_LANG_CODE, 0);*/

//         setAlarmManager();

 /*           switch (status) {

                case STATUS_RUN_ALL_CITIES:

                    Intent service1 = new Intent(context, GetDataService.class)
                            .putExtra(PARAM_TASK, mTask) // get weather data for all cities
                            .putExtra(PARAM_LANG_CODE, languageIndex); // language index

                    // start service for added a city details and weather data
                    context.startService(service1);

                    break;

              case STATUS_GET_WEATHER_ONE_CITY_START:

                    Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ONE_CITY_START");


                   /// MainActivity.showMessageBar(context.getString(R.string.message_wait_for_data), true);

                    break;

                case STATUS_GET_WEATHER_ONE_CITY_FINISH:

                    Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ONE_CITY_FINISH");

                  //  refreshData();

                    // How to send data to a running activity from Broadcast Receiver!!
                    // https://stackoverflow.com/questions/8570823/how-to-send-data-to-a-running-activity-from-broadcast-receiver?rq=1

                    break;

                case STATUS_GET_WEATHER_ALL_CITIES_START:

                    Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ALL_CITIES_START");

                    ///       showMessageBar(getString(R.string.message_wait_for_data), true);

                    break;

                case STATUS_GET_WEATHER_ALL_CITIES_FINISH:

                    Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ALL_CITIES_FINISH");

///                    refreshData();

                    break;
            }*/
// unlock
//  wl.release();
//      }

/*

        if (action.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {

            Log.d("AAAAA", "CustomBroadcastReceiver: ACTION_BOOT_COMPLETED");

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"YOUR TAG");

        //Осуществляем блокировку
        wl.acquire();



      /// int task = intent.getIntExtra(PARAM_TASK, 0);
        int status = intent.getIntExtra(PARAM_STATUS, 0);

        ///     Log.d("AAAAA", "MainActivity: onReceive: task = " + task + ", status = " + status);

        int mTask = intent.getIntExtra(PARAM_TASK, 0);
        int languageIndex = intent.getIntExtra(PARAM_LANG_CODE, 0);




        switch (status) {

            case STATUS_RUN_ALL_CITIES:

                Intent service1 = new Intent(context, GetDataService.class)
                        .putExtra(PARAM_TASK, mTask) // get weather data for all cities
                        .putExtra(PARAM_LANG_CODE, languageIndex); // language index

                // start service for added a city details and weather data
                context.startService(service1);

                break;

            case STATUS_GET_WEATHER_ONE_CITY_START:

                Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ONE_CITY_START");

                ///     showMessageBar(getString(R.string.message_wait_for_data), true);

                Intent oneCityService = new Intent(context, GetDataService.class)
                        .putExtra(PARAM_TASK, mTask) // get weather data for all cities
                        .putExtra(PARAM_LANG_CODE, languageIndex); // language index

                // start service for added a city details and weather data
                context.startService(oneCityService);

                break;

            case STATUS_GET_WEATHER_ONE_CITY_FINISH:

                Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ONE_CITY_FINISH");

                ///     refreshData();

                // How to send data to a running activity from Broadcast Receiver!!
                // https://stackoverflow.com/questions/6661801/how-can-i-send-result-data-from-broadcast-receiver-to-activity
                // https://stackoverflow.com/questions/8570823/how-to-send-data-to-a-running-activity-from-broadcast-receiver?rq=1

                break;

            case STATUS_GET_WEATHER_ALL_CITIES_START:

                Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ALL_CITIES_START");

                ///       showMessageBar(getString(R.string.message_wait_for_data), true);

*//*                Intent allCitiesService = new Intent(context, GetDataService.class)
                        .putExtra(PARAM_TASK, mTask) // get weather data for all cities
                        .putExtra(PARAM_LANG_CODE, languageIndex); // language index

                // start service for added a city details and weather data
                context.startService(allCitiesService);*//*

                break;

            case STATUS_GET_WEATHER_ALL_CITIES_FINISH:

                Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ALL_CITIES_FINISH");


                ///      refreshData();


                break;
        }

        //Разблокируем поток.
        wl.release();*/
//   }

 /*   private void setAlarmManager(){

        this.context = this;
        Intent alarm = new Intent(this.context, AlarmReceiver.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(this.context, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);
        if(alarmRunning == false) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, alarm, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1800000, pendingIntent);
        }
        */
 /*       PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");

        // lock
        wl.acquire();

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent runIntent = new Intent(mContext, GetDataService.class)
               .putExtra(PARAM_TASK, TASK_GET_DATA_PERIODICALLY) // get weather data for all cities
               .setAction(BROADCAST_DYNAMIC_ACTION);
                // .putExtra(PARAM_STATUS, STATUS_RUN_ALL_CITIES)
             //   .putExtra(PARAM_LANG_CODE, mMapLanguageIndex); // language index

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                RQS_2,
                runIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // FLAG_UPDATE_CURRENT // FLAG_CANCEL_CURRENT

        /// alarmManager.cancel(pendingIntent);

        alarmManager.setRepeating(AlarmManager.RTC,
                System.currentTimeMillis(),
                3 * 60 * 1000, // AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                pendingIntent);

        //Разблокируем поток.
        wl.release();*/
//  }


//}


/*
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

        /// getData();*/




/*

        String action = intent.getAction();

        if(action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {

                // Code to handle BOOT COMPLETED EVENT
                // TO-DO: I can start an service.. display a notification... start an activity

                // start AlarmManager action periodically after device boot up

                AlarmManager alarmManager=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);

              ///  Intent intent = new Intent(mContext, StaticBroadcastReceiver.class);

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

        Intent intent = new Intent(context, StaticBroadcastReceiver.class);

        intent.putExtra(ONE_TIME, Boolean.FALSE);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        //After 5 seconds
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5, pi);
    }


    public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, StaticBroadcastReceiver.class);

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

        Intent intent = new Intent(context, StaticBroadcastReceiver.class);

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