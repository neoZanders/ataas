package com.chalmers.atas.api.courseassignment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InviteTARequest {
    @NotBlank
    private String taEmail;
}
