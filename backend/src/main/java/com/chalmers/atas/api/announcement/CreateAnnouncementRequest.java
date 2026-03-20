package com.chalmers.atas.api.announcement;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateAnnouncementRequest {

    @NotBlank
    private String title;
    
    @NotBlank
    private String body;
    
    private boolean sendByEmail;
}
