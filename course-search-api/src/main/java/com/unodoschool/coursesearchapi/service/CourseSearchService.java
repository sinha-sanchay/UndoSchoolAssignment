package com.unodoschool.coursesearchapi.service;

import com.unodoschool.coursesearchapi.document.CourseDocument;
import com.unodoschool.coursesearchapi.dto.CourseSearchResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public CourseSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public CourseSearchResponse searchCourses(
            String q,
            String category,
            String type,
            Integer minAge,
            Integer maxAge,
            Double minPrice,
            Double maxPrice,
            String startDate,
            int page,
            int size,
            String sort
    ) {
        Criteria criteria = new Criteria();

        // Full-text search on title and description
        if (q != null && !q.isBlank()) {
            String fuzziness = q.length() < 5 ? "1" : "2";

            Criteria titleCriteria = new Criteria("title").matches(q).fuzzy(fuzziness);
            Criteria descCriteria = new Criteria("description").matches(q);

            criteria = criteria.and(new Criteria().or(titleCriteria).or(descCriteria));
        }

        // Filtering conditions
        if (category != null && !category.isBlank()) {
            criteria = criteria.and(new Criteria("category").is(category));
        }

        if (type != null && !type.isBlank()) {
            criteria = criteria.and(new Criteria("type").is(type));
        }

        if (minAge != null) {
            criteria = criteria.and(new Criteria("minAge").greaterThanEqual(minAge));
        }

        if (maxAge != null) {
            criteria = criteria.and(new Criteria("maxAge").lessThanEqual(maxAge));
        }

        if (minPrice != null) {
            criteria = criteria.and(new Criteria("price").greaterThanEqual(minPrice));
        }

        if (maxPrice != null) {
            criteria = criteria.and(new Criteria("price").lessThanEqual(maxPrice));
        }

        if (startDate != null && !startDate.isBlank()) {
            try {
                ZonedDateTime date = ZonedDateTime.parse(startDate);
                criteria = criteria.and(new Criteria("nextSessionDate").greaterThanEqual(date));
            } catch (Exception e) {
                System.err.println("Invalid startDate: " + startDate);
            }
        }

        // Sorting logic
        Sort sortBy;
        if ("priceasc".equalsIgnoreCase(sort)) {
            sortBy = Sort.by("price").ascending();
        } else if ("pricedesc".equalsIgnoreCase(sort)) {
            sortBy = Sort.by("price").descending();
        } else {
            sortBy = Sort.by("nextSessionDate").ascending(); // default
        }

        PageRequest pageable = PageRequest.of(page, size, sortBy);
        CriteriaQuery query = new CriteriaQuery(criteria, pageable);

        SearchHits<CourseDocument> hits = elasticsearchOperations.search(query, CourseDocument.class);

        List<CourseDocument> resultList = new ArrayList<>();
        for (SearchHit<CourseDocument> hit : hits) {
            resultList.add(hit.getContent());
        }

        return new CourseSearchResponse(hits.getTotalHits(), resultList);
    }
}
