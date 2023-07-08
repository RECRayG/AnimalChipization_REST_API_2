package ru.chipization.achip.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AreaRequest {
    private String name;
    private LocationRequest[] areaPoints;
}
