package by.shakhau.ps.auth.integration;

import by.shakhau.ps.auth.config.SecurityProps;
import by.shakhau.ps.auth.messaging.consumer.CreateUserConsumer;
import by.shakhau.ps.auth.messaging.consumer.UpdateUserConsumer;
import by.shakhau.ps.auth.messaging.consumer.UpdateUserStatusConsumer;
import by.shakhau.ps.auth.messaging.producer.UserRegistrationProducer;
import by.shakhau.ps.auth.model.Role;
import by.shakhau.ps.auth.model.UserCredential;
import by.shakhau.ps.auth.repository.RefreshTokenRepository;
import by.shakhau.ps.auth.repository.UserCredentialRepository;
import by.shakhau.ps.auth.service.UserCredentialService;
import by.shakhau.ps.auth.service.impl.JwtService;
import by.shakhau.ps.auth.service.model.UserInfo;
import io.jsonwebtoken.Claims;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    protected static final String PUBLIC_KEY = UUID.randomUUID().toString();
    protected static final String USER_ID = UUID.randomUUID().toString();
    protected static final String SESSION_ID = UUID.randomUUID().toString();
    protected static final String ADMIN_SECRET = UUID.randomUUID().toString();
    protected static final String ADMIN_SECRET_HASH = DigestUtils.sha256Hex(ADMIN_SECRET);

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected SecurityProps securityProps;

    @MockitoBean
    private CreateUserConsumer createUserConsumer;

    @MockitoBean
    private UpdateUserConsumer updateUserConsumer;

    @MockitoBean
    private UpdateUserStatusConsumer userStatusConsumer;

    @MockitoBean
    private UserRegistrationProducer userRegistrationProducer;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserCredentialService userCredentialService;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected CacheManager cacheManager;

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withDatabaseName("test-db")
                    .withUsername("test-user")
                    .withPassword("test-password");

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:8.8-alpine")
                    .withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @BeforeEach
    public void setUp() {
        refreshTokenRepository.deleteAll();
        userCredentialRepository.deleteAll();

        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());

        Claims claims = mock(Claims.class);

        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 1000000));
        when(claims.getSubject()).thenReturn(USER_ID);
        when(claims.get("session_id")).thenReturn(SESSION_ID);
        when((List<String>) claims.get("roles")).thenReturn(Collections.singletonList("ROLE_ADMIN"));
        when(jwtService.getClaims(any())).thenReturn(claims);
        when(jwtService.getPublicKeyAsString()).thenReturn(PUBLIC_KEY);

        when(securityProps.getAdminInitSecretHash()).thenReturn(ADMIN_SECRET_HASH);
        when(securityProps.getMaxSessionCount()).thenReturn(5);
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    protected UserCredential createUser() {
        var userInfo = new UserInfo();

        userInfo.setFirstName("Ivan");
        userInfo.setLastName("Ivanov");
        userInfo.setBirthDate(LocalDate.of(1995, 1, 1));
        userInfo.setEmail("ivan@test.com");
        userInfo.setPassword(new StringBuilder("Password1!"));
        userInfo.setActive(true);
        userInfo.setPasswordActive(true);

        return userCredentialService.registerUser(userInfo, Role.ROLE_USER);
    }
}
