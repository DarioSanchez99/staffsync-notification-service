package com.staffsync.notification.infrastructure.adapter.out.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class EmployeeClient {

    private final RestTemplate restTemplate;
    private final String employeeServiceUrl;

    public EmployeeClient(RestTemplate restTemplate,
                          @Value("${services.employee-url:http://localhost:8082}") String employeeServiceUrl) {
        this.restTemplate = restTemplate;
        this.employeeServiceUrl = employeeServiceUrl;
    }

    @SuppressWarnings("unchecked")
    public Optional<UUID> resolveUserId(UUID employeeId) {
        try {
            Map<String, Object> employee = restTemplate.getForObject(
                    employeeServiceUrl + "/employees/" + employeeId, Map.class);
            if (employee != null && employee.get("userId") != null) {
                return Optional.of(UUID.fromString(employee.get("userId").toString()));
            }
        } catch (Exception e) {
            log.warn("Could not resolve userId for employeeId {}: {}", employeeId, e.getMessage());
        }
        return Optional.empty();
    }
}
