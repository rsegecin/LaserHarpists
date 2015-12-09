package com.rmscore.music;

import com.rmscore.datamodels.NoteData;

/**
 * Created by Rinaldi on 21/11/2015.
 */
public interface IMusicManager {
    void onNoteReceived(NoteData noteData);

    void onMusicStopped();
}
