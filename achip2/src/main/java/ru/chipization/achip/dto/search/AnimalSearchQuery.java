package ru.chipization.achip.dto.search;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AnimalSearchQuery {
    private Instant startDateTime;
    private Instant endDateTime;
    private Integer chipperId;
    private Long chippingLocationId;
    private String lifeStatus;
    private String gender;
    private Integer from;
    private Integer size;
}
