package org.example.adresarzam;

import org.example.adresarzam.controller.EmployeeRestController;
import org.example.adresarzam.entity.Employee;
import org.example.adresarzam.repository.EmployeeRepository;
import org.example.adresarzam.response.ResponseFactory;
import org.example.adresarzam.service.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Real controller, validation, service and exception handler; no connection to the user's MySQL.
@WebMvcTest(EmployeeRestController.class)
@Import({EmployeeServiceImpl.class, ResponseFactory.class})
class AdresarZamApplicationTests {
    private static final String VALID = """
            {"firstName":"Olena","lastName":"Koval","birthDate":"1998-05-12",
             "email":"olena@example.com","phone":"+421901234567",
             "jobTitle":"Java Developer","salary":2500,"fullTime":true}
            """;

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockitoBean private EmployeeRepository repository;

    @BeforeEach
    void setUp() {
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            if (employee.getId() == null) employee.setId(1);
            return employee;
        });
    }

    @Test
    void listsEmployeesAndWrapsEmptyList() throws Exception {
        when(repository.findAll()).thenReturn(List.of());
        assertEnvelope(mvc.perform(get("/api/employees"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty()).andReturn());
    }

    @Test
    void findsOneEmployee() throws Exception {
        when(repository.findById(1)).thenReturn(Optional.of(employee(1)));
        assertEnvelope(mvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(1)).andReturn());
    }

    @Test
    void createsEmployeeAndIgnoresSubmittedId() throws Exception {
        ObjectNode body = (ObjectNode) mapper.readTree(VALID);
        body.put("id", 999);
        assertEnvelope(mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.id").value(1)).andReturn());
        verify(repository, never()).findById(any());
    }

    @Test
    void updatesUsingUrlIdAndAllowsOwnEmail() throws Exception {
        Employee existing = employee(5);
        when(repository.findById(5)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("olena@example.com")).thenReturn(Optional.of(existing));
        ObjectNode body = (ObjectNode) mapper.readTree(VALID);
        body.put("id", 999);
        body.put("jobTitle", "Senior Developer");
        assertEnvelope(mvc.perform(put("/api/employees/5").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.jobTitle").value("Senior Developer")).andReturn());
        verify(repository).save(argThat(e -> e.getId().equals(5)));
    }

    @Test
    void deletesWithJsonBody() throws Exception {
        when(repository.existsById(5)).thenReturn(true);
        assertEnvelope(mvc.perform(delete("/api/employees/5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty()).andReturn());
        verify(repository).deleteById(5);
    }

    @Test
    void missingEmployeesReturn404ForReadUpdateAndDelete() throws Exception {
        assertEnvelope(mvc.perform(get("/api/employees/999")).andExpect(status().isNotFound()).andReturn());
        assertEnvelope(mvc.perform(put("/api/employees/999").contentType(MediaType.APPLICATION_JSON)
                .content(VALID)).andExpect(status().isNotFound()).andReturn());
        assertEnvelope(mvc.perform(delete("/api/employees/999")).andExpect(status().isNotFound()).andReturn());
        verify(repository, never()).save(any());
        verify(repository, never()).deleteById(any());
    }

    @Test
    void rejectsDuplicateEmailOnCreateAndUpdate() throws Exception {
        when(repository.existsByEmail("olena@example.com")).thenReturn(true);
        assertEnvelope(mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(VALID)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0].field").value("email")).andReturn());
        when(repository.findById(5)).thenReturn(Optional.of(employee(5)));
        when(repository.findByEmail("olena@example.com")).thenReturn(Optional.of(employee(1)));
        assertEnvelope(mvc.perform(put("/api/employees/5").contentType(MediaType.APPLICATION_JSON)
                .content(VALID)).andExpect(status().isConflict()).andReturn());
        verify(repository, never()).save(any());
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("firstName", " "), Arguments.of("lastName", "A"),
                Arguments.of("birthDate", null), Arguments.of("birthDate", "2999-01-01"),
                Arguments.of("salary", null), Arguments.of("salary", 1000),
                Arguments.of("phone", "0901234567"), Arguments.of("phone", null),
                Arguments.of("jobTitle", ""), Arguments.of("jobTitle", "A".repeat(256)),
                Arguments.of("email", null), Arguments.of("fullTime", null));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void validatesBothCreateAndUpdate(String field, Object value) throws Exception {
        ObjectNode body = (ObjectNode) mapper.readTree(VALID);
        body.set(field, mapper.valueToTree(value));
        String json = mapper.writeValueAsString(body);
        assertEnvelope(mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == '" + field + "')]").isNotEmpty()).andReturn());
        assertEnvelope(mvc.perform(put("/api/employees/5").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andReturn());
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@ukf", ".user@example.com", "user.@example.com", "a..b@example.com",
            "user@-example.com", "user@example-.com", "user@example..com", "user#example.com",
            "user name@example.com", "user@example.c"})
    void rejectsInvalidEmails(String email) throws Exception {
        ObjectNode body = (ObjectNode) mapper.readTree(VALID);
        body.put("email", email);
        assertEnvelope(mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')]").isNotEmpty()).andReturn());
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first.last+tag@example.com", "o'connor@sub.example.sk", "user@my-company.sk",
            "user_name@example.travel"})
    void acceptsCommonEmailFormats(String email) throws Exception {
        ObjectNode body = (ObjectNode) mapper.readTree(VALID);
        body.put("email", email);
        assertEnvelope(mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body))).andExpect(status().isCreated()).andReturn());
    }

    @Test
    void enforcesEmailLocalPartLimit() throws Exception {
        ObjectNode body = (ObjectNode) mapper.readTree(VALID);
        body.put("email", "a".repeat(65) + "@example.com");
        assertEnvelope(mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest()).andReturn());
    }

    @Test
    void wrapsInvalidIdMalformedJsonAndWrongValueTypes() throws Exception {
        assertEnvelope(mvc.perform(get("/api/employees/abc")).andExpect(status().isBadRequest()).andReturn());
        for (String body : List.of("{", VALID.replace("1998-05-12", "not-a-date"),
                VALID.replace("2500", "\"not-a-number\""))) {
            assertEnvelope(mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                    .content(body)).andExpect(status().isBadRequest()).andReturn());
        }
        assertEnvelope(mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn());
    }

    @Test
    void keepsFrameworkErrorStatuses() throws Exception {
        assertEnvelope(mvc.perform(get("/api/unknown")).andExpect(status().isNotFound()).andReturn());
        assertEnvelope(mvc.perform(patch("/api/employees/5"))
                .andExpect(status().isMethodNotAllowed()).andReturn());
        assertEnvelope(mvc.perform(post("/api/employees").contentType(MediaType.TEXT_PLAIN).content(VALID))
                .andExpect(status().isUnsupportedMediaType()).andReturn());
    }

    @Test
    void hidesUnexpectedInternalErrors() throws Exception {
        when(repository.findAll()).thenThrow(new IllegalStateException("internal database detail"));
        MvcResult result = mvc.perform(get("/api/employees"))
                .andExpect(status().isInternalServerError()).andReturn();
        assertEnvelope(result);
        assertThat(result.getResponse().getContentAsString()).doesNotContain("internal database detail");
    }

    private Employee employee(int id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName("Olena");
        employee.setLastName("Koval");
        employee.setBirthDate(LocalDate.of(1998, 5, 12));
        employee.setEmail("olena@example.com");
        employee.setPhone("+421901234567");
        employee.setJobTitle("Java Developer");
        employee.setSalary(2500);
        employee.setFullTime(true);
        return employee;
    }

    private void assertEnvelope(MvcResult result) throws Exception {
        assertThat(result.getResponse().getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        JsonNode json = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.size()).isEqualTo(4);
        for (String key : List.of("data", "message", "errors", "timestamp")) {
            assertThat(json.has(key)).as(key).isTrue();
        }
        assertThat(json.get("timestamp").asText()).endsWith("Z");
        assertThat(Instant.parse(json.get("timestamp").asText())).isBeforeOrEqualTo(Instant.now());
    }
}
