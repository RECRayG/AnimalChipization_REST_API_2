package ru.chipization.achip.compare;

import lombok.*;
import ru.chipization.achip.dto.request.LocationRequest;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Vector {
    private LocationRequest startPoint;
    private LocationRequest endPoint;
    private Double xComponent;
    private Double yComponent;

    public Vector(LocationRequest startPoint, LocationRequest endPoint) {
        super();

        this.startPoint = startPoint;
        this.endPoint = endPoint;

        xComponent = endPoint.getLongitude() - startPoint.getLongitude();
        yComponent = endPoint.getLatitude() - startPoint.getLatitude();
    }
}
