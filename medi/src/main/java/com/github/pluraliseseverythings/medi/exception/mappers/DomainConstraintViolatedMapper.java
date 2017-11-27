package com.github.pluraliseseverythings.medi.exception.mappers;

import com.github.pluraliseseverythings.medi.exception.DomainConstraintViolated;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class DomainConstraintViolatedMapper implements ExceptionMapper<DomainConstraintViolated> {
    @Override
    public Response toResponse(DomainConstraintViolated domainConstraintViolated) {
        return Response.status(Response.Status.BAD_REQUEST).entity(domainConstraintViolated.getMessage()).build();
    }
}
