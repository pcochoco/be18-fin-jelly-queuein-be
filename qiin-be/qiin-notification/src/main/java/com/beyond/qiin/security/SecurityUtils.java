package com.beyond.qiin.security;

import com.beyond.qiin.security.exceptions.AuthException;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        Object principal =
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails user) { //authentication 객체의 principal에 custom user details 저장
            return user.getUserId();
        }
        throw AuthException.unauthorized();
    }
}
