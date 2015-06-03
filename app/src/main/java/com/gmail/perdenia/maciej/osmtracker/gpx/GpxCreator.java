package com.gmail.perdenia.maciej.osmtracker.gpx;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.gmail.perdenia.maciej.osmtracker.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class GpxCreator {

    public static  final String TAG = GpxCreator.class.getSimpleName();
    public static final String LINE_END = "\r\n";

    public static char[] createGpxTrack(
            Context context, String author, String filename, String name, String description,
            ArrayList<WayPoint> trackPoints) {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_END +
                "<gpx version=\"1.0\"" + LINE_END +
                "creator=\"" + context.getResources().getString(R.string.app_name) + "\"" + LINE_END +
                "xmlns=\"http://www.topografix.com/GPX/1/0\">" + LINE_END +
                "<author>" + author + "</author>" + LINE_END +
                "<name>" + filename + "</name>" + LINE_END +
                "<trk>" + LINE_END + "<name>" + name + "</name>" + LINE_END +
                "<desc>" + description + "</desc>" + LINE_END +
                "<trkseg>" + LINE_END;
        for (WayPoint tp : trackPoints) {
            result += "<trkpt lat=\"" + tp.getLatitude() + "\" lon=\"" + tp.getLongitude() + "\">" +
                    "<ele>" + tp.getElevation() + "</ele>" +
                    "<time>" + tp.getTimeString() + "</time></trkpt>" + LINE_END;
        }
        result += "</trkseg>" + LINE_END + "</trk>" + LINE_END + "</gpx>";

        return result.toCharArray();
    }

    public static char[] createGpxWayPoint(
            Context context, String author, String filename, String name, String description,
            WayPoint wpt) {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_END +
                "<gpx version=\"1.0\"" + LINE_END +
                "creator=\"" + context.getResources().getString(R.string.app_name) + "\"" + LINE_END +
                "xmlns=\"http://www.topografix.com/GPX/1/0\">" + LINE_END +
                "<author>" + author + "</author>" + LINE_END +
                "<name>" + filename + "</name>" + LINE_END +
                "<trk><trkseg>" + LINE_END +
                "<trkpt lat=\"" + wpt.getLatitude() + "\" lon=\"" + wpt.getLongitude() + "\">" +
                "<time>" + wpt.getTimeString() + "</time></trkpt>" + LINE_END +
                "</trkseg></trk>" + LINE_END +
                "<wpt lat=\"" + wpt.getLatitude() + "\" lon=\"" + wpt.getLongitude() + "\">" +
                LINE_END +
                "<ele>" + wpt.getElevation() + "</ele>" + LINE_END +
                "<time>" + wpt.getTimeString() + "</time>" + LINE_END +
                "<name>" + name + "</name>" + LINE_END +
                "<desc>" + description + "</desc>" + LINE_END +
                "</wpt>" + LINE_END + "</gpx>";

        return result.toCharArray();
    }

    public static void saveOnInternalStorage(Context context, String filename, String gpx) {
        try {
            FileOutputStream fileOutputStream =
                    context.openFileOutput(filename + ".gpx", Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(gpx);
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Złapano wyjątek (FileNotFoundException)");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Złapano wyjątek (IOException)");
            e.printStackTrace();
        }
    }

    public static void saveOnExternalStorage(String filename, String gpx) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            String root;
            if (Build.VERSION.SDK_INT >= 19) {
                root = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        .toString();
            } else {
                root = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .toString();
            }
            File gpxFilesDir = new File(root + "/GPX Files");
            //noinspection ResultOfMethodCallIgnored
            gpxFilesDir.mkdirs();
            File gpxFile = new File(gpxFilesDir, filename + ".gpx");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(gpxFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                outputStreamWriter.write(gpx);
                outputStreamWriter.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Złapano wyjątek (FileNotFoundException)");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "Złapano wyjątek (IOException)");
                e.printStackTrace();
            }
        }
    }
}
