package de.lukeslog.alarmclock.actions;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.alarmactivity.AmbientAlarmActivity;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.UISupport;

/**
 * Created by lukas on 04.04.14.
 */
public class CountdownAction extends AmbientAction
{

    int durationInSeconds=3600;

    public CountdownAction(String actionName, int durationInSeconds)
    {
        super(actionName);
        this.durationInSeconds=durationInSeconds;
    }

    public CountdownAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
        this.durationInSeconds  = Integer.parseInt(configBundle.getString("durationInSeconds"));
    }

    public void setDurationInSeconds(int seconds)
    {
        this.durationInSeconds=seconds;
    }

    public int getDurationInSeconds()
    {
        return durationInSeconds;
    }

    @Override
    public void action()
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
    public void defineSettingsView(final LinearLayout configView)
    {
        LinearLayout mainLayout = createLayout(configView);


        TextView name = createNameTextView(configView);

        ImageView icon = createActionIcon(configView);

        mainLayout.addView(icon);
        mainLayout.addView(name);
        configView.addView(mainLayout);
    }

    private LinearLayout createLayout(final LinearLayout configView)
    {
        LinearLayout mainLayout = new LinearLayout(configView.getContext());
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setMinimumHeight(40);
        mainLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "click");
                openConfigurationActivity(configView.getContext());
            }
        });
        return mainLayout;
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
        icon.setImageResource(R.drawable.countdown_action_icon);
        TableRow.LayoutParams params = new TableRow.LayoutParams(100, TableLayout.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(params);
        return icon;
    }

    @Override
    protected ActionConfigBundle setConfigurationData()
    {
        ActionConfigBundle configBundle = new ActionConfigBundle();
        configBundle.putString("durationInSeconds", ""+this.durationInSeconds);
        return configBundle;
    }

    @Override
    public Class getConfigActivity()
    {
        return CountdownActionActivity.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {
        Log.d(TAG, "update Layout is called");
        LinearLayout content = (LinearLayout) alarmActivity.findViewById(R.id.content);

        DateTime now = new DateTime();

        DateTime alarmTime = ambientAlarm.getLastAlarmTime();
        alarmTime = alarmTime.plusSeconds(this.durationInSeconds);
        Log.d(TAG, alarmTime.toString());
        int seconds = Seconds.secondsBetween(now, alarmTime).getSeconds();
        Log.d(TAG, "SECONDS: "+seconds);
        TextView textview;
        if(content.findViewById(17493)!=null)
        {
            textview = (TextView) content.findViewById(17493);
            //textview.setText(ambientAlarm.getTimeAsString());
        }
        else
        {
            //TODO: this is not centered yet.
            textview = new TextView(content.getContext());
            //textview.setText(ambientAlarm.getTimeAsString());
            textview.setTextSize(40);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textview.setLayoutParams(params);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            textview.setLayoutParams(params);
            textview.setId(17493);
            content.addView(textview);
        }
        textview.setText(UISupport.secondsToCountdownString(seconds));
        if(seconds==0)
        {
            ambientAlarm.awakeButtonPressed();
            alarmActivity.awakeButtonPressedRemotely();
        }
    }

    public int getCountDownDuration()
    {
        return durationInSeconds;
    }
}
