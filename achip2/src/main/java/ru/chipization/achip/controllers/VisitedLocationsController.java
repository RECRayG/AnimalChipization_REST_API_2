package ru.chipization.achip.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.chipization.achip.dto.request.VisitedLocationsRequest;
import ru.chipization.achip.dto.response.VisitedLocationResponse;
import ru.chipization.achip.dto.search.Request;
import ru.chipization.achip.dto.search.SearchRequest;
import ru.chipization.achip.dto.search.VisitedLocationsSearchQuery;
import ru.chipization.achip.model.*;
import ru.chipization.achip.repository.AnimalRepository;
import ru.chipization.achip.repository.ChippingLocationRepository;
import ru.chipization.achip.repository.VisitedLocationRepository;
import ru.chipization.achip.service.FilterSpecification;
import ru.chipization.achip.service.OffsetBasedPageRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/animals")
@AllArgsConstructor
public class VisitedLocationsController {
    @Autowired
    private FilterSpecification<VisitedLocation> filterSpecification;
    @Autowired
    private VisitedLocationRepository visitedLocationRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private ChippingLocationRepository chippingLocationRepository;

    private final static DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]'Z'").withZone(ZoneId.from(ZoneOffset.UTC));

    @GetMapping("/{animalId}/locations")
    public ResponseEntity<?> searchAnimals(@PathVariable Long animalId, @ModelAttribute VisitedLocationsSearchQuery visitedLocationsSearchQuery) {
        try {
            // Проверка на 400 - валидность данных
            if(animalId == null || animalId <= 0) {
                return ResponseEntity.status(400).body("Id: " + animalId + " is not correct");
            }

            if(visitedLocationsSearchQuery.getFrom() == null) {
                visitedLocationsSearchQuery.setFrom(0);
            }
            if(visitedLocationsSearchQuery.getSize() == null) {
                visitedLocationsSearchQuery.setSize(10);
            }
            if(visitedLocationsSearchQuery.getFrom() < 0) {
                return ResponseEntity.status(400).body("Count of \'from\': " + visitedLocationsSearchQuery.getFrom() + " is not correct");
            }
            if(visitedLocationsSearchQuery.getSize() <= 0) {
                return ResponseEntity.status(400).body("Count of \'size\': " + visitedLocationsSearchQuery.getSize() + " is not correct");
            }

            // Проверка на 404 - существование животного
            Optional<Animal> animalCheck = animalRepository.findById(animalId);
            if(animalCheck.isEmpty()) {
                return ResponseEntity.status(404).body("Animal with id: " + animalId + " not found");
            }
            Animal animal = animalCheck.get();

            Request request = new Request(new ArrayList<>());
            Pageable pageable = new OffsetBasedPageRequest(visitedLocationsSearchQuery.getFrom(), visitedLocationsSearchQuery.getSize());

            Boolean startTime = false;
            Boolean endTime = false;
            Instant startDateTime = null;
            Instant endDateTime = null;

            // Обработка критерия поиска "startDateTime", если таковой имеется
            if(visitedLocationsSearchQuery.getStartDateTime() != null) {
                try {
                    if(visitedLocationsSearchQuery.getStartDateTime().length() >= 26) {
                        visitedLocationsSearchQuery.setStartDateTime(visitedLocationsSearchQuery.getStartDateTime().substring(0, 26) + "Z");
                    }

                    visitedLocationsSearchQuery.setStartDateTime(
                            String.valueOf(Instant.parse(
                                    URLDecoder.decode(visitedLocationsSearchQuery.getStartDateTime(),
                                            StandardCharsets.UTF_8.toString()))));
                } catch(UnsupportedEncodingException uee) {
                    return ResponseEntity.status(400).body("Decode error");
                } catch(DateTimeParseException dtpe) {
                    return ResponseEntity.status(400).body("startDateTime is not to format ISO-8601");
                }
                try {
                    startDateTime = Instant.parse(dateTimeFormatter.format(Instant.parse(visitedLocationsSearchQuery.getStartDateTime())));
                } catch(DateTimeParseException dtpe) {
                    return ResponseEntity.status(400).body("startDateTime is not to format ISO-8601");
                }

                startTime = true;
                request.getSearchRequest().add(new SearchRequest("dateTimeOfVisitLocationPoint", startDateTime));
            }
            // Обработка критерия поиска "endDateTime", если таковой имеется
            if(visitedLocationsSearchQuery.getEndDateTime() != null) {
                try {
                    if(visitedLocationsSearchQuery.getEndDateTime().length() >= 26) {
                        visitedLocationsSearchQuery.setEndDateTime(visitedLocationsSearchQuery.getEndDateTime().substring(0, 26) + "Z");
                    }

                    visitedLocationsSearchQuery.setEndDateTime(
                            String.valueOf(Instant.parse(
                                    URLDecoder.decode(visitedLocationsSearchQuery.getEndDateTime(),
                                            StandardCharsets.UTF_8.toString()))));
                } catch(UnsupportedEncodingException uee) {
                    return ResponseEntity.status(400).body("Decode error");
                } catch(DateTimeParseException dtpe) {
                    return ResponseEntity.status(400).body("endDateTime is not to format ISO-8601");
                }

                try {
                    endDateTime = Instant.parse(dateTimeFormatter.format(Instant.parse(visitedLocationsSearchQuery.getEndDateTime().toString())));
                } catch(DateTimeParseException dtpe) {
                    return ResponseEntity.status(400).body("endDateTime is not to format ISO-8601");
                }

                endTime = true;
                request.getSearchRequest().add(new SearchRequest("dateTimeOfVisitLocationPoint", endDateTime));
            }

            Specification<VisitedLocation> searchSpecification = filterSpecification
                    .getSearchSpecificationVisitedLocation(request.getSearchRequest(), startTime, endTime);

            List<VisitedLocation> visitedLocations = visitedLocationRepository.findAll(searchSpecification, pageable).getContent().stream().sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList());

            List<VisitedLocationResponse> visitedLocationResponses = new ArrayList<>();
            visitedLocations.forEach(currentVisitedLocation -> {
                visitedLocationResponses.add(VisitedLocationResponse.builder()
                        .id(currentVisitedLocation.getId())
                        .locationPointId(currentVisitedLocation.getIdChippingLocation().getId())
                        .dateTimeOfVisitLocationPoint(currentVisitedLocation.getDateTimeOfVisitLocationPoint())
                        .build());
            });


            return ResponseEntity.status(200).body(visitedLocationResponses);
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/{animalId}/locations/{pointId}")
    public ResponseEntity<?> addVisitedLocation(@PathVariable Long animalId, @PathVariable Long pointId) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if(currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность атомарных данных
                if(animalId == null || animalId <= 0 ||
                    pointId == null || pointId <= 0) {

                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 404 - существование точки локации
                Optional<ChippingLocation> chippingLocationCheck = chippingLocationRepository.findById(pointId);
                if(chippingLocationCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Point of location with id: " + pointId + " not found");
                }
                ChippingLocation location = chippingLocationCheck.get();

                // Проверка на 404 - существование животного
                Optional<Animal> animalCheck = animalRepository.findById(animalId);
                if(animalCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Animal with id: " + animalId + " not found");
                }
                Animal animal = animalCheck.get();

                // Проверка на 400 - статус животного
                if(animal.getIdAnimalLifeStatus().getLifeStatus().equals("DEAD")) {
                    return ResponseEntity.status(400).body("Animal is dead");
                }

                // Проверка на 400 - Животное находится в точке чипирования и никуда не перемещалось,
                // попытка добавить точку локации, равную точке чипирования
                // [1,2,1] - можно, [1], если у animal точка чипирования = 1 - нет
                if(animal.getIdChippingLocation().equals(location) && animal.getVisitedLocationCollection().isEmpty()) {
                    return ResponseEntity.status(400).body("Point of chipping = point of visiting AND animal also don\'t visited locations");
                }

                // Проверка на 400 - Попытка добавить точку локации, в которой уже находится животное
                if(!animal.getVisitedLocationCollection().isEmpty()) {
                    // Последняя посещённая точка
                    VisitedLocation visitedLocationLast = animal.getVisitedLocationCollection().stream().sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList()).stream().reduce((first, second) -> second).get();
                    if(visitedLocationLast.getIdChippingLocation().equals(location)) {
                        return ResponseEntity.status(400).body("The animal is already at that point");
                    }
                }

                // Запись посещённой точки в базу данных
                var visitedLocation = VisitedLocation.builder()
                                                    .idAnimal(animal)
                                                    .idChippingLocation(location)
                                                    .dateTimeOfVisitLocationPoint(Instant.parse(dateTimeFormatter.format(Instant.now())))
                                                    .build();

                VisitedLocation currentVisitedLocations = visitedLocationRepository.save(visitedLocation);

                return ResponseEntity.status(201).body(VisitedLocationResponse.builder()
                                                        .id(currentVisitedLocations.getId())
                                                        .dateTimeOfVisitLocationPoint(currentVisitedLocations.getDateTimeOfVisitLocationPoint())
                                                        .locationPointId(currentVisitedLocations.getIdChippingLocation().getId())
                                                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/{animalId}/locations")
    public ResponseEntity<?> updateVisitedLocation(@PathVariable Long animalId, @RequestBody VisitedLocationsRequest updateVisitedLocation) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if(currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность данных
                if(animalId == null || animalId <= 0 ||
                    updateVisitedLocation.getLocationPointId() == null || updateVisitedLocation.getLocationPointId() <= 0 ||
                    updateVisitedLocation.getVisitedLocationPointId() == null || updateVisitedLocation.getVisitedLocationPointId() <= 0) {

                    return ResponseEntity.status(400).body("Not valid data");
                }

                // Проверка на 404 - существование животного
                Optional<Animal> animalCheck = animalRepository.findById(animalId);
                if(animalCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Animal with id: " + animalId + " not found");
                }
                Animal animal = animalCheck.get();

                // Проверка на 404 - существование посещённой точки
                Optional<VisitedLocation> visitedLocationCheck = visitedLocationRepository.findById(updateVisitedLocation.getVisitedLocationPointId());
                if(visitedLocationCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Visited location with id: " + updateVisitedLocation.getVisitedLocationPointId() + " not found");
                }
                VisitedLocation visitedLocation = visitedLocationCheck.get();

                // Проверка на 404 - существование точки локации
                Optional<ChippingLocation> chippingLocationCheck = chippingLocationRepository.findById(updateVisitedLocation.getLocationPointId());
                if(chippingLocationCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Point of location with id: " + updateVisitedLocation.getLocationPointId() + " not found");
                }
                ChippingLocation location = chippingLocationCheck.get();

                // Проверка на 404 - У животного нет объекта с информацией о посещенной точке локации с visitedLocationPointId
                if(animal.getVisitedLocationCollection().isEmpty()) {
                    return ResponseEntity.status(404).body("The animal hasn\'t visited this point of location with id: " + updateVisitedLocation.getVisitedLocationPointId());
                } else {
                    Long countVisited = animal.getVisitedLocationCollection()
                                            .stream()
                                            .filter(vl -> vl.getId().equals(visitedLocation.getId()))
                                            .count();
                    if(countVisited == 0) {
                        return ResponseEntity.status(404).body("The animal hasn\'t visited this point of location with id: " + updateVisitedLocation.getVisitedLocationPointId());
                    }
                }

                // Проверка на 400 - Обновление первой посещенной точки на точку чипирования
                if(!animal.getVisitedLocationCollection().isEmpty()) {
                    VisitedLocation test = animal.getVisitedLocationCollection().stream()
                                                    .sorted(Comparator.comparing(VisitedLocation::getId))
                                                    .collect(Collectors.toList())
                                                    .stream()
                                                    .findFirst()
                                                    .get();

                    if(animal.getIdChippingLocation().getId().equals(location.getId()) &&
                        test.getId().equals(visitedLocation.getId())) {

                        return ResponseEntity.status(400).body("Try to update the first point of visited");
                    }
                }

                // Проверка на 400 - Обновление точки локации на точку,
                // совпадающую со следующей и/или с предыдущей точками
                // И
                // Проверка на 400 - Обновление точки на такую же точку
                if(!animal.getVisitedLocationCollection().isEmpty()) {
                    List<VisitedLocation> searchVl = animal.getVisitedLocationCollection().stream().sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList());
        //            List<VisitedLocation> searchVl = visitedLocationRepository.findVisitedLocationByIdAnimal(animal).get().stream().sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList());

                    if(searchVl.size() > 1) {
                        for(int i = 0; i < searchVl.size(); i++) {
                            if(searchVl.get(i).getId().equals(visitedLocation.getId())) {
                                // Если сравнение происходит в середине
                                if(i + 1 < searchVl.size() && i - 1 >= 0) {
                                    ChippingLocation prev = searchVl.get(i - 1).getIdChippingLocation();
                                    ChippingLocation next = searchVl.get(i + 1).getIdChippingLocation();
                                    ChippingLocation curr = searchVl.get(i).getIdChippingLocation();

                                    if(location.getId().equals(prev.getId()) ||
                                            location.getId().equals(next.getId())) {

                                        return ResponseEntity.status(400).body("Previous or(and) Next pointId = new pointId: " + updateVisitedLocation.getLocationPointId());
                                    }
                                    if(curr.equals(location)) {
                                        return ResponseEntity.status(400).body("Location point = updated location point");
                                    }
                                } // Если обновление первого элемента
                                else if(i - 1 < 0) {
                                    ChippingLocation next = searchVl.get(i + 1).getIdChippingLocation();
                                    ChippingLocation curr = searchVl.get(i).getIdChippingLocation();

                                    if(location.getId().equals(next.getId())) {
                                        return ResponseEntity.status(400).body("Next pointId = new pointId: " + updateVisitedLocation.getLocationPointId());
                                    }
                                    if(curr.equals(location)) {
                                        return ResponseEntity.status(400).body("Location point = updated location point");
                                    }
                                } // Если обновление последнего элемента
                                else if(i + 1 >= searchVl.size()) {
                                    ChippingLocation prev = searchVl.get(i - 1).getIdChippingLocation();
                                    ChippingLocation curr = searchVl.get(i).getIdChippingLocation();

                                    if(location.getId().equals(prev.getId())) {
                                        return ResponseEntity.status(400).body("Previous pointId = new pointId: " + updateVisitedLocation.getLocationPointId());
                                    }
                                    if(curr.equals(location)) {
                                        return ResponseEntity.status(400).body("Location point = updated location point");
                                    }
                                }

                                break;
                            }
                        }
                    }
                }

                visitedLocation.setIdChippingLocation(location);

                visitedLocationRepository.save(visitedLocation);

                return ResponseEntity.status(200).body(VisitedLocationResponse.builder()
                                                        .id(visitedLocation.getId())
                                                        .dateTimeOfVisitLocationPoint(visitedLocation.getDateTimeOfVisitLocationPoint())
                                                        .locationPointId(visitedLocation.getIdChippingLocation().getId())
                                                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @DeleteMapping("/{animalId}/locations/{visitedPointId}")
    public ResponseEntity<?> deleteVisitedLocation(@PathVariable Long animalId, @PathVariable Long visitedPointId) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {

                // Проверка на 400 - валидность данных
                if (animalId == null || animalId <= 0 ||
                        visitedPointId == null || visitedPointId <= 0) {

                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 404 - существование животного
                Optional<Animal> animalCheck = animalRepository.findById(animalId);
                if (animalCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Animal with id: " + animalId + " not found");
                }
                Animal animal = animalCheck.get();

                // Проверка на 404 - существование посещённой точки
                Optional<VisitedLocation> visitedLocationCheck = visitedLocationRepository.findById(visitedPointId);
                if (visitedLocationCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Visited location with id: " + visitedPointId + " not found");
                }
                VisitedLocation visitedLocation = visitedLocationCheck.get();

                // Проверка на 404 - У животного нет объекта с информацией о посещенной точке локации с visitedLocationPointId
                if (animal.getVisitedLocationCollection().isEmpty()) {
                    return ResponseEntity.status(404).body("The animal hasn\'t visited this point of location with id: " + visitedPointId);
                } else {
                    Long countVisited = animal.getVisitedLocationCollection()
                            .stream()
                            .filter(vl -> vl.getId().equals(visitedPointId))
                            .count();
                    if (countVisited == 0) {
                        return ResponseEntity.status(404).body("The animal hasn\'t visited this point of location with id: " + visitedPointId);
                    }
                }

                // Проверка на удаление первой посещенной точки с точкой чипирования после неё
                List<VisitedLocation> test = visitedLocationRepository.findVisitedLocationByIdAnimal(animal).get().stream().sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList());
                if (test.size() > 1) {
                    if (test.get(0).getId().equals(visitedLocation.getId()) && test.get(1).getIdChippingLocation().getId().equals(animal.getIdChippingLocation().getId())) {
                        VisitedLocation temp = test.get(1);
                        VisitedLocation temp2 = test.get(0);

                        test.remove(test.get(1));
                        //                test.remove(visitedLocation);
                        test.remove(test.get(0));
                        animal.setVisitedLocationCollection(test);
                        animalRepository.save(animal);

                        visitedLocationRepository.deleteById(temp2.getId());
                        visitedLocationRepository.deleteById(temp.getId());
                    } else {
                        test.remove(visitedLocation);
                        animal.setVisitedLocationCollection(test);
                        animalRepository.save(animal);
                        visitedLocationRepository.deleteById(visitedLocation.getId());
                    }
                } else {
                    test.remove(visitedLocation);
                    animal.setVisitedLocationCollection(test);
                    animalRepository.save(animal);
                    visitedLocationRepository.deleteById(visitedLocation.getId());
                }

                return ResponseEntity.status(200).body("Successful removal");
            } // USER & CHIPPER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }


    @PostMapping("/locations/")
    public ResponseEntity<?> addVisitedLocationEmpty() {
        return ResponseEntity.status(400).body("Id\'s is null");
    }

    @GetMapping("/locations/")
    public ResponseEntity<?> getVisitedLocationEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PutMapping("/locations")
    public ResponseEntity<?> updateVisitedLocationEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }
}
