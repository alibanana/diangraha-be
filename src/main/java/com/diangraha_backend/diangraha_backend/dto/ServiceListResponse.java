package com.diangraha_backend.diangraha_backend.dto;

import lombok.*;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder

    public class ServiceListResponse {
        private Long id;
        private String name;
        private String longDesc;
        private String imageUrl;
    }

