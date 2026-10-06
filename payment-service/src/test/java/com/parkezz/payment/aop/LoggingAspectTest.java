package com.parkezz.payment.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LoggingAspectTest {

    @Test
    void logServiceExecution_successfulMethod_returnsResult() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(signature.toShortString()).thenReturn("PaymentService.pay(..)");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(
                new Object[]{"test", Integer.valueOf(10)}
        );
        when(joinPoint.proceed()).thenReturn("SUCCESS");

        Object result = aspect.logServiceExecution(joinPoint);

        assertThat(result).isEqualTo("SUCCESS");

        verify(joinPoint).getSignature();
        verify(joinPoint).getArgs();
        verify(joinPoint).proceed();
    }

    @Test
    void logServiceExecution_noArguments_returnsResult() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(signature.toShortString()).thenReturn("PaymentService.test()");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.logServiceExecution(joinPoint);

        assertThat(result).isEqualTo("OK");
        verify(joinPoint).proceed();
    }

    @Test
    void logServiceExecution_nullArguments_returnsResult() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(signature.toShortString()).thenReturn("PaymentService.test()");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.logServiceExecution(joinPoint);

        assertThat(result).isEqualTo("OK");
        verify(joinPoint).proceed();
    }

    @Test
    void logServiceExecution_nullArgument_returnsResult() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(signature.toShortString()).thenReturn("PaymentService.test(..)");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(
                new Object[]{null, "value"}
        );
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.logServiceExecution(joinPoint);

        assertThat(result).isEqualTo("OK");
        verify(joinPoint).proceed();
    }

    @Test
    void logServiceExecution_exception_rethrowsException() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        RuntimeException exception =
                new RuntimeException("Payment failed");

        when(signature.toShortString()).thenReturn("PaymentService.pay(..)");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"request"});
        when(joinPoint.proceed()).thenThrow(exception);

        assertThatThrownBy(() -> aspect.logServiceExecution(joinPoint))
                .isSameAs(exception);

        verify(joinPoint).proceed();
    }
}