package com.chalmers.atas.api.announcement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateAnnouncementRequest {

    @NotBlank
    private String title;
    
    @NotBlank
    private String body;
    
    @NotNull
    private Boolean sendByEmail;
}
