package com.chalmers.atas.api.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateCourseRequest {
    @NotBlank
    private String courseCode;
}
