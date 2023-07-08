package ru.chipization.achip.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.chipization.achip.dto.request.AnimalRequest;
import ru.chipization.achip.dto.request.AnimalTypeRequest;
import ru.chipization.achip.dto.response.AnimalResponse;
import ru.chipization.achip.dto.search.AnimalSearchQuery;
import ru.chipization.achip.dto.search.Request;
import ru.chipization.achip.dto.search.SearchRequest;
import ru.chipization.achip.exception.AlreadyExistException;
import ru.chipization.achip.exception.BadRequestException;
import ru.chipization.achip.exception.NotFoundException;
import ru.chipization.achip.model.*;
import ru.chipization.achip.repository.*;
import ru.chipization.achip.service.FilterSpecification;
import ru.chipization.achip.service.OffsetBasedPageRequest;
import ru.chipization.achip.model.Animal;
import ru.chipization.achip.model.AnimalType;
import ru.chipization.achip.model.AnimalGender;
import ru.chipization.achip.model.AnimalsLifeStatus;

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
public class AnimalsController {
    @Autowired
    private FilterSpecification<Animal> filterSpecification;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AnimalTypesRepository animalTypesRepository;

    @Autowired
    private ChippingLocationRepository chippingLocationRepository;

    @Autowired
    private AnimalGendersRepository animalGendersRepository;

    @Autowired
    private AnimalLifeStatusRepository animalLifeStatusRepository;

    @Autowired
    private AAtIdentityRepository aAtIdentityRepository;

    @Autowired
    private VisitedLocationRepository visitedLocationRepository;

