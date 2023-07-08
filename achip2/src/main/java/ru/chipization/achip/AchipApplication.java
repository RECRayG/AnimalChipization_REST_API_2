package ru.chipization.achip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.chipization.achip.dto.request.RegisterRequest;
import ru.chipization.achip.model.UserRoles;
import ru.chipization.achip.repository.UserRepository;
import ru.chipization.achip.repository.UserRolesRepository;
import ru.chipization.achip.service.RegisterService;

import javax.sql.DataSource;

@SpringBootApplication
public class AchipApplication implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RegisterService registerService;

	@Autowired
	private UserRolesRepository userRolesRepository;

	public static void main(String[] args) {
		SpringApplication.run(AchipApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Проверка на наличие в БД соответствующих ролей и создание таковых,
		// если роль не была найдена
		if(!userRolesRepository.existsUserRolesByRole("ADMIN")) {
			userRolesRepository.save(UserRoles.builder().role("ADMIN").build());
		}
		if(!userRolesRepository.existsUserRolesByRole("CHIPPER")) {
			userRolesRepository.save(UserRoles.builder().role("CHIPPER").build());
		}
		if(!userRolesRepository.existsUserRolesByRole("USER")) {
			userRolesRepository.save(UserRoles.builder().role("USER").build());
		}

		// Проверка на наличие в БД соответствующих пользователей и создание таковых,
		// если пользователь ещё не создан
		if(!userRepository.existsUserByEmail("admin@simbirsoft.com")) {
			registerService.register(RegisterRequest.builder()
					.firstName("adminFirstName")
					.lastName("adminLastName")
					.email("admin@simbirsoft.com")
					.password("qwerty123")
					.role("ADMIN")
					.build());
		}
		if(!userRepository.existsUserByEmail("chipper@simbirsoft.com")) {
			registerService.register(RegisterRequest.builder()
					.firstName("chipperFirstName")
					.lastName("chipperLastName")
					.email("chipper@simbirsoft.com")
					.password("qwerty123")
					.role("CHIPPER")
					.build());
		}
		if(!userRepository.existsUserByEmail("user@simbirsoft.com")) {
			registerService.register(RegisterRequest.builder()
					.firstName("userFirstName")
					.lastName("userLastName")
					.email("user@simbirsoft.com")
					.password("qwerty123")
					.role("USER")
					.build());
		}
	}
}
