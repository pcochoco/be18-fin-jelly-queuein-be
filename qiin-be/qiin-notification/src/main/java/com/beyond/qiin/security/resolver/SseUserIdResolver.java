package com.beyond.qiin.security.resolver;

import com.beyond.qiin.security.exceptions.AuthException;
import com.beyond.qiin.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class SseUserIdResolver implements HandlerMethodArgumentResolver { //controller parameter 해석하는 resolver class

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter){ //해당 resolver이 특정 parameter 처리 가능한지에 대한 판단
        return methodParameter.hasParameterAnnotation(SseUserId.class) //@SseUserId
                && methodParameter.getParameterType().equals(Long.class); //Long userId
    }

    @Override
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory){
        String token = nativeWebRequest.getParameter("accessToken");

        //token 유효하지 않은 경우 인증 실패
        if(token == null || token.isBlank()){
            throw AuthException.unauthorized();
        }


        return jwtTokenProvider.getUserId(token);

    }

}
