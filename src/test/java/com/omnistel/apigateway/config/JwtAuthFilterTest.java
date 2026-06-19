package com.omnistel.apigateway.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private ReactiveJwtDecoder jwtDecoder;

    @Mock
    private WebFilterChain chain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Captor
    private ArgumentCaptor<ServerWebExchange> exchangeCaptor;

    @Test
    void filter_ShouldAllowPublicPaths() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void filter_ShouldReturn401_WhenNoAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/tickets").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_ShouldReturn401_WhenInvalidAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Invalid")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_ShouldReturn401_WhenJwtDecodeFails() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(jwtDecoder.decode("invalid-token")).thenReturn(Mono.error(new RuntimeException("Invalid JWT")));

        jwtAuthFilter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void filter_ShouldAddHeaders_WhenJwtValid() {
        Jwt jwt = Jwt.withTokenValue("valid-token")
                .header("alg", "RS256")
                .subject("1")
                .claim("role", "CLIENT")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtDecoder.decode("valid-token")).thenReturn(Mono.just(jwt));
        when(chain.filter(any())).thenReturn(Mono.empty());

        jwtAuthFilter.filter(exchange, chain).block();

        verify(chain).filter(exchangeCaptor.capture());
        ServerWebExchange mutatedExchange = exchangeCaptor.getValue();
        assertEquals("1", mutatedExchange.getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals("CLIENT", mutatedExchange.getRequest().getHeaders().getFirst("X-User-Role"));
    }
}
