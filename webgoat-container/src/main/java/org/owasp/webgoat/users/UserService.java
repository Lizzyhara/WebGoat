// Generated by delombok at Sun Nov 21 11:47:17 UTC 2021
package org.owasp.webgoat.users;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.owasp.webgoat.session.WebSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.function.Function;

/**
 * @author nbaars
 * @since 3/19/17.
 */
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserTrackerRepository userTrackerRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Function<String, Flyway> flywayLessons;

    @Override
    public WebGoatUser loadUserByUsername(String username) throws UsernameNotFoundException {
        WebGoatUser webGoatUser = userRepository.findByUsername(username);
        if (webGoatUser == null) {
            throw new UsernameNotFoundException("User not found");
        } else {
            webGoatUser.createUser();
        }
        return webGoatUser;
    }

    public void addUser(String username, String password) {
        //get user if there exists one by the name
        var userAlreadyExists = userRepository.existsByUsername(username);
        var webGoatUser = userRepository.save(new WebGoatUser(username, password));
        if (!userAlreadyExists) {
            userTrackerRepository.save(new UserTracker(username)); //if user previously existed it will not get another tracker
            createLessonsForUser(webGoatUser);
        }
    }

    private void createLessonsForUser(WebGoatUser webGoatUser) {
        jdbcTemplate.execute("CREATE SCHEMA \"" + webGoatUser.getUsername() + "\" authorization dba");
        flywayLessons.apply(webGoatUser.getUsername()).migrate();
    }

    public List<WebGoatUser> getAllUsers() {
        return userRepository.findAll();
    }

    @java.lang.SuppressWarnings("all")
    public UserService(final UserRepository userRepository, final UserTrackerRepository userTrackerRepository, final JdbcTemplate jdbcTemplate, final Function<String, Flyway> flywayLessons) {
        this.userRepository = userRepository;
        this.userTrackerRepository = userTrackerRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.flywayLessons = flywayLessons;
    }
}
