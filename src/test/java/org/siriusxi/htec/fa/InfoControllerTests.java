package org.siriusxi.htec.fa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.test.context.support.WithMockUser;
import org.siriusxi.htec.fa.infra.security.JwtTokenFilter;
import org.siriusxi.htec.fa.repository.UserRepository;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.siriusxi.htec.fa.api.InfoController;

@WebMvcTest(controllers = InfoController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.version=v1",
    "spring.security.user.name=test",
    "spring.security.user.password=test",
    "logging.level.org.springframework=WARN",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.flyway.enabled=false"
})
public class InfoControllerTests {

    @Autowired
    private MockMvc mvc;

    // Mock security components to avoid complex setup
    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void infoVersionReturnsVersionAndUptime() throws Exception {
        mvc.perform(get("/v1/info/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value("v1"))
            .andExpect(jsonPath("$.uptimeMillis").isNumber());
    }
}
