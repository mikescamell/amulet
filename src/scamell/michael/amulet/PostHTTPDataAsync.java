package scamell.michael.amulet;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PostHTTPDataAsync extends AsyncTask<String, Void, String> {

    private final OnPostHttpData listener;
    private final StringEntity stringEntity;

    public PostHTTPDataAsync(OnPostHttpData listener, StringEntity stringEntity) {
        this.listener = listener;
        this.stringEntity = stringEntity;
    }

    @Override
    protected void onPostExecute(String httpData) {
        listener.onPostTaskCompleted(httpData);
    }

    @Override
    protected String doInBackground(String... urls) {
        String responseData = null;
        if (urls.length > 0) {
            responseData = postHTTPData(urls[0]);
        }
        return responseData;
    }

    private String postHTTPData(String urls) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(urls);
        httpPost.setEntity(stringEntity);
        try {
            HttpResponse response = client.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            String responseHeaderString = response.getFirstHeader("X-SubmissionResponse").toString();
            if (statusCode == 202 || responseHeaderString.contains("SUCCESS")) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else if (responseHeaderString.contains("INVALID")) {
                return "POST_DATA_INVALID";
            } else {
                return "SERVER_ERROR_RESPONSE " + statusCode;
            }
        } catch (Exception e) {
            Log.e("POST_HTTP_DATA_ASYNC", "Server Exception Error");
            e.printStackTrace();
            return "SERVER_ERROR_RESPONSE";
        }
        return builder.toString();
    }
}
