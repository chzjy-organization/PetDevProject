package com.punuo.sys.app.feed.plan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

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
    private boolean isFuture(long targetTime) {
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.set(Calendar.YEAR, 2019);
        nowCalendar.set(Calendar.MONTH, 1);
        nowCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return nowCalendar.getTimeInMillis() < targetTime;
    }
    //是否为过去时间（只从时分秒上判断）
    private boolean isPast(long targetTime){
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.set(Calendar.YEAR,2019);
        nowCalendar.set(Calendar.MONTH,1);
        nowCalendar.set(Calendar.DAY_OF_MONTH,1);
        return nowCalendar.getTimeInMillis()>targetTime;
    }
    //生成当天计划表上的时间的时间戳
    private long getTodayTaskTime(long targetTime) {
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTimeInMillis(targetTime);
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, targetCalendar.get(Calendar.HOUR_OF_DAY));
        todayCalendar.set(Calendar.MINUTE, targetCalendar.get(Calendar.MINUTE));
        todayCalendar.set(Calendar.SECOND, targetCalendar.get(Calendar.SECOND));
        return todayCalendar.getTimeInMillis();
    }

    public long getTomorrowTaskTime(long targetTime){
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTimeInMillis(targetTime);
        Calendar tomorrowCalendar = Calendar.getInstance();
        tomorrowCalendar.set(Calendar.DAY_OF_MONTH,tomorrowCalendar.get(Calendar.DAY_OF_MONTH)+1);
        tomorrowCalendar.set(Calendar.HOUR_OF_DAY, targetCalendar.get(Calendar.HOUR_OF_DAY));
        tomorrowCalendar.set(Calendar.MINUTE, targetCalendar.get(Calendar.MINUTE));
        tomorrowCalendar.set(Calendar.SECOND, targetCalendar.get(Calendar.SECOND));
        return tomorrowCalendar.getTimeInMillis();
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
        if (isFuture(targetTime)) {
            Intent intent = new Intent();
            intent.setAction(ACTION_FEED_PLAN);
            intent.putExtra("count", feedPlan.count);
            int requestCode = key.hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);
//            mAlarmManager.set(AlarmManager.RTC_WAKEUP, getTodayTaskTime(targetTime), pendingIntent);
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,getTodayTaskTime(targetTime),pendingIntent);
            }else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
                mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,getTodayTaskTime(targetTime),pendingIntent);
            }else{
                mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,getTodayTaskTime(targetTime),AlarmManager.INTERVAL_DAY,pendingIntent);
            }
            Log.i("plan", "成功设置alarm ");
            mAlarmTasks.put(key, targetIntent);
            Log.i("plan", ""+mAlarmTasks);
        }

        //过去时间(只比较时分秒)
        if(isPast(targetTime)){
            Intent intent = new Intent();
            intent.setAction(ACTION_FEED_PLAN);
            intent.putExtra("count",feedPlan.count);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,getTomorrowTaskTime(targetTime),24*60*60*1000,pendingIntent);
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,getTomorrowTaskTime(targetTime),pendingIntent);
            }else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
                mAlarmManager.setExact(AlarmManager.RTC_WAKEUP,getTomorrowTaskTime(targetTime),pendingIntent);
            }else{
                mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,getTomorrowTaskTime(targetTime),AlarmManager.INTERVAL_DAY,pendingIntent);
            }
            mAlarmTasks.put(key, targetIntent);
            Log.i("plan", "测试设置已过时间");
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
        if(isPast(targetTime)) {
            Intent intent = new Intent();
            intent.setAction(ACTION_FEED_PLAN);
            intent.putExtra("count", feedPlan.count);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, getTodayTaskTime(targetTime), pendingIntent);
            mAlarmTasks.put(key, targetIntent);
        }
    }

}
