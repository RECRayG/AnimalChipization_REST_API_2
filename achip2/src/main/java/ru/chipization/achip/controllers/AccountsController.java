package ru.chipization.achip.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.chipization.achip.dto.request.RegisterRequest;
import ru.chipization.achip.dto.response.UserResponse;
import ru.chipization.achip.dto.search.UserSearchQuery;
import ru.chipization.achip.dto.search.Request;
import ru.chipization.achip.dto.search.SearchRequest;
import ru.chipization.achip.exception.AlreadyExistException;
import ru.chipization.achip.exception.BadRequestException;
import ru.chipization.achip.repository.AnimalRepository;
import ru.chipization.achip.repository.UserRepository;
import ru.chipization.achip.repository.UserRolesRepository;
import ru.chipization.achip.service.FilterSpecification;
import ru.chipization.achip.service.OffsetBasedPageRequest;
import ru.chipization.achip.model.User;
import ru.chipization.achip.service.RegisterService;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@CrossOrigin("*")
@RequestMapping("/accounts")
@AllArgsConstructor
public class AccountsController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRolesRepository userRolesRepository;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private FilterSpecification<User> filterSpecification;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable Integer accountId) {
        try {
            if(accountId == null || accountId <= 0) {
                return ResponseEntity.status(400).body("Id: " + accountId + " is not correct");
            }

            User user;

            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // Получение пользователя из БД по переданному accountId
            Optional<User> userCheck = userRepository.findById(Long.valueOf(accountId));

            // ADMIN
            if(currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {
                // Проверка на существование искомого аккаунта
                if(userCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("ADMIN: User with id: " + accountId + " not found");
                }
                user = userCheck.get();

            } // CHIPPER & USER
            else {
                // Проверка на существование искомого аккаунта
                if(userCheck.isEmpty()) {
                    return ResponseEntity.status(403).body("CHIPPER or USER: User with id: " + accountId + " not found");
                }
                user = userCheck.get();

                // Проверка на 403 - получение не своего аккаунта
                if(currentUser.getId() != user.getId()) {
                    return ResponseEntity.status(403).body("CHIPPER or USER: Get not your account");
                }
            }

            return ResponseEntity.status(200).body(UserResponse.builder()
                    .id(user.getId().intValue())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .role(user.getUserRolesByIdUserRole().getRole())
                    .build());
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAccounts(@ModelAttribute UserSearchQuery userSearchQuery//,
                                            /*@RequestParam("from") Integer from,*/
                                            /*@RequestParam("size") Integer size*/) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if(currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {

                if (userSearchQuery.getFrom() == null) {
                    userSearchQuery.setFrom(0);
                }
                if (userSearchQuery.getSize() == null) {
                    userSearchQuery.setSize(10);
                }
                if (userSearchQuery.getFrom() < 0) {
                    return ResponseEntity.status(400).body("Count of \'from\': " + userSearchQuery.getFrom() + " is not correct");
                }
                if (userSearchQuery.getSize() <= 0) {
                    return ResponseEntity.status(400).body("Count of \'size\': " + userSearchQuery.getSize() + " is not correct");
                }

                Request request = new Request(new ArrayList<>());
                Pageable pageable = new OffsetBasedPageRequest(
                        userSearchQuery.getFrom() != null && userSearchQuery.getFrom() > 0 ? userSearchQuery.getFrom() : 0,
                        userSearchQuery.getSize() != null && userSearchQuery.getSize() > 0 ? userSearchQuery.getSize() : 10);

                if (userSearchQuery.getFirstName() != null) {
                    request.getSearchRequest().add(new SearchRequest("firstName", userSearchQuery.getFirstName()));
                }
                if (userSearchQuery.getLastName() != null) {
                    request.getSearchRequest().add(new SearchRequest("lastName", userSearchQuery.getLastName()));
                }
                if (userSearchQuery.getEmail() != null) {
                    request.getSearchRequest().add(new SearchRequest("email", userSearchQuery.getEmail()));
                }

                Specification<User> searchSpecification = filterSpecification
                        .getSearchSpecification(request.getSearchRequest());


                List<User> users = userRepository.findAll(searchSpecification, pageable).getContent();
                List<UserResponse> userResponses = new ArrayList<>();
                users.forEach(user -> {
                    userResponses.add(UserResponse.builder()
                            .id(user.getId().intValue())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .role(user.getUserRolesByIdUserRole().getRole())
                            .build());
                });


                return ResponseEntity.status(200).body(userResponses);
            } // CHIPPER & USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping()
    public ResponseEntity<?> addAccount(@RequestBody RegisterRequest insertAccount) {
        try {
            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // Добавление нового аккаунта
            UserResponse user;
            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {
                // Проверка на 400 - валидность данных
                if (insertAccount.getRole() != null) {
                    // Проверка на 400 - соответствие определённым ролям
                    if (!roleCheck(insertAccount.getRole())) {
                        return ResponseEntity.status(400).body("Not valid data");
                    }
                }

                // Проверка на всё остальное (метод регистрации)
                try {
                    user = registerService.register(insertAccount);
                } catch (AlreadyExistException aee) {
                    return ResponseEntity.status(409).body(aee.getMessage());
                } catch (BadRequestException bre) {
                    return ResponseEntity.status(400).body(bre.getMessage());
                }

            } // CHIPPER & USER
            else {
                return ResponseEntity.status(403).body("You are not ADMIN");
            }

            return ResponseEntity.status(201).body(UserResponse.builder()
                    .id(user.getId().intValue())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build());
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<?> updateAccount(@PathVariable Integer accountId, @RequestBody RegisterRequest updateAccount) {
        try {
            // Проверка на 400 - валидность данных
            Pattern pattern = Pattern.compile(EMAIL_PATTERN);
            if (accountId == null || accountId <= 0 ||
                    updateAccount.getFirstName() == null || updateAccount.getFirstName().trim().equals("") ||
                    updateAccount.getLastName() == null || updateAccount.getLastName().trim().equals("") ||
                    updateAccount.getEmail() == null || updateAccount.getEmail().trim().equals("") || !pattern.matcher(updateAccount.getEmail()).matches() ||
                    updateAccount.getPassword() == null || updateAccount.getPassword().trim().equals("")) {

                return ResponseEntity.status(400).body("Not valid data");
            }

            if (updateAccount.getRole() != null) {
                if (!roleCheck(updateAccount.getRole()))
                    return ResponseEntity.status(400).body("Not valid data");
            }

            User user;

            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {
                // Проверка на 404 - существование аккаунта
                Optional<User> userCheck = userRepository.findById(Long.valueOf(accountId));
                if (userCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("ADMIN: Account with id: " + accountId + " is not found");
                }
                user = userCheck.get();
            } // CHIPPER & USER
            else {
                // Проверка на 403 - существование аккаунта
                Optional<User> userCheck = userRepository.findById(Long.valueOf(accountId));
                if (userCheck.isEmpty()) {
                    return ResponseEntity.status(403).body("CHIPPER or USER: Account with id: " + accountId + " is not found");
                }
                user = userCheck.get();

                // Проверка на 403 - обновление не своего аккаунта
                if (currentUser.getId() != user.getId()) {
                    return ResponseEntity.status(403).body("CHIPPER or USER: Update not your account");
                }
            }

            // Проверка на 409 - email уже существует
            List<User> userList = userRepository.findAll();
            try {
                Long tempId = user.getId();
                userList.forEach(us -> {
                    if (us.getEmail().equals(updateAccount.getEmail()) && !tempId.equals(us.getId())) {
                        throw new AlreadyExistException("User with email: " + updateAccount.getEmail() + " already exist");
                    }
                });
            } catch (AlreadyExistException eae) {
                return ResponseEntity.status(409).body(eae.getMessage());
            }

            user.setFirstName(updateAccount.getFirstName());
            user.setLastName(updateAccount.getLastName());
            user.setEmail(updateAccount.getEmail());
            user.setPassword(passwordEncoder.encode(updateAccount.getPassword()));
            if (updateAccount.getRole() != null) {
                user.setUserRolesByIdUserRole(userRolesRepository.findUserRolesByRole(updateAccount.getRole()).get());
            }

            userRepository.save(user);

            return ResponseEntity.status(200).body(UserResponse.builder()
                    .id(user.getId().intValue())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .role(user.getUserRolesByIdUserRole().getRole())
                    .build());
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> deleteAccount(@PathVariable Integer accountId) {
        try {
            // Проверка на 400 - валидность данных
            if (accountId == null || accountId <= 0) {
                return ResponseEntity.status(400).body("Id: " + accountId + " is not correct");
            }

            User user;

            // Получение нформации о пользователе, отправившего запрос
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ADMIN
            if (currentUser.getUserRolesByIdUserRole().getRole().equals("ADMIN")) {
                // Проверка на 404 - существование аккаунта
                Optional<User> userCheck = userRepository.findById(Long.valueOf(accountId));
                if (userCheck.isEmpty()) {
                    return ResponseEntity.status(404).body("ADMIN: Account with id: " + accountId + " is not found");
                }
                user = userCheck.get();
            }
            // CHIPPER & USER
            else {
                // Проверка на 403 - существование аккаунта
                Optional<User> userCheck = userRepository.findById(Long.valueOf(accountId));
                if (userCheck.isEmpty()) {
                    return ResponseEntity.status(403).body("CHIPPER or USER: Account with id: " + accountId + " is not found");
                }
                user = userCheck.get();

                // Проверка на 403 - удаление не своего аккаунта
                if (currentUser.getId() != user.getId()) {
                    return ResponseEntity.status(403).body("CHIPPER or USER: Delete not your account");
                }
            }

            // Проверка на 400 - связь аккаунта с животными
//        List<Animal> animalsCheck = animalRepository.findAnimalsByIdChipper(user).get();
            if (!user.getAnimalCollection().isEmpty()) {
                return ResponseEntity.status(400).body("Account associated with animals");
            } else {
                userRepository.delete(user);
                return ResponseEntity.status(200).body("Successful removal");
            }
        } catch(Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getAccountIdEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PutMapping("/")
    public ResponseEntity<?> updateAccountEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @PostMapping("/")
    public ResponseEntity<?> addAccountEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteAccountEmpty() {
        return ResponseEntity.status(400).body("Id is null");
    }

    public boolean roleCheck(String roleName) {
        boolean isCorrectRole = false;

        switch(roleName) {
            case "ADMIN":
                isCorrectRole = true;
                break;
            case "CHIPPER":
                isCorrectRole = true;
                break;
            case "USER":
                isCorrectRole = true;
                break;
        }

        return isCorrectRole;
    }
}
