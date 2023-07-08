package ru.chipization.achip.dto.response;

import lombok.*;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AnimalResponse {
    private Long id;
    private Long[] animalTypes;
    private Float weight;
    private Float length;
    private Float height;
    private String gender;
    private String lifeStatus;
    private Instant chippingDateTime;
    private Integer chipperId;
    private Long chippingLocationId;
    private Long[] visitedLocations;
    private Instant deathDateTime;
}
