package com.re.badmintonsystem.aspect;

import com.re.badmintonsystem.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    private final AuditLogService auditLogService;

    public LoggingAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Pointcut("@annotation(com.re.badmintonsystem.annotation.LogExecutionTime)")
    public void logExecutionTimeAnnotation() {
    }

    @Pointcut("execution(* com.re.badmintonsystem.service..*.*(..))")
    public void allServiceMethods() {
    }

    @Around("logExecutionTimeAnnotation() || allServiceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info(">> Entering: {}.{}() with args: {}", className, methodName, sanitizeArgs(args));

        Object result;
        String status;
        String detail;

        try {
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            status = "SUCCESS";
            detail = String.format("Method: %s.%s, Args: %s, Time: %dms",
                    className, methodName, sanitizeArgs(args), executionTime);
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - start;
            status = "FAILURE";
            detail = String.format("Method: %s.%s, Args: %s, Error: %s, Time: %dms",
                    className, methodName, sanitizeArgs(args),
                    throwable.getMessage(), executionTime);
            throw throwable;
        }

        long executionTime = System.currentTimeMillis() - start;
        log.info("<< Exiting: {}.{}() - {} ({}ms)", className, methodName, status, executionTime);

        try {
            auditLogService.log(className, null, methodName, null, null, detail);
        } catch (Exception e) {
            log.warn("Failed to save audit log: {}", e.getMessage());
        }

        return result;
    }

    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        String raw = Arrays.toString(args);
        // Truncate long strings / password fields for security
        if (raw.length() > 200) {
            raw = raw.substring(0, 200) + "...]";
        }
        return raw;
    }
}
