package ru.chipization.achip.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class VisitedLocationsRequest {
    private Long visitedLocationPointId;
    private Long locationPointId;
}
