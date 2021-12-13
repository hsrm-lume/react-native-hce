package com.reactnativehce.apps.nfc;

import android.nfc.NdefRecord;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import java.nio.charset.StandardCharsets;
import com.reactnativehce.utils.BinaryUtils;

public class NdefEntity {

  public static String TAG = "NdefEntity";
  NdefRecord record;

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public NdefEntity(String type, String content) {

    if (type.equals("text")) {
      record = createTextRecord("en", content);
    } else if (type.equals("url")) {
      record = createUrlRecord(content);
    } else if (type.equals("json")) {
      record = createJsonRecord(content);
    } else if (type.equals("app")) {
      record = createAppRecord(content);
    } else {
      throw new IllegalArgumentException("Wrong NFC tag content type");
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public static NdefRecord createTextRecord(String language, String text) {
    byte[] languageBytes;
    byte[] textBytes;

    languageBytes = language.getBytes(StandardCharsets.US_ASCII);
    textBytes = text.getBytes(StandardCharsets.UTF_8);

    byte[] recordPayload = new byte[1 + (languageBytes.length & 0x03F) + textBytes.length];

    int zeroVal = languageBytes.length & 0x03F;
    recordPayload[0] = (byte) zeroVal;

    System.arraycopy(languageBytes, 0, recordPayload, 1, languageBytes.length & 0x03F);
    System.arraycopy(textBytes, 0, recordPayload,1 + (languageBytes.length & 0x03F), textBytes.length);

    Log.i(TAG, BinaryUtils.ByteArrayToHexString(recordPayload));

    return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, null, recordPayload);
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public static NdefRecord createUrlRecord(String text) {
    byte[] textBytes;

    textBytes = text.getBytes(StandardCharsets.UTF_8);

    byte[] recordPayload = new byte[1 + textBytes.length];

    System.arraycopy(textBytes,0, recordPayload,1, textBytes.length);

    Log.i(TAG, BinaryUtils.ByteArrayToHexString(recordPayload));

    return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, null, recordPayload);
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public static NdefRecord createJsonRecord(String text) {
    byte[] textBytes;

    textBytes = text.getBytes(StandardCharsets.UTF_8);

    byte[] recordPayload = new byte[1 + textBytes.length];

    System.arraycopy(textBytes,0, recordPayload,1, textBytes.length);

    Log.i(TAG, BinaryUtils.ByteArrayToHexString(recordPayload));

    return NdefRecord.createMime("application/json", textBytes);
  }

  public static NdefRecord createAppRecord(String text) {
    return NdefRecord.createApplicationRecord(text);
  }
}
