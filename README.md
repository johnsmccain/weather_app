# Weather Information App

A JavaFX application that displays current weather data and forecasts using the OpenWeatherMap API.

## Project Structure

weather-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/example/
│   │   │   │   └── WeatherApp.java      # Main application class
│   │   │   └── resources/               # Resource files (images, CSS)
│   │   └── test/
│   │       ├── java/org/example/
│   │       │   └── WeatherAppTest.java  # Unit tests
│   │       └── resources/
│   └── build/
├── gradle/                              # Gradle wrapper files
├── src/                                 # (Optional legacy source folder)
├── build.gradle                         # Project build configuration
├── gradle.properties                    # Gradle properties
├── settings.gradle                      # Project settings
└── README.md                            # This file
```

## Features

- Real-time weather data display
- 5-day forecast
- Location search history
- Unit conversion (Celsius/Fahrenheit)
- Weather condition icons

## Requirements

- Java 21+
- Gradle 8.0+
- OpenWeatherMap API key

## Setup

1. Get an API key from [OpenWeatherMap](https://openweathermap.org/api)
2. Add your key to `WeatherApp.java`:
   ```java
   private static final String API_KEY = "your_api_key_here";
   ```
3. Build and run:
   ```bash
   gradle build
   gradle run
   ```

## Configuration

Modify `build.gradle` to:
- Change Java version
- Add dependencies
- Configure main class

## Troubleshooting

If you encounter issues:
- Verify API key is valid
- Check internet connection
- Ensure Java 21+ is installed
- Review console error messages

## Screenshuts
![Demo GIF](./images/Screenshot%20from%202025-03-26%2015-34-51.png)
![Demo GIF](./images/Screenshot%20from%202025-03-26%2015-44-36.png)
![Demo GIF](./images/Screenshot%20from%202025-03-26%2015-44-47.png)
![Demo GIF](./images/Screenshot%20from%202025-03-26%2015-45-10.png)

## License

[MIT](LICENSE)
```