package scamell.michael.amulet;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class Sound {

    private final SparseIntArray soundPoolMap;
    private final AudioManager mgr;
    private SoundPool soundPool;

    public Sound(Context context, int numStreams) {
        soundPoolMap = new SparseIntArray();
        soundPool = new SoundPool(numStreams, AudioManager.STREAM_MUSIC, 0);
        mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void loadSound(Context context, int soundName, int sound, int priority) {
        soundPoolMap.put(soundName, soundPool.load(context, sound, priority));
    }

    public void playSound(int sound) {
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = streamVolumeCurrent / streamVolumeMax;
        soundPool.play(soundPoolMap.get(sound), volume, volume, 1, 0, 1f);
    }

    public void shutDownSoundPool() {
        soundPool.release();
        soundPool = null;
    }
}
