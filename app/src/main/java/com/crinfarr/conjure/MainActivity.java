package com.crinfarr.conjure;

import androidx.appcompat.app.AppCompatActivity;

import com.crinfarr.conjure.helpers.CardFactory;
import com.iposprinter.iposprinterservice.*;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static final String logTag = "PrinterTest";
    protected PrinterWrapper printer;
    protected Button printButton;
    protected Button showButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //!
        //!Declaration Block
        //!
        printButton = findViewById(R.id.printbutton);
        showButton = findViewById(R.id.showButton);
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
//        Bitmap card = new CardFactory(
//                this,
//                "Progenitus",
//                "wwuubbrrgg",
//                "Legendary Creature - Hydra Avatar",
//                "Protection from everything\n"+
//                        "\n"+
//                        "If Progenitus would be put into a graveyard from anywhere, reveal Progenitus and shuffle it into its owner’s library instead.",
//                new String[]{"10", "10"}
//        ).cardBitmap;
        Bitmap card = new CardFactory(
                this,
                "Anzrag, the Quake-Mole",
                "2rg",
                "Legendary Creature - Mole God",
                "Whenever Anzrag, the Quake-Mole becomes blocked, untap each creature you control. After this phase, there is an additional combat phase.\n" +
                        "\n" +
                        "{3}{R}{R}{G}{G}: Anzrag must be blocked each combat this turn if able.",
                new String[]{"8", "4"}
        ).cardBitmap;
        printButton.setOnClickListener(v -> {
            printer.printImage(card, 16, 0);
            printer.feed(true);
        });
        showButton.setOnClickListener(v -> {
            ImageView imgView = findViewById(R.id.preview);
            imgView.setImageBitmap(card);
        });
    }

}