package by.shakhau.ps.auth.controller.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFirstAdminRequest extends CreateUserRequest {

    private String adminInitSecret;
}
