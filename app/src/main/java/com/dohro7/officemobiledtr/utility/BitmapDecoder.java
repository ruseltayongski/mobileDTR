package com.dohro7.officemobiledtr.utility;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class BitmapDecoder {

    public static Bitmap screenShotView(Activity activity) {
        View view = activity.getWindow().getDecorView().getRootView();
        view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY));
        view.layout((int) view.getX(), (int) view.getY(), (int) view.getX() + view.getMeasuredWidth(), (int) view.getY() + view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }


    public static String convertBitmapToString(String filePath) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(filePath), null, options);
            bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream); //compress to which format you want.
            byte[] byte_arr = stream.toByteArray();
            return Base64.encodeToString(byte_arr, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            return "";
        }
    }

}

