package com.scarsz.discordsrv.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtil {

    public static String requestHttp(String uri) throws IOException {
        URL url = new URL(uri);
        URLConnection urlConnection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                urlConnection.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder builder = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            builder.append(inputLine);
        in.close();

        return builder.toString();
    }

}
