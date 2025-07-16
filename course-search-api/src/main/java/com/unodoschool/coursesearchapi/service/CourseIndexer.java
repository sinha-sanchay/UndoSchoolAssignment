package com.unodoschool.coursesearchapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unodoschool.coursesearchapi.document.CourseDocument;
import com.unodoschool.coursesearchapi.repository.CourseRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class CourseIndexer {

    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    public CourseIndexer(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // to support ZonedDateTime
    }

    @PostConstruct
    public void init() {
        try (InputStream inputStream = getClass().getResourceAsStream("/sample-courses.json")) {

            if (inputStream == null) {
                System.err.println("sample-courses.json not found in resources folder");
                return;
            }

            List<CourseDocument> courses = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<CourseDocument>>() {}
            );

            // Save all courses to Elasticsearch
            courseRepository.saveAll(courses);

            System.out.println("✅ Successfully indexed " + courses.size() + " courses to Elasticsearch.");

        } catch (Exception e) {
            System.err.println("❌ Failed to index courses: " + e.getMessage());
        }
    }
}
