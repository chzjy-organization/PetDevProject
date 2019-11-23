package com.punuo.sys.app.feed;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.punuo.sys.sdk.PnApplication;
import com.punuo.sys.sip.model.FeedPlan;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by han.chen.
 * Date on 2019-11-23.
 **/
public class FeedAlarmManager {
    private AlarmManager mAlarmManager;
    private static FeedAlarmManager sFeedAlarmManager;
    public static final String ACTION_FEED_PLAN = "com.punuo.sys.app.FEED";
    public HashMap<String, PendingIntent> mAlarmTasks = new HashMap<>();

    public static FeedAlarmManager getInstance() {
        if (sFeedAlarmManager == null) {
            synchronized (FeedAlarmManager.class) {
                sFeedAlarmManager = new FeedAlarmManager();
            }
        }
        return sFeedAlarmManager;
    }


    public FeedAlarmManager() {
            mAlarmManager = (AlarmManager) PnApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
    }

    //是否为未来时间（只从时分秒上判断）
    private boolean isFeature(long targetTime) {
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.set(Calendar.YEAR, 2019);
        nowCalendar.set(Calendar.MONTH, 1);
        nowCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return nowCalendar.getTimeInMillis() < targetTime;
    }
    //生成当天计划标上的时间的时间戳
    private long getTodayTaskTime(long targetTime) {
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTimeInMillis(targetTime);
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, targetCalendar.get(Calendar.HOUR_OF_DAY));
        todayCalendar.set(Calendar.MINUTE, targetCalendar.get(Calendar.MINUTE));
        todayCalendar.set(Calendar.SECOND, targetCalendar.get(Calendar.SECOND));
        return todayCalendar.getTimeInMillis();
    }
    //即时添加定时任务
    public void addAlarmTask(Context context, FeedPlan feedPlan) {
        long targetTime = feedPlan.time * 1000;
        String key = String.valueOf(targetTime);
        PendingIntent targetIntent = mAlarmTasks.get(key);
        if (targetIntent != null) {
            targetIntent.cancel();
            mAlarmTasks.remove(key);
        }
        //未来时间
        if (isFeature(targetTime)) {
            Intent intent = new Intent();
            intent.setAction(ACTION_FEED_PLAN);
            intent.putExtra("count", feedPlan.count);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, getTodayTaskTime(targetTime), pendingIntent);
            mAlarmTasks.put(key, targetIntent);
        }
    }
    //重新生成定时任务
    public void createAlarmTask(Context context, FeedPlan feedPlan) {
        long targetTime = feedPlan.time * 1000;
        String key = String.valueOf(targetTime);
        PendingIntent targetIntent = mAlarmTasks.get(key);
        if (targetIntent != null) {
            targetIntent.cancel();
            mAlarmTasks.remove(key);
        }
        Intent intent = new Intent();
        intent.setAction(ACTION_FEED_PLAN);
        intent.putExtra("count", feedPlan.count);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, getTodayTaskTime(targetTime), pendingIntent);
        mAlarmTasks.put(key, targetIntent);
    }
}
