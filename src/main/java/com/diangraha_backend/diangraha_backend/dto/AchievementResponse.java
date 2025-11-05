package com.diangraha_backend.diangraha_backend.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private Instant createdAt;
}
