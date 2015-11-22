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

        //Do34
        pattern = Pattern.compile("([A-H])(i|o)(\\d{2}|\\d{1})");
        matcher = pattern.matcher(message);

        while (matcher.find()) {
            NoteData noteData = new NoteData();
            noteData.Chord = ((int) matcher.group(1).charAt(0)) - ((int) 'A');
            noteData.NoteDirection = (matcher.group(2).charAt(0) == 'i') ? NoteData.eNoteDirection.Input : NoteData.eNoteDirection.Output;
            noteData.Height = Integer.valueOf(matcher.group(3));
            rmsService.musicManager.onNoteReceived(noteData);
            message = message.substring(matcher.group(0).length() + matcher.start(), message.length());
        }
    }

}
