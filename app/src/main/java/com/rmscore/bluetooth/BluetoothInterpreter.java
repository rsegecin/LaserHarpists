package com.rmscore.bluetooth;

import com.rmscore.RMSService;
import com.rmscore.datamodels.NoteData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rinaldi on 19/11/2015.
 * This Interpreter is already within the same thread as the service
 */
public class BluetoothInterpreter {

    RMSService rmsService;
    String message = "";

    public BluetoothInterpreter(RMSService rmsServiceParam) {
        rmsService = rmsServiceParam;
    }

    public void ReadMessage(String msg) {
        Pattern pattern;
        Matcher matcher;

        message += msg;

        pattern = Pattern.compile("(([A-H])(i|o)(\\d{2}|\\d{1}),)+");
        matcher = pattern.matcher(message);

        while (matcher.find()) {
            NoteData noteData = new NoteData();
            noteData.Chord = Integer.valueOf(matcher.group(1)) - 17; // ASCii 'A'(65) - '0'(48) = 17
            noteData.NoteDirection = (matcher.group(2) == "i") ? NoteData.eNoteDirection.Input : NoteData.eNoteDirection.Output;
            noteData.Height = Integer.valueOf(matcher.group(3));
            rmsService.musicManager.onNoteReceived(noteData);
        }
    }

}
