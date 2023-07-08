package ru.chipization.achip.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.chipization.achip.compare.LineSegment;
import ru.chipization.achip.dto.request.AreaRequest;
import ru.chipization.achip.dto.request.LocationRequest;
import ru.chipization.achip.dto.response.*;
import ru.chipization.achip.dto.search.AreaAnalyticSearchQuery;
import ru.chipization.achip.dto.search.Request;
import ru.chipization.achip.dto.search.SearchRequest;
import ru.chipization.achip.exception.*;
import ru.chipization.achip.model.*;
import ru.chipization.achip.repository.*;
import ru.chipization.achip.service.FilterSpecification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/areas")
@AllArgsConstructor
public class AreasController {
    @Autowired
    private AreasRepository areasRepository;

    @Autowired
    private AAlIdentityRepository aAlIdentityRepository;

    @Autowired
    private AreaLocationsRepository areaLocationsRepository;

    @Autowired
    private VisitedLocationRepository visitedLocationRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AnimalTypesRepository animalTypesRepository;

    @Autowired
    private FilterSpecification<VisitedLocation> filterSpecification;

    private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/{areaId}")
    public ResponseEntity<?> getAreaById(@PathVariable Long areaId) {
        try {
            // Проверка на 400 - валидность данных
            if (areaId == null || areaId <= 0) {
                return ResponseEntity.status(400).body("Id: " + areaId + " is not correct");
            }

            // Проверка на 404 - существование зоны
            Optional<Areas> areaCheck = areasRepository.findById(areaId);
            if (areaCheck.isEmpty()) {
                return ResponseEntity.status(404).body("Area with id: " + areaId + " not found");
            }

            Areas area = areaCheck.get();

            LocationRequest[] areaPointsList = aAlIdentityRepository.findAAlIdentitiesByIdArea(area).get()
                    .stream()
                    .sorted(Comparator.comparing(AAlIdentity::getId))
                    .collect(Collectors.toList())
                    .stream().map(al -> {
                        return LocationRequest.builder()
                                .longitude(al.getIdAreaLocation().getLongitude())
                                .latitude(al.getIdAreaLocation().getLatitude())
                                .build();
                    }).collect(Collectors.toList()).toArray(LocationRequest[]::new);

            return new ResponseEntity<AreaResponse>(AreaResponse.builder()
                    .id(area.getIdArea())
                    .name(area.getName())
                    .areaPoints(areaPointsList)
                    .build(),
                    HttpStatus.OK);
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping()
    public ResponseEntity<?> addArea(@RequestBody AreaRequest insertArea) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {

                // Проверка на 400 - валидность атомарных данных
                if (insertArea.getName() == null || insertArea.getName().trim().equals("") ||
                        insertArea.getAreaPoints() == null || insertArea.getAreaPoints().length < 3) {

                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 400 - валидность элементов массива точек границ зоны
                try {
                    Arrays.stream(insertArea.getAreaPoints()).forEach(al -> {
                        if (al == null ||
                                al.getLatitude() == null || al.getLatitude() < -90 || al.getLatitude() > 90 ||
                                al.getLongitude() == null || al.getLongitude() < -180 || al.getLongitude() > 180) {
                            throw new BadRequestException("No valid element of array: " + al.toString());
                        }
                    });
                } catch (BadRequestException bre) {
                    return ResponseEntity.status(400).body(bre.getMessage());
                }

                // Проверка на 409 - Зона с таким name уже существует
                if (areasRepository.existsAreasByName(insertArea.getName())) {
                    return ResponseEntity.status(409).body("Area with name: " + insertArea.getName() + " already exist");
                }

                // Проверка на 400 - Новая зона имеет дубликаты точек
                if (Arrays.stream(insertArea.getAreaPoints()).distinct().count() != insertArea.getAreaPoints().length) {
                    return ResponseEntity.status(400).body("Area contains of duplicates");
                }

                // Проверка на 400 - Все точки лежат на одной прямой
                if (checkIsLine(insertArea.getAreaPoints()))
                    return ResponseEntity.status(400).body("Area points are collinear");

                ////(Начало) Проверка на 409 - Зона, состоящая из таких точек, уже существует
                // (При этом важен порядок, в котором указаны точки, но не важна начальная точка)
                // (1 -> 2 -> 3) = (2 -> 3 -> 1) - одна и та же зона (кольцевой, замкнутый список)

                // Выберем первую точку как точку отсчёта
                LocationRequest startPoint = insertArea.getAreaPoints()[0];

                // Получение всех коллекций, содержащих 1-ую точку, поскольку начальная точка не имеет значения при поиске
                // и массив точек имеет как минимум 3 точки (проверка до этого)
                List<Areas> searchingAreaPoints = null;

                Optional<List<Areas>> searchingAreaPointsCheck = areasRepository
                        .FindAllAreasByContainsPoint(startPoint.getLongitude(),
                                startPoint.getLatitude());
                if(!searchingAreaPointsCheck.isEmpty()) {
                    searchingAreaPoints = searchingAreaPointsCheck.get();
                }

                try {
                    searchingAreaPoints.forEach(area -> {
                        // Сравниваем всегда с первой позиции, поскольку 0-й элемент присутствует везде
                        int compareIndex = 1;
                        boolean isEqual = true;

                        // Гарантируется, что дубликатов точек внутри каждого массива не существует (согласно ТЗ)
                        LocationRequest[] currentArray = area.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                })
                                .toArray(LocationRequest[]::new);

                        // Имеет смысл сравнивать на идентичность только те зоны, у которых кол-во точек одинаковое,
                        // иначе это разные зоны
                        if (currentArray.length == insertArea.getAreaPoints().length) {
                            // Гарантируется, что искомая точка присутствует внутри каждого из массивов
                            int staticStartIndex = Arrays.asList(currentArray).indexOf(startPoint);

                            // Если начальная точка находится в конце
                            if (staticStartIndex + 1 == currentArray.length) {
                                // Перебор всех значений, за исключением последнего
                                for (int i = 0; i < currentArray.length - 1; i++, compareIndex++) {
                                    var point = LocationRequest.builder()
                                            .longitude(currentArray[i].getLongitude())
                                            .latitude(currentArray[i].getLatitude())
                                            .build();

                                    // Если есть хотя бы одно несовпадение, то они не равны
                                    if (!point.equals(insertArea.getAreaPoints()[compareIndex])) {
                                        isEqual = false;
                                        break;
                                    }
                                }
                            } // Если начальная точка находится в начале
                            else if (staticStartIndex == 0) {
                                // Перебор всех значений, за исключением первого
                                for (int i = 1; i < currentArray.length; i++, compareIndex++) {
                                    var point = LocationRequest.builder()
                                            .longitude(currentArray[i].getLongitude())
                                            .latitude(currentArray[i].getLatitude())
                                            .build();

                                    // Если есть хотя бы одно несовпадение, то они не равны
                                    if (!point.equals(insertArea.getAreaPoints()[compareIndex])) {
                                        isEqual = false;
                                        break;
                                    }
                                }
                            } // Если начальная точка находится в середине
                            else if (staticStartIndex > 0 && staticStartIndex + 1 < currentArray.length) {
                                // Перебор всех значений, с найденного индекса (не включительно) до конца массива
                                for (int i = staticStartIndex + 1; i < currentArray.length; i++, compareIndex++) {
                                    var point = LocationRequest.builder()
                                            .longitude(currentArray[i].getLongitude())
                                            .latitude(currentArray[i].getLatitude())
                                            .build();

                                    // Если есть хотя бы одно несовпадение, то они не равны
                                    if (!point.equals(insertArea.getAreaPoints()[compareIndex])) {
                                        isEqual = false;
                                        break;
                                    }
                                }

                                // Продолжаем перебор только в случае полного совпадения в правой части массива
                                if (isEqual) {
                                    // Перебор всех значений, с начала массива до найденного индекса (не включительно)
                                    for (int i = 0; i < staticStartIndex; i++, compareIndex++) {
                                        var point = LocationRequest.builder()
                                                .longitude(currentArray[i].getLongitude())
                                                .latitude(currentArray[i].getLatitude())
                                                .build();

                                        // Если есть хотя бы одно несовпадение, то они не равны
                                        if (!point.equals(insertArea.getAreaPoints()[compareIndex])) {
                                            isEqual = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (isEqual) {
                                throw new AlreadyExistException("Area '" + area.getName() + "' already exists with points: " + Arrays.toString(currentArray));
                            }
                        }
                    });
                } catch (AlreadyExistException aee) {
                    return ResponseEntity.status(409).body(aee.getMessage());
                }
                ////(Конец) Проверка на 409 - Зона, состоящая из таких точек, уже существует

                //!!! Далее гарантируется, что создаваемая зона не повторяет ни одну из существующих,
                // а если и точки одинаковые, то последовательность соединения другая

                // Проверка на 400 - Границы новой зоны пересекаются между собой
                if (checkIntersectionThemself(insertArea.getAreaPoints()))
                    return ResponseEntity.status(400).body("Area line segment intersects between themself");

                ////(Начало) Проверка на 400 - Границы новой зоны пересекают границы уже существующей зоны
                // Получение проекций зоны на оси
                // (гарантируется, что все проекции существуют, поскольку зона состоит из, как минимум, 3-х точек)
                Double minX = Arrays.stream(insertArea.getAreaPoints()).min(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
                Double maxX = Arrays.stream(insertArea.getAreaPoints()).max(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
                Double minY = Arrays.stream(insertArea.getAreaPoints()).min(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();
                Double maxY = Arrays.stream(insertArea.getAreaPoints()).max(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();

                // Получение всех зон, находящихся рядом с новой зоной
                // (только зоны, находящиеся внутри проекций на осях (включительно))
                List<Areas> searchingNearArea = null;

                Optional<List<Areas>> searchingNearAreaCheck = areasRepository.FindAllAreasByProjectionXY(minX, maxX, minY, maxY);
                if(!searchingNearAreaCheck.isEmpty()) {
                    searchingNearArea = searchingNearAreaCheck.get();
                }

                try {
                    // Обход всех найденных зон (гарантируется, что все зоны уникальны)
                    searchingNearArea.forEach(area -> {
                        // Гарантируется, что дубликатов точек внутри каждого массива не существует (согласно ТЗ)
                        // Преобразование зоны в массив точек
                        LocationRequest[] currentArray = area.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                })
                                .toArray(LocationRequest[]::new);

                        // Проверка на пересечение новой зоны с текущей
                        if (checkIntersection(insertArea.getAreaPoints(), currentArray))
                            throw new AlreadyExistException("Area line segment intersect another area line segment: " + area.getName() + " into " + insertArea.getName());
                    });
                } catch (AlreadyExistException aee) {
                    return ResponseEntity.status(400).body(aee.getMessage());
                }
                ///(Конец) Проверка на 400 - Границы новой зоны пересекают границы уже существующей зоны

                // Проверка на 400 - Граница новой зоны находятся внутри границ существующей зоны
                // Проверка на 400 - Границы существующей зоны находятся внутри границ новой зоны
                List<Areas> allAreas = areasRepository.findAll();
                try {
                    allAreas.forEach(area -> {
                        // Преобразование объекта в массив
                        LocationRequest[] currentArray = area.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                })
                                .toArray(LocationRequest[]::new);

                        // Проверка на нахождение точек новой зоны в существующей, и наоборот
                        try {
                            checkAreaInsideArea(insertArea.getAreaPoints(), currentArray);
                        } catch (NewAreaInsideExistAreaException nie) {
                            throw new NewAreaInsideExistAreaException("The NEW AREA is contained inside EXISTED AREA: " + insertArea.getName() + " into " + area.getName() + ", Point(" + nie.getMessage() + ")");
                        } catch (ExistAreaInsideNewAreaException ein) {
                            throw new ExistAreaInsideNewAreaException("The EXISTED AREA is contained inside NEW AREA: " + area.getName() + " into " + insertArea.getName() + ", Point(" + ein.getMessage() + ")");
                        }
                    });
                } catch (NewAreaInsideExistAreaException nie) {
                    return ResponseEntity.status(400).body(nie.getMessage());
                } catch (ExistAreaInsideNewAreaException ein) {
                    return ResponseEntity.status(400).body(ein.getMessage());
                }

                // На данном этапе гарантируется, что новая зона не содержится в другой, либо другая в ней,
                // а также гарантируется, что новая зона не пересекает ни одну из существующих.
                // Единственное, что осталось проверить - нахождение границ новой зоны на границах других (ложное копирование)

                // Проверка на 400 - Новая зона состоит из части точек существующей зоны и находится на площади существующей зоны
                try {
                    // Обход всех найденных зон (гарантируется, что все зоны уникальны)
                    searchingNearArea.forEach(area -> {
                        // Гарантируется, что дубликатов точек внутри каждого массива не существует (согласно ТЗ)
                        // Преобразование зоны в массив точек
                        LocationRequest[] currentArray = area.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                })
                                .toArray(LocationRequest[]::new);

                        // Проверка на нахождение новой зоны внутри другой
                        if (checkContainsAreaInsideArea(insertArea.getAreaPoints(), currentArray))
                            throw new AlreadyExistException("NEW AREA maybe contains points another area AND contained inside another area: " + area.getName() + " into " + insertArea.getName());
                    });
                } catch (AlreadyExistException aee) {
                    return ResponseEntity.status(400).body(aee.getMessage());
                }

                // Осталось добавить новую зону в базу
                // Сохранение точек новой зоны
                List<AreaLocations> areaLocationsList = new ArrayList<>();
                Arrays.stream(insertArea.getAreaPoints()).forEach(area -> {
                    areaLocationsList.add(areaLocationsRepository.save(AreaLocations.builder()
                            .longitude(area.getLongitude())
                            .latitude(area.getLatitude())
                            .build()));
                });

                // Сохранение новной зоны
                var area = Areas.builder().name(insertArea.getName()).build();

                // Установка связей между зоной и её точками
                List<AAlIdentity> aAlIdentityList = new ArrayList<>();
                areaLocationsList.forEach(areaLocation -> {
                    aAlIdentityList.add(AAlIdentity.builder()
                            .idArea(area)
                            .idAreaLocation(areaLocation)
                            .build());
                });

                area.setAAlIdentityCollection(aAlIdentityList);

                final Areas currentArea = areasRepository.save(area);

                return ResponseEntity.status(201).body(AreaResponse.builder()
                        .id(currentArea.getIdArea())
                        .name(currentArea.getName())
                        .areaPoints(currentArea.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                }).toArray(LocationRequest[]::new))
                        .build());
            } // USER & CHIPPER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/{areaId}")
    public ResponseEntity<?> updateArea(@PathVariable Long areaId, @RequestBody AreaRequest updateArea) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {
                // Проверка на 400 - валидность атомарных данных
                if (areaId == null || areaId <= 0 ||
                        updateArea.getName() == null || updateArea.getName().trim().equals("") ||
                        updateArea.getAreaPoints() == null || updateArea.getAreaPoints().length < 3) {

                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 404 - существование зоны
                Optional<Areas> areaCheck = areasRepository.findById(Long.valueOf(areaId));
                if (areaCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Area with id: " + areaId + " is not found");
                }
                Areas updatedArea = areaCheck.get();

                // Проверка на 400 - валидность элементов массива точек границ зоны
                try {
                    Arrays.stream(updateArea.getAreaPoints()).forEach(al -> {
                        if (al == null ||
                                al.getLatitude() == null || al.getLatitude() < -90 || al.getLatitude() > 90 ||
                                al.getLongitude() == null || al.getLongitude() < -180 || al.getLongitude() > 180) {

                            throw new BadRequestException("No valid element of array: " + al.toString());
                        }
                    });
                } catch (BadRequestException bre) {
                    return ResponseEntity.status(400).body(bre.getMessage());
                }

                // Проверка на 409 - Зона с таким name уже существует
                List<Areas> allAreas = areasRepository.findAll();
                try {
                    allAreas.forEach(area -> {
                        if (area.getName().equals(updateArea.getName()) && !area.getIdArea().equals(updatedArea.getIdArea())) {
                            throw new AlreadyExistException("Area with name: " + updateArea.getName() + " already exist");
                        }
                    });
                } catch (AlreadyExistException eae) {
                    return ResponseEntity.status(409).body(eae.getMessage());
                }

                // Проверка на 400 - Новая зона имеет дубликаты точек
                if (Arrays.stream(updateArea.getAreaPoints()).distinct().count() != updateArea.getAreaPoints().length) {
                    return ResponseEntity.status(400).body("Area contains of duplicates");
                }

                // Проверка на 400 - Все точки лежат на одной прямой
                if (checkIsLine(updateArea.getAreaPoints()))
                    return ResponseEntity.status(400).body("Area points are collinear");

                ////(Начало) Проверка на 409 - Зона, состоящая из таких точек, уже существует
                // (При этом важен порядок, в котором указаны точки, но не важна начальная точка)
                // (1 -> 2 -> 3) = (2 -> 3 -> 1) - одна и та же зона (кольцевой, замкнутый список)

                // Выберем первую точку как точку отсчёта
                LocationRequest startPoint = updateArea.getAreaPoints()[0];

                // Получение всех коллекций, содержащих 1-ую точку, поскольку начальная точка не имеет значения при поиске
                // и массив точек имеет как минимум 3 точки (проверка до этого)
                List<Areas> searchingAreaPoints = null;

                Optional<List<Areas>> searchingAreaPointsCheck = areasRepository
                        .FindAllAreasByContainsPoint(startPoint.getLongitude(),
                                startPoint.getLatitude());
                if(!searchingAreaPointsCheck.isEmpty()) {
                    searchingAreaPoints = searchingAreaPointsCheck.get();
                }

                try {
                    searchingAreaPoints.forEach(area -> {
                        // Сравниваем всегда с первой позиции, поскольку 0-й элемент присутствует везде
                        int compareIndex = 1;
                        boolean isEqual = true;

                        // Гарантируется, что дубликатов точек внутри каждого массива не существует (согласно ТЗ)
                        LocationRequest[] currentArray = area.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                })
                                .toArray(LocationRequest[]::new);

                        // Имеет смысл сравнивать на идентичность только те зоны, у которых кол-во точек одинаковое,
                        // иначе это разные зоны
                        if (currentArray.length == updateArea.getAreaPoints().length) {
                            // Гарантируется, что искомая точка присутствует внутри каждого из массивов
                            int staticStartIndex = Arrays.asList(currentArray).indexOf(startPoint);

                            // Если начальная точка находится в конце
                            if (staticStartIndex + 1 == currentArray.length) {
                                // Перебор всех значений, за исключением последнего
                                for (int i = 0; i < currentArray.length - 1; i++, compareIndex++) {
                                    var point = LocationRequest.builder()
                                            .longitude(currentArray[i].getLongitude())
                                            .latitude(currentArray[i].getLatitude())
                                            .build();

                                    // Если есть хотя бы одно несовпадение, то они не равны
                                    if (!point.equals(updateArea.getAreaPoints()[compareIndex])) {
                                        isEqual = false;
                                        break;
                                    }
                                }
                            } // Если начальная точка находится в начале
                            else if (staticStartIndex == 0) {
                                // Перебор всех значений, за исключением первого
                                for (int i = 1; i < currentArray.length; i++, compareIndex++) {
                                    var point = LocationRequest.builder()
                                            .longitude(currentArray[i].getLongitude())
                                            .latitude(currentArray[i].getLatitude())
                                            .build();

                                    // Если есть хотя бы одно несовпадение, то они не равны
                                    if (!point.equals(updateArea.getAreaPoints()[compareIndex])) {
                                        isEqual = false;
                                        break;
                                    }
                                }
                            } // Если начальная точка находится в середине
                            else if (staticStartIndex > 0 && staticStartIndex + 1 < currentArray.length) {
                                // Перебор всех значений, с найденного индекса (не включительно) до конца массива
                                for (int i = staticStartIndex + 1; i < currentArray.length; i++, compareIndex++) {
                                    var point = LocationRequest.builder()
                                            .longitude(currentArray[i].getLongitude())
                                            .latitude(currentArray[i].getLatitude())
                                            .build();

                                    // Если есть хотя бы одно несовпадение, то они не равны
                                    if (!point.equals(updateArea.getAreaPoints()[compareIndex])) {
                                        isEqual = false;
                                        break;
                                    }
                                }

                                // Продолжаем перебор только в случае полного совпадения в правой части массива
                                if (isEqual) {
                                    // Перебор всех значений, с начала массива до найденного индекса (не включительно)
                                    for (int i = 0; i < staticStartIndex; i++, compareIndex++) {
                                        var point = LocationRequest.builder()
                                                .longitude(currentArray[i].getLongitude())
                                                .latitude(currentArray[i].getLatitude())
                                                .build();

                                        // Если есть хотя бы одно несовпадение, то они не равны
                                        if (!point.equals(updateArea.getAreaPoints()[compareIndex])) {
                                            isEqual = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (isEqual && !area.getIdArea().equals(areaId)) {
                                throw new AlreadyExistException("Area '" + area.getName() + "' already exists with points: " + Arrays.toString(currentArray));
                            }
                        }
                    });
                } catch (AlreadyExistException aee) {
                    return ResponseEntity.status(409).body(aee.getMessage());
                }
                ////(Конец) Проверка на 409 - Зона, состоящая из таких точек, уже существует

                //!!! Далее гарантируется, что создаваемая зона не повторяет ни одну из существующих,
                // а если и точки одинаковые, то последовательность соединения другая

                // Проверка на 400 - Границы новой зоны пересекаются между собой
                if (checkIntersectionThemself(updateArea.getAreaPoints()))
                    return ResponseEntity.status(400).body("Area line segment intersects between themself");

                ////(Начало) Проверка на 400 - Границы новой зоны пересекают границы уже существующей зоны
                // Получение проекций зоны на оси
                // (гарантируется, что все проекции существуют, поскольку зона состоит из, как минимум, 3-х точек)
                Double minX = Arrays.stream(updateArea.getAreaPoints()).min(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
                Double maxX = Arrays.stream(updateArea.getAreaPoints()).max(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
                Double minY = Arrays.stream(updateArea.getAreaPoints()).min(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();
                Double maxY = Arrays.stream(updateArea.getAreaPoints()).max(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();

                // Получение всех зон, находящихся рядом с новой зоной
                // (только зоны, находящиеся внутри проекций на осях (включительно))
                List<Areas> searchingNearArea = null;

                Optional<List<Areas>> searchingNearAreaCheck = areasRepository.FindAllAreasByProjectionXY(minX, maxX, minY, maxY);
                if(!searchingNearAreaCheck.isEmpty()) {
                    searchingNearArea = searchingNearAreaCheck.get();

                    // Проверяем всё, кроме зоны, которую хотим обновить, если таковая имеется
                    searchingNearArea.remove(updatedArea);
                }

                try {
                    // Обход всех найденных зон (гарантируется, что все зоны уникальны)
                    searchingNearArea.forEach(area -> {
                        // Гарантируется, что дубликатов точек внутри каждого массива не существует (согласно ТЗ)
                        // Преобразование зоны в массив точек
                        LocationRequest[] currentArray = area.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                })
                                .toArray(LocationRequest[]::new);

                        // Проверка на пересечение новой зоны с текущей
                        if (checkIntersection(updateArea.getAreaPoints(), currentArray))
                            throw new AlreadyExistException("Area line segment intersect another area line segment: " + area.getName() + " into " + updateArea.getName());
                    });
                } catch (AlreadyExistException aee) {
                    return ResponseEntity.status(400).body(aee.getMessage());
                }
                ///(Конец) Проверка на 400 - Границы новой зоны пересекают границы уже существующей зоны

                // Проверка на 400 - Граница новой зоны находятся внутри границ существующей зоны
                // Проверка на 400 - Границы существующей зоны находятся внутри границ новой зоны
                // Проверяем всё, кроми зоны, которую хотим обновить
                allAreas.remove(updatedArea);
                try {
                    allAreas.forEach(area -> {
                        // Преобразование объекта в массив
                        LocationRequest[] currentArray = area.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                })
                                .toArray(LocationRequest[]::new);

                        // Проверка на нахождение точек новой зоны в существующей, и наоборот
                        try {
                            checkAreaInsideArea(updateArea.getAreaPoints(), currentArray);
                        } catch (NewAreaInsideExistAreaException nie) {
                            throw new NewAreaInsideExistAreaException("The NEW AREA is contained inside EXISTED AREA: " + updateArea.getName() + " into " + area.getName() + ", Point(" + nie.getMessage() + ")");
                        } catch (ExistAreaInsideNewAreaException ein) {
                            throw new ExistAreaInsideNewAreaException("The EXISTED AREA is contained inside NEW AREA: " + area.getName() + " into " + updateArea.getName() + ", Point(" + ein.getMessage() + ")");
                        }
                    });
                } catch (NewAreaInsideExistAreaException nie) {
                    return ResponseEntity.status(400).body(nie.getMessage());
                } catch (ExistAreaInsideNewAreaException ein) {
                    return ResponseEntity.status(400).body(ein.getMessage());
                }

                // На данном этапе гарантируется, что новая зона не содержится в другой, либо другая в ней,
                // а также гарантируется, что новая зона не пересекает ни одну из существующих.
                // Единственное, что осталось проверить - нахождение границ новой зоны на границах других (ложное копирование)

                // Проверка на 400 - Новая зона состоит из части точек существующей зоны и находится на площади существующей зоны
                try {
                    // Обход всех найденных зон (гарантируется, что все зоны уникальны)
                    searchingNearArea.forEach(area -> {
                        // Гарантируется, что дубликатов точек внутри каждого массива не существует (согласно ТЗ)
                        // Преобразование зоны в массив точек
                        LocationRequest[] currentArray = area.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                })
                                .toArray(LocationRequest[]::new);

                        // Проверка на нахождение новой зоны внутри другой
                        if (checkContainsAreaInsideArea(updateArea.getAreaPoints(), currentArray))
                            throw new AlreadyExistException("NEW AREA maybe contains points another area AND contained inside another area: " + area.getName() + " into " + updateArea.getName());
                    });
                } catch (AlreadyExistException aee) {
                    return ResponseEntity.status(400).body(aee.getMessage());
                }

                // Осталось обновить зону в базе

                // Удаление всех связей и точек старой зоны, с сохранением id и name этой зоны,
                // поскольку количество точек и их расположение может кардинально отличаться от текущих
                List<AAlIdentity> tempSave = new ArrayList<>(updatedArea.getAAlIdentityCollection());

                updatedArea.setAAlIdentityCollection(new ArrayList<>());
                areasRepository.save(updatedArea);

                aAlIdentityRepository.deleteAll(tempSave);

                tempSave.forEach(identity -> {
                    areaLocationsRepository.delete(identity.getIdAreaLocation());
                });

                // Сохранение точек новой зоны
                List<AreaLocations> areaLocationsList = new ArrayList<>();
                Arrays.stream(updateArea.getAreaPoints()).forEach(point -> {
                    areaLocationsList.add(areaLocationsRepository.save(AreaLocations.builder()
                            .longitude(point.getLongitude())
                            .latitude(point.getLatitude())
                            .build()));
                });

                // Установка связей между зоной и её точками
                List<AAlIdentity> aAlIdentityList = new ArrayList<>();
                areaLocationsList.forEach(areaLocation -> {
                    aAlIdentityList.add(AAlIdentity.builder()
                            .idArea(updatedArea)
                            .idAreaLocation(areaLocation)
                            .build());
                });

                updatedArea.setAAlIdentityCollection(aAlIdentityList);
                updatedArea.setName(updateArea.getName());

                final Areas currentArea = areasRepository.save(updatedArea);

                return ResponseEntity.status(200).body(AreaResponse.builder()
                        .id(currentArea.getIdArea())
                        .name(currentArea.getName())
                        .areaPoints(currentArea.getAAlIdentityCollection().stream()
                                .sorted(Comparator.comparing(AAlIdentity::getId))
                                .collect(Collectors.toList())
                                .stream().map(al -> {
                                    return LocationRequest.builder()
                                            .longitude(al.getIdAreaLocation().getLongitude())
                                            .latitude(al.getIdAreaLocation().getLatitude())
                                            .build();
                                }).toArray(LocationRequest[]::new))
                        .build());
            } // USER & CHIPPER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @DeleteMapping("/{areaId}")
    public ResponseEntity<?> deleteArea(@PathVariable Long areaId) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {

                // Проверка на 400 - валидность данных
                if (areaId == null || areaId <= 0) {
                    return ResponseEntity.status(400).body("Id: " + areaId + " is not correct");
                }

                // Проверка на 404 - существование зоны
                Optional<Areas> areaCheck = areasRepository.findById(areaId);
                if (areaCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Area with id: " + areaId + " is not found");
                }
                Areas area = areaCheck.get();

                // Сохранение в локальной памяти списка точек, относящихся к зоне
                List<AreaLocations> areaLocationsList = new ArrayList<>();
                area.getAAlIdentityCollection().forEach(a -> {
                    areaLocationsList.add(a.getIdAreaLocation());
                });

                // Удаление зоны
                areasRepository.delete(area);

                // Удаление точек зоны из таблицы-справочника
                areaLocationsList.forEach(al -> {
                    areaLocationsRepository.delete(al);
                });

                return ResponseEntity.status(200).body("Successful removal");
            } // USER & CHIPPER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    private boolean checkIsLine(LocationRequest[] areaPoints) {
        // Гарантируется, что точки не совпадают
        if(areaPoints.length >= 3) {
            LocationRequest startPoint = areaPoints[0];
            LocationRequest endPoint = areaPoints[areaPoints.length - 1];

            try {
                Arrays.asList(areaPoints).forEach(point -> {
                    // Подставлять нужно промежуточные точки
                    if(!(point.equals(startPoint) || point.equals(endPoint))) {
                        // Если хотя бы 1 точка не лежит на прямой, то
                        // не все точки лежат на одной прямой
                        if(!point.isLine(startPoint, endPoint)) {
                            throw new NotFoundException("");
                        }
                    }
                });
            } catch(NotFoundException nfe) {
                return false;
            }
        }

        return true;
    }

    private boolean checkIntersection(LocationRequest[] newArea, LocationRequest[] existArea) {
        // Преобразуем точки в отрезки
        LineSegment[] newLines = convertToLines(newArea);
        LineSegment[] existLines = convertToLines(existArea);

        // Алгоритм Бентли-Оттмана
        // Находим все точки пересечения отрезков newArea и existArea
        List<LocationRequest> intersectionPoints = new ArrayList<>();
        for (int i = 0; i < newLines.length; i++) {
            for (int j = 0; j < existLines.length; j++) {
                // Поиск точки пересечения для 2-х отрезков
                Optional<LocationRequest> checkLR = newLines[i].getIntersectPoint(existLines[j]);
                // Если точка пересечения существует
                if(!checkLR.isEmpty() &&
                        checkLR.get().getLatitude() != null &&
                        checkLR.get().getLongitude() != null) {
                    intersectionPoints.add(checkLR.get());
                }
            }
        }

        // Сортируем точки пересечения в порядке возрастания по координате X,
        // но также рассмотрим частный случай по Y.
        // Также производим отсеивание повторных точек
        intersectionPoints = intersectionPoints.stream()
                .sorted((p1, p2) -> {
                    if (p1.getLongitude() < p2.getLongitude()) {
                        return -1;
                    } else if (p1.getLongitude() > p2.getLongitude()) {
                        return 1;
                    } else {
                        if (p1.getLatitude() < p2.getLatitude()) {
                            return -1;
                        } else if (p1.getLatitude() > p2.getLatitude()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                })
                .distinct()
                .collect(Collectors.toList());

        // Перебираем пары смежных пересечений и проверяем среднюю точку внутри зоны
        for (int i = 0; i < intersectionPoints.size() - 1; i++) {
            LocationRequest p1 = intersectionPoints.get(i);
            LocationRequest p2 = intersectionPoints.get(i + 1);
            LocationRequest midpoint = LocationRequest.builder()
                    .longitude((p1.getLongitude() + p2.getLongitude()) * 0.5)
                    .latitude((p1.getLatitude() + p2.getLatitude()) * 0.5)
                    .build();

            // Если средняя точка содержится внутри новой или существующей зоны
            // (с учётом соприкосновений линий или точек на границах)
            if(midpoint.isContainedIntoAreaWithoutBorder(newArea)/* && midpoint.isContainedIntoArea(existArea)*/) {
                return true;
            }
        }

        return false;
    }

    private void checkAreaInsideArea(LocationRequest[] newArea, LocationRequest[] existArea) {
        // Проверяем, находится ли какая-либо вершина newArea внутри existArea
        // (без учёта соприкосновений линий или точек на границах)
        for (int i = 0; i < newArea.length; i++) {
            if(newArea[i].isContainedIntoAreaWithoutBorder(existArea)) {
                throw new NewAreaInsideExistAreaException("Longitude: " + newArea[i].getLongitude() + ", Latitude: " + newArea[i].getLatitude());
            }
        }

        // Проверяем, находится ли какая-либо вершина existArea внутри newArea
        // (без учёта соприкосновений линий или точек на границах)
        for (int i = 0; i < existArea.length; i++) {
            if(existArea[i].isContainedIntoAreaWithoutBorder(newArea)) {
                throw new ExistAreaInsideNewAreaException("Longitude: " + existArea[i].getLongitude() + ", Latitude: " + existArea[i].getLatitude());
            }
        }
    }

    private boolean checkContainsAreaInsideArea(LocationRequest[] newArea, LocationRequest[] existArea) {
        // Преобразуем точки в отрезки
        LineSegment[] newLines = convertToLines(newArea);
//        LineSegment[] existLines = convertToLines(existArea);

        // Перебираем пары смежных пересечений и проверяем среднюю точку внутри зоны
        for (int i = 0; i < newLines.length; i++) {
            LocationRequest midpoint = LocationRequest.builder()
                    .longitude((newLines[i].getStartPoint().getLongitude() + newLines[i].getEndPoint().getLongitude()) * 0.5)
                    .latitude((newLines[i].getStartPoint().getLatitude() + newLines[i].getEndPoint().getLatitude()) * 0.5)
                    .build();

            // Если средняя точка содержится внутри существующей зоны
            if(midpoint.isContainedIntoAreaWithoutBorder(existArea)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkIntersectionThemself(LocationRequest[] areaPoints) {
        if(areaPoints.length >= 3) {
            LineSegment[] lines = convertToLines(areaPoints);

            // Реализация рекурсией (каждый отрезок сравнивается с каждым ровно 1 раз, без повторов)
            return checkIntersectionThemselfRecursion(lines, 0, 1);
        }

        return false;
    }

    private boolean checkIntersectionThemselfRecursion(LineSegment[] lines, int i, int j) {
        // Если перебрали весь массив, то пересечений нет
        if (i == lines.length - 1 && j == lines.length) {
            return false;
        }

        // Если второй индекс достиг конца массива
        if (j == lines.length) {
            return checkIntersectionThemselfRecursion(lines, i + 1, i + 2);
        } else {
            if(lines[i].isIntersectionMy(lines[j])) {
                // Если у отрезков есть общая точка (начало первого, начало второго)
                if((lines[i].getStartPoint().equals(lines[j].getStartPoint()))) {
                    // Если отрезки находятся на одной прямой прямой
                    // и первая точка не находится в пределах общего отрезка
                    if(lines[j].getEndPoint().isLine(lines[i].getStartPoint(), lines[i].getEndPoint()) &&
                        !(lines[j].getEndPoint().isBetween(lines[i].getEndPoint(), lines[j].getEndPoint()))) {
                        return true;
                    } else
                        return checkIntersectionThemselfRecursion(lines, i, j + 1);
                }

                // Если у отрезков есть общая точка (конец первого, начало второго)
                if(lines[i].getEndPoint().equals(lines[j].getStartPoint())) {
                    // Если отрезки находятся на одной прямой прямой
                    // и первая точка не находится в пределах общего отрезка
                    if(lines[j].getEndPoint().isLine(lines[i].getStartPoint(), lines[i].getEndPoint()) &&
                        !(lines[j].getEndPoint().isBetween(lines[i].getStartPoint(), lines[j].getEndPoint()))) {
                        return true;
                    } else
                        return checkIntersectionThemselfRecursion(lines, i, j + 1);
                }

                // Если у отрезков есть общая точка (начало первого, конец второго)
                if(lines[i].getStartPoint().equals(lines[j].getEndPoint())) {
                    // Если вторая точка лежит на первой прямой
                    // и первая точка не находится в пределах общего отрезка
                    if(lines[j].getStartPoint().isLine(lines[i].getStartPoint(), lines[i].getEndPoint()) &&
                        !(lines[j].getEndPoint().isBetween(lines[j].getStartPoint(), lines[i].getEndPoint()))) {
                        return true;
                    } else
                        return checkIntersectionThemselfRecursion(lines, i, j + 1);
                }

                // Если у отрезков есть общая точка (конец первого, конец второго)
                if(lines[i].getEndPoint().equals(lines[j].getEndPoint())) {
                    // Если вторая точка лежит на первой прямой
                    // и первая точка не находится в пределах первой прямой
                    if(lines[j].getStartPoint().isLine(lines[i].getStartPoint(), lines[i].getEndPoint()) &&
                        !(lines[j].getEndPoint().isBetween(lines[i].getStartPoint(), lines[j].getStartPoint()))) {
                        return true;
                    } else
                        return checkIntersectionThemselfRecursion(lines, i, j + 1);
                }

                return true;
            } else {
                return checkIntersectionThemselfRecursion(lines, i, j + 1);
            }
        }
    }

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


    @GetMapping("/{areaId}/analytics")
    public ResponseEntity<?> getAreaAnalytics(@PathVariable Long areaId, @ModelAttribute AreaAnalyticSearchQuery areaAnalyticSearchQuery) {
        try {
            // Проверка на 400 - валидность атомарных данных
            if (areaId == null || areaId <= 0) {
                return ResponseEntity.status(400).body("No valid data");
            }

            // Проверка на 404 - существование зоны
            Optional<Areas> areaCheck = areasRepository.findById(areaId);
            if (areaCheck.isEmpty()) {
                return ResponseEntity.status(404).body("Area with id: " + areaId + " not found");
            }
            Areas area = areaCheck.get();

            Request request = new Request(new ArrayList<>());

            LocalDate startDate = null;
            LocalDate endDate = null;
            Boolean startTime = false;
            Boolean endTime = false;

            // Обработка критерия поиска "startDate", если таковой имеется
            if (areaAnalyticSearchQuery.getStartDate() != null && !areaAnalyticSearchQuery.getStartDate().trim().equals("")) {
                try {
                    startDate = LocalDate.parse(areaAnalyticSearchQuery.getStartDate(), dateFormatter);
                } catch (DateTimeParseException dtpe) {
                    return ResponseEntity.status(400).body("startDate is not to format ISO-8601: yyyy-MM-dd");
                }

                startTime = true;
                request.getSearchRequest().add(new SearchRequest("dateTimeOfVisitLocationPoint", startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            // Обработка критерия поиска "endDate", если таковой имеется
            if (areaAnalyticSearchQuery.getEndDate() != null && !areaAnalyticSearchQuery.getEndDate().trim().equals("")) {
                try {
                    endDate = LocalDate.parse(areaAnalyticSearchQuery.getEndDate(), dateFormatter);
                } catch (DateTimeParseException dtpe) {
                    return ResponseEntity.status(400).body("endDate is not to format ISO-8601: yyyy-MM-dd");
                }

                endTime = true;
                request.getSearchRequest().add(new SearchRequest("dateTimeOfVisitLocationPoint", endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            // Проверка на правильный диапазон даты
            if (startTime && endTime) {
                if (startDate.isAfter(endDate) || startDate.equals(endDate)) {
                    return ResponseEntity.status(400).body("startTime >= endTime");
                }
            }

            // Поиск всех точек посещения в заданный промежуток времени
            Specification<VisitedLocation> visitedLocationSpecification = filterSpecification
                    .getSearchSpecificationVisitedLocation(request.getSearchRequest(), startTime, endTime);

            List<VisitedLocation> visitedLocations = visitedLocationRepository.findAll(visitedLocationSpecification);

            // Если точка посещения находится внутри проекций зоны на оси,
            // значит, это животное нам нужно для последующей аналитики
            LocationRequest[] areaPoints = area.getAAlIdentityCollection().stream().map(identity -> {
                return LocationRequest.builder()
                        .longitude(identity.getIdAreaLocation().getLongitude())
                        .latitude(identity.getIdAreaLocation().getLatitude())
                        .build();
            }).toArray(LocationRequest[]::new);
            // Получение проекций на оси
            Double minX = Arrays.stream(areaPoints).min(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
            Double maxX = Arrays.stream(areaPoints).max(Comparator.comparingDouble(LocationRequest::getLongitude)).get().getLongitude();
            Double minY = Arrays.stream(areaPoints).min(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();
            Double maxY = Arrays.stream(areaPoints).max(Comparator.comparingDouble(LocationRequest::getLatitude)).get().getLatitude();

            visitedLocations = visitedLocations.stream()
                    .filter(vl -> {
                        if (vl.getIdChippingLocation().getLongitude() >= minX && vl.getIdChippingLocation().getLongitude() <= maxX &&
                                vl.getIdChippingLocation().getLatitude() >= minY && vl.getIdChippingLocation().getLatitude() <= maxY) {
                            return true;
                        } else
                            return false;
                    })
                    .collect(Collectors.toList());

            // Создание списка животных - виновников этих посещений (строго на проекциях зоны)
            List<Animal> animalsList = visitedLocations.stream().map(vl -> {
                return vl.getIdAnimal();
            }).collect(Collectors.toList());

            // Создание новой спецификации для фильтра
            request.getSearchRequest().clear();

            request.getSearchRequest().add(new SearchRequest("chippingDateTime", startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            request.getSearchRequest().add(new SearchRequest("chippingDateTime", endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            // Поиск всех точек чипирования животных в заданный промежуток времени
            Specification<Animal> animalsSpecification = filterSpecification
                    .getSearchSpecificationAnimal(request.getSearchRequest(), startTime, endTime);

            // Добавление животных с точкой чипирования к найденным животным по точкам перемещения (строго на проекциях зоны)
            List<Animal> animalTempPointChipping = animalRepository.findAll(animalsSpecification);
            animalTempPointChipping = animalTempPointChipping.stream()
                    .filter(animal -> {
                        if (animal.getIdChippingLocation().getLongitude() >= minX && animal.getIdChippingLocation().getLongitude() <= maxX &&
                                animal.getIdChippingLocation().getLatitude() >= minY && animal.getIdChippingLocation().getLatitude() <= maxY) {
                            return true;
                        } else
                            return false;
                    })
                    .collect(Collectors.toList());
            animalsList.addAll(animalTempPointChipping);
            // Удаление дубликатов
            animalsList = animalsList.stream().distinct().collect(Collectors.toList());

            // К этому моменту у меня есть полный список животных,
            // которые перемещались или были чипированы в заданный промежуток времени
            // на проекциях зоны

            // Алгоритм поиска вхождений в зону:
            // --Нужно найти на пути животного такие 2 точки, чтобы 1 была внутри зоны, 2 была снаружи или на границе,
            //   что будет эквивалентно выходу животного из зоны.
            // --При этом, точки посещения должны удовлетворять условию поиска по времени.
            // --Эти точки создадут прямую.
            // --Если количество таких прямых чётно, то животное находится вне зоны, если нечётно - внутри зоны.
            // --Если количество таких прямых равно 0, а точка есть, значит, либо животное находилось и перемещалось
            //   внутри зоны всегда, либо не перемещалось вовсе, но было чипировано там,
            //   либо от края зоны до края зоны было перемещение, без промежуточных остановок внутри зоны.

            final boolean start = startTime;
            final boolean end = endTime;
            final Instant startDateFinal = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            final Instant endDateFinal = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Map<String, Long[]> calculateEachType = new HashMap<>();
            AtomicLong totalQuantityAnimals = new AtomicLong();
            AtomicLong totalAnimalsArrived = new AtomicLong();
            AtomicLong totalAnimalsGone = new AtomicLong();

            animalsList.forEach(animal -> {
                // Получение массива точек для текущего животного, удовлетворяющего параметрам интервала времени
                List<ChippingLocation> currentAnimalWalk = animal.getVisitedLocationCollection().stream()
                        .filter(vl -> {
                            // Если указаны обе даты: начала и конца
                            if (start && end) {
                                // Если точка посещения удовлетворяет интервалу времени
                                if ((vl.getDateTimeOfVisitLocationPoint().isAfter(startDateFinal) ||
                                        vl.getDateTimeOfVisitLocationPoint().equals(startDateFinal)) &&
                                        (vl.getDateTimeOfVisitLocationPoint().isBefore(endDateFinal) ||
                                                vl.getDateTimeOfVisitLocationPoint().equals(endDateFinal))
                                ) {
                                    return true;
                                } else
                                    return false;
                            } // Если указана 1 дата: начала
                            else if (start && !end) {
                                // Если точка посещения удовлетворяет интервалу времени
                                if (vl.getDateTimeOfVisitLocationPoint().isAfter(startDateFinal) ||
                                        vl.getDateTimeOfVisitLocationPoint().equals(startDateFinal)
                                ) {
                                    return true;
                                } else
                                    return false;
                            } // Если указана 1 дата: конца
                            else if (!start && end) {
                                // Если точка посещения удовлетворяет интервалу времени
                                if (vl.getDateTimeOfVisitLocationPoint().isBefore(endDateFinal) ||
                                        vl.getDateTimeOfVisitLocationPoint().equals(endDateFinal)
                                ) {
                                    return true;
                                } else
                                    return false;
                            } // Если не указана ни одна из дат
                            else {
                                return true;
                            }
                        })
                        .sorted(Comparator.comparing(VisitedLocation::getId))
                        .map(vl -> {
                            return vl.getIdChippingLocation();
                        }).collect(Collectors.toList());

                // Вставка в начало точки чипирования, если она удовлетворяет условиям
                if (start && end) {
                    // Если точка посещения удовлетворяет интервалу времени
                    if ((animal.getChippingDateTime().isAfter(startDateFinal) ||
                            animal.getChippingDateTime().equals(startDateFinal)) &&
                            (animal.getChippingDateTime().isBefore(endDateFinal) ||
                                    animal.getChippingDateTime().equals(endDateFinal))
                    ) {
                        currentAnimalWalk.add(0, animal.getIdChippingLocation());
                    }
                } // Если указана 1 дата: начала
                else if (start && !end) {
                    // Если точка посещения удовлетворяет интервалу времени
                    if (animal.getChippingDateTime().isAfter(startDateFinal) ||
                            animal.getChippingDateTime().equals(startDateFinal)
                    ) {
                        currentAnimalWalk.add(0, animal.getIdChippingLocation());
                    }
                } // Если указана 1 дата: конца
                else if (!start && end) {
                    // Если точка посещения удовлетворяет интервалу времени
                    if (animal.getChippingDateTime().isBefore(endDateFinal) ||
                            animal.getChippingDateTime().equals(endDateFinal)
                    ) {
                        currentAnimalWalk.add(0, animal.getIdChippingLocation());
                    }
                } // Если не указана ни одна из дат
                else {
                    currentAnimalWalk.add(0, animal.getIdChippingLocation());
                }

                LocationRequest[] animalPointsInterval = currentAnimalWalk.stream().map(cl -> {
                    return LocationRequest.builder().longitude(cl.getLongitude()).latitude(cl.getLatitude()).build();
                }).toArray(LocationRequest[]::new);

                // К этому моменту в цикле у меня есть список точек перемещений животного в заданный интервал времени

                // Расчёт посещений, выходов и присутствия внутри зоны.
                // Может быть такое, что в проекции попадает, а в зону нет
                Long[] tempCalculate = calculateParameters(areaPoints, animalPointsInterval);

                // Записать общие параметры
                if (tempCalculate[0] == 1L)
                    totalQuantityAnimals.addAndGet(1L);
                if (tempCalculate[1] == 1L)
                    totalAnimalsArrived.addAndGet(1L);
                if (tempCalculate[2] == 1L)
                    totalAnimalsGone.addAndGet(1L);

                // Логика заполнения (обновления) Map для отдельно взятого типа животного
                animal.getAAtIdentityCollection().forEach(at -> {
                    // Если для данного типа животного ещё нет записи в Map и это животное внутри зоны бывало
                    if (!calculateEachType.containsKey(at.getIdAnimalType().getType())) {
                        calculateEachType.put(at.getIdAnimalType().getType(), new Long[]{0L, 0L, 0L});

                        calculateEachType.get(at.getIdAnimalType().getType())[0] =
                                calculateEachType.get(at.getIdAnimalType().getType())[0] + tempCalculate[0];

                        // Если вход и выход были учтены, то повторно не учитывать.
                        // для каждого животного учитывается 1 вход и 1 выход из зоны по ТЗ
                        // но у животного может быть несколько типов, так что тут всё учитываем
                        calculateEachType.get(at.getIdAnimalType().getType())[1] =
                                calculateEachType.get(at.getIdAnimalType().getType())[1] + tempCalculate[1];

                        calculateEachType.get(at.getIdAnimalType().getType())[2] =
                                calculateEachType.get(at.getIdAnimalType().getType())[2] + tempCalculate[2];
                    } // Если ключ с таким типом есть
                    else {
                        calculateEachType.get(at.getIdAnimalType().getType())[0] =
                                calculateEachType.get(at.getIdAnimalType().getType())[0] + tempCalculate[0];

                        // Если вход и выход были учтены, то повторно не учитывать.
                        // для каждого животного учитывается 1 вход и 1 выход из зоны по ТЗ,
                        // но у животного может быть несколько типов, так что тут всё учитываем
                        calculateEachType.get(at.getIdAnimalType().getType())[1] =
                                calculateEachType.get(at.getIdAnimalType().getType())[1] + tempCalculate[1];

                        calculateEachType.get(at.getIdAnimalType().getType())[2] =
                                calculateEachType.get(at.getIdAnimalType().getType())[2] + tempCalculate[2];
                    }
                });
            });

            // Заполнение массивов для вывода информации
            List<AnimalType> allTypes = animalTypesRepository.findAll();
            List<AnimalAnalyticResponse> animalAnalyticResponseList = new ArrayList<>();
            allTypes.forEach(type -> {
                // Если в Map есть текущий тип
                if (calculateEachType.containsKey(type.getType())) {
                    animalAnalyticResponseList.add(AnimalAnalyticResponse.builder()
                            .animalType(type.getType())
                            .animalTypeId(type.getId())
                            .quantityAnimals(calculateEachType.get(type.getType())[0])
                            .animalsArrived(calculateEachType.get(type.getType())[1])
                            .animalsGone(calculateEachType.get(type.getType())[2])
                            .build());
                }
            });

            return ResponseEntity.status(200).body(AreaAnalyticResponse.builder()
                    .totalQuantityAnimals(totalQuantityAnimals.get())
                    .totalAnimalsArrived(totalAnimalsArrived.get())
                    .totalAnimalsGone(totalAnimalsGone.get())
                    .animalsAnalytics(animalAnalyticResponseList.stream().toArray(AnimalAnalyticResponse[]::new))
                    .build());
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    private Long[] calculateParameters(LocationRequest[] areaPoints, LocationRequest[] animalPoints) {
        // Создание массива с 3 параметрами:
        // 1 - Находится ли данное животное внутри зоны ( 0 - нет, 1 - да )
        // 2 - Прибыло ли это животное в зону ( 0 - нет, 1 - да )
        // 3 - Выходило ли это животное из зоны ( 0 - нет, 1 - да )
        Long[] tempCalculate = new Long[]{0L, 0L, 0L};

        // Проверка на 1 параметр
//        boolean midIntersect = checkIntersection(areaPoints, animalPoints);
//        for (int i = 0; i < animalPoints.length; i++) {
            // Если хотя бы 1 посещённая точка или точка чипирования находится внутри зоны,
            // значит животное точно было внутри данной зоны в установленный интервал времени
            // (без учёта нахождения точки на границах зоны)
            // ИЛИ
            // Если, например, животное перешло от границы зоны к другой грнаце этой же зоны
            // через данную зону в заданный интервал времени - тоже учесть (расчёт по средней точке)

//        }

        // Если самая последняя посещённая точка животного внутри зоны или на границах
        if(animalPoints[animalPoints.length - 1].isContainedIntoAreaWithBorder(areaPoints)) {
            tempCalculate[0] = 1L;
        }

        // Если животное ни разу не было внутри зоны, значит, проверять дальше нет смысла
//        if(tempCalculate[0] != 1L) {
//            return tempCalculate;
//        }

        // Если длина точек посещения равна 1, то это точка чипирования без перемещений
//        if(animalPoints.length == 1) {
//            return tempCalculate;
//        }

        // Проверка на 2 и 3 параметры
        // Преобразуем точки в отрезки (удаляя последнюю связь с первой точкой для точек животного)
        List<LineSegment> lineTemp = Arrays.stream(convertToLines(animalPoints)).collect(Collectors.toList());
        lineTemp.remove(lineTemp.size() - 1);
        LineSegment[] animalSegments = lineTemp.toArray(LineSegment[]::new);

        // Если есть центральная точка внутри зоны была, то животное точно пришло и ушло
//        LocationRequest midpoint = new LocationRequest();
//        if(midIntersect) {
//            tempCalculate[1] = 1L;
//            tempCalculate[2] = 1L;
//
//        } else {
            boolean arrived = findArrivedAnimal(areaPoints, animalSegments);
            boolean gone = findGoneAnimal(areaPoints, animalSegments);

            if(arrived) {
                tempCalculate[1] = 1L;
            }
            if(gone) {
                tempCalculate[2] = 1L;
            }
//        }

        return tempCalculate;
    }

    private boolean findArrivedAnimal(LocationRequest[] areaPoints, LineSegment[] animalSegments) {
        try {
            Arrays.stream(animalSegments).forEach(animalLine -> {
                if(//areaLine.isIntersectionMy(animalLine) &&
                    !animalLine.getStartPoint().isContainedIntoAreaWithBorder(areaPoints) &&
                    animalLine.getEndPoint().isContainedIntoAreaWithBorder((areaPoints))) {

                    throw new AlreadyExistException("");
                }
            });
        } catch(AlreadyExistException aee) {
            return true;
        }

        return false;
    }

    private boolean findGoneAnimal(LocationRequest[] areaPoints, LineSegment[] animalSegments) {
        try {
            Arrays.stream(animalSegments).forEach(animalLine -> {
                if(//areaLine.isIntersectionMy(animalLine) &&
                    animalLine.getStartPoint().isContainedIntoAreaWithBorder((areaPoints)) &&
                    !animalLine.getEndPoint().isContainedIntoAreaWithBorder((areaPoints))) {

                    throw new AlreadyExistException("");
                }
            });
        } catch(AlreadyExistException aee) {
            return true;
        }

        return false;
    }





    @GetMapping("/")
    public ResponseEntity<?> getAreaByIdEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PutMapping("/")
    public ResponseEntity<?> updateAreaEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteAreaEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PostMapping("/")
    public ResponseEntity<?> addAreaEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }
}
