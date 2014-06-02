package de.lukeslog.alarmclock.ambientalarm;

import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;

import java.util.ArrayList;

import de.lukeslog.alarmclock.datatabse.AmbientAlarmDatabase;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.main.NotificationManagement;
import de.lukeslog.alarmclock.startup.NotificationService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;

/**
 * Created by lukas on 31.03.14.
 *
 * Manages the set of alarms generated by the user, also manages access to the database.
 */
public class AmbientAlarmManager
{

    private static ArrayList<AmbientAlarm> registeredAlarms;

    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    /**
     *
     * This method informs all currently active alarms of the current time, this time may then
     * be transformed into actions by these alarms
     *
     * @param currentTime to inform all alerts of an identical time the current Time is provided by
     *                    the calling method
     */
    public static void notifyActiveAlerts(DateTime currentTime)
    {
        if(registeredAlarms!=null)
        {
            //Log.d(TAG, "--tick--" + registeredAlarms.size());
            for (AmbientAlarm alarm : registeredAlarms)
            {
                if (alarm.isActive())
                {
                    alarm.notifyOfCurrentTime(currentTime);
                }
            }
        }
        else
        {
            updateListFromDataBase();
        }
    }

    /**
     * add a new ambient alarm to the registry
     *
     * @param ambientAlarm
     */
    public static void addNewAmbientAlarm(AmbientAlarm ambientAlarm)
    {
        Logger.d(TAG, "  addNewAmbientAlarm(AmbientAlarm)");
        if(registeredAlarms!=null)
        {
            registeredAlarms.add(ambientAlarm);
        }
        else
        {
            updateListFromDataBase();
            registeredAlarms.add(ambientAlarm);
        }
        AmbientAlarmDatabase.updateAmbientAlarm(ambientAlarm);
    }

    public static void updateDataBaseEntry(AmbientAlarm ambientAlarm)
    {
        Logger.d(TAG, "  updateDatabaseEntry(AmbientAlarm)");
        AmbientAlarmDatabase.updateAmbientAlarm(ambientAlarm);
    }

    public static ArrayList<AmbientAlarm> getListOfAmbientAlarms()
    {
        Logger.d(TAG, "  getListOfAmbientAlarms()");
        return registeredAlarms;
    }

    public static void updateListFromDataBase()
    {
        Logger.d(TAG, "  update Alarm List from Database");
        //Log.d(TAG, "  Listsize="+registeredAlarms.size());
        registeredAlarms = AmbientAlarmDatabase.getAllAlarmsFromDatabase();
        Logger.d(TAG, "  Listsize="+registeredAlarms.size());
    }

    public static void startAlarmActivity(final AmbientAlarm ambientAlarm)
    {
        Logger.d(TAG, "  startAlarmActivity!");
        final Context ctx = ClockWorkService.getContext();
        if(ctx!=null)
        {
            new Thread(new Runnable()
            {
                @SuppressWarnings("unchecked")
                public void run()
                {
                    Logger.d(TAG, "  ctx is not null");
                    String alarmID = ambientAlarm.getAlarmID();
                    Logger.d(TAG, "  alarmid is " + alarmID);
                    Intent intent = new Intent(ctx, ambientAlarm.getAlarmActivity());
                    Logger.d(TAG, "  Intent created....");
                    // intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                    Logger.d(TAG, "  strage stuff...");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    Logger.d(TAG, "  now start stuff....");
                    intent.putExtra("ambientAlarmID", alarmID);
                    ctx.startActivity(intent);
                    try
                    {
                        Thread.sleep(3000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    //check if the activity is running...
                    if(!AmbientAlarmActivity.running)
                    {
                        Logger.d(TAG, "apparently it is running....");
                        ctx.startActivity(intent);
                    }
                }
            }).start();

        }
        else
        {
            Logger.e(TAG, "CONTEXT WAS NULL!!!!! ALARMACTIVITY COULD NOT BE STARTED!");
        }
    }

    public static void deleteAmbientAlarm(int position)
    {
        Logger.d(TAG, "  delete Ambient Alarm!");
        if(registeredAlarms!=null)
        {
            AmbientAlarmDatabase.removeAmbientAlarm(registeredAlarms.get(position));
            registeredAlarms.remove(position);
        }
    }

    public static AmbientAlarm getAlarmById(String alarmID)
    {
        if(registeredAlarms!=null)
        {
            Logger.d(TAG, "  getAlarmByID " + alarmID);
            Logger.d(TAG, "  size of AlarmList " + registeredAlarms.size());
            for (AmbientAlarm alarm : registeredAlarms)
            {
                Logger.d(TAG, "    -->" + alarm.getAlarmID());
                if (alarm.getAlarmID().equals(alarmID))
                {
                    return alarm;
                }
            }
            return null;
        }
        else
        {
            updateListFromDataBase();
            return getAlarmById(alarmID);
        }
    }
}
