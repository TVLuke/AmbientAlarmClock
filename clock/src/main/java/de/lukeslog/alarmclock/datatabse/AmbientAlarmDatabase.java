package de.lukeslog.alarmclock.datatabse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.lukeslog.alarmclock.actions.ActionConfigBundle;
import de.lukeslog.alarmclock.actions.ActionManager;
import de.lukeslog.alarmclock.actions.AmbientAction;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.Day;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;

/**
 * Created by lukas on 31.03.14.
 */
public class AmbientAlarmDatabase
{
    private static SQLiteDatabase database;
    private static OpenHelper openHelper;
    public static String TAG = AlarmClockConstants.TAG+"_Database";

    public static final String TABLE_ALARM="ambientalarm";
    public static final String TABLE_ACTION="ambientaction";
    public static final String TABLE_ALARMTOACTION = "alarmtoaction";

    public static final String TABLE_ALARM_ID = "_id";
    public static final String TABLE_ALARM_ALARMID = "alarmid";
    public static final String TABLE_ALARM_ACTIVE = "isactive";
    public static final String TABLE_ALARM_SNOOZING = "issnoozing";
    public static final String TABLE_ALARM_SNOOZE_TIME = "snoozetime";
    public static final String TABLE_ALARM_LOCK = "lock";
    public static final String TABLE_ALARM_TIME_HOUR = "alarmhour";
    public static final String TABLE_ALARM_TIME_MINUTE = "alarmminute";
    public static final String TABLE_ALARM_DAY_MONDAY = "alarmmonday";
    public static final String TABLE_ALARM_DAY_TUESDAY = "alarmtuesday";
    public static final String TABLE_ALARM_DAY_WEDNESDAY = "alarmwednesday";
    public static final String TABLE_ALARM_DAY_THURSDAY = "alarmthursday";
    public static final String TABLE_ALARM_DAY_FRIDAY = "alarmfriday";
    public static final String TABLE_ALARM_DAY_SATURDAY = "alarmsaturday";
    public static final String TABLE_ALARM_DAY_SUNDAY = "alarmsunday";

    public static final String TABLE_ACTION_ID = "_id";
    public static final String TABLE_ACTION_ACTIONID = "actionid";
    public static final String TABLE_ACTION_TYPE = "actiontype";
    public static final String TABLE_ACTION_NAME = "actionname";
    public static final String TABLE_ACTION_CONFIG_BUNDLE_VALUES = "configbundlevalues";

    public static final String TABLE_ALARMTOACTION_ID = "_id";
    public static final String TABLE_ALARMTOACTION_TIMING = "alarmtoactiontiming";

    public static final String TABLE_ACTION_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ACTION +
                    " ("+TABLE_ACTION_ID + " integer primary key autoincrement, "+
                    ""+TABLE_ACTION_ACTIONID +" text, " +
                    ""+TABLE_ACTION_TYPE +" text, "+
                    ""+TABLE_ACTION_NAME + " text, " +
                    ""+TABLE_ACTION_CONFIG_BUNDLE_VALUES + " text "+
                    ");";

    public static final String TABLE_ALARMTOACTION_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ALARMTOACTION +
                    " ("+TABLE_ALARMTOACTION_ID +" integer primary key autoincrement, "+
                    ""+TABLE_ALARM_ALARMID+" text, "+
                    ""+TABLE_ALARMTOACTION_TIMING+" text, "+
                    ""+TABLE_ACTION_ACTIONID+" text "+
                    ");";

    public static final String TABLE_ALARM_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ALARM +
                    " ("+TABLE_ALARM_ID +" integer primary key autoincrement, " +
                    ""+TABLE_ALARM_ALARMID+" text, " +
                    ""+TABLE_ALARM_ACTIVE+" integer, " +
                    ""+TABLE_ALARM_SNOOZING+" integer, " +
                    ""+TABLE_ALARM_SNOOZE_TIME+" integer, " +
                    ""+TABLE_ALARM_LOCK+" integer, "+
                    ""+TABLE_ALARM_TIME_HOUR+" integer, " +
                    ""+TABLE_ALARM_TIME_MINUTE+" integer, " +
                    ""+TABLE_ALARM_DAY_MONDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_TUESDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_WEDNESDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_THURSDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_FRIDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_SATURDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_SUNDAY+" integer " +
                    ");";

