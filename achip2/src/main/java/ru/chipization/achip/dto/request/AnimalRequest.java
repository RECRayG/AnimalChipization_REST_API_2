package ru.chipization.achip.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AnimalRequest {
    private Long[] animalTypes;
    private Float weight;
    private Float length;
    private Float height;
    private String gender;
    private Integer chipperId;
    private Long chippingLocationId;
}
