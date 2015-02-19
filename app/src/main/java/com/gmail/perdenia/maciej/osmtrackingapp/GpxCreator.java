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

    public static  final String TAG = "GpxCreator";
    public static final String LINE_END = "\n";

    public static void saveAsGpxOnInternalStorage(
            Context context, String filename, ArrayList<TrackPoint> trackPoints) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(
                    filename + ".gpx", Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_END
                            + "<gpx version=\"1.0\"" + LINE_END
                            + "creator=\"" + context.getResources().getString(R.string.app_name)
                            + "\"" + LINE_END
                            + "xmlns=\"http://www.topografix.com/GPX/1/0\">" + LINE_END
                            + "<name>" + filename + "</name>" + LINE_END
                            + "<trk><name>" + filename + "</name><trkseg>" + LINE_END);

            for (TrackPoint tp : trackPoints) {
                outputStreamWriter.write(
                        "<trkpt lat=\"" + tp.getLat() + "\" lon=\"" + tp.getLon() + "\">"
                                + "<time>" + tp.getTime() + "</time></trkpt>");
            }

            outputStreamWriter.write(
                    "</trkseg></trk>" + LINE_END + "</gpx>");

            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "saveAsGpxOnInternalStorage: FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "saveAsGpxOnInternalStorage: IOException");
            e.printStackTrace();
        }
    }

    public static void saveAsGpxOnExternalStorage(
            Context context, String filename, ArrayList<TrackPoint> trackPoints) {
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
            File gpxFilesDir = new File(root + "/OSM Gpx Files");
            gpxFilesDir.mkdirs();
            File gpxFile = new File(gpxFilesDir, filename + ".gpx");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(gpxFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

                outputStreamWriter.write(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_END
                                + "<gpx version=\"1.0\"" + LINE_END
                                + "creator=\"" + context.getResources().getString(R.string.app_name)
                                + "\"" + LINE_END
                                + "xmlns=\"http://www.topografix.com/GPX/1/0\">" + LINE_END
                                + "<name>" + filename + "</name>" + LINE_END
                                + "<trk><name>" + filename + "</name><trkseg>" + LINE_END);

                for (TrackPoint tp : trackPoints) {
                    outputStreamWriter.write(
                            "<trkpt lat=\"" + tp.getLat() + "\" lon=\"" + tp.getLon() + "\">"
                                    + "<time>" + tp.getTime() + "</time></trkpt>");
                }

                outputStreamWriter.write(
                        "</trkseg></trk>" + LINE_END + "</gpx>");

                outputStreamWriter.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "saveAsGpxOnExternalStorage: FileNotFoundException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "saveAsGpxOnExternalStorage: IOException");
                e.printStackTrace();
            }
        }
    }

}
