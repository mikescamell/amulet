package scamell.michael.amulet;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RetrieveHTTPDataAsync extends AsyncTask<String, Void, String> {

    private final OnRetrieveHttpData listener;

    public RetrieveHTTPDataAsync(OnRetrieveHttpData listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(String httpData) {
        listener.onRetrieveTaskCompleted(httpData);
    }

    @Override
    protected String doInBackground(String... urls) {
        String responseData = null;
        if (urls.length > 0) {
            responseData = getHTTPData(urls[0]);
        }
        return responseData;
    }

    protected String getHTTPData(String urls) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(urls);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                return "SERVER_ERROR_RESPONSE";
            }
        } catch (Exception e) {
            Log.e("getHTTPData", "Couldn't get data");
            return "ERROR_CONTACTING_SERVER";
        }
        return builder.toString();
    }
}
