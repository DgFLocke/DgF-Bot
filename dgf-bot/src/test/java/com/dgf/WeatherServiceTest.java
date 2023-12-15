package com.dgf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class WeatherServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockStringResponse;    

    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        WeatherService.setHttpClient(mockHttpClient);
    }

    @Test
    void testGetWeatherSuccess() throws Exception {
        String sampleJson = "{\"main\":{\"temp\":15.0}}";
        when(mockResponse.body()).thenReturn(sampleJson);
        when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

        String expected = "The current temperature in London is 15.0°C.";
        String actual = WeatherService.getWeather("London");
        assertEquals(expected, actual);
    }

    @Test
    void testGetWeatherFailure() throws Exception {
        when(mockHttpClient.send(any(), any())).thenThrow(new RuntimeException("Failed to connect"));
        
        String actual = WeatherService.getWeather("London");
        assertEquals("Unable to get weather data.", actual);
    }

    @Test
    void testSuccessfulWeatherFetch() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<Object> mockResponse = mock(HttpResponse.class);

        when(mockResponse.body()).thenReturn("{\"main\":{\"temp\":20.5}}");
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);

        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            String weather = WeatherService.getWeather("London");
            assertEquals("The current temperature in London is 20.5°C.", weather);
        }
    }

    @Test
    void testApiReturnsError() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.body()).thenReturn(""); // Simulate an error response
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);

        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            String weather = WeatherService.getWeather("InvalidCity");
            assertEquals("Unable to get weather data.", weather);
        }
    }

    @Test
    void testNetworkIssues() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);

        when(mockHttpClient.send(any(HttpRequest.class), any())).thenThrow(new IOException());

        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            String weather = WeatherService.getWeather("London");
            assertEquals("Unable to get weather data.", weather);
        }
    }

    @Test
    void testInvalidJsonResponse() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.body()).thenReturn("Invalid JSON");
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);

        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            String weather = WeatherService.getWeather("London");
            assertEquals("Unable to get weather data.", weather);
    
        }
    }

    @Test
    void testMissingTemperatureInJson() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.body()).thenReturn("{\"main\":{}}"); // Missing temperature
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);

        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);

            String weather = WeatherService.getWeather("London");
            assertEquals("Unable to get weather data.", weather);
        }
    }

    @Test
    void testApiKeyNotSet() {
        System.clearProperty("OPENWEATHER_API_KEY"); // Ensure API key is not set
    
        String weather = WeatherService.getWeather("London");
        assertEquals("API key not set.", weather); // Adjust based on your implementation
    }

    @Test
    void testEmptyCityName() {
        String weather = WeatherService.getWeather("");
        assertEquals("City name cannot be empty.", weather); // Adjust based on your implementation
    }

    @Test
    void testNullCityName() {
        String weather = WeatherService.getWeather(null);
        assertEquals("City name cannot be null.", weather); // Adjust based on your implementation
    }

    @Test
    void testApiTimeout() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
    
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenThrow(new java.net.http.HttpTimeoutException("Request timed out"));
    
        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
    
            String weather = WeatherService.getWeather("London");
            assertEquals("Request timed out.", weather);
        }
    }

    @Test
    void testCityNotFound() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
    
        when(mockResponse.body()).thenReturn("{\"cod\":\"404\",\"message\":\"city not found\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    
        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
    
            String weather = WeatherService.getWeather("UnknownCity");
            assertEquals("City not found.", weather);
        }
    }

    @Test
    void testEmptyResponseBody() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
    
        when(mockResponse.body()).thenReturn("");
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    
        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
    
            String weather = WeatherService.getWeather("London");
            assertEquals("Weather data is empty.", weather);
        }
    }

    @Test
    void testInvalidApiKey() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
    
        when(mockResponse.body()).thenReturn("{\"cod\":401, \"message\":\"Invalid API key\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    
        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
    
            String weather = WeatherService.getWeather("London");
            assertEquals("Invalid API key.", weather);
        }
    }

    @Test
    void testUnexpectedJsonFormat() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
    
        when(mockResponse.body()).thenReturn("{\"unexpected\":\"format\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    
        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
    
            String weather = WeatherService.getWeather("London");
            assertEquals("Unexpected data format.", weather);
        }
    }

    @Test
    void testApiRateLimitExceeded() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
    
        when(mockResponse.body()).thenReturn("{\"cod\":429, \"message\":\"API rate limit exceeded\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    
        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
    
            String weather = WeatherService.getWeather("London");
            assertEquals("API rate limit exceeded.", weather);
        }
    }

    @Test
    void testHttpErrorStatus() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
    
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    
        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
    
            String weather = WeatherService.getWeather("London");
            assertEquals("HTTP error occurred.", weather);
        }
    }

    @Test
    void testTimeoutException() throws Exception {
        HttpClient mockHttpClient = mock(HttpClient.class);
    
        when(mockHttpClient.send(any(HttpRequest.class), any())).thenThrow(new java.net.http.HttpTimeoutException("Request timed out"));
    
        try (MockedStatic<HttpClient> mockedStatic = Mockito.mockStatic(HttpClient.class)) {
            mockedStatic.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
    
            String weather = WeatherService.getWeather("London");
            assertEquals("Request timed out.", weather);
        }
    }

}
