package com.reactnativehce.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import com.reactnativehce.IHCEApplication;
import com.reactnativehce.apps.nfc.NFCTagType4;
import com.reactnativehce.utils.BinaryUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class CardService extends HostApduService {
    private static final String TAG = "CardService";
    private static byte[] SELECT_APDU_HEADER = BinaryUtils.HexStringToByteArray("00A40400");
    private static final byte[] CMD_OK = BinaryUtils.HexStringToByteArray("9000");
    private static final byte[] CMD_ERROR = BinaryUtils.HexStringToByteArray("6A82");

    private ArrayList<IHCEApplication> registeredHCEApplications = new ArrayList<IHCEApplication>();
    private IHCEApplication currentHCEApplication = null;

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
      if (currentHCEApplication != null) {
        return currentHCEApplication.processCommand(commandApdu);
      }

      byte[] header = Arrays.copyOfRange(commandApdu, 0, 4);

      if (Arrays.equals(SELECT_APDU_HEADER, header)) {
        for (IHCEApplication app : registeredHCEApplications) {
          if (app.assertSelectCommand(commandApdu)) {
            currentHCEApplication = app;
            return CMD_OK;
          }
        }
      }

      return CMD_ERROR;
    }

    @Override
    public void onCreate() {
      Log.d(TAG, "Starting service");

      SharedPreferences prefs = getApplicationContext()
        .getSharedPreferences("hce", Context.MODE_PRIVATE);

      prefs.getAll().forEach((k,v) -> {
        registeredHCEApplications.add(new NFCTagType4(k, v));
      });
    }

    @Override
    public void onDeactivated(int reason) {
      Log.d(TAG, "Finishing service: " + reason);
    }
}
