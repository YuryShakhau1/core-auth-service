package by.shakhau.ps.auth.util;

import by.shakhau.ps.auth.model.Password;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordUtil {

    public static boolean compare(StringBuilder password1, StringBuilder password2) {
        return Arrays.equals(toCharArray(password1), toCharArray(password2));
    }

    public static void clearPassword(StringBuilder password) {
        for (int i = 0; i < password.length(); i++) {
            password.setCharAt(i, '\0');
        }

        password.delete(0, password.length());
    }

    public static void clearPassword(Password password) {
        clearPassword(password.getPassword());
        password.setPassword(null);
    }

    public static void clearPassword(Password request, StringBuilder password) {
        clearPassword(request);
        clearPassword(password);
    }

    private static char[] toCharArray(StringBuilder password) {
        char[] pass = new char[password.length()];
        password.getChars(0, password.length(), pass, 0);
        return pass;
    }
}
