package ru.chipization.achip.compare;

import lombok.*;
import ru.chipization.achip.dto.request.LocationRequest;

import java.text.DecimalFormat;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class LineSegment {
    private LocationRequest startPoint;
    private LocationRequest endPoint;

    // Псевдоскалярное произведение векторов
    private double vectorCrossProduct(Vector v1, Vector v2) {
        return v1.getXComponent() * v2.getYComponent() - v2.getXComponent() * v1.getYComponent();
    }

    // Проверка на проекции
    private boolean rangeIntersection(Double a, Double b, Double c, Double d) {
        // Поменяем местами точки, поскольку подразумевается, что начальная точка отрезка находится левее конечной
        if(a > b) {
            double temp = a;
            a = b;
            b = temp;
        }

        if(c > d) {
            double temp = c;
            c = d;
            d = temp;
        }

        // Проверка проекции на ось
        return Math.max(a,c) <= Math.min(b,d);
    }

    // Проверка необходимого условия пересечения 2-х отрезков: пересечение на проекциях осей X и Y
    private boolean boundingBox(LineSegment ab, LineSegment cd) {
        // Longitude = X; Latitude = Y;
        // Проверка на проекцию точек отрезка на ось Х
        boolean xRangeIntersection = rangeIntersection(ab.getStartPoint().getLongitude(), ab.getEndPoint().getLongitude(),
                cd.getStartPoint().getLongitude(), cd.getEndPoint().getLongitude());
        // Проверка на проекцию точек отрезка на ось Y
        boolean yRangeIntersection = rangeIntersection(ab.getStartPoint().getLatitude(), ab.getEndPoint().getLatitude(),
                cd.getStartPoint().getLatitude(), cd.getEndPoint().getLatitude());

        return xRangeIntersection && yRangeIntersection;
    }

    public boolean isIntersectionMy(LineSegment cd) {
        // Метод проверяет пересечение двух прямых (отрезков)

        // Если проекции не пересекаются, то и отрезки точно не пересекаются
        if(!boundingBox(this, cd)) {
            return false;
        }

        // Для удобства обозначим переменные в качестве точек
        LocationRequest abStart = this.startPoint;
        LocationRequest abEnd = this.endPoint;

        LocationRequest cdStart = cd.getStartPoint();
        LocationRequest cdEnd = cd.getEndPoint();

        // Поменяем местами точки, поскольку подразумевается, что начальная точка отрезка находится левее конечной
        if (abEnd.getLongitude() < abStart.getLongitude()) {
            LocationRequest tmp = abStart;
            abStart = abEnd;
            abEnd = tmp;
        }
        if (cdEnd.getLongitude() < cdStart.getLongitude()) {
            LocationRequest tmp = cdStart;
            cdStart = cdEnd;
            cdEnd = tmp;
        }

        // Построение векторов
        Vector vAB = new Vector(abStart, abEnd);
        Vector vAC = new Vector(abStart, cdStart);
        Vector vAD = new Vector(abStart, cdEnd);

        Vector vCD = new Vector(cdStart, cdEnd);
        Vector vCA = new Vector(cdStart, abStart);
        Vector vCB = new Vector(cdStart, abEnd);

        // Вычисление псевдоскалярного произведения векторов
        double d1 = vectorCrossProduct(vAB, vAC);
        double d2 = vectorCrossProduct(vAB, vAD);

        double d3 = vectorCrossProduct(vCD, vCA);
        double d4 = vectorCrossProduct(vCD, vCB);

        // Если знаки разные, значит пересечение есть
        boolean firstLine = ((d1 <= 0 && d2 >= 0) || (d1 >= 0 && d2 <= 0));
        boolean secondLine = ((d3 <= 0 && d4 >= 0) || (d3 >= 0 && d4 <= 0));

        return firstLine && secondLine;
    }

    public Optional<LocationRequest> getIntersectPoint(LineSegment cd) {
        // Метод возвращает точку пересечения двух прямых (отрезков)

        // Точность
        DecimalFormat df = new DecimalFormat("#.##########");

        // Для удобства обозначим переменные в качестве точек
        LocationRequest p1 = this.startPoint;
        LocationRequest p2 = this.endPoint;

        LocationRequest p3 = cd.getStartPoint();
        LocationRequest p4 = cd.getEndPoint();

        // Поменяем местами точки, поскольку подразумевается, что начальная точка отрезка находится левее конечной
        if (p2.getLongitude() < p1.getLongitude()) {
            LocationRequest tmp = p1;
            p1 = p2;
            p2 = tmp;
        }
        if (p4.getLongitude() < p3.getLongitude()) {
            LocationRequest tmp = p3;
            p3 = p4;
            p4 = tmp;
        }


        // Проверим существование потенциального интервала для точки пересечения отрезков
        if (p2.getLongitude() < p3.getLongitude()) {
            // У отрезков нету взаимной абсциссы
            return Optional.empty();
        }

        // Если оба отрезка вертикальные
        if((p1.getLongitude() - p2.getLongitude() == 0) && (p3.getLongitude() - p4.getLongitude() == 0)) {
            // Если они лежат на одном X
            if(p1.getLongitude() == p3.getLongitude()) {
                // Проверим пересекаются ли они, т.е. есть ли у них общий Y
                // для этого возьмём отрицание от случая, когда они НЕ пересекаются
                if (!((Math.max(p1.getLatitude(), p2.getLatitude()) < Math.min(p3.getLatitude(), p4.getLatitude())) ||
                        (Math.min(p1.getLatitude(), p2.getLatitude()) > Math.max(p3.getLatitude(), p4.getLatitude())))) {

                    return Optional.of(LocationRequest.builder()
                            .longitude(null)
                            .latitude(null)
                            .build());
                }
            }
            return Optional.empty();
        }

        // Найдём коэффициенты уравнений, содержащих отрезки:
        // f1(x) = A1*x + b1 = y
        // f2(x) = A2*x + b2 = y

        // 1 случай
        // Если первый отрезок вертикальный
        if (p1.getLongitude() - p2.getLongitude() == 0) {
            // Найдём Xa, Ya - точки пересечения двух прямых
            double Xa = p1.getLongitude();
            double A2 = (p3.getLatitude() - p4.getLatitude()) / (p3.getLongitude() - p4.getLongitude());
            double b2 = p3.getLatitude() - A2 * p3.getLongitude();
            double Ya = A2 * Xa + b2;

            if (p3.getLongitude() <= Xa && p4.getLongitude() >= Xa && Math.min(p1.getLatitude(), p2.getLatitude()) <= Ya &&
                    Math.max(p1.getLatitude(), p2.getLatitude()) >= Ya) {

                return Optional.of(LocationRequest.builder()
                        .longitude(Double.valueOf(df.format(Xa).replace(',', '.')))
                        .latitude(Double.valueOf(df.format(Ya).replace(',', '.')))
                        .build());
            }
            return Optional.empty();
        }

        // 2 случай
        // Если второй отрезок вертикальный
        if (p3.getLongitude() - p4.getLongitude() == 0) {
            // Найдём Xa, Ya - точки пересечения двух прямых
            double Xa = p3.getLongitude();
            double A1 = (p1.getLatitude() - p2.getLatitude()) / (p1.getLongitude() - p2.getLongitude());
            double b1 = p1.getLatitude() - A1 * p1.getLongitude();
            double Ya = A1 * Xa + b1;

            if (p1.getLongitude() <= Xa && p2.getLongitude() >= Xa && Math.min(p3.getLatitude(), p4.getLatitude()) <= Ya &&
                    Math.max(p3.getLatitude(), p4.getLatitude()) >= Ya) {

                return Optional.of(LocationRequest.builder()
                        .longitude(Double.valueOf(df.format(Xa).replace(',', '.')))
                        .latitude(Double.valueOf(df.format(Ya).replace(',', '.')))
                        .build());
            }
            return Optional.empty();
        }

        // 3 случай (общий)
        // Если оба отрезка невертикальные
        double A1 = (p1.getLatitude() - p2.getLatitude()) / (p1.getLongitude() - p2.getLongitude());
        double A2 = (p3.getLatitude() - p4.getLatitude()) / (p3.getLongitude() - p4.getLongitude());
        double b1 = p1.getLatitude() - A1 * p1.getLongitude();
        double b2 = p3.getLatitude() - A2 * p3.getLongitude();

        // Если отрезки параллельны
        if (A1 == A2) {
            return Optional.empty();
        }

        // Xa - абсцисса точки пересечения двух прямых
        double Xa = (b2 - b1) / (A1 - A2);

        // Ya - ордината точки пересечения двух прямых
        double Ya = A1 * Xa + b1;

        // Если точка Xa находится вне пересечения проекций отрезков на ось X
        if ((Xa < Math.max(p1.getLongitude(), p3.getLongitude())) ||
                (Xa > Math.min(p2.getLongitude(), p4.getLongitude())))
            return Optional.empty();
        else {
            return Optional.of(LocationRequest.builder()
                    .longitude(Double.valueOf(df.format(Xa).replace(',', '.')))
                    .latitude(Double.valueOf(df.format(Ya).replace(',', '.')))
                    .build());
        }
    }
}
