package org.example;  // Must match directory structure

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class WeatherApp extends Application {
    // API configuration
    private static final String API_KEY = "5e121565d918b253b8b003e634adc9c9"; // Replace with your actual API key
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast?q=%s&units=%s&appid=%s";
    
    // UI Components
    private TextField locationInput;
    private ComboBox<String> unitSelector;
    private Label temperatureLabel, humidityLabel, windLabel, conditionLabel, timeLabel;
    private ImageView weatherIcon;
    private ListView<String> historyList;
    private VBox forecastContainer;
    
    // Data storage
    private List<String> searchHistory = new ArrayList<>();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
    
    // Unit constants
    private static final String METRIC = "metric";
    private static final String IMPERIAL = "imperial";
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Weather Information App");
        
        // Create main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Create top panel for input
        HBox topPanel = createTopPanel();
        root.setTop(topPanel);
        
        // Create center panel for weather display
        GridPane centerPanel = createCenterPanel();
        root.setCenter(centerPanel);
        
        // Create right panel for history
        VBox rightPanel = createRightPanel();
        root.setRight(rightPanel);
        
        // Create bottom panel for forecast
        forecastContainer = new VBox(10);
        forecastContainer.setPadding(new Insets(10));
        ScrollPane forecastScroll = new ScrollPane(forecastContainer);
        forecastScroll.setFitToWidth(true);
        root.setBottom(forecastScroll);
        
        // Set initial background based on time of day
        updateBackgroundBasedOnTime(root);
        
        // Set up the scene
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private HBox createTopPanel() {
        HBox topPanel = new HBox(10);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(10));
        
        // Location input
        locationInput = new TextField();
        locationInput.setPromptText("Enter city name");
        locationInput.setPrefWidth(300);
        
        // Search button
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> fetchWeatherData());
        
        // Unit selector
        unitSelector = new ComboBox<>();
        unitSelector.getItems().addAll("Celsius (℃)", "Fahrenheit (℉)");
        unitSelector.setValue("Celsius (℃)");
        unitSelector.setOnAction(e -> {
            if (locationInput.getText() != null && !locationInput.getText().isEmpty()) {
                fetchWeatherData();
            }
        });
        
        // Add components to top panel
        topPanel.getChildren().addAll(
            new Label("Location:"), locationInput,
            searchButton,
            new Label("Units:"), unitSelector
        );
        
        return topPanel;
    }
    
    private GridPane createCenterPanel() {
        GridPane centerPanel = new GridPane();
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.setHgap(20);
        centerPanel.setVgap(20);
        centerPanel.setPadding(new Insets(20));
        
        // Weather icon
        weatherIcon = new ImageView();
        weatherIcon.setFitWidth(100);
        weatherIcon.setFitHeight(100);
        GridPane.setConstraints(weatherIcon, 0, 0, 1, 3);
        
        // Condition label
        conditionLabel = new Label("--");
        conditionLabel.setFont(Font.font(20));
        GridPane.setConstraints(conditionLabel, 1, 0);
        
        // Temperature label
        temperatureLabel = new Label("--");
        temperatureLabel.setFont(Font.font(24));
        GridPane.setConstraints(temperatureLabel, 1, 1);
        
        // Humidity label
        humidityLabel = new Label("Humidity: --");
        GridPane.setConstraints(humidityLabel, 1, 2);
        
        // Wind label
        windLabel = new Label("Wind: --");
        GridPane.setConstraints(windLabel, 1, 3);
        
        // Time label
        timeLabel = new Label("Last updated: --");
        timeLabel.setFont(Font.font(10));
        GridPane.setConstraints(timeLabel, 1, 4);
        
        centerPanel.getChildren().addAll(
            weatherIcon, conditionLabel, temperatureLabel,
            humidityLabel, windLabel, timeLabel
        );
        
        return centerPanel;
    }
    
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(200);
        
        Label historyLabel = new Label("Search History");
        historyLabel.setFont(Font.font(14));
        
        historyList = new ListView<>();
        historyList.setPrefHeight(300);
        historyList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selected = historyList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String city = selected.split(" - ")[0];
                    locationInput.setText(city);
                    fetchWeatherData();
                }
            }
        });
        
        Button clearHistoryButton = new Button("Clear History");
        clearHistoryButton.setOnAction(e -> {
            searchHistory.clear();
            updateHistoryList();
        });
        
        rightPanel.getChildren().addAll(historyLabel, historyList, clearHistoryButton);
        return rightPanel;
    }
    
    private void fetchWeatherData() {
        String location = locationInput.getText().trim();
        if (location.isEmpty()) {
            showAlert("Error", "Please enter a location");
            return;
        }
        
        String units = unitSelector.getValue().equals("Celsius (℃)") ? METRIC : IMPERIAL;
        
        try {
            // Fetch current weather
            String weatherUrl = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s", 
            location, units, API_KEY);
            JSONObject weatherData = fetchDataFromAPI(weatherUrl);
            System.out.println("Raw API response: " + weatherData.toString());
            // Fetch forecast
            String forecastUrl = String.format(FORECAST_URL, location, units, API_KEY);
            JSONObject forecastData = fetchDataFromAPI(forecastUrl);
            
            // Update UI with weather data
            updateWeatherUI(weatherData, units);
            
            // Update forecast
            updateForecastUI(forecastData, units);
            
            // Add to history
            addToHistory(location);
            
        } catch (Exception e) {
            showAlert("Error", "Failed to fetch weather data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private JSONObject fetchDataFromAPI(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HTTP error code: " + responseCode);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return new JSONObject(response.toString());
    }
    
    private void updateWeatherUI(JSONObject weatherData, String units) {
        try {
            // Main weather info
            JSONObject main = weatherData.getJSONObject("main");
            double temp = main.getDouble("temp");
            int humidity = main.getInt("humidity");
            
            // Wind info
            JSONObject wind = weatherData.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");
            
            // Weather condition
            JSONObject weather = weatherData.getJSONArray("weather").getJSONObject(0);
            String description = weather.getString("description");
            String iconCode = weather.getString("icon");
            
            // Update UI
            temperatureLabel.setText(String.format("%.1f°%s", temp, units.equals(METRIC) ? "C" : "F"));
            humidityLabel.setText(String.format("Humidity: %d%%", humidity));
            windLabel.setText(String.format("Wind: %.1f %s", windSpeed, units.equals(METRIC) ? "m/s" : "mph"));
            conditionLabel.setText(description);
            timeLabel.setText("Last updated: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
            
            // Load weather icon
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            try {
                Image image = new Image(iconUrl, true);
                weatherIcon.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading weather icon: " + e.getMessage());
            }
            
        } catch (Exception e) {
            showAlert("Data Error", "Failed to parse weather data: " + e.getMessage());
        }
    }
    private void updateForecastUI(JSONObject forecastData, String units) {
        forecastContainer.getChildren().clear();
        
        Label forecastTitle = new Label("5-Day Forecast (3-hour intervals)");
        forecastTitle.setFont(Font.font(16));
        forecastContainer.getChildren().add(forecastTitle);
        
        // Group forecast by day
        Map<String, List<JSONObject>> dailyForecast = new LinkedHashMap<>();
        
        for (int i = 0; i < forecastData.getJSONArray("list").length(); i++) {
            JSONObject forecast = forecastData.getJSONArray("list").getJSONObject(i);
            String date = forecast.getString("dt_txt").split(" ")[0]; // Get date part
            
            dailyForecast.computeIfAbsent(date, k -> new ArrayList<>()).add(forecast);
        }
        
        // Display forecast for each day
        for (Map.Entry<String, List<JSONObject>> entry : dailyForecast.entrySet()) {
            HBox dayForecast = new HBox(10);
            dayForecast.setAlignment(Pos.CENTER_LEFT);
            
            // Date label
            try {
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = apiFormat.parse(entry.getKey());
                Label dateLabel = new Label(dateFormat.format(date));
                dateLabel.setPrefWidth(100);
                dayForecast.getChildren().add(dateLabel);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Forecast items for the day
            for (JSONObject forecast : entry.getValue()) {
                VBox forecastItem = new VBox(5);
                forecastItem.setAlignment(Pos.CENTER);
                forecastItem.setPadding(new Insets(5));
                forecastItem.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");
                
                // Time
                String time = forecast.getString("dt_txt").split(" ")[1].substring(0, 5);
                Label timeLabel = new Label(time);
                
                // Temperature
                double temp = forecast.getJSONObject("main").getDouble("temp");
                Label tempLabel = new Label(String.format("%.1f°%s", temp, units.equals(METRIC) ? "C" : "F"));
                
                // Weather icon
                String iconCode = forecast.getJSONArray("weather").getJSONObject(0).getString("icon");
                ImageView icon = new ImageView("https://openweathermap.org/img/wn/" + iconCode + ".png");
                icon.setFitWidth(30);
                icon.setFitHeight(30);
                
                forecastItem.getChildren().addAll(timeLabel, icon, tempLabel);
                dayForecast.getChildren().add(forecastItem);
            }
            
            forecastContainer.getChildren().add(dayForecast);
        }
    }
    
    private void addToHistory(String location) {
        String timestamp = new SimpleDateFormat("HH:mm").format(new Date());
        String entry = String.format("%s - %s", location, timestamp);
        
        searchHistory.removeIf(e -> e.startsWith(location + " - "));
        searchHistory.add(0, entry);
        
        if (searchHistory.size() > 10) {
            searchHistory = searchHistory.subList(0, 10);
        }
        
        historyList.getItems().setAll(searchHistory);
    }
    
    private void updateHistoryList() {
        historyList.getItems().clear();
        historyList.getItems().addAll(searchHistory);
    }
    
    private void updateBackgroundBasedOnTime(BorderPane root) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        String style;
        if (hour >= 6 && hour < 12) {
            style = "-fx-background-color: linear-gradient(to bottom, #87CEEB, #E0F7FA);"; // Morning
        } else if (hour >= 12 && hour < 18) {
            style = "-fx-background-color: linear-gradient(to bottom, #4682B4, #87CEEB);"; // Afternoon
        } else if (hour >= 18 && hour < 21) {
            style = "-fx-background-color: linear-gradient(to bottom, #FF7F50, #FFA07A);"; // Evening
        } else {
            style = "-fx-background-color: linear-gradient(to bottom, #191970, #000080);"; // Night
        }
        
        root.setStyle(style);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}