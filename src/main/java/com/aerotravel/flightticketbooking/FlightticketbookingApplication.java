package com.aerotravel.flightticketbooking;

import com.aerotravel.flightticketbooking.model.Role;
import com.aerotravel.flightticketbooking.model.User;
import com.aerotravel.flightticketbooking.repository.FlightRepository;
import com.aerotravel.flightticketbooking.repository.RoleRepository;
import com.aerotravel.flightticketbooking.services.UserService;
import com.aerotravel.flightticketbooking.services.aux.DataGenService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

@EnableJpaRepositories("com.aerotravel.flightticketbooking.repository")
@EntityScan("com.aerotravel.flightticketbooking.model")
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Flight ticket booking API", version = "00.06",
        contact = @Contact(name = "FTB API Support",
                url = "http://ya.ru",
                email = "4ernaja@dyr.a")))
@Slf4j
public class FlightticketbookingApplication {

    private final static String logEntryTemplate = "\n\n===================================\n\n" +
            "\t\t{}" +
            "\n\n===================================\n\n";
    @Autowired
    UserService userService;
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    DataGenService dataGenService;

    public static void main(String[] args) {
        SpringApplication.run(FlightticketbookingApplication.class, args);
    }

    @PostConstruct
    public void postConstructInit() throws InterruptedException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        createUsersAndRolesIfNeeded();
        createDataIfNeeded();
    }

    public void createUsersAndRolesIfNeeded() {
        log.info(logEntryTemplate, "Check roles available.");
        var roles = roleRepository.findAll();
        if (roles.isEmpty()) {
            var adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .build();
            var agentRole = Role.builder()
                    .name("ROLE_AGENT")
                    .build();
            log.info(logEntryTemplate, "Inserting ADMIN role.");
            roleRepository.save(adminRole);
            log.info(logEntryTemplate, "Inserting AGENT role.");
            roleRepository.save(agentRole);
            roles = roleRepository.findAll();
        }

        var users = userService.getAll();
        log.info(logEntryTemplate, "Check user accounts available.");
        createUserIfNeeded(roles, users, "ADMIN", "john", "$2a$10$dRM33.Fy7SYDraG5vMagXOgIhsB6Tl40VI9pwMlNhB4yfLaZpQj.m", "JohnTheAdmin@ftb.com", "John", "O.", "Ad-Mi", "\n\t\tAdmin user created: {}");
        createUserIfNeeded(roles, users, "ADMIN", "adm", "$2a$10$dRM33.Fy7SYDraG5vMagXOgIhsB6Tl40VI9pwMlNhB4yfLaZpQj.m", "SimplyTheAdmin@ftb.com", "Vasja", "O.", "Stojkov", "\n\t\tVasja-Admin has joined the chat: {}");
        createUserIfNeeded(roles, users, "AGENT", "mike", "$2a$10$vukSIdxmmtLYcy/uNMBUHeyj/qbNPcaX8lqTbXGciJ9HxaLQOmRO.", "MikeTheAgent@ftb.com", "Mike", "A.", "Gent", "\n\t\tAgent user created: {}");
    }

    private void createUserIfNeeded(List<Role> roles, List<User> users, String roleSign, String username, String password, String email, String name, String middlename, String lastname, String logMsg) {
        var role = roles.stream()
                .filter(r -> r.getName().toUpperCase().contains(roleSign)).findFirst().orElse(null);
        var candidate = users.stream()
                .filter(u -> {
                    if (!Objects.nonNull(u)) return false;
                    assert role != null;
                    return u.getRoles().stream().map(Role::getId).collect(Collectors.toList())
                    .contains(role.getId());
                })
                .findFirst().orElse(null);
        if (null == candidate) {
            assert role != null;
            var admin = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .firstname(name)
                    .middlename(middlename)
                    .lastname(lastname)
                    .roles(List.of(role))
                    .build();

            log.info(logEntryTemplate, "Inserting user, role: " + roleSign);
            var result = userService.save(admin);
            log.info(logMsg, result);
        }
    }

    private void createDataIfNeeded() {
        log.info(logEntryTemplate, "Check Flights available.");
        var existingFlights = flightRepository.findAll();
        if (existingFlights.isEmpty()) {
            log.info(logEntryTemplate, "Generating and inserting flights etc data.");
            var data = dataGenService.generateAll();
            log.info(logEntryTemplate, "Seems the DB was empty so added some data: \n" + data);
        }
    }
}