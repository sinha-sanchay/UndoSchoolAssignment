package com.unodoschool.coursesearchapi.repository;

import com.unodoschool.coursesearchapi.document.CourseDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

// This interface gives basic CRUD + search methods
public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {
}
