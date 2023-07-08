package ru.chipization.achip.dto.response;

import lombok.*;
import ru.chipization.achip.dto.request.LocationRequest;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AreaResponse {
    private Long id;
    private String name;
    private LocationRequest[] areaPoints;
}
