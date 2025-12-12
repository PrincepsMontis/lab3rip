package com.example.serviceb.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ServerLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        
        log.info("[SERVER IN] {} {}", method, path);
        
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    int status = exchange.getResponse().getStatusCode() != null 
                        ? exchange.getResponse().getStatusCode().value() 
                        : 0;
                    log.info("[SERVER OUT] {} {} - Status: {}", method, path, status);
                });
    }
}
