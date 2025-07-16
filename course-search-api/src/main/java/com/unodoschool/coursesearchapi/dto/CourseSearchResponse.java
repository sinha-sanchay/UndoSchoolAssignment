package com.unodoschool.coursesearchapi.dto;

import com.unodoschool.coursesearchapi.document.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CourseSearchResponse {

    private long total; // total hits

    private List<CourseDocument> courses; // list of matched courses
}
