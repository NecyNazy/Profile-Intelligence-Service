package com.backend_torch.Data.Persistence.API.helper;

import com.backend_torch.Data.Persistence.API.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class HttpShooter {

    private final WebClient.Builder webClientBuilder;

    /**
     * Non-blocking GET request with proper 502 propagation
     */
    public <T> Mono<T> getRequest(String uri, Class<T> responseType, String externalApiName) {

        return webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()

                // HTTP errors → 502
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(
                                        new ApiException(
                                                "502",
                                                externalApiName + " returned an invalid response"
                                        )
                                ))
                )

                .bodyToMono(responseType)

                // Handle empty response
                .switchIfEmpty(Mono.error(
                        new ApiException("502", externalApiName + " returned an invalid response")
                ))

                // Timeout
                .timeout(Duration.ofSeconds(30))
                .doOnSubscribe(sub -> System.out.println("Calling " + externalApiName + " -> " + uri))
                .doOnSuccess(res -> System.out.println(externalApiName + " SUCCESS"))
                .doOnError(err -> System.out.println(externalApiName + " ERROR: " + err.getMessage()))

                // Normalize ALL errors
                .onErrorMap(error -> {

                    if (error instanceof ApiException) {
                        return error;
                    }

                    if (error instanceof TimeoutException) {
                        return new ApiException(
                                "502",
                                externalApiName + " request timed out"
                        );
                    }

                    return new ApiException(
                            "502",
                            externalApiName + " returned an invalid response"
                    );
                });
    }
}