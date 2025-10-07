package org.siriusxi.htec.fa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.siriusxi.htec.fa.infra.security.JwtTokenFilter;
import org.siriusxi.htec.fa.repository.UserRepository;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.siriusxi.htec.fa.api.InfoController;

@WebMvcTest(controllers = InfoController.class)
public class InfoControllerTests {

    @Autowired
    private MockMvc mvc;

    // Security filter and repository are part of the application context in other tests;
    // mock them here so WebMvcTest loads only the controller under test.
    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    @MockBean
    private UserRepository userRepository;

    @Test
    void infoVersionReturnsVersionAndUptime() throws Exception {
        mvc.perform(get("/v1/info/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value("v1"))
            .andExpect(jsonPath("$.uptimeMillis").isNumber());
    }
}
