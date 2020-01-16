package com.punuo.sys.app.weighing.tool;

import android.util.Log;

import com.leplay.petwight.PetWeight;

import java.io.FileOutputStream;
import java.io.IOException;

public class
WeightReset {
    /**
     * 测得喂食器本身的重量，将其置零。
     */
    private PetWeight petWeight;
    public Boolean reset(){
        String filepath = "/dev/petfoodweighter";
        petWeight = new PetWeight();
        int value = petWeight.getWeight();
        Log.i("reset_weight", "获得质量为"+value);
        String cmd = String.valueOf(value);
        try{
            FileOutputStream fos = new FileOutputStream(filepath);
            fos.write(cmd.getBytes());
            fos.flush();
            fos.close();
        }catch(IOException e){
            Log.i("SerialService", "++liujihui++, IOException: " + e);
            return false;
        }
        Log.i("reset_weight", "成功置零");
        return true;
    }
}
