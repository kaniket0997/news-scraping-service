package com.example.news.controller;

import com.example.news.model.NewsArticle;
import com.example.news.service.NewsService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
public class NewsController {

  private final NewsService newsService;

  public NewsController(NewsService newsService) {
    this.newsService = newsService;
  }

  @GetMapping("/v1/category")
  public ResponseEntity<?> byCategory(
      @RequestParam String category, @RequestParam(defaultValue = "5") int limit) {
    List<NewsArticle> r = newsService.fetchNewsArticlesByCategory(category, limit);
    return ResponseEntity.ok(Map.of("articles", r, "count", r.size()));
  }

  @GetMapping("/v1/source")
  public ResponseEntity<?> bySource(
      @RequestParam String source, @RequestParam(defaultValue = "5") int limit) {
    List<NewsArticle> r = newsService.fetchNewsArticlesBySource(source, limit);
    return ResponseEntity.ok(Map.of("articles", r, "count", r.size()));
  }

  @GetMapping("/v1/score")
  public ResponseEntity<?> byScore(
      @RequestParam double minScore, @RequestParam(defaultValue = "5") int limit) {
    List<NewsArticle> r = newsService.fetchNewsArticlesByScore(minScore, limit);
    return ResponseEntity.ok(Map.of("articles", r, "count", r.size()));
  }

  @GetMapping("/v1/search")
  public ResponseEntity<?> search(
      @RequestParam String query, @RequestParam(defaultValue = "5") int limit) {
    List<NewsArticle> r = newsService.searchNewsArticles(query, limit);
    return ResponseEntity.ok(Map.of("articles", r, "count", r.size()));
  }

  @GetMapping("/v1/nearby")
  public ResponseEntity<?> nearby(
      @RequestParam double lat,
      @RequestParam double lon,
      @RequestParam(defaultValue = "10") double radiusKm,
      @RequestParam(defaultValue = "5") int limit) {
    List<NewsArticle> r = newsService.fetchNewsArticlesRelevantNearby(lat, lon, radiusKm, limit);
    return ResponseEntity.ok(Map.of("articles", r, "count", r.size()));
  }
}
