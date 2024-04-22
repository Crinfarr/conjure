package com.crinfarr.conjure.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.PatternMatcher;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
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
    private static final int charSize = 28;
    private static final int padding = 5;
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
    public CardFactory(MainActivity self, String name, String mana, String typeline, String oracletext, String[] corner) {
        //
        //DECLARES
        //
        parent = self;
        {
            defaultPaint.setTypeface(ResourcesCompat.getFont(parent.getBaseContext(), R.font.belerenbold));
            defaultPaint.setTextSize(charSize);
            defaultPaint.setColor(Color.argb(0xff, 0x00, 0x00, 0x00));
            defaultPaint.setTextAlign(Paint.Align.LEFT);
            defaultPaint.setLetterSpacing(0.05f);
        }
        TextPaint oraclePaint = new TextPaint() {{
            setTextSize(charSize);
            setLetterSpacing(0.05f);
            setTypeface(ResourcesCompat.getFont(parent.getBaseContext(), R.font.belerenbold));
        }};
        //
        //END DECLARES
        //

        //Mana cost
        StaticLayout mvLayout = StaticLayout.Builder.obtain(
                oracleBuilder(mana),
                0,
                mana.length(),
                oraclePaint,
                (int) Math.ceil(oraclePaint.measureText(mana))
        ).build();
        Bitmap mvBitmap = Bitmap.createBitmap(mvLayout.getWidth(), mvLayout.getHeight()+padding, Bitmap.Config.ARGB_8888);
        Canvas mvCanvas = new Canvas(mvBitmap) {{
            drawARGB(0xff, 0xff, 0xff, 0xff);
        }};
        mvLayout.draw(mvCanvas);

        //Name
        while (defaultPaint.measureText(name) + mvBitmap.getWidth() + padding > 388)
            defaultPaint.setTextScaleX(defaultPaint.getTextScaleX()-0.01f);
        Bitmap titlebarBitmap = Bitmap.createBitmap((int) defaultPaint.measureText(name), charSize +padding, Bitmap.Config.ARGB_8888);

        Canvas titlebarCanvas = new Canvas(titlebarBitmap) {{
            drawARGB(0xff, 0xff, 0xff, 0xff);
        }};
        titlebarCanvas.drawText(name, 0, -defaultPaint.ascent()-3f, defaultPaint);
        defaultPaint.setTextScaleX(1f);

        //Type line
        while (defaultPaint.measureText(typeline) > 388) defaultPaint.setTextScaleX(defaultPaint.getTextScaleX()-0.01f);
        Bitmap typelineBitmap = Bitmap.createBitmap(388, charSize +padding, Bitmap.Config.ARGB_8888);
        Canvas typelineCanvas = new Canvas(typelineBitmap) {{
            drawARGB(0xff, 0xff, 0xff, 0xff);
        }};
        typelineCanvas.drawText(typeline, 0, -defaultPaint.ascent()-3f, defaultPaint);
        defaultPaint.setTextScaleX(1f);

        //Oracle text
        oracletext = oracletext.replaceAll("(\\{(?=\\d))|((?<=\\d)\\})", "");
        StaticLayout oracleLayout = StaticLayout.Builder.obtain(oracleBuilder(oracletext), 0, oracletext.length(), oraclePaint, 388).build();
        Bitmap oracleBitmap = Bitmap.createBitmap(oracleLayout.getWidth(), oracleLayout.getHeight() + padding, Bitmap.Config.ARGB_8888);
        Canvas oracleCanvas = new Canvas(oracleBitmap) {{
            drawARGB(0xff, 0xff, 0xff, 0xff);
        }};
        oracleLayout.draw(oracleCanvas);

        //Corner number/s
        Bitmap cornerBitmap = Bitmap.createBitmap(
                (int) defaultPaint.measureText(String.join("/", corner))+padding,
                charSize + padding,
                Bitmap.Config.ARGB_8888);
        Canvas cornerCanvas = new Canvas(cornerBitmap) {{
            drawARGB(0xff, 0xff, 0xff, 0xff);
        }};
        if (corner.length == 0) {
            Log.i(TAG, "Skipping len=0 draw");
            cornerCanvas.drawARGB(0xff, 0xff, 0xff, 0xff);
        }else if (corner.length == 1) {
            Log.i(TAG, "Drawing loyalty/defense/other");
            cornerCanvas.drawText(corner[0], 0, -defaultPaint.ascent()-3f, defaultPaint);
        } else if (corner.length == 2) {
            Log.i(TAG, "Drawing P/T");
            cornerCanvas.drawText(corner[0]+"/"+corner[1], 0, -defaultPaint.ascent()-3f, defaultPaint);
        } else {
            try {
                throw new RuntimeException("Cannot create card with corner numeric "+ new JSONArray(corner));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        //Combine
        cardBitmap = Bitmap.createBitmap(
                388,
                titlebarBitmap.getHeight() +
                        typelineBitmap.getHeight() +
                        oracleBitmap.getHeight() +
                        cornerBitmap.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        cardCanvas = new Canvas(cardBitmap) {{
            //bg
            drawARGB(0xff, 0xff, 0xff, 0xff);

            //header
            drawBitmap(titlebarBitmap, 0, 0, defaultPaint);
            drawBitmap(mvBitmap, 388-mvBitmap.getWidth(), 0, defaultPaint);

            //type bar
            drawBitmap(typelineBitmap, 0, titlebarBitmap.getHeight(), defaultPaint);
            drawRoundRect(0, titlebarBitmap.getHeight()-2, 385, titlebarBitmap.getHeight()+typelineBitmap.getHeight()-2, 3f, 3f, new Paint() {{
                setStyle(Style.STROKE);
            }});

            //oracle text
            drawBitmap(oracleBitmap, 0, titlebarBitmap.getHeight()+typelineBitmap.getHeight(), defaultPaint);

            //Corner numeric
            drawBitmap(
                    cornerBitmap,
                    388-cornerBitmap.getWidth(),
                    titlebarBitmap.getHeight()+
                            typelineBitmap.getHeight()+
                            oracleBitmap.getHeight(),
                    defaultPaint
            );
            drawRoundRect(
                    388-cornerBitmap.getWidth()-2,
                    titlebarBitmap.getHeight()+
                            typelineBitmap.getHeight()+
                            oracleBitmap.getHeight()-2,
                    386,
                    titlebarBitmap.getHeight()+
                            typelineBitmap.getHeight()+
                            oracleBitmap.getHeight()+
                            cornerBitmap.getHeight()-2,
                    3f,
                    3f,
                    new Paint() {{
                        setStyle(Style.STROKE);
                        setStrokeWidth(2f);
                    }}
            );
        }};
    }
    public Spanned oracleBuilder(String oracletext) {
        SpannableString output = new SpannableString(oracletext);
        Matcher symbolReplacer = Pattern.compile("\\{([\\dwubrgWUBRG](\\/[wubrgpesWUBRGPES])?|\\d+)\\}").matcher(oracletext);
        while (symbolReplacer.find()) {
            Log.i(TAG, "Found "+symbolReplacer.group());
            if (Pattern.compile("\\{\\d+\\}").matcher(symbolReplacer.group()).matches())
                continue;//Don't replace numbers
            Log.d(TAG, "Inserting symbol");
            output.setSpan(
                    new ImageSpan(
                            parent.getBaseContext(),
                            Bitmap.createScaledBitmap(
                                    BitmapFactory.decodeResource(
                                            parent.getResources(),
                                            symMap.get(symbolReplacer.group().replaceAll("[\\{\\}\\/]", "").toLowerCase())
                                    ),
                                    charSize, charSize, true
                            ),
                            DynamicDrawableSpan.ALIGN_BASELINE
                    ),
                    symbolReplacer.start(),
                    symbolReplacer.end(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
            );
        }
        return output;
    }
}
