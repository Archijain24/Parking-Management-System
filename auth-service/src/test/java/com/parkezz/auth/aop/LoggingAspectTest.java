package com.parkezz.auth.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoggingAspectTest {

    private LoggingAspect loggingAspect;
    private ProceedingJoinPoint joinPoint;
    private Signature signature;

    @BeforeEach
    void setUp() {
        loggingAspect = new LoggingAspect();

        joinPoint = mock(ProceedingJoinPoint.class);
        signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("AuthServiceImpl.login(..)");
    }

    @Test
    void logServiceExecution_successfulMethod_returnsResult() throws Throwable {
        when(joinPoint.getArgs())
                .thenReturn(new Object[] { "test-value", Integer.valueOf(1) });

        when(joinPoint.proceed()).thenReturn("success");

        Object result = loggingAspect.logServiceExecution(joinPoint);

        assertThat(result).isEqualTo("success");
    }

    @Test
    void logServiceExecution_noArguments_returnsResult() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[0]);

        when(joinPoint.proceed()).thenReturn("success");

        Object result = loggingAspect.logServiceExecution(joinPoint);

        assertThat(result).isEqualTo("success");
    }

    @Test
    void logServiceExecution_nullArguments_returnsResult() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(null);

        when(joinPoint.proceed()).thenReturn("success");

        Object result = loggingAspect.logServiceExecution(joinPoint);

        assertThat(result).isEqualTo("success");
    }

    @Test
    void logServiceExecution_withNullArgument_returnsResult() throws Throwable {
        when(joinPoint.getArgs())
                .thenReturn(new Object[] { null, "test-value" });

        when(joinPoint.proceed()).thenReturn("success");

        Object result = loggingAspect.logServiceExecution(joinPoint);

        assertThat(result).isEqualTo("success");
    }

    @Test
    void logServiceExecution_exception_rethrowsException() throws Throwable {
        RuntimeException exception =
                new RuntimeException("Test exception");

        when(joinPoint.getArgs())
                .thenReturn(new Object[] { "test-value" });

        when(joinPoint.proceed()).thenThrow(exception);

        assertThatThrownBy(() ->
                loggingAspect.logServiceExecution(joinPoint))
                .isSameAs(exception);
    }
}