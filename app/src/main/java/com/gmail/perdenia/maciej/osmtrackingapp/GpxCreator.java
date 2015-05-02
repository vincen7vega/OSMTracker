package com.gmail.perdenia.maciej.osmtrackingapp;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

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
            Context context, String filename, String name, ArrayList<WayPoint> trackPoints) {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_END
                + "<gpx version=\"1.0\"" + LINE_END
                + "creator=\"" + context.getResources().getString(R.string.app_name)
                + "\"" + LINE_END
                + "xmlns=\"http://www.topografix.com/GPX/1/0\">" + LINE_END
                + "<name>" + filename + "</name>" + LINE_END
                + "<trk><name>" + name + "</name><trkseg>" + LINE_END;
        for (WayPoint tp : trackPoints) {
            result += "<trkpt lat=\"" + tp.getLatitude() + "\" lon=\"" + tp.getLongitude() + "\">"
                            + "<ele>" + tp.getAltitude() + "</ele>"
                            + "<time>" + tp.getTimeString() + "</time></trkpt>" + LINE_END;
        }
        result += "</trkseg></trk>" + LINE_END + "</gpx>";

        return result.toCharArray();
    }

    public static char[] createGpxWayPoint(
            Context context, String filename, String name, WayPoint wayPoint) {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_END
                + "<gpx version=\"1.0\"" + LINE_END
                + "creator=\"" + context.getResources().getString(R.string.app_name)
                + "\"" + LINE_END
                + "xmlns=\"http://www.topografix.com/GPX/1/0\">" + LINE_END
                + "<name>" + filename + "</name>" + LINE_END
                + "<trk><trkseg>" + LINE_END
                + "<trkpt lat=\"" + wayPoint.getLatitude() + "\" lon=\"" + wayPoint.getLongitude()
                + "\">" + "<time>" + wayPoint.getTimeString() + "</time></trkpt>"
                + "</trkseg></trk>" + LINE_END
                + "<wpt lat=\"" + wayPoint.getLatitude() + "\" lon=\"" + wayPoint.getLongitude()
                + "\">" + LINE_END + "<ele>" + wayPoint.getAltitude() + "</ele>" + LINE_END
                + "<time>" + wayPoint.getTimeString() + "</time>" + LINE_END
                + "<name>" + name + "</name>" + LINE_END
                + "</wpt>" + LINE_END + "</gpx>";

        return result.toCharArray();
    }

    public static char[] saveGpxTrackOnInternalStorage(
            Context context, String filename, String name, ArrayList<WayPoint> trackPoints) {
        char[] result = null;
        try {
            result = createGpxTrack(context, filename, name, trackPoints);
            FileOutputStream fileOutputStream = context.openFileOutput(
                    filename + ".gpx", Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(result);
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "saveGpxTrackOnInternalStorage: FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "saveGpxTrackOnInternalStorage: IOException");
            e.printStackTrace();
        }
        return result;
    }

    public static void saveGpxTrackOnExternalStorage(
            Context context, String filename, String name, ArrayList<WayPoint> trackPoints) {
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
            File gpxFilesDir = new File(root + "/Gpx Files");
            gpxFilesDir.mkdirs();
            File gpxFile = new File(gpxFilesDir, filename + ".gpx");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(gpxFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                outputStreamWriter.write(createGpxTrack(context, filename, name, trackPoints));
                outputStreamWriter.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "saveGpxTrackOnExternalStorage: FileNotFoundException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "saveGpxTrackOnExternalStorage: IOException");
                e.printStackTrace();
            }
        }
    }

    public static char[] saveGpxWayPointOnInternalStorage(
            Context context, String filename, String name, WayPoint wayPoint) {
        char[] result = null;
        try {
            result = createGpxWayPoint(context, filename, name, wayPoint);
            FileOutputStream fileOutputStream = context.openFileOutput(
                    filename + ".gpx", Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(result);
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "saveGpxWayPointOnInternalStorage: FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "saveGpxWayPointOnInternalStorage: IOException");
            e.printStackTrace();
        }

        return result;
    }

    public static void saveGpxWayPointOnExternalStorage(
            Context context, String filename, String name, WayPoint wayPoint) {
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
            File gpxFilesDir = new File(root + "/Gpx Files");
            gpxFilesDir.mkdirs();
            File gpxFile = new File(gpxFilesDir, filename + ".gpx");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(gpxFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                outputStreamWriter.write(createGpxWayPoint(context, filename, name, wayPoint));
                outputStreamWriter.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "saveGpxWayPointOnExternalStorage: FileNotFoundException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "saveGpxWayPointOnExternalStorage: IOException");
                e.printStackTrace();
            }
        }
    }
}
