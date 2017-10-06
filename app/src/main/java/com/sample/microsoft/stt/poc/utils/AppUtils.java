package com.sample.microsoft.stt.poc.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.sample.microsoft.stt.poc.data.POCApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sgarimella on 05/10/17.
 */

public class AppUtils {

    public static String getDeviceUniqueId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void writeToPdf(Activity context, String title, View view) {
        FileOutputStream fOut = null;
        try {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "Notes";
            File folder = new File(path);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String fileName = title + ".pdf";
            final File file = new File(path, fileName);
            file.createNewFile();
            fOut = new FileOutputStream(file);

            PdfDocument document = new PdfDocument();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            view.draw(page.getCanvas());

            document.finishPage(page);
            document.writeTo(fOut);
            document.close();
        } catch (IOException e) {
            Log.i("error", e.getLocalizedMessage());
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
