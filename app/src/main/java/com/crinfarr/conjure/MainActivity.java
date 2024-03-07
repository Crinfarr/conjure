package com.crinfarr.conjure;

import androidx.appcompat.app.AppCompatActivity;
import com.iposprinter.iposprinterservice.*;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String logTag = "PrinterTest";
    protected PrinterWrapper printer;
    protected Button printButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //!
        //!Declaration Block
        //!
        printButton = findViewById(R.id.printbutton);
        //!
        //!End Declaration Block
        //!
        printer = new PrinterWrapper(this, new PrinterWrapper.printWrapperReady() {
            @Override
            public void onReady() {
                printButton.setEnabled(true);
            }

            @Override
            public void onDisconnect() {
            }

            @Override
            public void onError(RuntimeException error) {
                throw new RuntimeException(error);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        printButton.setOnClickListener(v -> {
            try {
                printer.testPrint();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

}