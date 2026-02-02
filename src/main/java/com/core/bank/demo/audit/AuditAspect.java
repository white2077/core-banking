package com.core.bank.demo.audit;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.EntityManager;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.core.bank.demo.config.filter.header.RequestContextHolder;
import com.core.bank.demo.contract.Auditable;
import com.core.bank.demo.contract.Request;
import com.core.bank.demo.contract.Response;
import com.core.bank.demo.entity.AuditRecord;
import com.core.bank.demo.repository.AuditRepository;
import com.core.bank.demo.util.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditRepository repo;
    private final EntityManager entityManager;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        log.debug("Auditing: {}.{}", auditable.objectType(), auditable.action());

        Request request = extractRequest(pjp.getArgs());
        Map<String, Object> requestData = request != null ? request.getData() : null;
        String objectId = extractObjectId(requestData, auditable.idField());

        String oldValue = null;
        if (objectId != null && auditable.entityClass() != Void.class) {
            oldValue = fetchOldValue(auditable.entityClass(), objectId);
        }

        Object result = pjp.proceed();

        String newValue = null;
        if (result instanceof Response response && response.getData() != null) {
            newValue = JsonUtils.toJsonMasked(response.getData());
            if (objectId == null) {
                objectId = extractObjectIdFromResult(response.getData());
            }
        }

        AuditRecord record = new AuditRecord();
        record.setAction(auditable.action());
        record.setObjectType(auditable.objectType());
        record.setObjectId(objectId);
        record.setOldValue(oldValue);
        record.setNewValue(newValue);
        record.setActor(getCurrentActor());
        record.setRequestId(getRequestId());
        record.setCreatedAt(LocalDateTime.now());

        repo.save(record);
        log.debug("Audit saved: {}.{} id={}", auditable.objectType(), auditable.action(), objectId);

        return result;
    }

    private Request extractRequest(Object[] args) {
        if (args == null)
            return null;
        for (Object arg : args) {
            if (arg instanceof Request req) {
                return req;
            }
        }
        return null;
    }

    private String extractObjectId(Map<String, Object> data, String idField) {
        if (data == null)
            return null;
        Object id = data.get(idField);
        return id != null ? id.toString() : null;
    }

    private String extractObjectIdFromResult(Object data) {
        if (data == null)
            return null;
        try {
            var method = data.getClass().getMethod("getId");
            Object id = method.invoke(data);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            log.trace("Could not extract ID from result: {}", e.getMessage());
            return null;
        }
    }

    private String fetchOldValue(Class<?> entityClass, String objectId) {
        try {
            Object entity = entityManager.find(entityClass, objectId);
            if (entity != null) {
                entityManager.detach(entity);
                return JsonUtils.toJsonMasked(entity);
            }
        } catch (Exception e) {
            log.warn("Could not fetch old value for {} id={}: {}", entityClass.getSimpleName(), objectId,
                    e.getMessage());
        }
        return null;
    }

    private String getCurrentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    private String getRequestId() {
        var ctx = RequestContextHolder.get();
        return ctx != null ? ctx.getClientMessageId() : null;
    }
}
