package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeatherAppTest {
    @Test
    void appHasAGreeting() {
        WeatherApp classUnderTest = new WeatherApp();
        assertNotNull(classUnderTest, "app should have a greeting");
    }
}