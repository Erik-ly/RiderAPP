package com.bophia.erik.ykxrider.utils;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * 播放声音工具类
 */
public class PlaySoundUtils {

    public static void playSound(Context context,int sound){

        String uri = "android.resource://" + "com.bophia.erik.ykxrider" + "/" + sound;
        Uri soundUri = Uri.parse(uri);
        Ringtone mRingtone = RingtoneManager.getRingtone(context,soundUri);
        if (!mRingtone.isPlaying()){
            mRingtone.play();
        }
    }
}
