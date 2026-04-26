package com.chalmers.atas.api.taconstraint;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import com.chalmers.atas.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TAConstraintsResponse {

    private UserResponse ta;

    private UUID courseId;

    List<TAConstraintResponse> taConstraints;

    public static TAConstraintsResponse of(
            User ta,
            Course course,
            List<TACourseSessionConstraint> taConstraints) {
        return new TAConstraintsResponse(
                UserResponse.of(ta),
                course.getCourseId(),
                taConstraints.stream().map(TAConstraintResponse::of).toList()
        );
    }
}
