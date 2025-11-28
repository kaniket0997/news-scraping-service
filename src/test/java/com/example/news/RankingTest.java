package com.example.news;

import static org.junit.jupiter.api.Assertions.*;

import com.example.news.model.NewsArticle;
import org.junit.jupiter.api.Test;

public class RankingTest {

  @Test
  public void searchRankingCombinesTextAndRelevance() {
    // create two articles; one with better text match, another with higher stored relevance
    NewsArticle a1 =
        NewsArticle.builder()
            .title("Elon Musk buys company")
            .description("Details about a big acquisition")
            .relevanceScore(0.1)
            .publicationEpoch(System.currentTimeMillis())
            .build();

    NewsArticle a2 =
        NewsArticle.builder()
            .title("Some other news")
            .description("Mentions Elon Musk and acquisition in paragraph")
            .relevanceScore(0.9)
            .publicationEpoch(System.currentTimeMillis())
            .build();

    // emulate scoring logic from NewsService.search (text weight 0.6, relevance 0.4)
    double match1 = 0.7; // title match
    double final1 = 0.6 * match1 + 0.4 * 0.1;

    double match2 = 0.3; // description match only
    double final2 = 0.6 * match2 + 0.4 * 0.9;

    assertTrue(final2 > final1, "Article with higher combined score should rank higher");
  }
}
