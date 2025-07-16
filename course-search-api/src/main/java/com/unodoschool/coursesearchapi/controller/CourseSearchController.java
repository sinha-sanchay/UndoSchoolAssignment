package com.unodoschool.coursesearchapi.controller;

import com.unodoschool.coursesearchapi.dto.CourseSearchResponse;
import com.unodoschool.coursesearchapi.service.CourseSearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class CourseSearchController {

    private final CourseSearchService courseSearchService;

    public CourseSearchController(CourseSearchService courseSearchService) {
        this.courseSearchService = courseSearchService;
    }

    // This is the main search API with all supported filters and sorting
    @GetMapping
    public CourseSearchResponse searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String startDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        return courseSearchService.searchCourses(
                q, category, type, minAge, maxAge, minPrice, maxPrice, startDate, page, size, sort
        );
    }
}
