package Authorisation;


import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

public class PasswordManager {
    public static SCryptPasswordEncoder encoder = new SCryptPasswordEncoder(16, 8, 1, 16, 32);
    public static String hashPassword(String password) {
        return encoder.encode(password);
    }

    public static boolean checkPassword(String password, String encodedPassword){
        return encoder.matches(password, encodedPassword);
    }

    //scrypt
}
