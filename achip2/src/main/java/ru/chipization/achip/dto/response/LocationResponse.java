package ru.chipization.achip.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class LocationResponse {
    private Long id;
    private Double latitude;
    private Double longitude;
}
