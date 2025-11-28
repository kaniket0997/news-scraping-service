package com.example.news.model;

import java.util.List;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class NewsArticle {

  @Getter(onMethod_ = {@DynamoDbPartitionKey, @DynamoDbAttribute("id")})
  private String id;

  private String title;
  private String description;
  private String url;
  private Long publicationEpoch;
  private String sourceName;
  private List<String> category;
  private Double relevanceScore;
  private Double latitude;
  private Double longitude;
  private String llmSummary;
}
