package com.example.user_2.mango_player;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import io.github.nikhilbhutani.analyzer.DataAnalyzer;

import static com.example.user_2.mango_player.MainActivity.convertToStringRepresentation;

public class GetTrackInfo extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {

            Document doc = Jsoup.connect(Const.TRACK_INFO_URL).get();
            String first_letter = doc.getElementsByClass("boxed").select("p").get(1).toString().substring(29, doc.getElementsByClass("boxed").select("p").get(1).toString().length());

            String[] parts = first_letter.split("<br> <strong>Track:</strong> ");
            String first = parts[0];
            MainActivity.artist = first;
            String second = parts[1];
            MainActivity.track = second.substring(0, second.length() - 9);
            readJsonFromUrl("http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=" + MainActivity.artist.replace(" ", "%20") + "&api_key=" + Const.LAST_FM_KEY + "&format=json".replace(" ", "%20"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        MainActivity.setSongData();

    }

    public static String readJsonFromUrl(String url) throws IOException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return jsonText;
        } finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        Log.e("response", sb.toString());
        JSONObject dataJsonObj;
        //   try {
        MainActivity.album = "";
        return sb.toString();
    }

    static boolean isError(JSONObject dataJsonObj) {
        if (!dataJsonObj.toString().contains("{\"error\":6,\"message\":\"The artist you supplied could not be found\",\"links\":[]}")) {
            return true;
        }
        return false;
    }
}