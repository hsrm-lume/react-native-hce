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

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public NdefEntity(String type, String content) {

    if (type.equals("text")) {
      record = createTextRecord(content);
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

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public static NdefRecord createTextRecord(String text) {
    return NdefRecord.createTextRecord(null, text);
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public static NdefRecord createUrlRecord(String text) {
    return NdefRecord.createUri(text);
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public static NdefRecord createJsonRecord(String text) {
    return NdefRecord.createMime(
      "application/json",
      language.getBytes(StandardCharsets.US_ASCII),
      text.getBytes(StandardCharsets.UTF_8)
    );
  }

  public static NdefRecord createAppRecord(String text) {
    return NdefRecord.createApplicationRecord(text);
  }
}
