package com.dgf;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import org.json.JSONObject;

public class WeatherService {
    private static final String WEATHER_API_KEY = System.getenv("OPENWEATHER_API_KEY");
    private static HttpClient httpClient = HttpClient.newHttpClient();

    // Add this method to your WeatherService class
    public static void setHttpClient(HttpClient client) {
        WeatherService.httpClient = client;
    }

    public static String getWeather(String city) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + WEATHER_API_KEY + "&units=metric";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            double temp = jsonResponse.getJSONObject("main").getDouble("temp");
            return "The current temperature in " + city + " is " + temp + "°C.";
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Unable to get weather data.";
        }
    }
}
