package de.lukeslog.alarmclock.actions;

import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;

/**
 * Created by lukas on 29.04.14.
 */
public class EZControlHeatAction extends AmbientAction
{
    String ezControlIP="192.168.1.242";
    String heaternumber="1";
    String level = "1";

    public EZControlHeatAction(String actionName)
    {
        super(actionName);
    }

    public EZControlHeatAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
    }

    @Override
    public void action(boolean isFirstAlert)
    {

    }

    @Override
    public void snooze()
    {

    }

    @Override
    public void awake()
    {

    }

    @Override
    public void tick(DateTime currentTime)
    {

    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void defineSettingsView(LinearLayout configView, AmbientAlarm alarm)
    {
        LinearLayout mainLayout = createLayout(configView, alarm);


        TextView name = createNameTextView(configView);

        ImageView icon = createActionIcon(configView);

        mainLayout.addView(icon);
        mainLayout.addView(name);
        configView.addView(mainLayout);
    }

    private TextView createNameTextView(LinearLayout configView)
    {
        TextView name = new TextView(configView.getContext());
        name.setText(getActionName());
        return name;
    }

    private ImageView createActionIcon(LinearLayout configView)
    {
        ImageView icon = new ImageView(configView.getContext());
        icon.setImageResource(R.drawable.action_icon);
        TableRow.LayoutParams params = new TableRow.LayoutParams(100, TableLayout.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(params);
        return icon;
    }

    @Override
    protected ActionConfigBundle setConfigurationData()
    {
        return null;
    }

    @Override
    public Class getConfigActivity()
    {
        return EZControlHeatActionConfigurationFragment.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {

    }

    private void controlheat()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    URL oracle = new URL("http://"+ezControlIP+"/control?cmd=set_state_actuator&number="+heaternumber+"&function="+level+"&page=control.html");
                    URLConnection yc = oracle.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                    Log.d(TAG, "command->heat");
                }
                catch(Exception e)
                {
                    Log.e(TAG, "there was an error when setting the heat");
                }
            }
        }).start();
    }
}