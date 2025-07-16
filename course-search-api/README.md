# Course Search API

A Spring Boot application that provides a REST API for searching educational courses using Elasticsearch.

## Project Overview

This application allows users to search for educational courses with various filtering options:
- Full-text search on course title and description
- Filter by category and type
- Filter by age range (minAge/maxAge)
- Filter by price range (minPrice/maxPrice)
- Filter by session date (courses starting on or after a given date)
- Sort by upcoming session date or price (ascending/descending)
- Pagination support

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Elasticsearch 7.x or 8.x running on localhost:9200

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd course-search-api
```

### 2. Install and Run Elasticsearch

If you don't have Elasticsearch running already:

1. Download Elasticsearch from [elastic.co](https://www.elastic.co/downloads/elasticsearch)
2. Extract the archive and navigate to the Elasticsearch directory
3. Run Elasticsearch:
   ```bash
   bin\elasticsearch.bat
   ```
4. Verify Elasticsearch is running by visiting http://localhost:9200 in your browser

### 3. Build and Run the Application

```bash
mvn clean install
mvn spring:run
```

Or run the JAR file directly:

```bash
java -jar target/course-search-api-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080 by default.

## Elasticsearch Configuration

The application is configured to connect to Elasticsearch at http://localhost:9200. This configuration is defined in `src/main/resources/application.yml`:

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    connection-timeout: 5s
    socket-timeout: 10s
```

If you need to connect to Elasticsearch at a different location, modify these properties accordingly.

## Data Model

The application uses the following data model for courses:

| Field           | Type          | Description                                   |
|-----------------|---------------|-----------------------------------------------|
| id              | String        | Unique identifier for the course              |
| title           | Text          | Course title (used for full-text search)      |
| description     | Text          | Course description (used for full-text search)|
| category        | Keyword       | Course category (used for exact filtering)    |
| type            | Keyword       | Course type (used for exact filtering)        |
| gradeRange      | Keyword       | Grade range for the course                    |
| minAge          | Integer       | Minimum age requirement                       |
| maxAge          | Integer       | Maximum age limit                             |
| price           | Double        | Course price                                  |
| nextSessionDate | Date          | Date of the next available session            |

## Data Ingestion

The application automatically loads sample course data from `src/main/resources/sample-courses.json` when it starts up. This is handled by the `CourseIndexer` component, which:

1. Reads the JSON file from the resources folder
2. Deserializes the data into `CourseDocument` objects
3. Bulk-indexes the documents into the Elasticsearch "courses" index

You can verify the data ingestion by:

1. Checking the application logs for a success message: "✅ Successfully indexed X courses to Elasticsearch."
2. Querying the Elasticsearch index directly:
   ```bash
   curl -X GET "http://localhost:9200/courses/_count"
   ```

## API Documentation

### Search Endpoint

```
GET /api/search
```

#### Query Parameters

| Parameter   | Type    | Required | Description                                                |
|-------------|---------|----------|------------------------------------------------------------|
| q           | String  | No       | Search keyword for title and description                   |
| category    | String  | No       | Filter by exact category match                             |
| type        | String  | No       | Filter by exact type match                                 |
| minAge      | Integer | No       | Filter courses with minAge >= specified value              |
| maxAge      | Integer | No       | Filter courses with maxAge <= specified value              |
| minPrice    | Double  | No       | Filter courses with price >= specified value               |
| maxPrice    | Double  | No       | Filter courses with price <= specified value               |
| startDate   | String  | No       | ISO-8601 date to filter courses starting on/after this date|
| sort        | String  | No       | Sort order: "upcoming" (default), "priceAsc", "priceDesc"  |
| page        | Integer | No       | Page number (0-based, default: 0)                          |
| size        | Integer | No       | Page size (default: 10)                                    |

#### Response Format

```json
{
  "total": 42,
  "courses": [
    {
      "id": "course123",
      "title": "Introduction to Programming",
      "description": "Learn the basics of programming...",
      "category": "Computer Science",
      "type": "Online",
      "gradeRange": "9-12",
      "minAge": 14,
      "maxAge": 18,
      "price": 199.99,
      "nextSessionDate": "2025-08-15T09:00:00Z"
    },
    {
      "id": "course456",
      "title": "Advanced Data Science",
      "description": "Explore machine learning algorithms...",
      "category": "Data Science",
      "type": "Hybrid",
      "gradeRange": "College",
      "minAge": 18,
      "maxAge": 99,
      "price": 499.99,
      "nextSessionDate": "2025-09-01T18:30:00Z"
    }
  ]
}
```

### Example API Calls

#### Basic Search

Search for courses containing "math" in title or description:

```bash
curl -X GET "http://localhost:8080/api/search?q=math"
```

#### Filtered Search

Search for online math courses for ages 10-12 with price under $100:

```bash
curl -X GET "http://localhost:8080/api/search?q=math&type=Online&minAge=10&maxAge=12&maxPrice=100"
```

#### Category Filter with Sorting

Get all science courses sorted by price (low to high):

```bash
curl -X GET "http://localhost:8080/api/search?category=Science&sort=priceAsc"
```

#### Date Filter with Pagination

Get courses starting after July 20, 2025, page 2 with 5 results per page:

```bash
curl -X GET "http://localhost:8080/api/search?startDate=2025-07-20T00:00:00Z&page=1&size=5"
```

#### Combined Filters

Search for art courses for teenagers (13-19) starting in August 2025 or later, sorted by price (high to low):

```bash
curl -X GET "http://localhost:8080/api/search?category=Art&minAge=13&maxAge=19&startDate=2025-08-01T00:00:00Z&sort=priceDesc"
```

## Testing & Verification

### Expected Behavior

1. **Full-text search**: Searching for keywords should match courses with those terms in title or description, with fuzzy matching for typos.
2. **Filtering**: 
   - Category and type filters should match exactly
   - Age range filters should return courses where the specified age falls within the course's min-max age range
   - Price filters should return courses within the specified price range
   - Date filter should return only courses with sessions on or after the specified date
3. **Sorting**:
   - Default sort should show upcoming courses first (earliest nextSessionDate)
   - priceAsc should sort from lowest to highest price
   - priceDesc should sort from highest to lowest price
4. **Pagination**: 
   - page and size parameters should limit results appropriately
   - total count should reflect the total number of matching courses, not just the current page

### Verification Steps

1. Start the application and verify data ingestion in logs
2. Try various search combinations using the example curl commands
3. Verify that results match the expected behavior for each filter and sort option
4. Check pagination by comparing total count with returned results and trying different page numbers

## Development

### Key Components

- **CourseDocument**: Entity class mapping to Elasticsearch documents
- **CourseRepository**: Spring Data repository for basic CRUD operations
- **CourseIndexer**: Component that loads and indexes sample data
- **CourseSearchService**: Service implementing the search functionality
- **CourseSearchController**: REST controller exposing the search API

### Adding Custom Features

To extend the application with additional features:

1. Modify the CourseDocument class to add new fields
2. Update the search implementation in CourseSearchService
3. Add new parameters to the CourseSearchController as needed