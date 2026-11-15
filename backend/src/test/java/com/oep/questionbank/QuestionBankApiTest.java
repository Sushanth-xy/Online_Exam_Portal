package com.oep.questionbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.Role;
import com.oep.auth.User;
import com.oep.auth.UserRepository;
import com.oep.auth.dto.LoginRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionBankApiTest {

    private static final String PASSWORD = "Password@123";
    private static final String URL = "/api/v1/questions";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        questionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        questionRepository.deleteAll();
        userRepository.deleteAll();
    }

    // Makes an instructor, logs in, returns the token
    private String instructorToken() throws Exception {
        userRepository.deleteAll();
        userRepository.save(new User("Inst", "inst@oep-test.com",
                passwordEncoder.encode(PASSWORD), Role.INSTRUCTOR));
        String body = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new LoginRequest("inst@oep-test.com", PASSWORD))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @DisplayName("TC-QB-01: create SHORT_ANSWER question")
    void createShortAnswer() throws Exception {
        mockMvc.perform(post(URL)
                .header("Authorization", "Bearer " + instructorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prompt\":\"Capital of France?\",\"questionType\":\"SHORT_ANSWER\","
                        + "\"topic\":\"Geography\",\"correctAnswer\":\"Paris\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionType").value("SHORT_ANSWER"));
    }
        @Test
    @DisplayName("TC-QB-01: create MCQ question")
    void createMcq() throws Exception {
        mockMvc.perform(post(URL)
                .header("Authorization", "Bearer " + instructorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prompt\":\"2+2?\",\"questionType\":\"MCQ\","
                        + "\"topic\":\"Math\",\"options\":[\"3\",\"4\",\"5\"],"
                        + "\"correctAnswer\":\"4\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionType").value("MCQ"))
                .andExpect(jsonPath("$.correctAnswer").value("4"));
    }

    @Test
    @DisplayName("TC-QB-01: create TRUE_FALSE question")
    void createTrueFalse() throws Exception {
        mockMvc.perform(post(URL)
                .header("Authorization", "Bearer " + instructorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prompt\":\"The sun is a star.\",\"questionType\":\"TRUE_FALSE\","
                        + "\"topic\":\"Science\",\"correctAnswer\":\"True\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionType").value("TRUE_FALSE"));
    }
}