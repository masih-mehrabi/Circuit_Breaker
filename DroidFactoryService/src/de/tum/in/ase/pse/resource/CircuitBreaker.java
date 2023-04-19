package de.tum.in.ase.pse.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


@Component
public class CircuitBreaker<T, R> {
    private static final long WAITING_TIME = 10_000_000_000L;
    private static final int MAX_NUMBER_OF_ATTEMPTS_WITH_UNSUCCESSFUL_REPLY = 2;
    //Do NOT make these attributes final, this will cause the tests to fail.
    private Map<T, AtomicInteger> unsuccessfulAttemptsPerEndpoint = new ConcurrentHashMap<>();
    private Map<T, CircuitBreakerState> circuitBreakerStatePerEndpoint = new ConcurrentHashMap<>();
    private Map<T, Long> waitingStartTimePerEndpoint = new ConcurrentHashMap<>();
    
    public ResponseEntity<R> protectedCall(T object, Function<T, R> operation) {
        CircuitBreakerState state = circuitBreakerStatePerEndpoint.computeIfAbsent(object, k -> CircuitBreakerState.CLOSED);
        AtomicInteger unsuccessfulAttempts = unsuccessfulAttemptsPerEndpoint.computeIfAbsent(object, k -> new AtomicInteger());
        Long waitingStartTime = waitingStartTimePerEndpoint.get(object);
    
        if (state == CircuitBreakerState.OPEN && waitingStartTime != null) {
            long currentTime = System.nanoTime();
            if (currentTime - waitingStartTime < WAITING_TIME) {
                return ResponseEntity.status(500).build();
            } else {
                state = CircuitBreakerState.HALF_OPEN;
            }
        }
    
        try {
            R result = operation.apply(object);
            unsuccessfulAttempts.set(0);
            circuitBreakerStatePerEndpoint.put(object, CircuitBreakerState.CLOSED);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            if (state == CircuitBreakerState.HALF_OPEN) {
                circuitBreakerStatePerEndpoint.put(object, CircuitBreakerState.OPEN);
                waitingStartTimePerEndpoint.put(object, System.nanoTime());
            } else if (state == CircuitBreakerState.CLOSED) {
                int currentAttempts = unsuccessfulAttempts.incrementAndGet();
                if (currentAttempts >= MAX_NUMBER_OF_ATTEMPTS_WITH_UNSUCCESSFUL_REPLY) {
                    circuitBreakerStatePerEndpoint.put(object, CircuitBreakerState.OPEN);
                    waitingStartTimePerEndpoint.put(object, System.nanoTime());
                }
            }
            throw e;
        }
    }
}
