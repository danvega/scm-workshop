package dev.danvega.scm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

@Component
public class VirtualThreadDemo implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(VirtualThreadDemo.class);
    private final RestClient restClient;
    private boolean runThisDemo = false;

    public VirtualThreadDemo(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://httpbin.org").build();
    }

    @Override
    public void run(String... args) throws Exception {
        if(runThisDemo) {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                // Submit multiple tasks
                List<Future<String>> futures = IntStream.range(0, 5)
                        .mapToObj(i -> executor.submit(() -> makeBlockingCall(i)))
                        .toList();

                // Wait for all tasks to complete
                for (Future<String> future : futures) {
                    log.info(future.get());
                }
            }
        }
    }

    private String makeBlockingCall(int i) {
        ResponseEntity<Void> response = restClient.get()
                .uri("/delay/1")
                .retrieve()
                .toBodilessEntity();

        return "Task %d completed on %s".formatted(i, Thread.currentThread());
    }
}
