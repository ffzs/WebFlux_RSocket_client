package com.ffzs.webflux.rsocket_client;

import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@SpringBootApplication
public class RsocketClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsocketClientApplication.class, args);
    }

}


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Temperature {
    Long id;
    Double temperature;
    LocalDateTime time;
}

interface Client {
    Flux<Temperature> temperature ();
}

@Service
@RequiredArgsConstructor
class RSocketClient implements Client {

    private final RSocketRequester rSocketRequester;

    public Flux<Temperature> temperature () {
        return rSocketRequester.route("server")
                .retrieveFlux(Temperature.class);
    }
}

@Configuration
class ClientConfiguration {
    @Bean
    public Client client (RSocketRequester rSocketRequester) {
        return new RSocketClient(rSocketRequester);
    }

    @Bean
    public RSocketRequester rSocketRequester (RSocketRequester.Builder builder) {
        return builder.connectTcp("localhost", 8081).block();
    }
}

@RestController
@RequiredArgsConstructor
@RequestMapping("client")
class RSocketClientController {

    private final RSocketClient rSocketClient;

    @GetMapping(produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Temperature> temperature() {
        return rSocketClient.temperature();
    }
}