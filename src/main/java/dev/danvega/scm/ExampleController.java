package dev.danvega.scm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api")
public class ExampleController {

    private final RestClient restClient;
    private static final Logger log = LoggerFactory.getLogger(ExampleController.class);

    public ExampleController(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://jsonplaceholder.typicode.com").build();
    }

    @GetMapping("/todos")
    public String getData() {
        // Blocking operation - ideal for Virtual Threads
        ResponseEntity<String> response = restClient.get()
                .uri("/todos")
                .retrieve()
                .toEntity(String.class);

        log.info("Executed on thread: {}", Thread.currentThread());
        return response.getBody();
    }
}
