package ru.chipization.achip.dto.request;

import lombok.*;
import ru.chipization.achip.compare.LineSegment;

import java.util.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class LocationRequest {
    private static Double epsilon = 0.0000000001;
    private Double longitude;
    private Double latitude;

    public boolean isLine(LocationRequest startPoint, LocationRequest endPoint) {
        // Метод вызывается у точки, которая проверяется на присутствие на прямой first -> second
        Double x1 = startPoint.getLongitude();
        Double y1 = startPoint.getLatitude();

        Double x2 = endPoint.getLongitude();
        Double y2 = endPoint.getLatitude();

        // Если площадь треугольника first -> this -> second = 0, то все точки лежат на одной прямой
        if ( (x1 - this.longitude)*(y2 - this.latitude) == (x2 - this.longitude)*(y1 - this.latitude))
            return true;
        else
            return false;
    }

    public boolean isBetween(LocationRequest startPoint, LocationRequest endPoint) {
        // Метод вызывается у точки, которая проверяется на присутствик на прямой first -> second
        Double x1 = startPoint.getLongitude();
        Double y1 = startPoint.getLatitude();

        Double x2 = endPoint.getLongitude();
        Double y2 = endPoint.getLatitude();

        // Если отрезок вертикальный
        if((x1 - x2 == 0)) {
            // Поменяем местами точки, поскольку подразумевается, что начальная точка отрезка находится ниже конечной
            if (y2 < y1) {
                Double temp = y1;
                y1 = y2;
                y2 = temp;
            }

            if(this.latitude >= y1 && this.latitude <= y2)
                return true;
            else
                return false;
        } // Если отрезок не вертикальный
        else {
            // Поменяем местами точки, поскольку подразумевается, что начальная точка отрезка находится левее конечной
            if (x2 < x1) {
                Double temp = x1;
                x1 = x2;
                x2 = temp;
            }

            if(this.longitude >= x1 && this.longitude <= x2)
                return true;
            else
                return false;
        }
    }

    public boolean isContainedAtProjection(Double minX, Double maxX, Double minY, Double maxY) {
        return (this.getLongitude() <= maxX && this.getLongitude() >= minX) &&
                (this.getLatitude() <= maxY && this.getLatitude() >= minY);
    }

    public boolean isContainedIntoAreaWithoutBorder(LocationRequest[] area) {
//        Метод перебирает каждый сегмент линии зоны и проверяет,
//        лежит ли точка на той же стороне каждой линии, что и остальная часть зоны.
//        Переменная 'c' отслеживает чётность количества пересечений сегментов прямого луча из переданной точки.
//        Если число пересечений нечётное, то точка находится внутри зоны.

        // Также проверка на соответствие проекциям
        Double _minX = Arrays.stream(area).min(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
        Double _maxX = Arrays.stream(area).max(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
        Double _minY = Arrays.stream(area).min(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();
        Double _maxY = Arrays.stream(area).max(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();

        boolean isProjection = isContainedAtProjection(_minX, _maxX, _minY, _maxY);

        int npoints = area.length;
        Double[] yp = Arrays.asList(area).stream().map(areaInside -> areaInside.getLatitude()).toArray(Double[]::new);
        Double[] xp = Arrays.asList(area).stream().map(areaInside -> areaInside.getLongitude()).toArray(Double[]::new);
        int i, j;
        boolean c = false;
        for (i = 0, j = npoints - 1; i < npoints; j = i++) {
            // Векторное произведение каждого сегмента линии и вектор от начальной точки сегмента линии до точки
            if ((((yp[i] <= this.getLatitude()) && (this.getLatitude() < yp[j])) || ((yp[j] <= this.getLatitude()) && (this.getLatitude() < yp[i]))) &&
                (this.getLongitude() <= (xp[j] - xp[i]) * (this.getLatitude() - yp[i]) / (yp[j] - yp[i]) + xp[i])) {

                c = !c;
            }
        }

        // Не учитывать нахождение точки на прямой
        LineSegment[] lines = convertToLines(area);
        for(i = 0; i < lines.length; i++) {
            // Если точка лежит на прямой
            if(this.isLine(lines[i].getStartPoint(), lines[i].getEndPoint())) {
                c = false;
                break;
            }
        }

        return c && isProjection;
    }

    public boolean isContainedIntoAreaWithBorder(LocationRequest[] area) {
//        Метод перебирает каждый сегмент линии зоны и проверяет,
//        лежит ли точка на той же стороне каждой линии, что и остальная часть зоны.
//        Переменная 'c' отслеживает чётность количества пересечений сегментов прямого луча из переданной точки.
//        Если число пересечений нечётное, то точка находится внутри зоны.

        // Также проверка на соответствие проекциям
        Double _minX = Arrays.stream(area).min(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
        Double _maxX = Arrays.stream(area).max(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
        Double _minY = Arrays.stream(area).min(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();
        Double _maxY = Arrays.stream(area).max(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();

        boolean isProjection = isContainedAtProjection(_minX, _maxX, _minY, _maxY);

        int npoints = area.length;
        Double[] yp = Arrays.asList(area).stream().map(areaInside -> areaInside.getLatitude()).toArray(Double[]::new);
        Double[] xp = Arrays.asList(area).stream().map(areaInside -> areaInside.getLongitude()).toArray(Double[]::new);
        int i, j;
        boolean c = false;
        for (i = 0, j = npoints - 1; i < npoints; j = i++) {
            // Векторное произведение каждого сегмента линии и вектор от начальной точки сегмента линии до точки
            if ((((yp[i] <= this.getLatitude()) && (this.getLatitude() < yp[j])) || ((yp[j] <= this.getLatitude()) && (this.getLatitude() < yp[i]))) &&
                    (this.getLongitude() <= (xp[j] - xp[i]) * (this.getLatitude() - yp[i]) / (yp[j] - yp[i]) + xp[i])) {

                c = !c;
            }
        }

        // Учитывать нахождение точки на прямой
        LineSegment[] lines = convertToLines(area);
        for(i = 0; i < lines.length; i++) {
            // Если точка лежит на прямой
            if(this.isLine(lines[i].getStartPoint(), lines[i].getEndPoint())) {
                c = true;
                break;
            }
        }

        return c && isProjection;
    }

    // Преобразвание точек в отрезки
    private LineSegment[] convertToLines(LocationRequest[] areaPoints) {
        List<LineSegment> listLines = new ArrayList<>();

        for(int i = 0; i < areaPoints.length; i++) {
            // Закончить крайней точкой
            if(i + 1 >= areaPoints.length) {
                listLines.add(LineSegment.builder()
                        .startPoint(LocationRequest.builder()
                                .longitude(areaPoints[areaPoints.length - 1].getLongitude())
                                .latitude(areaPoints[areaPoints.length - 1].getLatitude())
                                .build())
                        .endPoint(LocationRequest.builder()
                                .longitude(areaPoints[0].getLongitude())
                                .latitude(areaPoints[0].getLatitude())
                                .build())
                        .build());
                break;
            } // Последовательное добавление точек для прямой
            else {
                listLines.add(LineSegment.builder()
                        .startPoint(LocationRequest.builder()
                                .longitude(areaPoints[i].getLongitude())
                                .latitude(areaPoints[i].getLatitude())
                                .build())
                        .endPoint(LocationRequest.builder()
                                .longitude(areaPoints[i + 1].getLongitude())
                                .latitude(areaPoints[i + 1].getLatitude())
                                .build())
                        .build());
            }
        }

        return listLines.stream().toArray(LineSegment[]::new);
    }
}
