package com.example.news.service;

import com.example.news.model.NewsArticle;
import com.example.news.repository.NewsRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class NewsService {

  private final NewsRepository newsRepository;
  private final LLMService llmService;

  public NewsService(NewsRepository newsRepository, LLMService llmService) {
    this.newsRepository = newsRepository;
    this.llmService = llmService;
  }

  public List<NewsArticle> fetchNewsArticlesByCategory(String category, int limit) {
    List<NewsArticle> list =
        newsRepository.scanWithFilter(
            article ->
                    !CollectionUtils.isEmpty(article.getCategory())
                    && article.getCategory().stream().anyMatch(c -> c.equalsIgnoreCase(category)));
    list.sort(
        Comparator.comparing(
            NewsArticle::getPublicationEpoch, Comparator.nullsLast(Comparator.reverseOrder())));
    return enrichAndLimit(list, limit);
  }

  public List<NewsArticle> fetchNewsArticlesBySource(String source, int limit) {
    List<NewsArticle> list =
        newsRepository.scanWithFilter(
            article ->
                StringUtils.hasText(article.getSourceName())
                    && article.getSourceName().equalsIgnoreCase(source));
    list.sort(
        Comparator.comparing(
            NewsArticle::getPublicationEpoch, Comparator.nullsLast(Comparator.reverseOrder())));
    return enrichAndLimit(list, limit);
  }

  public List<NewsArticle> fetchNewsArticlesByScore(double minScore, int limit) {
    List<NewsArticle> list =
        newsRepository.scanWithFilter(
            article ->
                Objects.nonNull(article.getRelevanceScore()) && article.getRelevanceScore() >= minScore);
    list.sort(
        Comparator.comparing(
            NewsArticle::getRelevanceScore, Comparator.nullsLast(Comparator.reverseOrder())));
    return enrichAndLimit(list, limit);
  }

  public List<NewsArticle> searchNewsArticles(String q, int limit) {
    String qLower = q.toLowerCase();

      // Since dynamoDB does not support full text search, we are doing a scan with filter here.
      List<NewsArticle> list =
        newsRepository.scanWithFilter(
            article ->
                (StringUtils.hasText(article.getTitle())
                        && article.getTitle().toLowerCase().contains(qLower))
                    || (StringUtils.hasText(article.getDescription())
                        && article.getDescription().toLowerCase().contains(qLower)));

    List<NewsArticle> scored =
        list.stream()
            .peek(
                article -> {
                  double match = 0;
                  if (StringUtils.hasText(article.getTitle())
                      && article.getTitle().toLowerCase().contains(qLower)) match += 0.7;
                  if (StringUtils.hasText(article.getDescription())
                      && article.getDescription().toLowerCase().contains(qLower)) match += 0.3;
                  double relevance =
                      Objects.isNull(article.getRelevanceScore()) ? 0.0 : article.getRelevanceScore();
                  double finalScore = 0.6 * match + 0.4 * relevance;
                  article.setRelevanceScore(finalScore);
                })
            .sorted(
                Comparator.comparing(
                    NewsArticle::getRelevanceScore,
                    Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

    return enrichAndLimit(scored, limit);
  }

  public List<NewsArticle> fetchNewsArticlesRelevantNearby(double lat, double lon, double radiusKm, int limit) {
    List<NewsArticle> list =
        newsRepository.scanWithFilter(
            article -> Objects.nonNull(article.getLatitude()) && Objects.nonNull(article.getLongitude()));
    List<NewsArticle> withDist =
        list.stream()
            .peek(
                article -> {
                  double d = haversineKm(lat, lon, article.getLatitude(), article.getLongitude());
                  // store distance temporarily in relevanceScore for sorting
                  article.setRelevanceScore(d);
                })
            .filter(article -> article.getRelevanceScore() <= radiusKm)
            .sorted(Comparator.comparing(NewsArticle::getRelevanceScore))
            .collect(Collectors.toList());

    return enrichAndLimit(withDist, limit);
  }

  public double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double article =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(article), Math.sqrt(1 - article));
    return R * c;
  }

  private List<NewsArticle> enrichAndLimit(List<NewsArticle> list, int limit) {
    List<NewsArticle> out = list.stream().limit(limit).collect(Collectors.toList());
    for (NewsArticle article : out) {
      if (article.getLlmSummary() == null || article.getLlmSummary().isEmpty()) {
        String summary =
            llmService.summarizeArticleSync(article.getTitle(), article.getDescription());
        article.setLlmSummary(summary);
        newsRepository.save(article);
      }
    }
    return out;
  }
}