    private final static DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]'Z'").withZone(ZoneId.from(ZoneOffset.UTC));

    @GetMapping("/{animalId}")
    public ResponseEntity<?> getAnimalById(@PathVariable Long animalId) {
        try {
            // Проверка на 400 - валидность данных
            if (animalId == null || animalId <= 0) {
                return ResponseEntity.status(400).body("Id: " + animalId + " is not correct");
            }

            // Проверка на 404 - существование животного
            Optional<Animal> animalCheck = animalRepository.findById(animalId);
            if (animalCheck.isEmpty()) {
                return ResponseEntity.status(404).body("Animal with id: " + animalId + " not found");
            }

            Animal animal = animalCheck.get();

            Long[] animalTypeList = aAtIdentityRepository.findAAtIdentitiesByIdAnimal(animal).get()
                    .stream().map(at -> {
                        return at.getIdAnimalType().getId();
                    }).collect(Collectors.toList()).toArray(Long[]::new);

            return new ResponseEntity<AnimalResponse>(AnimalResponse.builder()
                    .id(animal.getId())
                    .animalTypes(animalTypeList)
                    .weight(animal.getWeight())
                    .length(animal.getLength())
                    .height(animal.getHeight())
                    .gender(animal.getIdAnimalGender().getGender())
                    .lifeStatus(animal.getIdAnimalLifeStatus().getLifeStatus())
                    .chippingDateTime(animal.getChippingDateTime())
                    .chipperId(animal.getIdChipper().getId().intValue())
                    .chippingLocationId(animal.getIdChippingLocation().getId())
                    .visitedLocations(animal.getVisitedLocationCollection().stream()
                            .sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList())
                            .stream().map(vl -> {
                                return vl.getId();
                            }).toArray(Long[]::new))
                    .deathDateTime(animal.getDeathDateTime())
                    .build(),
                    HttpStatus.OK);
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAnimals(@ModelAttribute AnimalSearchQuery animalSearchQuery) {
        try {
            if (animalSearchQuery.getFrom() == null) {
                animalSearchQuery.setFrom(0);
            }
            if (animalSearchQuery.getSize() == null) {
                animalSearchQuery.setSize(10);
            }
            if (animalSearchQuery.getFrom() < 0) {
                return ResponseEntity.status(400).body("Count of \'from\': " + animalSearchQuery.getFrom() + " is not correct");
            }
            if (animalSearchQuery.getSize() <= 0) {
                return ResponseEntity.status(400).body("Count of \'size\': " + animalSearchQuery.getSize() + " is not correct");
            }

            Request request = new Request(new ArrayList<>());
            Pageable pageable = new OffsetBasedPageRequest(
                    animalSearchQuery.getFrom() != null && animalSearchQuery.getFrom() > 0 ? animalSearchQuery.getFrom() : 0,
                    animalSearchQuery.getSize() != null && animalSearchQuery.getSize() > 0 ? animalSearchQuery.getSize() : 10);

            Boolean startTime = false;
            Boolean endTime = false;

            // Обработка критерия поиска "startDateTime", если таковой имеется
            if (animalSearchQuery.getStartDateTime() != null) {
                try {
                    animalSearchQuery.setStartDateTime(
                            Instant.parse(
                                    URLDecoder.decode(animalSearchQuery.getStartDateTime().toString(),
                                            StandardCharsets.UTF_8.toString())));
                } catch (UnsupportedEncodingException uee) {
                    return ResponseEntity.status(400).body("Decode error");
                }
                try {
                    Instant.parse(dateTimeFormatter.format(Instant.parse(animalSearchQuery.getStartDateTime().toString())));
                } catch (DateTimeParseException dtpe) {
                    return ResponseEntity.status(400).body("startDateTime is not to format ISO-8601");
                }
                startTime = true;
                request.getSearchRequest().add(new SearchRequest("chippingDateTime", animalSearchQuery.getStartDateTime().toString()));
            }
            // Обработка критерия поиска "endDateTime", если таковой имеется
            if (animalSearchQuery.getEndDateTime() != null) {
                try {
                    animalSearchQuery.setEndDateTime(
                            Instant.parse(
                                    URLDecoder.decode(animalSearchQuery.getEndDateTime().toString(),
                                            StandardCharsets.UTF_8.toString())));
                } catch (UnsupportedEncodingException uee) {
                    return ResponseEntity.status(400).body("Decode error");
                }
                try {
                    Instant.parse(dateTimeFormatter.format(Instant.parse(animalSearchQuery.getEndDateTime().toString())));
                } catch (DateTimeParseException dtpe) {
                    return ResponseEntity.status(400).body("endDateTime is not to format ISO-8601");
                }
                endTime = true;
                request.getSearchRequest().add(new SearchRequest("chippingDateTime", animalSearchQuery.getEndDateTime().toString()));
            }
            // Обработка критерия поиска "chipperId", если таковой имеется
            if (animalSearchQuery.getChipperId() != null) {
                // Проверка на 400 - валидность id
                if (animalSearchQuery.getChipperId() <= 0) {
                    return ResponseEntity.status(400).body("No valid data");
                }

                Optional<User> userCheck = userRepository.findById(Long.valueOf(animalSearchQuery.getChipperId()));
                if (userCheck.isEmpty())
                    request.getSearchRequest().add(new SearchRequest("idChipper", null));
                else
                    request.getSearchRequest().add(new SearchRequest("idChipper", userCheck.get()));
            }
            // Обработка критерия поиска "lifeStatus", если таковой имеется
            if (animalSearchQuery.getLifeStatus() != null) {
                // Проверка на 400 - валидность статуса животного
                Boolean flagLifeStatus = false;
                switch (animalSearchQuery.getLifeStatus()) {
                    case "ALIVE":
                        flagLifeStatus = true;
                        break;
                    case "DEAD":
                        flagLifeStatus = true;
                        break;
                }
                if (!flagLifeStatus) {
                    return ResponseEntity.status(400).body("No valid life status");
                }

                Optional<AnimalsLifeStatus> lifeStatusCheck = animalLifeStatusRepository.findAnimalsLifeStatusByLifeStatus(animalSearchQuery.getLifeStatus());
                if (lifeStatusCheck.isEmpty())
                    request.getSearchRequest().add(new SearchRequest("idAnimalLifeStatus", null));
                else
                    request.getSearchRequest().add(new SearchRequest("idAnimalLifeStatus", lifeStatusCheck.get()));
            }
            // Обработка критерия поиска "gender", если таковой имеется
            if (animalSearchQuery.getGender() != null) {
                // Проверка на 400 - валидность пола животного
                boolean flagGender = false;
                switch (animalSearchQuery.getGender()) {
                    case "MALE":
                        flagGender = true;
                        break;
                    case "FEMALE":
                        flagGender = true;
                        break;
                    case "OTHER":
                        flagGender = true;
                        break;
                }
                if (!flagGender) {
                    return ResponseEntity.status(400).body("No valid gender");
                }

                Optional<AnimalGender> animalGenderCheck = animalGendersRepository.findAnimalGenderByGender(animalSearchQuery.getGender());
                if (animalGenderCheck.isEmpty())
                    request.getSearchRequest().add(new SearchRequest("idAnimalGender", null));
                else
                    request.getSearchRequest().add(new SearchRequest("idAnimalGender", animalGenderCheck.get()));
            }
            // Обработка критерия поиска "chippingLocationId", если таковой имеется
            if (animalSearchQuery.getChippingLocationId() != null) {
                // Проверка на 400 - валидность id
                if (animalSearchQuery.getChippingLocationId() <= 0) {
                    return ResponseEntity.status(400).body("No valid data");
                }

                Optional<ChippingLocation> chippingLocationCheck = chippingLocationRepository.findById(Long.valueOf(animalSearchQuery.getChippingLocationId()));
                if (chippingLocationCheck.isEmpty())
                    request.getSearchRequest().add(new SearchRequest("idChippingLocation", null));
                else
                    request.getSearchRequest().add(new SearchRequest("idChippingLocation", chippingLocationCheck.get()));
            }

            Specification<Animal> searchSpecification = filterSpecification
                    .getSearchSpecificationAnimal(request.getSearchRequest(), startTime, endTime);


            List<Animal> animals = animalRepository.findAll(searchSpecification, pageable).getContent();

            List<AnimalResponse> animalResponses = new ArrayList<>();
            animals.forEach(currentAnimal -> {
                List<AAtIdentity> aAtIdentityList = aAtIdentityRepository.findAAtIdentitiesByIdAnimal(currentAnimal).get();

                animalResponses.add(AnimalResponse.builder()
                        .id(currentAnimal.getId())
                        .animalTypes(aAtIdentityList.stream().map(at -> {
                            return at.getIdAnimalType().getId();
                        }).collect(Collectors.toList()).toArray(Long[]::new))
                        .weight(currentAnimal.getWeight())
                        .length(currentAnimal.getLength())
                        .height(currentAnimal.getHeight())
                        .gender(currentAnimal.getIdAnimalGender().getGender())
                        .lifeStatus(currentAnimal.getIdAnimalLifeStatus().getLifeStatus())
                        .chippingDateTime(currentAnimal.getChippingDateTime())
                        .chipperId(currentAnimal.getIdChipper().getId().intValue())
                        .chippingLocationId(currentAnimal.getIdChippingLocation().getId())
                        .visitedLocations(currentAnimal.getVisitedLocationCollection().stream()
                                .sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList())
                                .stream().map(vl -> {
                                    return vl.getId();
                                }).toArray(Long[]::new))
                        .deathDateTime(currentAnimal.getDeathDateTime())
                        .build());
            });


            return ResponseEntity.status(200).body(animalResponses);
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping()
    public ResponseEntity<?> addAnimal(@RequestBody AnimalRequest insertAnimal) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность атомарных данных
                if (insertAnimal.getAnimalTypes() == null || insertAnimal.getAnimalTypes().length <= 0 ||
                        insertAnimal.getWeight() == null || insertAnimal.getWeight() <= 0 ||
                        insertAnimal.getLength() == null || insertAnimal.getLength() <= 0 ||
                        insertAnimal.getHeight() == null || insertAnimal.getHeight() <= 0 ||
                        insertAnimal.getGender() == null ||
                        insertAnimal.getChipperId() == null || insertAnimal.getChipperId() <= 0 ||
                        insertAnimal.getChippingLocationId() == null || insertAnimal.getChippingLocationId() <= 0) {

                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 400 - валидность пола животного
                boolean flagGender = false;
                switch (insertAnimal.getGender()) {
                    case "MALE":
                        flagGender = true;
                        break;
                    case "FEMALE":
                        flagGender = true;
                        break;
                    case "OTHER":
                        flagGender = true;
                        break;
                }
                if (!flagGender) {
                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 404 - существование аккаунта
                Optional<User> userCheck = userRepository.findById(Long.valueOf(insertAnimal.getChipperId()));
                if (userCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Account with id: " + insertAnimal.getChipperId() + " is not found");
                }
                User user = userCheck.get();

                // Проверка на 404 - существование точки локации
                Optional<ChippingLocation> chippingLocationCheck = chippingLocationRepository.findById(insertAnimal.getChippingLocationId());
                if (chippingLocationCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Point of location with id: " + insertAnimal.getChippingLocationId() + " not found");
                }
                ChippingLocation location = chippingLocationCheck.get();

                // Проверка на 400 - валидность элементов массива типов животного
                try {
                    Arrays.stream(insertAnimal.getAnimalTypes()).forEach(at -> {
                        if (at == null || at <= 0) {
                            throw new BadRequestException("No valid element of array: " + at);
                        } else {
                            // Проверка на 404 - существование типа животного
                            if (animalTypesRepository.findById(at).isEmpty()) {
                                throw new NotFoundException("Type of animal with id: " + at + " not found");
                            }
                        }
                    });
                } catch (BadRequestException bre) {
                    return ResponseEntity.status(400).body(bre.getMessage());
                } catch (NotFoundException nfe) {
                    return ResponseEntity.status(404).body(nfe.getMessage());
                }

                // Проверка на 409 - содержание дубликатов в массиве типов животного
                if (Arrays.stream(insertAnimal.getAnimalTypes()).distinct().count() != insertAnimal.getAnimalTypes().length) {
                    return ResponseEntity.status(409).body("Array of animal types contains duplicates");
                }

                // Вставка пола животного, если таковой не существует
                AnimalGender animalGender;
                Optional<AnimalGender> animalGenderCheck = animalGendersRepository.findAnimalGenderByGender(insertAnimal.getGender());
                if (animalGenderCheck.isEmpty()) {
                    animalGendersRepository.save(AnimalGender.builder()
                            .gender(insertAnimal.getGender())
                            .animalCollection(new ArrayList<>())
                            .build());
                    animalGender = animalGendersRepository.findAnimalGenderByGender(insertAnimal.getGender()).get();
                } else {
                    animalGender = animalGenderCheck.get();
                }

                // Вставка статуса животного (ALIVE и DEAD), если таковой не существует
                AnimalsLifeStatus animalsLifeStatus;
                Optional<AnimalsLifeStatus> animalsLifeStatusCheckAlive = animalLifeStatusRepository.findAnimalsLifeStatusByLifeStatus("ALIVE");
                Optional<AnimalsLifeStatus> animalsLifeStatusCheckDead = animalLifeStatusRepository.findAnimalsLifeStatusByLifeStatus("DEAD");
                if (animalsLifeStatusCheckAlive.isEmpty()) {
                    animalLifeStatusRepository.save(AnimalsLifeStatus.builder()
                            .lifeStatus("ALIVE")
                            .animalCollection(new ArrayList<>())
                            .build());
                    animalsLifeStatus = animalLifeStatusRepository.findAnimalsLifeStatusByLifeStatus("ALIVE").get();
                } else {
                    animalsLifeStatus = animalsLifeStatusCheckAlive.get();
                }

                if (animalsLifeStatusCheckDead.isEmpty()) {
                    animalLifeStatusRepository.save(AnimalsLifeStatus.builder()
                            .lifeStatus("DEAD")
                            .animalCollection(new ArrayList<>())
                            .build());
                }

                // Запись животного в базу данных (без привязки к типу(-ам))
                var animal = Animal.builder()
                        .weight(insertAnimal.getWeight())
                        .length(insertAnimal.getLength())
                        .height(insertAnimal.getHeight())
                        .idAnimalGender(animalGender)
                        .idAnimalLifeStatus(animalsLifeStatus)
                        .chippingDateTime(Instant.parse(dateTimeFormatter.format(Instant.now())))
                        .idChipper(user)
                        .idChippingLocation(location)
                        .deathDateTime(null)
                        .aAtIdentityCollection(new ArrayList<>())
                        .visitedLocationCollection(new ArrayList<>())
                        .build();

                Animal animalTemp = animalRepository.save(animal);

                // Обновление типа(-ов)) животного в базе данных
                List<AAtIdentity> aAtIdentityList = new ArrayList<>();
                Arrays.stream(insertAnimal.getAnimalTypes()).forEach(at -> {
                    aAtIdentityList.add(AAtIdentity.builder()
                            .idAnimal(animalTemp)
                            .idAnimalType(animalTypesRepository.findById(at).get())
                            .build());
                });

                animalTemp.setAAtIdentityCollection(aAtIdentityList);

                final Animal currentAnimal = animalRepository.save(animalTemp);


                return ResponseEntity.status(201).body(AnimalResponse.builder()
                        .id(currentAnimal.getId())
                        .animalTypes(aAtIdentityList.stream().map(at -> {
                            return at.getIdAnimalType().getId();
                        }).collect(Collectors.toList()).toArray(Long[]::new))
                        .weight(currentAnimal.getWeight())
                        .length(currentAnimal.getLength())
                        .height(currentAnimal.getHeight())
                        .gender(currentAnimal.getIdAnimalGender().getGender())
                        .lifeStatus(currentAnimal.getIdAnimalLifeStatus().getLifeStatus())
                        .chippingDateTime(currentAnimal.getChippingDateTime())
                        .chipperId(currentAnimal.getIdChipper().getId().intValue())
                        .chippingLocationId(currentAnimal.getIdChippingLocation().getId())
                        .visitedLocations(currentAnimal.getVisitedLocationCollection().stream()
                                .sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList())
                                .stream().map(vl -> {
                                    return vl.getIdChippingLocation().getId();
                                }).toArray(Long[]::new))
                        .deathDateTime(currentAnimal.getDeathDateTime())
                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/{animalId}")
    public ResponseEntity<?> updateAnimal(@PathVariable Long animalId, @RequestBody AnimalResponse updateAnimal) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность данных
                if (animalId == null || animalId <= 0 ||
                        updateAnimal.getWeight() == null || updateAnimal.getWeight() <= 0 ||
                        updateAnimal.getLength() == null || updateAnimal.getLength() <= 0 ||
                        updateAnimal.getHeight() == null || updateAnimal.getHeight() <= 0 ||
                        updateAnimal.getGender() == null ||
                        updateAnimal.getChipperId() == null || updateAnimal.getChipperId() <= 0 ||
                        updateAnimal.getChippingLocationId() == null || updateAnimal.getChippingLocationId() <= 0) {

                    return ResponseEntity.status(400).body("Not valid data");
                }

                // Проверка на 400 - валидность пола животного
                boolean flagGender = false;
                switch (updateAnimal.getGender()) {
                    case "MALE":
                        flagGender = true;
                        break;
                    case "FEMALE":
                        flagGender = true;
                        break;
                    case "OTHER":
                        flagGender = true;
                        break;
                }
                if (!flagGender) {
                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 404 - существование аккаунта
                Optional<User> userCheck = userRepository.findById(Long.valueOf(updateAnimal.getChipperId()));
                if (userCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Account with id: " + updateAnimal.getChipperId() + " is not found");
                }
                User user = userCheck.get();

                // Проверка на 404 - существование точки локации
                Optional<ChippingLocation> chippingLocationCheck = chippingLocationRepository.findById(updateAnimal.getChippingLocationId());
                if (chippingLocationCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Point of location with id: " + updateAnimal.getChippingLocationId() + " not found");
                }
                ChippingLocation location = chippingLocationCheck.get();

                // Проверка на 404 - существование животного
                Optional<Animal> animalCheck = animalRepository.findById(animalId);
                if (animalCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Animal with id: " + animalId + " not found");
                }
                Animal animal = animalCheck.get();

                // Вставка пола животного, если таковой не существует
                AnimalGender animalGender;
                Optional<AnimalGender> animalGenderCheck = animalGendersRepository.findAnimalGenderByGender(updateAnimal.getGender());
                if (animalGenderCheck.isEmpty()) {
                    animalGendersRepository.save(AnimalGender.builder()
                            .gender(updateAnimal.getGender())
                            .animalCollection(new ArrayList<>())
                            .build());
                    animalGender = animalGendersRepository.findAnimalGenderByGender(updateAnimal.getGender()).get();
                } else {
                    animalGender = animalGenderCheck.get();
                }

                // Вставка статуса животного (ALIVE и DEAD), если таковой не существует
                AnimalsLifeStatus animalsLifeStatus;
                Optional<AnimalsLifeStatus> animalsLifeStatusCheckAlive = animalLifeStatusRepository.findAnimalsLifeStatusByLifeStatus("ALIVE");
                Optional<AnimalsLifeStatus> animalsLifeStatusCheckDead = animalLifeStatusRepository.findAnimalsLifeStatusByLifeStatus("DEAD");
                if (animalsLifeStatusCheckAlive.isEmpty()) {
                    animalLifeStatusRepository.save(AnimalsLifeStatus.builder()
                            .lifeStatus("ALIVE")
                            .animalCollection(new ArrayList<>())
                            .build());
                    animalsLifeStatus = animalLifeStatusRepository.findAnimalsLifeStatusByLifeStatus("ALIVE").get();
                } else {
                    animalsLifeStatus = animalsLifeStatusCheckAlive.get();
                }

                if (animalsLifeStatusCheckDead.isEmpty()) {
                    animalLifeStatusRepository.save(AnimalsLifeStatus.builder()
                            .lifeStatus("DEAD")
                            .animalCollection(new ArrayList<>())
                            .build());
                }

                if (updateAnimal.getLifeStatus().equals("ALIVE")) {
                    animalsLifeStatus = animalsLifeStatusCheckAlive.get();
                } else if (updateAnimal.getLifeStatus().equals("DEAD")) {
                    animalsLifeStatus = animalsLifeStatusCheckDead.get();
                }

                // Проверка на 400 - Установка lifeStatus = “ALIVE”, если у животного lifeStatus = “DEAD”
                if (animal.getIdAnimalLifeStatus().getLifeStatus().equals("DEAD") && updateAnimal.getLifeStatus().equals("ALIVE")) {
                    return ResponseEntity.status(400).body("The animal can\'t be resurrected");
                }

                // Проверка на 400 - Новая точка чипирования совпадает с первой посещенной точкой локации
                if (!animal.getVisitedLocationCollection().isEmpty()) {
                    try {
                        animal.getVisitedLocationCollection().forEach(vl -> {
                            if (vl.getIdChippingLocation().getId().equals(location.getId())) {
                                throw new BadRequestException("New location point matches the first point of location");
                            } else {
                                throw new NotFoundException("");
                            }
                        });
                    } catch (BadRequestException bre) {
                        return ResponseEntity.status(400).body(bre.getMessage());
                    } catch (NotFoundException nfe) {
                    }
                }

                animal.setWeight(updateAnimal.getWeight());
                animal.setLength(updateAnimal.getLength());
                animal.setHeight(updateAnimal.getHeight());
                animal.setIdAnimalGender(animalGender);
                animal.setIdChipper(user);
                animal.setIdChippingLocation(location);
                animal.setIdAnimalLifeStatus(animalsLifeStatus);
                if (updateAnimal.getLifeStatus().equals("DEAD")) {
                    animal.setDeathDateTime(Instant.parse(dateTimeFormatter.format(Instant.now())));
                }

                animalRepository.save(animal);

                List<AAtIdentity> aAtIdentityList = aAtIdentityRepository.findAAtIdentitiesByIdAnimal(animal).get();

                return ResponseEntity.status(200).body(AnimalResponse.builder()
                        .id(animal.getId())
                        .animalTypes(aAtIdentityList.stream().map(at -> {
                            return at.getIdAnimalType().getId();
                        }).collect(Collectors.toList()).toArray(Long[]::new))
                        .weight(animal.getWeight())
                        .length(animal.getLength())
                        .height(animal.getHeight())
                        .gender(animal.getIdAnimalGender().getGender())
                        .lifeStatus(animal.getIdAnimalLifeStatus().getLifeStatus())
                        .chippingDateTime(animal.getChippingDateTime())
                        .chipperId(animal.getIdChipper().getId().intValue())
                        .chippingLocationId(animal.getIdChippingLocation().getId())
                        .visitedLocations(animal.getVisitedLocationCollection().stream()
                                .sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList())
                                .stream().map(vl -> {
                                    return vl.getId();
                                }).toArray(Long[]::new))
                        .deathDateTime(animal.getDeathDateTime())
                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @DeleteMapping("/{animalId}")
    public ResponseEntity<?> deleteAnimal(@PathVariable Long animalId) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {

                // Проверка на 400 - валидность данных
                if (animalId == null || animalId <= 0) {
                    return ResponseEntity.status(400).body("Id: " + animalId + " is not correct");
                }

                // Проверка на 404 - существование животного
                Optional<Animal> animalCheck = animalRepository.findById(animalId);
                if (animalCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Animal with id: " + animalId + " is not found");
                }
                Animal animal = animalCheck.get();

                // Проверка на 400 - Животное покинуло локацию чипирования, при этом есть другие посещенные точки
                if (!animal.getVisitedLocationCollection().isEmpty()) {
                    // Последняя посещённая точка
                    VisitedLocation visitedLocationLast = animal.getVisitedLocationCollection().stream().reduce((first, second) -> second).get();
                    if (visitedLocationLast.getIdChippingLocation().getId() != animal.getIdChippingLocation().getId()) {
                        return ResponseEntity.status(400).body("Animal have another location points");
                    }
                }

                animalRepository.delete(animal);
                return ResponseEntity.status(200).body("Successful removal");
            } // USER & CHIPPER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/{animalId}/types/{typeId}")
    public ResponseEntity<?> addAnimalType(@PathVariable Long animalId, @PathVariable Long typeId) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность атомарных данных
                if (animalId == null || animalId <= 0 ||
                        typeId == null || typeId <= 0) {

                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 404 - существование животного
                Optional<Animal> animalCheck = animalRepository.findById(animalId);
                if (animalCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Animal with id: " + animalId + " not found");
                }
                Animal animal = animalCheck.get();

                // Проверка на 404 - существование типа животного
                Optional<AnimalType> animalTypeCheck = animalTypesRepository.findById(typeId);
                if (animalTypeCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Type of animal with id: " + typeId + " not found");
                }
                AnimalType animalType = animalTypeCheck.get();

                // Проверка на 409 - тип животного уже присутствует у животного
                List<AAtIdentity> aAtIdentityList = aAtIdentityRepository.findAAtIdentitiesByIdAnimal(animal).get();
                try {
                    aAtIdentityList.forEach(identity -> {
                        if (identity.getIdAnimalType().getId().equals(animalType.getId())) {
                            throw new AlreadyExistException("Animal\'s type: " + animalType.getType() + " already exist");
                        }
                    });
                } catch (AlreadyExistException eae) {
                    return ResponseEntity.status(409).body(eae.getMessage());
                }

                // Добавление типа животного к нему
                aAtIdentityList.add(AAtIdentity.builder()
                        .idAnimal(animal)
                        .idAnimalType(animalType)
                        .build());

                animal.setAAtIdentityCollection(aAtIdentityList);
                final Animal currentAnimal = animalRepository.save(animal);

                return ResponseEntity.status(201).body(AnimalResponse.builder()
                        .id(currentAnimal.getId())
                        .animalTypes(aAtIdentityList.stream().map(at -> {
                            return at.getIdAnimalType().getId();
                        }).collect(Collectors.toList()).toArray(Long[]::new))
                        .weight(currentAnimal.getWeight())
                        .length(currentAnimal.getLength())
                        .height(currentAnimal.getHeight())
                        .gender(currentAnimal.getIdAnimalGender().getGender())
                        .lifeStatus(currentAnimal.getIdAnimalLifeStatus().getLifeStatus())
                        .chippingDateTime(currentAnimal.getChippingDateTime())
                        .chipperId(currentAnimal.getIdChipper().getId().intValue())
                        .chippingLocationId(currentAnimal.getIdChippingLocation().getId())
                        .visitedLocations(currentAnimal.getVisitedLocationCollection().stream()
                                .sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList())
                                .stream().map(vl -> {
                                    return vl.getId();
                                }).toArray(Long[]::new))
                        .deathDateTime(currentAnimal.getDeathDateTime())
                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/{animalId}/types")
    public ResponseEntity<?> updateAnimalType(@PathVariable Long animalId, @RequestBody AnimalTypeRequest updateAnimalType) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность данных
                if (animalId == null || animalId <= 0 ||
                        updateAnimalType.getNewTypeId() == null || updateAnimalType.getNewTypeId() <= 0 ||
                        updateAnimalType.getOldTypeId() == null || updateAnimalType.getOldTypeId() <= 0) {

                    return ResponseEntity.status(400).body("Not valid data");
                }

                // Проверка на 404 - существование животного
                Optional<Animal> animalCheck = animalRepository.findById(animalId);
                if (animalCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Animal with id: " + animalId + " not found");
                }
                Animal animal = animalCheck.get();

                // Проверка на 404 - существование старого типа животного
                Optional<AnimalType> animalTypeCheck = animalTypesRepository.findById(updateAnimalType.getOldTypeId());
                if (animalTypeCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Old type of animal with id: " + updateAnimalType.getOldTypeId() + " not found");
                }
                AnimalType animalTypeOld = animalTypeCheck.get();

                // Проверка на 404 - существование нового типа животного
                animalTypeCheck = animalTypesRepository.findById(updateAnimalType.getNewTypeId());
                if (animalTypeCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("New type of animal with id: " + updateAnimalType.getNewTypeId() + " not found");
                }
                AnimalType animalTypeNew = animalTypeCheck.get();

                // Проверка на 404 - Типа животного с oldTypeId нет у животного с animalId
                Boolean flagExist = false;
                List<AAtIdentity> aAtIdentityList = aAtIdentityRepository.findAAtIdentitiesByIdAnimal(animal).get();
                try {
                    aAtIdentityList.forEach(identity -> {
                        if (identity.getIdAnimalType().getId().equals(animalTypeOld.getId())) {
                            throw new AlreadyExistException("Exist");
                        }
                    });
                } catch (AlreadyExistException eae) {
                    flagExist = true;
                }
                if (!flagExist) {
                    return ResponseEntity.status(404).body("Old type of animal with id: " + updateAnimalType.getOldTypeId() + " not exist");
                }

                // Проверка на 409 - Тип животного с newTypeId уже есть у животного с animalId
                flagExist = false;
                Boolean flagExistOld = false;
                try {
                    aAtIdentityList.forEach(identity -> {
                        if (identity.getIdAnimalType().getId().equals(animalTypeNew.getId())) {
                            throw new AlreadyExistException("New animal\'s type with id: " + animalTypeNew.getId() + " already exist");
                        }
                    });
                } catch (AlreadyExistException eae) {
                    return ResponseEntity.status(409).body(eae.getMessage());
                }

                // Проверка на 409 - Животное с animalId уже имеет типы с oldTypeId и newTypeId
                // ???????????????


                AAtIdentity tempAAtIdentity = aAtIdentityRepository.findAAtIdentitiesByIdAnimalAndAndIdAnimalType(animal, animalTypeOld).get();
                try {
                    aAtIdentityList.forEach(identity -> {
                        if (identity.getIdAnimalType().getId().equals(tempAAtIdentity.getIdAnimalType().getId())) {
                            identity.setIdAnimalType(animalTypeNew);
                            throw new AlreadyExistException("Done");
                        }
                    });
                } catch (AlreadyExistException aee) {
                }

                animal.setAAtIdentityCollection(aAtIdentityList);
                animalRepository.save(animal);

                return ResponseEntity.status(200).body(AnimalResponse.builder()
                        .id(animal.getId())
                        .animalTypes(aAtIdentityList.stream().map(at -> {
                            return at.getIdAnimalType().getId();
                        }).collect(Collectors.toList()).toArray(Long[]::new))
                        .weight(animal.getWeight())
                        .length(animal.getLength())
                        .height(animal.getHeight())
                        .gender(animal.getIdAnimalGender().getGender())
                        .lifeStatus(animal.getIdAnimalLifeStatus().getLifeStatus())
                        .chippingDateTime(animal.getChippingDateTime())
                        .chipperId(animal.getIdChipper().getId().intValue())
                        .chippingLocationId(animal.getIdChippingLocation().getId())
                        .visitedLocations(animal.getVisitedLocationCollection().stream()
                                .sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList())
                                .stream().map(vl -> {
                                    return vl.getId();
                                }).toArray(Long[]::new))
                        .deathDateTime(animal.getDeathDateTime())
                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @DeleteMapping("/{animalId}/types/{typeId}")
    public ResponseEntity<?> deleteAnimalType(@PathVariable Long animalId, @PathVariable Long typeId) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность данных
                if (animalId == null || animalId <= 0 ||
                        typeId == null || typeId <= 0) {

                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 404 - существование животного
                Optional<Animal> animalCheck = animalRepository.findById(animalId);
                if (animalCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Animal with id: " + animalId + " is not found");
                }
                Animal animal = animalCheck.get();

                // Проверка на 404 - существование старого типа животного
                Optional<AnimalType> animalTypeCheck = animalTypesRepository.findById(typeId);
                if (animalTypeCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Type of animal with id: " + typeId + " not found");
                }
                AnimalType animalType = animalTypeCheck.get();

                // Проверка на 404 - У животного с animalId нет типа с typeId
                Boolean flagExist = false;
                List<AAtIdentity> aAtIdentityList = aAtIdentityRepository.findAAtIdentitiesByIdAnimal(animal).get();
                try {
                    aAtIdentityList.forEach(identity -> {
                        if (identity.getIdAnimalType().getId().equals(animalType.getId())) {
                            throw new AlreadyExistException("Exist");
                        }
                    });
                } catch (AlreadyExistException eae) {
                    flagExist = true;
                }
                if (!flagExist) {
                    return ResponseEntity.status(404).body("Type of animal with id: " + typeId + " not exist");
                }

                // Проверка на 400 - У животного только один тип и это тип с typeId
                if (aAtIdentityList.size() == 1) {
                    if (aAtIdentityList.stream().findFirst().get().getIdAnimalType().getId().equals(typeId)) {
                        return ResponseEntity.status(400).body("The animal has only one type and this is the typeId: " + typeId);
                    }
                }

                AAtIdentity tempAAtIdentity = aAtIdentityRepository.findAAtIdentitiesByIdAnimalAndAndIdAnimalType(animal, animalType).get();
                aAtIdentityRepository.deleteById(tempAAtIdentity.getId());

                aAtIdentityList.remove(tempAAtIdentity);
                animal.setAAtIdentityCollection(aAtIdentityList);
                animalRepository.save(animal);

                return ResponseEntity.status(200).body(AnimalResponse.builder()
                        .id(animal.getId())
                        .animalTypes(aAtIdentityList.stream().map(at -> {
                            return at.getIdAnimalType().getId();
                        }).collect(Collectors.toList()).toArray(Long[]::new))
                        .weight(animal.getWeight())
                        .length(animal.getLength())
                        .height(animal.getHeight())
                        .gender(animal.getIdAnimalGender().getGender())
                        .lifeStatus(animal.getIdAnimalLifeStatus().getLifeStatus())
                        .chippingDateTime(animal.getChippingDateTime())
                        .chipperId(animal.getIdChipper().getId().intValue())
                        .chippingLocationId(animal.getIdChippingLocation().getId())
                        .visitedLocations(animal.getVisitedLocationCollection()
                                .stream().sorted(Comparator.comparing(VisitedLocation::getId)).collect(Collectors.toList())
                                .stream().map(vl -> {
                                    return vl.getIdChippingLocation().getId();
                                }).toArray(Long[]::new))
                        .deathDateTime(animal.getDeathDateTime())
                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }



    @GetMapping("/")
    public ResponseEntity<?> getAnimalByIdEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PutMapping("/")
    public ResponseEntity<?> updateAnimalEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PutMapping("/types")
    public ResponseEntity<?> updateAnimalTypeEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteAnimalEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PostMapping("/types/")
    public ResponseEntity<?> addAnimalTypeEmpty() {
        return ResponseEntity.status(400).body("Id\'s is null");
    }
}
