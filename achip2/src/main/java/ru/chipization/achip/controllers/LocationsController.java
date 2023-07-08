package ru.chipization.achip.controllers;

import ch.hsr.geohash.GeoHash;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.chipization.achip.dto.request.LocationRequest;
import ru.chipization.achip.dto.response.LocationResponse;
import ru.chipization.achip.exception.AlreadyExistException;
import ru.chipization.achip.model.ChippingLocation;
import ru.chipization.achip.model.User;
import ru.chipization.achip.repository.AnimalRepository;
import ru.chipization.achip.repository.ChippingLocationRepository;
import ru.chipization.achip.repository.VisitedLocationRepository;

import java.util.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/locations")
@AllArgsConstructor
public class LocationsController {
    @Autowired
    private ChippingLocationRepository chippingLocationRepository;

    @Autowired
    private VisitedLocationRepository visitedLocationRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @GetMapping("/{pointId}")
    public ResponseEntity<?> getLocationById(@PathVariable Long pointId) {
        try {
            // Проверка на 400 - валидность данных
            if (pointId == null || pointId <= 0) {
                return ResponseEntity.status(400).body("Id: " + pointId + " is not correct");
            }

            // Проверка на 404 - существование точки локации
            Optional<ChippingLocation> chippingLocationCheck = chippingLocationRepository.findById(pointId);
            if (chippingLocationCheck.isEmpty()) {
                return ResponseEntity.status(404).body("Point of location with id: " + pointId + " not found");
            }

            ChippingLocation location = chippingLocationCheck.get();

            return new ResponseEntity<LocationResponse>(LocationResponse.builder()
                    .id(location.getId())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build(),
                    HttpStatus.OK);
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping()
    public ResponseEntity<?> addLocation(@RequestBody LocationRequest insertLocation) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность данных
                if (insertLocation.getLatitude() == null || insertLocation.getLatitude() < -90 || insertLocation.getLatitude() > 90 ||
                        insertLocation.getLongitude() == null || insertLocation.getLongitude() < -180 || insertLocation.getLongitude() > 180) {

                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 409 - точка локации уже существует
                List<ChippingLocation> chippingLocationList = chippingLocationRepository.findAll();
                try {
                    chippingLocationList.forEach(chl -> {
                        if (chl.getLatitude().equals(insertLocation.getLatitude()) &&
                                chl.getLongitude().equals(insertLocation.getLongitude())) {
                            throw new AlreadyExistException("Location with (latitude;longitude): (" + insertLocation.getLatitude() + "; " + insertLocation.getLongitude() + ") already exist");
                        }
                    });
                } catch (AlreadyExistException eae) {
                    return ResponseEntity.status(409).body(eae.getMessage());
                }

                var location = ChippingLocation.builder()
                        .latitude(insertLocation.getLatitude())
                        .longitude(insertLocation.getLongitude())
                        .build();

                chippingLocationRepository.save(location);

                return ResponseEntity.status(201).body(LocationResponse.builder()
                        .id(chippingLocationRepository.findChippingLocationByLongitudeAndLatitude(location.getLongitude(), location.getLatitude()).get().getId())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude())
                        .build());

            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/{pointId}")
    public ResponseEntity<?> updatePointType(@PathVariable Long pointId, @RequestBody LocationRequest updateLocation) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность данных
                if (pointId == null || pointId <= 0 ||
                        updateLocation.getLatitude() == null || updateLocation.getLatitude() < -90 || updateLocation.getLatitude() > 90 ||
                        updateLocation.getLongitude() == null || updateLocation.getLongitude() < -180 || updateLocation.getLongitude() > 180) {

                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 404 - существование точки локации
                Optional<ChippingLocation> chippingLocationCheck = chippingLocationRepository.findById(pointId);
                if (chippingLocationCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Point of location with id: " + pointId + " not found");
                }

                ChippingLocation location = chippingLocationCheck.get();

                // Проверка на 400 - Если точка используется как точка чипирования или как посещенная точка,
                // то её изменять нельзя
                if (!visitedLocationRepository
                        .findVisitedLocationByIdChippingLocation_LongitudeAndIdChippingLocation_Latitude(
                                location.getLongitude(),
                                location.getLatitude())
                        .isEmpty() ||
                        !animalRepository
                                .findAnimalByIdChippingLocation_LongitudeAndIdChippingLocation_Latitude(
                                        location.getLongitude(),
                                        location.getLatitude())
                                .isEmpty()
                ) {
                    return ResponseEntity.status(400).body("Location with (latitude;longitude): (" + location.getLatitude() + "; " + location.getLongitude() + ") are point of chipping or visited point");
                }

                // Проверка на 409 - точка локации уже существует
                List<ChippingLocation> chippingLocationList = chippingLocationRepository.findAll();
                try {
                    chippingLocationList.forEach(chl -> {
                        if (chl.getLatitude().equals(updateLocation.getLatitude()) &&
                                chl.getLongitude().equals(updateLocation.getLongitude())) {
                            throw new AlreadyExistException("Location with (latitude;longitude): (" + updateLocation.getLatitude() + "; " + updateLocation.getLongitude() + ") already exist");
                        }
                    });
                } catch (AlreadyExistException eae) {
                    return ResponseEntity.status(409).body(eae.getMessage());
                }

                location.setLatitude(updateLocation.getLatitude());
                location.setLongitude(updateLocation.getLongitude());

                chippingLocationRepository.save(location);

                return ResponseEntity.status(200).body(LocationResponse.builder()
                        .id(location.getId())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude())
                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @DeleteMapping("/{pointId}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long pointId) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {

                // Проверка на 400 - валидность данных
                if (pointId == null || pointId <= 0) {
                    return ResponseEntity.status(400).body("Id: " + pointId + " is not correct");
                }

                // Проверка на 404 - существование точки локации
                Optional<ChippingLocation> chippingLocationCheck = chippingLocationRepository.findById(pointId);
                if (chippingLocationCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Point of location with id: " + pointId + " not found");
                }

                ChippingLocation location = chippingLocationCheck.get();

                // Проверка на 400 - связь точки локации с животными
                if (!location.getAnimalCollection().isEmpty() ||
                    !location.getVisitedLocationCollection().isEmpty()) {
                    return ResponseEntity.status(400).body("Location with (latitude;longitude): (" + location.getLatitude() + "; " + location.getLongitude() + ") associated with animals");
                } else {
                    chippingLocationRepository.delete(location);
                    return ResponseEntity.status(200).body("Successful removal");
                }

            } // USER & CHIPPER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // Секретные эндпоинты:
    // Эндпоинт 1 (и 2 тест)
    @GetMapping()
    public ResponseEntity<?> getLocationByCoords(@ModelAttribute LocationRequest locationSearchQuery) {
        try {
            // Проверка на 404 - существование точки локации
            Optional<ChippingLocation> checkLocation = chippingLocationRepository.findChippingLocationByLongitudeAndLatitude(locationSearchQuery.getLongitude(), locationSearchQuery.getLatitude());
            if (checkLocation.isEmpty()) {
                return ResponseEntity.status(404).body("Location with (latitude;longitude): (" + locationSearchQuery.getLatitude() + "; " + locationSearchQuery.getLongitude() + ") not found");
            }
            ChippingLocation location = checkLocation.get();

            return ResponseEntity.status(200).body(location.getId());
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // Эндпоинт 2
    @GetMapping("/geohash")
    public ResponseEntity<?> getLocationByCoordsGeohash(@ModelAttribute LocationRequest locationSearchQuery) {
        try {
            // Получение из долготы и широты геохэша
            GeoHash geoHash = GeoHash.withCharacterPrecision(locationSearchQuery.getLatitude(),
                    locationSearchQuery.getLongitude(),
                    12);
            String geoHashString = geoHash.toBase32();

            return ResponseEntity.status(200).body(geoHashString);
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // Эндпоинт 3
    @GetMapping("/geohashv2")
    public ResponseEntity<?> getLocationByCoordsGeohashV2(@ModelAttribute LocationRequest locationSearchQuery) {
        try {
            // Получение из долготы и широты геохэша
            GeoHash geoHash = GeoHash.withCharacterPrecision(locationSearchQuery.getLatitude(),
                    locationSearchQuery.getLongitude(),
                    12);
            String geohashString = geoHash.toBase32();

            // Перевод в base64
            byte[] geohashBytes = geohashString.getBytes();
            byte[] base64Bytes = Base64.getEncoder().encode(geohashBytes);
            String base64String = new String(base64Bytes);

            return ResponseEntity.status(200).body(base64String);
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // Эндпоинт 4
    @GetMapping("/geohashv3")
    public ResponseEntity<?> getLocationByCoordsGeohashV3(@ModelAttribute LocationRequest locationSearchQuery) {
        try {
            // Кодировал - декодировал, кодировал - декодировал, да не выдекодировал
            // Пробовал преобразовывать и разбивать по-байтово, так и не нашёл закономерностей
            // Пробовал и конкатенировать строки, и получать из base64 строки долготы и ширины двоичный код, с ним всякое делать
            // Пробовал от обратного, попытаться расшифровать нужную строку из тестов
            // Не догадался :(

            String preciseGeohashStr = toMyBase64(locationSearchQuery.getLatitude(), locationSearchQuery.getLongitude());

            byte[] geohashBytes = preciseGeohashStr.getBytes();
            byte[] base64Bytes = Base64.getEncoder().encode(geohashBytes);
            String base64String = new String(base64Bytes);

            return ResponseEntity.status(200).body(base64String);
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    public static String toMyBase64(double latitude, double longitude) {
        final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+/======================================================================================";
        int precision = 18;
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};

        StringBuilder geohash = new StringBuilder();

        while (geohash.length() < precision) {
            double mid;
            int bit = 0;
            int ch = 0;

            for (int i = 0; i < 7; i++) {
                if ((geohash.length() % 2) == 0) {
                    mid = (lonRange[0] + lonRange[1]) / 2.0;
                    if (longitude > mid) {
                        ch |= (1 << (6 - bit));
                        lonRange[0] = mid;
                    } else {
                        lonRange[1] = mid;
                    }
                } else {
                    mid = (latRange[0] + latRange[1]) / 2.0;
                    if (latitude > mid) {
                        ch |= (1 << (6 - bit));
                        latRange[0] = mid;
                    } else {
                        latRange[1] = mid;
                    }
                }
                bit++;
            }
            geohash.append(CHARACTERS.charAt(ch));
        }
        return geohash.toString();
    }

    @GetMapping("/")
    public ResponseEntity<?> getLocationByIdEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PutMapping("/")
    public ResponseEntity<?> updateLocationEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteLocationEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }
}
