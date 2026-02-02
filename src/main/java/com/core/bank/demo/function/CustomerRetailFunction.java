package com.core.bank.demo.function;

import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.core.bank.demo.config.exception.BusinessException;
import com.core.bank.demo.config.exception.ErrorCode;
import com.core.bank.demo.contract.Auditable;
import com.core.bank.demo.contract.Function;
import com.core.bank.demo.contract.Operation;
import com.core.bank.demo.contract.Request;
import com.core.bank.demo.contract.Response;
import com.core.bank.demo.dto.CustomerCreateRetailDto;
import com.core.bank.demo.dto.CustomerUpdateRetailDto;
import com.core.bank.demo.entity.CustomerRetail;
import com.core.bank.demo.enums.CustomerStatus;
import com.core.bank.demo.repository.CustomerRetailRepository;
import com.core.bank.demo.util.DataMapperUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Function("CUSTOMER_RETAIL")
@RequiredArgsConstructor
public class CustomerRetailFunction {

    private final CustomerRetailRepository customerRetailRepository;

    @Operation("CREATE")
    @Transactional
    @Auditable(action = "CREATE", objectType = "CUSTOMER_RETAIL")
    @PreAuthorize("hasAuthority('TELLER')")
    public Response create(Request req) {
        log.info("Creating retail customer");

        CustomerCreateRetailDto dto = DataMapperUtil.toObjectWithValidation(req.getData(),
                CustomerCreateRetailDto.class);

        CustomerRetail retail = DataMapperUtil.toObject(dto, CustomerRetail.class);
        retail.setStatus(CustomerStatus.CREATED.toString());

        customerRetailRepository.save(retail);
        log.info("Retail customer created with id: {}", retail.getId());

        return Response.ok(retail);
    }

    @Operation("UPDATE")
    @Transactional
    @Auditable(action = "UPDATE", objectType = "CUSTOMER_RETAIL", entityClass = CustomerRetail.class)
    @PreAuthorize("hasAuthority('TELLER')")
    public Response update(Request req) {
        String id = (String) req.getData().get("id");
        log.info("Updating retail customer: {}", id);

        CustomerRetail existing = customerRetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Customer not found: " + id));

        CustomerUpdateRetailDto dto = DataMapperUtil.toObjectWithValidation(req.getData(),
                CustomerUpdateRetailDto.class);
        BeanUtils.copyProperties(dto, existing);

        customerRetailRepository.save(existing);
        log.info("Retail customer updated: {}", id);

        return Response.ok(existing);
    }

    @Operation("DELETE")
    @Transactional
    @Auditable(action = "DELETE", objectType = "CUSTOMER_RETAIL", entityClass = CustomerRetail.class)
    @PreAuthorize("hasAuthority('ADMIN')")
    public Response delete(Request req) {
        String id = (String) req.getData().get("id");
        log.info("Deleting retail customer: {}", id);

        CustomerRetail existing = customerRetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Customer not found: " + id));

        customerRetailRepository.delete(existing);
        log.info("Retail customer deleted: {}", id);

        return Response.ok(existing);
    }

    @Operation("GET")
    @PreAuthorize("hasAuthority('TELLER')")
    public Response getById(Request req) {
        String id = (String) req.getData().get("id");
        log.info("Getting retail customer: {}", id);

        CustomerRetail customer = customerRetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Customer not found: " + id));

        return Response.ok(customer);
    }
}
