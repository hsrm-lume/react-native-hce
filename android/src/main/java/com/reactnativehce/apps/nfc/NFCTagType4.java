package com.reactnativehce.apps.nfc;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.reactnativehce.utils.BinaryUtils;
import com.reactnativehce.IHCEApplication;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NFCTagType4 implements IHCEApplication {
  private static final String TAG = "NFCTag";

  private static final byte[] CMD_CAPABILITY_CONTAINER_OK = BinaryUtils.HexStringToByteArray("00A4000C02E103");
  private static final byte[] CMD_READ_CAPABILITY_CONTAINER = BinaryUtils.HexStringToByteArray("00B000000F");
  private static final byte[] CMD_READ_CAPABILITY_CONTAINER_RESPONSE = BinaryUtils.HexStringToByteArray("001120FFFFFFFF0406E104FFFE00FF9000");
  private static final byte[] CMD_NDEF_SELECT_OK = BinaryUtils.HexStringToByteArray("00A4000C02E104");
  private static final byte[] CMD_NDEF_READ_BINARY_NLEN = BinaryUtils.HexStringToByteArray("00B0000002");
  private static final byte[] CMD_NDEF_READ_BINARY = BinaryUtils.HexStringToByteArray("00B0");
  private static final byte[] CMD_OK = BinaryUtils.HexStringToByteArray("9000");
  private static final byte[] CMD_ERROR = BinaryUtils.HexStringToByteArray("6A82");

  private final List<NdefEntity> ndefEntities = new ArrayList<>();
  public byte[] byteArray;
  public byte[] lengthArray;

  private boolean READ_CAPABILITY_CONTAINER_CHECK = false;

  @RequiresApi(api = Build.VERSION_CODES.N)
  public NFCTagType4(Map<String,?> entities) {
    // add all NdefEntities
    entities.forEach((k,v) -> this.ndefEntities.add(new NdefEntity(k,(String) v)));
    // collect data
    NdefRecord[] l = this.ndefEntities.stream().map(x -> x.record).toArray(NdefRecord[]::new);
    NdefMessage m = new NdefMessage(l);
    byteArray = m.toByteArray();
    // calc size of byteArray and store in lengthArray
    lengthArray = fillByteArrayToFixedDimension(
      BigInteger.valueOf(byteArray.length).toByteArray(),
      2
    );
  }

  public boolean assertSelectCommand(byte[] command) {
    byte[] selectCommand = BinaryUtils.HexStringToByteArray("00A4040007D276000085010100");
    return Arrays.equals(command, selectCommand);
  }

  private static byte[] fillByteArrayToFixedDimension(byte[] array, int fixedSize) {
    if (array.length == fixedSize) {
      return array;
    }

    byte[] start = BinaryUtils.HexStringToByteArray("00");
    byte[] filledArray = new byte[start.length + array.length];
    System.arraycopy(start, 0, filledArray, 0, start.length);
    System.arraycopy(array, 0, filledArray, start.length, array.length);
    return fillByteArrayToFixedDimension(filledArray, fixedSize);
  }

  public byte[] processCommand(byte[] command) {
    if (Arrays.equals(CMD_CAPABILITY_CONTAINER_OK, command)) {
      Log.i(TAG, "Requesting CAPABILITY");
      return CMD_OK;
    }

    if (Arrays.equals(CMD_READ_CAPABILITY_CONTAINER, command) && !READ_CAPABILITY_CONTAINER_CHECK) {
      Log.i(TAG, "Requesting CAPABILITY CONTAINER");
      READ_CAPABILITY_CONTAINER_CHECK = true;
      return CMD_READ_CAPABILITY_CONTAINER_RESPONSE;
    }

    if (Arrays.equals(CMD_NDEF_SELECT_OK, command)) {
      Log.i(TAG, "Confirming CMD_NDEF_SELECT");
      return CMD_OK;
    }

    if (Arrays.equals(CMD_NDEF_READ_BINARY_NLEN, command)) {
      Log.i(TAG, "Requesting CMD_NDEF_READ_BINARY_NLEN");



      byte[] response = new byte[lengthArray.length + CMD_OK.length];
      System.arraycopy(lengthArray, 0, response, 0, lengthArray.length);
      System.arraycopy(CMD_OK, 0, response, lengthArray.length, CMD_OK.length);

      READ_CAPABILITY_CONTAINER_CHECK = false;
      return response;
    }

    if (Arrays.equals(CMD_NDEF_READ_BINARY, Arrays.copyOfRange(command, 0, 2))) {
      Log.i(TAG, "Requesting NDEF Content");

      int offset = Integer.parseInt(BinaryUtils.ByteArrayToHexString(Arrays.copyOfRange(command, 2, 4)), 16);
      int length = Integer.parseInt(BinaryUtils.ByteArrayToHexString(Arrays.copyOfRange(command, 4, 5)), 16);

      byte[] fullResponse = new byte[lengthArray.length + byteArray.length];
      System.arraycopy(lengthArray, 0, fullResponse, 0, lengthArray.length);
      System.arraycopy(byteArray,0, fullResponse, lengthArray.length, byteArray.length);

      byte[] slicedResponse = Arrays.copyOfRange(fullResponse, offset, fullResponse.length);

      int realLength = Math.min(slicedResponse.length, length);
      byte[] response = new byte[realLength + CMD_OK.length];

      System.arraycopy(slicedResponse, 0, response, 0, realLength);
      System.arraycopy(CMD_OK, 0, response, realLength, CMD_OK.length);

      READ_CAPABILITY_CONTAINER_CHECK = false;
      return response;
    }

    Log.i(TAG, "Unknown command.");

    return CMD_ERROR;
  }
}
