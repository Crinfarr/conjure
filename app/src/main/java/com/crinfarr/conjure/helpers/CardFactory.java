package com.crinfarr.conjure.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.crinfarr.conjure.MainActivity;
import com.crinfarr.conjure.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.NotImplementedError;

public class CardFactory {
    private static final String TAG = "CardFactory";
    private static final int charHeight = 30;
    private static final HashMap<String, Integer> symMap = new HashMap<String, Integer>() {{
        put("w", R.drawable.mana_w);
        put("u", R.drawable.mana_u);
        put("b", R.drawable.mana_b);
        put("r", R.drawable.mana_r);
        put("g", R.drawable.mana_g);
    }};
    private final MainActivity parent;
    public Bitmap cardBitmap;
    protected Canvas cardCanvas;
    private static final Paint defaultPaint = new Paint();
    public CardFactory(MainActivity self, String name, String mana) {
        //
        //DECLARES
        //
        parent = self;
        {
            defaultPaint.setTypeface(ResourcesCompat.getFont(parent.getBaseContext(), R.font.belerenbold));
            defaultPaint.setTextSize(charHeight);
            defaultPaint.setColor(Color.argb(0xff, 0x00, 0x00, 0x00));
            defaultPaint.setTextAlign(Paint.Align.LEFT);
        }
        cardBitmap = Bitmap.createBitmap(388, charHeight, Bitmap.Config.ARGB_8888);
        cardCanvas = new Canvas(cardBitmap) {{
            drawARGB(0xff, 0xff, 0xff, 0xff);
        }};
        //
        //END DECLARES
        //
        Bitmap titlebarBitmap = Bitmap.createBitmap((int) defaultPaint.measureText(name), charHeight, Bitmap.Config.ARGB_8888);
        Bitmap manacostBitmap = manaCost(mana);
        Canvas titlebarCanvas = new Canvas(titlebarBitmap) {{
            drawARGB(0xff, 0xff, 0xff, 0xff);
        }};

        titlebarCanvas.drawText(name, 0, -defaultPaint.ascent()-0.5f, defaultPaint);
        cardCanvas.drawBitmap(titlebarBitmap, 0, 0, defaultPaint);
        cardCanvas.drawBitmap(manacostBitmap, 388-manacostBitmap.getWidth(), 0, defaultPaint);
    }
    public Bitmap manaCost(String manacost){
        Stack<String> manaSymbols = new Stack<>();
        float strWidth = 0;
        Matcher manamatcher = Pattern.compile("(^\\d+)|([wubrgWUBRG](\\/[wubrgWUBRGpPsS])?)").matcher(manacost);
        while (manamatcher.find())
            manaSymbols.push(manamatcher.group());
        for (String symbol : manaSymbols) {
            if (Pattern.compile("\\d").matcher(symbol).matches()) {
                strWidth += defaultPaint.measureText(symbol);
            } else {
                strWidth += charHeight;
            }
        }
        Bitmap costmap = Bitmap.createBitmap((int) strWidth, 48, Bitmap.Config.ARGB_8888);
        Canvas costCanvas = new Canvas(costmap){{
            drawARGB(0xff, 0xff, 0xff, 0xff);
        }};
        float LOffset = 0;
        for (String symbol : manaSymbols) {
            if (Pattern.compile("\\d").matcher(symbol).matches()) {
                costCanvas.drawText(symbol, LOffset, -defaultPaint.ascent()-0.5f, defaultPaint);
                LOffset += defaultPaint.measureText(symbol);
            } else {
                if (symMap.get(symbol.toLowerCase()) == null) {
                    throw new NotImplementedError("Invalid symbol");
                }
                costCanvas.drawBitmap(
                        Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                                parent.getResources(),
                                symMap.get(symbol.toLowerCase())
                        ), charHeight, charHeight, true),
                        LOffset,
                        0,
                        defaultPaint);
                LOffset += charHeight;
            }
        }
        return costmap;
    }
}
