package ru.chipization.achip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.chipization.achip.dto.request.RegisterRequest;
import ru.chipization.achip.dto.response.UserResponse;
import ru.chipization.achip.exception.BadRequestException;
import ru.chipization.achip.exception.AlreadyExistException;
import ru.chipization.achip.model.UserRoles;
import ru.chipization.achip.repository.UserRepository;

import java.util.List;
import java.util.regex.Pattern;
import ru.chipization.achip.model.User;
import ru.chipization.achip.repository.UserRolesRepository;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UserRepository userRepository;
    private final UserRolesRepository userRolesRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public UserResponse register(RegisterRequest request) {
        // Проверка на 400 - валидность данных
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        if(request.getFirstName() == null || request.getFirstName().trim().equals("") ||
            request.getLastName() == null || request.getLastName().trim().equals("") ||
            request.getEmail() == null || request.getEmail().trim().equals("") || !pattern.matcher(request.getEmail()).matches() ||
            request.getPassword() == null || request.getPassword().trim().equals("")) {

            throw new BadRequestException("No valid data");
        }

        // Проверка на 409 - email уже существует
        List<User> userList = userRepository.findAll();
        userList.forEach(us -> {
            if(us.getEmail().equals(request.getEmail())) {
                throw new AlreadyExistException("User with email: " + request.getEmail() + " already exist");
            }
        });

        // По умолчанию роль USER
        UserRoles userRole = userRolesRepository.findUserRolesByRole("USER").get();

        var user = User.builder().build();
        if(request.getRole() != null) {
            user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .userRolesByIdUserRole(userRolesRepository.findUserRolesByRole(request.getRole()).get())
                    .build();
        } else {
            user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .userRolesByIdUserRole(userRole)
                    .build();
        }


        userRepository.save(user);

        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getUserRolesByIdUserRole().getRole())
                .id(userRepository.findByEmail(user.getEmail()).get().getId().intValue())
                .build();
    }
}
