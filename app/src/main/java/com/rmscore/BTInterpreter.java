package com.rmscore;

import com.rmscore.datamodels.NoteData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rinaldi on 19/11/2015.
 */
public class BTInterpreter {

    RMSService rmsService;
    String message = "";

    public BTInterpreter(RMSService rmsServiceParam) {
        rmsService = rmsServiceParam;
    }

    public void ReadMessage(String msg) {
        Pattern pattern;
        Matcher matcher;

        message += msg;

        pattern = Pattern.compile("(([A-H])(\\d{2}|\\d{1}),)+");
        matcher = pattern.matcher(message);

        while (matcher.find()) {
            NoteData noteData = new NoteData();
            noteData.Chord = Integer.valueOf(matcher.group(1)) - 17; // ASCii 'A'(65) - '0'(48) = 17
            noteData.Height = Integer.valueOf(matcher.group(2));
            rmsService.musicManager.onNoteReceived(noteData);
        }
    }

}
