package com.rmscore.bases;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.rmscore.datamodels.NoteData;
import com.rmscore.music.IMusicManager;
import com.rmscore.music.MusicManager;

/**
 * Created by Rinaldi on 21/11/2015.
 */
public class UIHandler extends Handler {

    private BaseActivity baseActivity;

    public UIHandler(BaseActivity baseActivityParam, Looper looperParam) {
        super(Looper.getMainLooper());
        baseActivity = baseActivityParam;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        if (baseActivity instanceof IMusicManager) {
            switch (msg.what) {
                case MusicManager.NOTE_PLAYED:
                    ((IMusicManager) baseActivity).onNoteReceived((NoteData) msg.obj);
                    break;
                case MusicManager.NOTE_RECEIVED:
                    ((IMusicManager) baseActivity).onNoteReceived((NoteData) msg.obj);
                    break;
                case MusicManager.MUSIC_STOPPED:
                    ((IMusicManager) baseActivity).onMusicStopped();
                    break;
            }

        }
    }
}
