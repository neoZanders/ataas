package com.chalmers.atas.api.taconstraint;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import com.chalmers.atas.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TAConstraintsResponse {

    private UserResponse ta;

    List<TAConstraintResponse> taConstraints;

    public static TAConstraintsResponse of(
            User ta,
            List<TACourseSessionConstraint> taConstraints) {
        return new TAConstraintsResponse(
                UserResponse.of(ta),
                taConstraints.stream().map(TAConstraintResponse::of).toList()
        );
    }
}