    public static void createDataBase(Context context)
    {
        openHelper = new OpenHelper(context);
        try {
            database = openHelper.getWritableDatabase();
        } catch(Exception e){
            database.close();
            database = openHelper.getWritableDatabase();
        }

    }

    public static void updateAmbientAction(AmbientAction ambientAction)
    {
        Logger.d(TAG, "upate Action Database " + ambientAction.getActionID());
        ContentValues cValues = new ContentValues();
        cValues.put(TABLE_ACTION_NAME, ambientAction.getActionName());
        cValues.put(TABLE_ACTION_TYPE, ambientAction.getClass().toString());
        String configString=ambientAction.getConfigurationData().toString();
        cValues.put(TABLE_ACTION_CONFIG_BUNDLE_VALUES, configString);
        if(actionEntryExists(ambientAction))
        {
            Logger.d(TAG, "we make an update "+ambientAction.getActionID());
            String[] args={ambientAction.getActionID()};
            database.update(TABLE_ACTION, cValues, TABLE_ACTION_ACTIONID + " = ?", args);
        }
        else
        {
            cValues.put(TABLE_ACTION_ACTIONID, ambientAction.getActionID());
            Logger.d(TAG, "we create a new entry "+ambientAction.getActionID());
            database.insert(TABLE_ACTION, null, cValues);
        }
    }

    public static ArrayList<AmbientAction> getAllActionsFromDatabase()
    {
        Logger.i(TAG, "get all Actions from Database");

        Cursor c = queryDatabaseForAllActions();

        Logger.i(TAG, "cursorsize: "+c.getCount());


        ArrayList<AmbientAction> ambientActions = createActionListFromCursor(c);

        c.close();

        Logger.d(TAG, "List of ambient actions is done...");

        return ambientActions;
    }

    public static void removeAmbientAction(AmbientAction ambientAction)
    {
        database.delete(TABLE_ACTION, TABLE_ACTION_ACTIONID+" = '"+ambientAction.getActionID()+"'", null);
    }

    private static ArrayList<AmbientAction> createActionListFromCursor(Cursor c)
    {
        ArrayList<AmbientAction> ambientActions = new ArrayList<AmbientAction>();
        while (c.moveToNext())
        {
            AmbientAction ambientAction = createActionFromCursorElement(c);
            Logger.d(TAG, ambientAction.getActionName());
            ambientActions.add(ambientAction);
        }
        return ambientActions;
    }

    private static AmbientAction createActionFromCursorElement(Cursor c)
    {
        String configString = c.getString(c.getColumnIndex(TABLE_ACTION_CONFIG_BUNDLE_VALUES));
        String className = c.getString(c.getColumnIndex(TABLE_ACTION_TYPE));
        Logger.d(TAG, "ACTION TYPE NAME="+className);
        ActionConfigBundle acb = new ActionConfigBundle(configString);
        AmbientAction aa = ActionManager.createActionFromConfigBundle(acb, className);
        Logger.d(TAG, "created "+aa.getActionName()+" from Cursor");
        return aa;
    }

    private static Cursor queryDatabaseForAllActions()
    {
         Cursor c= database.query(TABLE_ACTION, new String[] {
                            TABLE_ACTION_ACTIONID,
                            TABLE_ACTION_TYPE,
                            TABLE_ACTION_NAME,
                            TABLE_ACTION_CONFIG_BUNDLE_VALUES},
                    null,
                    null,
                    null,
                    null,
                    null);
        return c;
    }

    private static boolean actionEntryExists(AmbientAction ambientAction)
    {
        String[] args={ambientAction.getActionID()};
        Logger.d(TAG, "alarmID="+ambientAction.getActionID());
        Cursor cursor = database.rawQuery("SELECT * FROM "+TABLE_ACTION+" WHERE "+TABLE_ACTION_ACTIONID+" = ? ", args);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        Logger.d(TAG, "existst? "+exists);
        return exists;
    }

