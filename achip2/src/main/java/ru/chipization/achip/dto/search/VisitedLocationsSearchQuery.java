package ru.chipization.achip.dto.search;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class VisitedLocationsSearchQuery {
    private String startDateTime;
    private String endDateTime;
    private Integer from;
    private Integer size;
}
