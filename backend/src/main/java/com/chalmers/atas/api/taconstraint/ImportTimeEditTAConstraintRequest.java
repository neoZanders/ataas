package com.chalmers.atas.api.taconstraint;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImportTimeEditTAConstraintRequest {

    @NotNull
    private String courseCode;
}
