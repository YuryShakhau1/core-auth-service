package by.shakhau.ps.auth.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddUserRolesRequest {

    @NotNull
    private List<String> roleNames;
}
