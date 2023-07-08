package ru.chipization.achip.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AnimalAnalyticResponse {
    private String animalType;
    private Long animalTypeId;
    private Long quantityAnimals;
    private Long animalsArrived;
    private Long animalsGone;
}
