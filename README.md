# News Scraping Service (Java + Spring Boot + DynamoDB)

A small Spring Boot service to load news JSON files into DynamoDB and expose endpoints for category, source, score, search, and nearby queries. Articles are enriched with LLM-generated summaries.

## Prerequisites
- Java 17+
- Maven
- Docker (optional, for DynamoDB Local)
- OPENAI_API_KEY (optional; if not provided LLM calls will be skipped or return empty)

## Quick start (local with DynamoDB Local)

1. Start DynamoDB Local:

```bash
docker run -p 8000:8000 amazon/dynamodb-local
```

2. Create the DynamoDB table (you can use AWS CLI):

```bash
aws dynamodb create-table \
  --table-name NewsArticle \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --endpoint-url http://localhost:8000
```

3. Build:

```bash
mvn clean package -DskipTests
```

4. Set environment variables:

```bash
export AWS_ACCESS_KEY_ID=dummy
export AWS_SECRET_ACCESS_KEY=dummy
export OPENAI_API_KEY="sk_..."   # optional
```

5. Place sample data (or your `news_data.json`) into `data/` (one or more JSON files). A sample file `data/news_sample.json` is included.

6. Run:

```bash
java -jar target/news-service-0.0.1-SNAPSHOT.jar
```

7. Test endpoints (examples):

```bash
curl "http://localhost:8080/api/news/v1/category?category=General&limit=5"
curl "http://localhost:8080/api/news/v1/score?minScore=0&limit=5"
curl "http://localhost:8080/api/news/v1/source?source=DW&limit=5"
curl "http://localhost:8080/api/news/v1/search?query=Paris&limit=5"
curl "http://localhost:8080/api/news/v1/nearby?lat=21.754075&lon=80.560129&radiusKm=50&limit=5"
```

## Notes
- Summaries from the LLM are cached back into DynamoDB to reduce cost.

