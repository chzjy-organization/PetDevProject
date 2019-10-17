package com.punuo.sys.app.led;

import android.util.Log;



import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by 75716 on 2019/10/16.
 */

public class LedControl {

    public Boolean turnOnCustom1Light(){
        String filepath = "/sys/devices/platform/leds-mt65xx/leds/custom1-led/brightness";
        String cmd = "255";  //0-255灯光亮度控制，自己根据实际情况设定值
        try{
            FileOutputStream fos = new FileOutputStream(filepath);
            fos.write(cmd.getBytes());
            fos.flush();
            fos.close();
        }catch(IOException e){
            Log.e("SerialService", "++liujihui++, IOException: " + e);
            return false;
        }
        return true;

    }
    public Boolean turnOffCustom1Light(){
        String filepath = "/sys/devices/platform/leds-mt65xx/leds/custom1-led/brightness";
        String cmd = "0";  //0-255灯光亮度控制，自己根据实际情况设定值
        try{
            FileOutputStream fos = new FileOutputStream(filepath);
            fos.write(cmd.getBytes());
            fos.flush();
            fos.close();
        }catch(IOException e){
            Log.e("SerialService", "++liujihui++, IOException: " + e);
            return false;
        }
        return true;

    }
    public Boolean turnOnCustom2Light(){
        String filepath = "/sys/devices/platform/leds-mt65xx/leds/custom1-led/brightness  ";
        String cmd = "255";  //0-255灯光亮度控制，自己根据实际情况设定值
        try{
            FileOutputStream fos = new FileOutputStream(filepath);
            fos.write(cmd.getBytes());
            fos.flush();
            fos.close();
        }catch(IOException e){
            Log.e("SerialService", "++liujihui++, IOException: " + e);
            return false;
        }
        return true;
    }
    public Boolean turnOffCustom2Light(){
        String filepath = "/sys/devices/platform/leds-mt65xx/leds/custom1-led/brightness  ";
        String cmd = "0";  //0-255灯光亮度控制，自己根据实际情况设定值
        try{
            FileOutputStream fos = new FileOutputStream(filepath);
            fos.write(cmd.getBytes());
            fos.flush();
            fos.close();
        }catch(IOException e){
            Log.e("SerialService", "++liujihui++, IOException: " + e);
            return false;
        }
        return true;
    }
}
