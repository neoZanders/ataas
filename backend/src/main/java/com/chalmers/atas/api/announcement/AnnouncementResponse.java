package com.chalmers.atas.api.announcement;

import java.time.Instant;
import java.util.UUID;

import com.chalmers.atas.domain.announcement.Announcement;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnnouncementResponse {

    private UUID id;

    private String title;

    private String body;

    private boolean sendByEmail;

    private Instant createdAt;

    public static AnnouncementResponse of(Announcement announcement){
        return new AnnouncementResponse(
            announcement.getAnnouncementId(), 
            announcement.getTitle(), 
            announcement.getAnnouncement(), 
            announcement.isSendByEmail(), 
            announcement.getCreatedAt()
        );
    }

}
