package ru.chipization.achip.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AreaAnalyticResponse {
    private Long totalQuantityAnimals;
    private Long totalAnimalsArrived;
    private Long totalAnimalsGone;
    private AnimalAnalyticResponse[] animalsAnalytics;
}
