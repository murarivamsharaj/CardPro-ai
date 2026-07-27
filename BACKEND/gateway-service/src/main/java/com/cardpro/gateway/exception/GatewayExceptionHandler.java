package com.cardpro.gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Order(-1)
@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(org.springframework.web.server.ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof NotFoundException) {
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        } else if (ex instanceof ResponseStatusException statusException) {
            response.setStatusCode(statusException.getStatusCode());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String body = "{\"status\":\"error\",\"message\":\"" + ex.getMessage() + "\"}";
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}
