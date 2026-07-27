package by.shakhau.ps.auth.util;

import by.shakhau.ps.auth.model.Password;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordUtil {

    public static void clearPassword(Password request) {
        Arrays.fill(request.getPassword(), '0');
        request.setPassword(null);
    }

    public static void clearPassword(StringBuilder password) {
        for (int i = 0; i < password.length(); i++) {
            password.setCharAt(i, '0');
        }
        password.delete(0, password.length());
    }

    public static void clearPassword(Password request, StringBuilder password) {
        clearPassword(request);
        clearPassword(password);
    }
}