    public static void updateAmbientAlarm(AmbientAlarm ambientAlarm)
    {
        Logger.d(TAG, "update Database");
        ContentValues cValues = new ContentValues();
        cValues.put(TABLE_ALARM_ACTIVE, boolToInt(ambientAlarm.isActive()));
        cValues.put(TABLE_ALARM_SNOOZING, boolToInt(ambientAlarm.isSnoozing()));
        cValues.put(TABLE_ALARM_SNOOZE_TIME, ambientAlarm.getSnoozeTimeInMinutes());
        cValues.put(TABLE_ALARM_LOCK, boolToInt(ambientAlarm.islocked()));
        cValues.put(TABLE_ALARM_TIME_HOUR, ambientAlarm.getAlarmTime().getHourOfDay());
        cValues.put(TABLE_ALARM_TIME_MINUTE, ambientAlarm.getAlarmTime().getMinuteOfHour());
        cValues.put(TABLE_ALARM_DAY_MONDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.MONDAY)));
        cValues.put(TABLE_ALARM_DAY_TUESDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.TUESDAY)));
        cValues.put(TABLE_ALARM_DAY_WEDNESDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.WEDNESDAY)));
        cValues.put(TABLE_ALARM_DAY_THURSDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.THURSDAY)));
        cValues.put(TABLE_ALARM_DAY_FRIDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.FRIDAY)));
        cValues.put(TABLE_ALARM_DAY_SATURDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.SATURDAY)));
        cValues.put(TABLE_ALARM_DAY_SUNDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.SUNDAY)));
        if(alarmEntryExists(ambientAlarm))
        {
            Logger.d(TAG, "we make an update "+ambientAlarm.getAlarmID());
            String[] args={ambientAlarm.getAlarmID()};
            database.update(TABLE_ALARM, cValues, TABLE_ALARM_ALARMID + " = ?", args);
        }
        else
        {
            cValues.put(TABLE_ALARM_ALARMID, ambientAlarm.getAlarmID());
            Logger.d(TAG, "we create a new entry");
            database.insert(TABLE_ALARM, null, cValues);
        }
        Set<String> keys = ambientAlarm.getRegisteredActions().keySet();
        Iterator<String> iterator = keys.iterator();
        while(iterator.hasNext())
        {
            String actiontime = iterator.next();
            ArrayList<AmbientAction> actions = ambientAlarm.getRegisteredActions().get(actiontime);
            for(AmbientAction action: actions)
            {
                updateAmbientAction(action);
            }
        }
        updateActionsForAmbientAlarm(ambientAlarm);
    }

    public static ArrayList<AmbientAlarm> getAllAlarmsFromDatabase()
    {
        Logger.i(TAG, "get all...");
        ArrayList<AmbientAlarm> ambientAlarms = new ArrayList<AmbientAlarm>();
        try
        {
            Cursor c = queryDatabaseForAllAlarms();

            Logger.i(TAG, "cursorsize: "+c.getCount());


            ambientAlarms = createAlarmListFromCursor(c);

            c.close();
        }
        catch(Exception e)
        {
            Logger.e(TAG, "ERROR IN GETTING ALARMS"+e.getLocalizedMessage());
            Logger.e(TAG, ""+e.toString());
            Logger.e(TAG, ""+e.getMessage());
        }


        return ambientAlarms;
    }

    public static void removeAmbientAlarm(AmbientAlarm ambientAlarm)
    {
        database.delete(TABLE_ALARM, TABLE_ALARM_ALARMID + " = '" + ambientAlarm.getAlarmID() + "'", null);
        removeActionsRelatedToAlarm(ambientAlarm);
    }

    private static void updateActionsForAmbientAlarm(AmbientAlarm alarm)
    {
        Logger.d(TAG, "updateActionsforAmbietAlarm");
        removeActionsRelatedToAlarm(alarm);
        HashMap<String, ArrayList<AmbientAction>> actions = alarm.getRegisteredActions();
        Set<String> keyset = actions.keySet();
        for(String s : keyset)
        {
            ArrayList<AmbientAction> ambientactions = actions.get(s);
            for(AmbientAction action : ambientactions)
            {
                Logger.d(TAG, "add action name-> "+action.getActionName());
                addToAlarmToActionDatabase(alarm.getAlarmID(), s, action.getActionID());
            }

        }
    }

    private static void removeActionsRelatedToAlarm(AmbientAlarm alarm)
    {
        Logger.d(TAG, "remove all related Actions");
        database.delete(TABLE_ALARMTOACTION, TABLE_ALARM_ALARMID + " = '" + alarm.getAlarmID() + "'", null);
    }

    private static ArrayList<String> registerActionsToAlarm(AmbientAlarm alarm)
    {
        Logger.d(TAG,"register actions to alarm...");
        Logger.d(TAG,"register actions to alarm...");
        Logger.d(TAG,"register actions to alarm...");
        Logger.d(TAG,"register actions to alarm...");
        Logger.d(TAG,"register actions to alarm...");
        Logger.d(TAG,"register actions to alarm...");
        Logger.d(TAG,"register actions to alarm...");
        Logger.d(TAG,"register actions to alarm...");
        ArrayList<String> relatedActions = new ArrayList<String>();
        String[] args={alarm.getAlarmID()};
        Cursor cursor = database.rawQuery("SELECT * FROM "+TABLE_ALARMTOACTION+" WHERE "+TABLE_ALARM_ALARMID+" = ? ", args);

        ActionManager.updateActionList();

        ArrayList<AmbientAction> actions = ActionManager.getActionList();
        Logger.d(TAG, "    actions list size() => "+actions.size());
        while(cursor.moveToNext())
        {
            Logger.d(TAG, "    next");
            String actionID = cursor.getString(cursor.getColumnIndex(TABLE_ACTION_ACTIONID));
            Logger.d(TAG, "actionID = "+actionID);
            String actionTiming = cursor.getString(cursor.getColumnIndex(TABLE_ALARMTOACTION_TIMING));

            for(AmbientAction action : actions)
            {
                Logger.d(TAG, "action!=null"+(action!=null));
                if(action.getActionID().equals(actionID))
                {
                    alarm.registerAction(actionTiming, action);
                }
            }

        }
        return relatedActions;
    }

    private static void addToAlarmToActionDatabase(String alarmID, String s, String actionID)
    {
        Logger.d(TAG, "ADD TO ALARM TO ACTION DATABASE"+alarmID+" "+actionID);
        ContentValues cValues = new ContentValues();
        cValues.put(TABLE_ALARM_ALARMID, alarmID);
        cValues.put(TABLE_ALARMTOACTION_TIMING, s);
        cValues.put(TABLE_ACTION_ACTIONID, actionID);
        database.insert(TABLE_ALARMTOACTION, null, cValues);
    }

    private static boolean alarmEntryExists(AmbientAlarm ambientAlarm)
    {
        boolean exists = false;
        try {
            Logger.d(TAG, " alarmEntryExists()");
            String[] args = {ambientAlarm.getAlarmID()};
            Logger.d(TAG, "alarmID=" + ambientAlarm.getAlarmID());
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_ALARM + " WHERE " + TABLE_ALARM_ALARMID + " = ? ", args);
            exists = (cursor.getCount() > 0);
            cursor.close();
            Logger.d(TAG, "existst? " + exists);
        } catch( Exception e){
            Logger.e(TAG, "alarmEntryExists threw an error.");
        }
        return exists;
    }

    private static ArrayList<AmbientAlarm> createAlarmListFromCursor(Cursor c)
    {
        Logger.d(TAG, "createAlarmListFromCursor(");
        ArrayList<AmbientAlarm> ambientAlarms = new ArrayList<AmbientAlarm>();
        while (c.moveToNext())
        {
            AmbientAlarm ambientAlarm = createAlarmFromCursorElement(c);
            ambientAlarms.add(ambientAlarm);
        }
        return ambientAlarms;
    }

    private static AmbientAlarm createAlarmFromCursorElement(Cursor c)
    {
        Logger.d(TAG, "createAlarmFromCursorElement()");
        AmbientAlarm ambientAlarm = new AmbientAlarm(c.getString(c.getColumnIndex(TABLE_ALARM_ALARMID)));
        DateTime alarmTime = new DateTime();
        alarmTime = alarmTime.withHourOfDay(c.getInt(c.getColumnIndex(TABLE_ALARM_TIME_HOUR)));
        alarmTime = alarmTime.withMinuteOfHour(c.getInt(c.getColumnIndex(TABLE_ALARM_TIME_MINUTE)));
        ambientAlarm.setAlarmTime(alarmTime);
        ambientAlarm.setActive(intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_ACTIVE))));
        Logger.d(TAG, "" + ambientAlarm.isActive());
        ambientAlarm.setSnoozing(intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_SNOOZING))));
        Logger.d(TAG, "" + ambientAlarm.isSnoozing());
        ambientAlarm.setSnoozeTimeInMinutes(c.getInt(c.getColumnIndex(TABLE_ALARM_SNOOZE_TIME)));
        Logger.d(TAG, "olol");
        ambientAlarm.setLocked(intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_LOCK))));
        ambientAlarm.setAlarmStateForDay(Day.MONDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_MONDAY))));
        ambientAlarm.setAlarmStateForDay(Day.TUESDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_TUESDAY))));
        ambientAlarm.setAlarmStateForDay(Day.WEDNESDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_WEDNESDAY))));
        ambientAlarm.setAlarmStateForDay(Day.THURSDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_THURSDAY))));
        ambientAlarm.setAlarmStateForDay(Day.FRIDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_FRIDAY))));
        ambientAlarm.setAlarmStateForDay(Day.SATURDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_SATURDAY))));
        ambientAlarm.setAlarmStateForDay(Day.SUNDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_SUNDAY))));

        registerActionsToAlarm(ambientAlarm);

        return ambientAlarm;
    }

    private static Cursor queryDatabaseForAllAlarms()
    {
        Logger.d(TAG, "querry Database for All Alarms");
        Logger.d(TAG, "database!=null => "+(database!=null));
        if(database!=null)
        {
            Cursor c = database.query(TABLE_ALARM, new String[]{
                            TABLE_ALARM_ALARMID,
                            TABLE_ALARM_ACTIVE,
                            TABLE_ALARM_SNOOZING,
                            TABLE_ALARM_SNOOZE_TIME,
                            TABLE_ALARM_LOCK,
                            TABLE_ALARM_TIME_HOUR,
                            TABLE_ALARM_TIME_MINUTE,
                            TABLE_ALARM_DAY_MONDAY,
                            TABLE_ALARM_DAY_TUESDAY,
                            TABLE_ALARM_DAY_WEDNESDAY,
                            TABLE_ALARM_DAY_THURSDAY,
                            TABLE_ALARM_DAY_FRIDAY,
                            TABLE_ALARM_DAY_SATURDAY,
                            TABLE_ALARM_DAY_SUNDAY},
                    null,
                    null,
                    null,
                    null,
                    null
            );
            return c;
        }
        else
        {
            try
            {
                database = openHelper.getWritableDatabase();
                return queryDatabaseForAllAlarms();
            }
            catch(Exception e)
            {
                return null;
            }


        }
    }

    private static int boolToInt(boolean b)
    {
        if(b)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    private static boolean intToBool(int i)
    {
        if(i==0)
        {
            return false;
        }
        return true;
    }

    public static class OpenHelper extends SQLiteOpenHelper
    {

        OpenHelper(Context context)
        {
            super(context, DatabaseConstants.DATABASE_NAME, null,  DatabaseConstants.DATABASE_VERSION);
            Logger.d(TAG, "Open Helper");
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            Logger.i(TAG, "onCreate for EntryDB");
            db.execSQL(TABLE_ALARM_CREATE);
            db.execSQL(TABLE_ACTION_CREATE);
            db.execSQL(TABLE_ALARMTOACTION_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Logger.i(TAG, "onUpgrade for EntryDB");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMTOACTION);
            onCreate(db);
        }
    }
}
