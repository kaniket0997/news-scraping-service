package com.example.news;

import static org.junit.jupiter.api.Assertions.*;

import com.example.news.service.NewsService;
import org.junit.jupiter.api.Test;

public class HaversineTest {

  @Test
  public void haversineZeroDistance() {
    NewsService svc = new NewsService(null, null);
    double d = svc.haversineKm(0, 0, 0, 0);
    assertEquals(0.0, d, 1e-6);
  }

  @Test
  public void haversineKnownDistance() {
    NewsService svc = new NewsService(null, null);
    // Approx distance between Paris (48.8566,2.3522) and London (51.5074,-0.1278) ~ 343 km
    double d = svc.haversineKm(48.8566, 2.3522, 51.5074, -0.1278);
    assertTrue(d > 340 && d < 350, "Expected ~343 km, got " + d);
  }
}
