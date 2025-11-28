package com.example.news.loader;

import com.example.news.model.NewsArticle;
import com.example.news.repository.NewsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

  private final NewsRepository repo;
  private final ObjectMapper mapper = new ObjectMapper();

  public DataLoader(NewsRepository repo) {
    this.repo = repo;
  }

  @Override
  public void run(String... args) throws Exception {
    File dataDir = new File("data");
    if (!dataDir.exists()) {
      System.out.println("No data dir found at ./data — skipping loader.");
      return;
    }
    File[] files = dataDir.listFiles((d, name) -> name.endsWith(".json"));
    if (files == null) return;
    for (File f : files) {
      JsonNode root = mapper.readTree(f);
      if (root.isArray()) {
        for (JsonNode node : root) {
          NewsArticle.NewsArticleBuilder b = NewsArticle.builder();

          String id = node.path("id").asText(null);
          if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
          b.id(id);

          b.title(node.path("title").asText(null));
          b.description(node.path("description").asText(null));
          b.url(node.path("url").asText(null));

          // publication_date can be ISO string or epoch. We normalize to epoch millis.
          Long publicationEpoch = null;
          if (node.has("publication_date") && !node.path("publication_date").isNull()) {
            String pd = node.path("publication_date").asText(null);
            if (pd != null && !pd.isBlank()) {
              // Try to parse as ISO first
              try {
                Instant ins = Instant.parse(pd);
                publicationEpoch = ins.toEpochMilli();
              } catch (DateTimeParseException ex) {
                // Maybe it's numeric epoch (seconds or millis)
                try {
                  long v = Long.parseLong(pd);
                  // Heuristic: if v has 10 digits -> seconds, convert to millis
                  if (String.valueOf(v).length() == 10) {
                    publicationEpoch = v * 1000L;
                  } else {
                    publicationEpoch = v;
                  }
                } catch (NumberFormatException nfe) {
                  // fallback to now
                  publicationEpoch = Instant.now().toEpochMilli();
                }
              }
            } else {
              publicationEpoch = Instant.now().toEpochMilli();
            }
          } else {
            publicationEpoch = Instant.now().toEpochMilli();
          }
          b.publicationEpoch(publicationEpoch);

          b.sourceName(node.path("source_name").asText(null));

          if (node.has("category") && node.get("category").isArray()) {
            List<String> cats = new ArrayList<>();
            node.get("category").forEach(c -> cats.add(c.asText()));
            b.category(cats);
          }

          if (node.has("relevance_score") && node.path("relevance_score").isNumber()) {
            b.relevanceScore(node.get("relevance_score").asDouble());
          }

          if (node.has("latitude") && node.path("latitude").isNumber()) {
            b.latitude(node.get("latitude").asDouble());
          }

          if (node.has("longitude") && node.path("longitude").isNumber()) {
            b.longitude(node.get("longitude").asDouble());
          }

          NewsArticle a = b.build();
          repo.save(a);
        }
      }
    }
    System.out.println("Data loader finished.");
  }
}
