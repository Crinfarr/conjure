package com.crinfarr.conjure;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.crinfarr.conjure.helpers.BitmapHelpers;
import com.iposprinter.iposprinterservice.IPosPrinterCallback;
import com.iposprinter.iposprinterservice.IPosPrinterService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PrinterWrapper {
    private final MainActivity parent;
    private final Map<String, Bitmap> manamap = new HashMap<String, Bitmap>();
    private static final String TAG = "PrinterWrapper";
    private static final IPosPrinterCallback defaultCallback = new IPosPrinterCallback.Stub() {
        @Override
        public void onRunResult(boolean isSuccess) throws RemoteException {

        }
        @Override
        public void onReturnString(String result) throws RemoteException {}
    };
    private static final Intent printIntent = new Intent().setPackage("com.iposprinter.iposprinterservice").setAction("com.iposprinter.iposprinterservice.IPosPrintService");
    private final ServiceConnection serviceConnection;
    private IPosPrinterService printerService;
    public interface printWrapperReady {
        void onReady();
        void onDisconnect();
        void onError(RuntimeException error);
    }
    public PrinterWrapper(MainActivity mainInstance, printWrapperReady callback) {
        parent = mainInstance;
        {
            manamap.put("W", BitmapFactory.decodeResource(parent.getResources(), R.drawable.mana_w));
            manamap.put("U", BitmapFactory.decodeResource(parent.getResources(), R.drawable.mana_u));
            manamap.put("B", BitmapFactory.decodeResource(parent.getResources(), R.drawable.mana_b));
            manamap.put("R", BitmapFactory.decodeResource(parent.getResources(), R.drawable.mana_r));
            manamap.put("G", BitmapFactory.decodeResource(parent.getResources(), R.drawable.mana_g));
        }
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                printerService = IPosPrinterService.Stub.asInterface(service);
                Log.i(TAG, "Connected print service");
                try {
                    Log.i(TAG, "Attempting to initialize printer");
                    printerService.printerInit(new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) throws RemoteException {
                            Log.i(TAG, "Printer initialized");
                            callback.onReady();
                        }

                        @Override
                        public void onReturnString(String result) throws RemoteException {

                        }
                    });
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                callback.onDisconnect();
            }

        };
        parent.bindService(printIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Prints text to the receipt
     * @param text String to print
     * @param fontSize Must be 16, 24, 32, or 48; defaults to 24 if a bad value is specified
     * @param alignment 0: left, 1: center, 2: right
     */
    public void printText(String text, int fontSize, int alignment) {
        try {
            printerService.PrintSpecFormatText(text, "ST", fontSize, alignment, defaultCallback);
        }
        catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param bmp Bitmap instance to print
     * @param size Bit depth 1-16 [*24]
     * @param alignment 0: left, 1: center, 2: right
     */
    public void printImage(Bitmap bmp, int size, int alignment) {
        try {
            printerService.printBitmap(alignment, size, bmp, defaultCallback);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public String getStatus() throws RemoteException {
        switch (printerService.getPrinterStatus()) {
            case 0:
                return "PRINTER_NORMAL";
            case 1:
                return "PRINTER_PAPERLESS";
            case 2:
                return "PRINTER_THP_HIGH_TEMPERATURE";
            case 3:
                return "PRINTER_MOTOR_HIGH_TEMPERATURE";
            case 4:
                return "PRINTER_IS_BUSY";
            case 5:
                return "PRINTER_ERROR_UNKNOWN";
            default:
                throw new RemoteException("Invalid status");
        }
    }
    public void testPrint() throws RemoteException{
        for (int i = 1; i <= 10; i++) {
            printerService.setPrinterPrintDepth(i, defaultCallback);
            printText("Print depth "+i, 24, 1);
            feed(false);
        }
        printerService.setPrinterPrintDepth(6, defaultCallback);
        feed(false);
        for (String key: new String[]{"W", "U", "B", "R", "G"}) {
            printImage(Objects.requireNonNull(manamap.get(key)), 3, 1);
        }
        feed(false);
        for (int fontsize: new int[]{16, 24, 32, 48}) {
            printText(fontsize+"px", fontsize, 1);
            feed(false);
        }
        printText("Status:\n"+getStatus(), 24, 1);
        feed(true);
    }
    public void feed(Boolean isFinal) {
        try {
            printerService.printerPerformPrint((isFinal)?120:0, defaultCallback);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
