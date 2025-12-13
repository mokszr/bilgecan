package net.bilgecan.config;

import java.util.Set;
import java.util.regex.Pattern;

public interface ConstantConfigs {

    int MIN_USERNAME_LENGTH = 4;
    int MAX_USERNAME_LENGTH = 16;

    int MIN_PASSWORD_LENGTH = 6;
    int MAX_PASSWORD_LENGTH = 12;

    String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{6,12}$";

    Pattern PASSWORD_POLICY = Pattern.compile(PASSWORD_REGEX);

    String USERNAME_REGEX = "^(?=.{4,16}$)(?![_.-])(?!.*[_.-]{2})(?=.*[a-zA-Z])[a-zA-Z0-9._-]+(?<![_.-])$";

    Pattern USERNAME_POLICY = Pattern.compile(USERNAME_REGEX);

    String EMAIL_REGEX = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$";

    Pattern EMAIL_POLICY = Pattern.compile(EMAIL_REGEX);

}
