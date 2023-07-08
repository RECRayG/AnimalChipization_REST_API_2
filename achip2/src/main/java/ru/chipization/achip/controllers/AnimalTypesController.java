package ru.chipization.achip.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.chipization.achip.dto.response.AnimalTypeResponse;
import ru.chipization.achip.exception.AlreadyExistException;
import ru.chipization.achip.model.AnimalType;
import ru.chipization.achip.model.User;
import ru.chipization.achip.repository.AnimalTypesRepository;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/animals/types")
@AllArgsConstructor
public class AnimalTypesController {
    @Autowired
    private AnimalTypesRepository animalTypesRepository;

    @GetMapping("/{typeId}")
    public ResponseEntity<?> getAnimalTypeById(@PathVariable Long typeId) {
        try {
            // Получение нформации о пользователе, отправившего запрос
//        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
//        if(currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
//                currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

            // Проверка на 400 - валидность данных
            if (typeId == null || typeId <= 0) {
                return ResponseEntity.status(400).body("Id: " + typeId + " is not correct");
            }

            // Проверка на 404 - существование типа животного
            Optional<AnimalType> animalTypeCheck = animalTypesRepository.findById(typeId);
            if (animalTypeCheck.isEmpty()) {
                return ResponseEntity.status(404).body("Type of animal with id: " + typeId + " not found");
            }

            AnimalType animalType = animalTypeCheck.get();

            return new ResponseEntity<AnimalTypeResponse>(AnimalTypeResponse.builder()
                    .id(animalType.getId())
                    .type(animalType.getType())
                    .build(),
                    HttpStatus.OK);
//        } // USER
//        else {
//            return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
//        }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping()
    public ResponseEntity<?> addAnimalsType(@RequestBody AnimalTypeResponse insertAnimalsType) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность данных
                if (insertAnimalsType.getType() == null || insertAnimalsType.getType().trim().equals("")) {
                    return ResponseEntity.status(400).body("No valid data");
                }

                // Проверка на 409 - тип животного уже существует
                List<AnimalType> animalTypeList = animalTypesRepository.findAll();
                try {
                    animalTypeList.forEach(at -> {
                        if (at.getType().equals(insertAnimalsType.getType())) {
                            throw new AlreadyExistException("Animal\'s type: " + insertAnimalsType.getType() + " already exist");
                        }
                    });
                } catch (AlreadyExistException eae) {
                    return ResponseEntity.status(409).body(eae.getMessage());
                }

                var animalsType = AnimalType.builder()
                        .type(insertAnimalsType.getType())
                        .build();

                animalTypesRepository.save(animalsType);

                return ResponseEntity.status(201).body(AnimalTypeResponse.builder()
                        .id(animalTypesRepository.findByType(animalsType.getType()).get().getId())
                        .type(animalsType.getType())
                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/{typeId}")
    public ResponseEntity<?> updateAnimalType(@PathVariable Long typeId, @RequestBody AnimalTypeResponse updateAnimalType) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN or CHIPPER
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN") ||
                    currentUser.getUserRolesByIdUserRole().getRole().equals("CHIPPER")) {

                // Проверка на 400 - валидность данных
                if (typeId == null || typeId <= 0 ||
                        updateAnimalType.getType() == null || updateAnimalType.getType().trim().equals("")) {

                    return ResponseEntity.status(400).body("Not valid data");
                }

                // Проверка на 404 - существование типа животного
                Optional<AnimalType> animalTypeCheck = animalTypesRepository.findById(typeId);
                if (animalTypeCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("Type of animal with id: " + typeId + " not found");
                }

                AnimalType animalType = animalTypeCheck.get();

                // Проверка на 409 - тип животного уже существует
                List<AnimalType> animalTypeList = animalTypesRepository.findAll();
                try {
                    animalTypeList.forEach(at -> {
                        if (at.getType().equals(updateAnimalType.getType())) {
                            throw new AlreadyExistException("Animal\'s type: " + updateAnimalType.getType() + " already exist");
                        }
                    });
                } catch (AlreadyExistException eae) {
                    return ResponseEntity.status(409).body(eae.getMessage());
                }

                animalType.setType(updateAnimalType.getType());

                animalTypesRepository.save(animalType);

                return ResponseEntity.status(200).body(AnimalTypeResponse.builder()
                        .id(animalType.getId())
                        .type(animalType.getType())
                        .build());
            } // USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN or CHIPPER");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @DeleteMapping("/{typeId}")
    public ResponseEntity<?> deleteAnimalType(@PathVariable Long typeId) {
        try {
            // Проверка на 400 - валидность данных
            if (typeId == null || typeId <= 0) {
                return ResponseEntity.status(400).body("Id: " + typeId + " is not correct");
            }

            // Проверка на 404 - существование аккаунта
            Optional<AnimalType> animalTypeCheck = animalTypesRepository.findById(typeId);
            if (animalTypeCheck.isEmpty()) {
                return ResponseEntity.status(404).body("Animal\'s type with id: " + typeId + " is not found");
            }
            AnimalType animalType = animalTypeCheck.get();


            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {
                // Проверка на 400 - связь типа животного с животными
                if (!animalType.getAAtIdentities().isEmpty()) {
                    return ResponseEntity.status(400).body("Animal\'s type: " + animalType.getType() + " associated with animals");
                } else {
                    animalTypesRepository.delete(animalType);
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

    @GetMapping("/")
    public ResponseEntity<?> getAnimalTypeByIdEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PutMapping("/")
    public ResponseEntity<?> updateAnimalTypeEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteAnimalTypeEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }
}
