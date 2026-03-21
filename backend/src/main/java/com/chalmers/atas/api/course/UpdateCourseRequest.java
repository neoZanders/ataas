package com.chalmers.atas.api.course;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCourseRequest {

    private String description;

    @NotNull
    private Boolean canTASeeAllSchedules;

    @NotNull
    private Boolean canTACreateAnnouncements;
}
