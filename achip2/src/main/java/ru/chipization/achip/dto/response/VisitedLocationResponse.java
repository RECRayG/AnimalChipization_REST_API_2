package ru.chipization.achip.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class VisitedLocationResponse {
    private Long id;
    private Instant dateTimeOfVisitLocationPoint;
    private Long locationPointId;
}
