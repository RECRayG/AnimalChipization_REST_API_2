package ru.chipization.achip.dto.search;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AreaAnalyticSearchQuery {
    private String startDate;
    private String endDate;
}
