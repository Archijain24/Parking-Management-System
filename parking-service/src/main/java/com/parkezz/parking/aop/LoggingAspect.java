package com.parkezz.parking.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Cross-cutting method logging for the service layer.
 *
 * Logs entry (argument types only), successful exit (with elapsed time) and
 * any exception raised, without touching the business classes themselves.
 * Only argument class names are logged (see safeArguments()) so that request
 * DTOs carrying a password or hashed password never reach the log file.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.parkezz.parking.service..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        log.info("Entering {} with arguments={}", signature, safeArguments(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = System.currentTimeMillis() - startTime;
            log.info("Exiting {} successfully in {}ms", signature, elapsedMs);
            return result;
        } catch (Exception ex) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            log.error("Exception in {} after {}ms: {}", signature, elapsedMs, ex.getMessage());
            throw ex;
        }
    }

    private String safeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            builder.append(arg == null ? "null" : arg.getClass().getSimpleName());
            if (i < args.length - 1) {
                builder.append(", ");
            }
        }
        return builder.append("]").toString();
    }
}
