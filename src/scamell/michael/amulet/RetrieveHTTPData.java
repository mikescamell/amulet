package scamell.michael.amulet;

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

public class RetrieveHTTPData {

    private final OnRetrieveHttpData listener;

    public RetrieveHTTPData(OnRetrieveHttpData listener) {
        this.listener = listener;
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
