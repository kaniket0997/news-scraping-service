package com.example.news.repository;

import com.example.news.model.NewsArticle;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class NewsRepository {
  private final DynamoDbTable<NewsArticle> table;

  public NewsRepository(DynamoDbEnhancedClient enhancedClient) {
    this.table = enhancedClient.table("NewsArticle", TableSchema.fromBean(NewsArticle.class));
  }

  public void save(NewsArticle article) {
    table.putItem(article);
  }

  public Optional<NewsArticle> findById(String id) {
    return Optional.ofNullable(table.getItem(r -> r.key(k -> k.partitionValue(id))));
  }

  public List<NewsArticle> scanAll() {
    List<NewsArticle> out = new ArrayList<>();
    table.scan().items().forEach(out::add);
    return out;
  }

  public List<NewsArticle> scanWithFilter(Predicate<NewsArticle> predicate) {
    return scanAll().stream().filter(predicate).collect(Collectors.toList());
  }
}
