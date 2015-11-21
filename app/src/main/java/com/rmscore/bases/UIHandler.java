package com.rmscore.bases;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.rmscore.datamodels.NoteData;
import com.rmscore.music.INoteReceiver;

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

        if (baseActivity instanceof INoteReceiver) {
            ((INoteReceiver) baseActivity).onNoteReceived((NoteData) msg.obj);
        }
    }
}
