package com.dgf;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TriviaService {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static String sessionToken = null;

    public static List<TriviaQuestions> getTriviaQuestions(String category, int amount, String difficulty, String type) {
        String categoryId = mapCategoryToId(category);
        String token = getSessionToken(); // Retrieve or generate session token
        String url = "https://opentdb.com/api.php?amount=" + amount + "&category=" + categoryId + 
                     "&difficulty=" + difficulty + "&type=" + type + "&token=" + token;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray questionsArray = jsonResponse.getJSONArray("results");
            if (questionsArray.length() == 0) {
                return Collections.emptyList();
            }

            List<TriviaQuestions> triviaQuestionsList = new ArrayList<>();
            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionData = questionsArray.getJSONObject(i);

                String questionText = questionData.getString("question");
                String correctAnswer = questionData.getString("correct_answer");
                JSONArray incorrectAnswersArray = questionData.getJSONArray("incorrect_answers");

                List<String> incorrectAnswers = new ArrayList<>();
                for (int j = 0; j < incorrectAnswersArray.length(); j++) {
                    incorrectAnswers.add(incorrectAnswersArray.getString(j));
                }

                triviaQuestionsList.add(new TriviaQuestions(questionText, correctAnswer, incorrectAnswers));
            }
            return triviaQuestionsList;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static String mapCategoryToId(String category) {
        Map<String, String> categoryMap = new HashMap<>();
        
        // Mapping categories to their IDs
        categoryMap.put("General Knowledge", "9");
        categoryMap.put("Entertainment: Books", "10");
        categoryMap.put("Entertainment: Film", "11");
        categoryMap.put("Entertainment: Music", "12");
        categoryMap.put("Entertainment: Musicals & Theatres", "13");
        categoryMap.put("Entertainment: Television", "14");
        categoryMap.put("Entertainment: Video Games", "15");
        categoryMap.put("Entertainment: Board Games", "16");
        categoryMap.put("Science & Nature", "17");
        categoryMap.put("Science: Computers", "18");
        categoryMap.put("Science: Mathematics", "19");
        categoryMap.put("Mythology", "20");
        categoryMap.put("Sports", "21");
        categoryMap.put("Geography", "22");
        categoryMap.put("History", "23");
        categoryMap.put("Politics", "24");
        categoryMap.put("Art", "25");
        categoryMap.put("Celebrities", "26");
        categoryMap.put("Animals", "27");
        categoryMap.put("Vehicles", "28");
        categoryMap.put("Entertainment: Comics", "29");
        categoryMap.put("Science: Gadgets", "30");
        categoryMap.put("Entertainment: Japanese Anime & Manga", "31");
        categoryMap.put("Entertainment: Cartoon & Animations", "32");
    
        return categoryMap.getOrDefault(category, ""); // Default to empty string if category not found
    }
    
    private static String getSessionToken() {
        if (sessionToken == null || sessionToken.isEmpty()) {
            sessionToken = requestSessionToken();
        }
        return sessionToken;
    }

    private static String requestSessionToken() {
        String url = "https://opentdb.com/api_token.php?command=request";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getString("token");
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // Return empty string in case of an error
        }
    }

    public static void resetSessionToken() {
        String url = "https://opentdb.com/api_token.php?command=reset&token=" + sessionToken;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            sessionToken = requestSessionToken(); // Reset and get a new token
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
