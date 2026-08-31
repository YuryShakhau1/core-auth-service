package by.shakhau.ps.auth.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRoleNameResponse(List<String> roleNames) { }
