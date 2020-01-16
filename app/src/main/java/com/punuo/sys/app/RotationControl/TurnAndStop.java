package com.punuo.sys.app.RotationControl;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class TurnAndStop {

    public Boolean turnRight(){
            String filepath = "/dev/petfoodweighter";
            String cmd = "1";

            try{
                FileOutputStream fos = new FileOutputStream(filepath);
                fos.write(cmd.getBytes());
                fos.flush();
                fos.close();
            }catch(IOException e){
                Log.e("SerialService", "++liujihui++, IOException: "+e);
                return false;
            }

//        try {
//            Process process = Runtime.getRuntime().exec("pet_right");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return true;
    }

    public Boolean turnStop(){
        String filepath = "/dev/petfoodweighter";
        String cmd = "0";

        try{
            FileOutputStream fos = new FileOutputStream(filepath);
            fos.write(cmd.getBytes());
            fos.flush();
            fos.close();
        }catch(IOException e){
            Log.e("SerialService", "++liujihui++, IOException: "+e);
            return false;
        }
        return true;
    }
}
