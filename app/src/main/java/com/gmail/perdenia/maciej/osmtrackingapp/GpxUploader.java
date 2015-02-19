package com.gmail.perdenia.maciej.osmtrackingapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

// TODO pomyśleć nad użyciem Volley'a
// TODO pomyśleć na użyciem OAuth

public class GpxUploader {

    public static final String TAG = "GpxUploader";

    public static final String UPLOAD_GPX_URL = "http://api.openstreetmap.org/api/0.6/gpx/create";
    public static final String UPLOAD_GPX_TEST_URL =
            "http://api06.dev.openstreetmap.org/api/0.6/gpx/create";
    private static final int BUFFER_SIZE = 65535;
    private static final String LINE_END = "\r\n";

    private String mBoundary;
    private Context mContext;

    private AsyncResponse mDelegate;

    public GpxUploader(Context context) {
        mBoundary = "AiR" + System.currentTimeMillis() + "AiR";
        mContext = context;
        mDelegate = null;
    }

    public void setDelegate (AsyncResponse delegate) {
        mDelegate = delegate;
    }

    public void doUploadGpxTask(String username, String password, String filename,
                                String description, String tags) {
        new UploadGpxTask().execute(username, password, filename, description, tags);
    }

    private Wrapper uploadGpx(
            String username, String password, String filename, String description, String tags) {
        Wrapper wrapper = new Wrapper();
        try {
            String filePath = mContext.getFilesDir().toString() + "/" + filename + ".gpx";
            File gpxFile = new File(filePath);
            Log.i(TAG, "uploading " + gpxFile.getAbsolutePath() + " to openstreetmap.org");

            HttpURLConnection con = (HttpURLConnection) (new URL(UPLOAD_GPX_URL)).openConnection();
            /*HttpURLConnection con = (HttpURLConnection) (
                    new URL(UPLOAD_GPX_TEST_URL)).openConnection();*/
            con.setRequestMethod("POST");
            con.setConnectTimeout(15000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            Log.i(TAG, "url: " + con.getURL());

            String credentials = username + ":" + password;
            final String basicAuth = "Basic " + Base64.encodeToString(credentials.getBytes(),
                    Base64.NO_WRAP);

            con.addRequestProperty("Authorization", basicAuth);
            con.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + mBoundary);
            con.addRequestProperty("Connection", "close");
            con.addRequestProperty("Expect", "");

            con.connect();

            DataOutputStream os  = new DataOutputStream(new BufferedOutputStream(
                    con.getOutputStream()));

            addFilePart(os, "file", gpxFile);
            addFormPart(os, "description", description);
            addFormPart(os, "tags", tags);
            addFormPart(os, "public", "1");
            addFormPart(os, "visibility", "identifiable");

            os.writeBytes("--" + mBoundary + "--" + LINE_END);
            os.flush();

            int responseCode = con.getResponseCode();
            wrapper.mResponseCode = responseCode;
            String responseMessage = con.getResponseMessage();
            wrapper.mResponseMessage = responseMessage;
            Log.i(TAG, "return code: " + responseCode + " " + responseMessage);
            if (responseCode != 200) {
                if (con.getHeaderField("Error") != null)
                    responseMessage += "\n" + con.getHeaderField("Error");
                Log.e(TAG, responseCode + " " + responseMessage);
            }
            os.close();
            con.disconnect();
        } catch (MalformedURLException e) {
            Log.e(TAG, "uploadGpx: MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "uploadGpx: IOException");
            e.printStackTrace();
        }
        return wrapper;
    }

    public void addFilePart(DataOutputStream os, String name, File gpxFile) throws IOException {
        os.writeBytes("--" + mBoundary + LINE_END);
        os.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" +
                gpxFile.getName() + "\"" + LINE_END);
        os.writeBytes("Content-Type: application/octet-stream" + LINE_END);
        os.writeBytes(LINE_END);

        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        InputStream is = new BufferedInputStream(new FileInputStream(gpxFile));
        while((read = is.read(buffer)) >= 0) {
            os.write(buffer, 0, read);
            os.flush();
        }
        is.close();
        os.writeBytes(LINE_END);
    }

    public void addFormPart(DataOutputStream os, String name, String value) throws IOException {
        os.writeBytes("--" + mBoundary + LINE_END);
        os.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_END);
        os.writeBytes(LINE_END);
        os.writeBytes(value + LINE_END);
    }

    private class UploadGpxTask extends AsyncTask<String, Void, Wrapper> {

        protected Wrapper doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String filename = params[2];
            String description = params[3];
            String tags = params[4];

            return uploadGpx(username, password, filename, description, tags);
        }

        protected void onPostExecute(Wrapper result) {
            mDelegate.processFinish(result);
        }
    }

    public class Wrapper {
        public int mResponseCode;
        public String mResponseMessage;
    }

    public interface AsyncResponse {
        void processFinish(Wrapper output);
    }
}
