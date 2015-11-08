package com.rmscore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import com.rmscore.laserharpists.BuildConfig;

/**
 * Auxiliary methods
 * Created by sash0k on 29.01.14.
 */
public class Utils {
    static final String TAG = "LaserHarpists";

    /**
     * The general method of output debugging messages to the log
     */
    public static void log(String message) {
        if (BuildConfig.DEBUG) {
            if (message != null) Log.i(TAG, message);
        }
    }
    // ============================================================================

    /**
     * The general method of output debugging messages to the log
     */
    public static void logError(String message) {
        if (BuildConfig.DEBUG) {
            if (message != null) Log.e(TAG, message);
        }
    }
    // ============================================================================


    /**
     * Convert hex- string teams to display
     */
    public static String printHex(String hex) {
        StringBuilder sb = new StringBuilder();
        int len = hex.length();
        try {
            for (int i = 0; i < len; i += 2) {
                sb.append("0x").append(hex.substring(i, i + 2)).append(" ");
            }
        } catch (NumberFormatException e) {
            log("printHex NumberFormatException: " + e.getMessage());

        } catch (StringIndexOutOfBoundsException e) {
            log("printHex StringIndexOutOfBoundsException: " + e.getMessage());
        }
        return sb.toString();
    }
    // ============================================================================


    /**
     * Translation imposed ASCII- command to hex byte .
     * @param hex - command
     * @return - byte array command
     */
    public static byte[] toHex(String hex) {
        int len = hex.length();
        byte[] result = new byte[len];
        try {
            int index = 0;
            for (int i = 0; i < len; i += 2) {
                result[index] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
                index++;
            }
        } catch (NumberFormatException e) {
            log("toHex NumberFormatException: " + e.getMessage());

        } catch (StringIndexOutOfBoundsException e) {
            log("toHex StringIndexOutOfBoundsException: " + e.getMessage());
        }
        return result;
    }
    // ============================================================================


    /**
     * The method merges two arrays into one
     */
    public static byte[] concat(byte[] A, byte[] B) {
        byte[] C = new byte[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }
    // ============================================================================


    /**
     * Getting id saved in the game sound set
     */
    public static String getPrefence(Context context, String item) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(item, TAG);
    }
    // ============================================================================


    /**
     * Receiving the flag of the settings
     */
    public static boolean getBooleanPrefence(Context context, String tag) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(tag, true);
    }
    // ============================================================================


    /**
     * Class filter input fields
     */
    // ============================================================================
    public static class InputFilterHex implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))
                        && source.charAt(i) != 'A' && source.charAt(i) != 'D'
                        && source.charAt(i) != 'B' && source.charAt(i) != 'E'
                        && source.charAt(i) != 'C' && source.charAt(i) != 'F'
                        ) {
                    return "";
                }
            }
            return null;
        }
    }
    // ============================================================================
}
