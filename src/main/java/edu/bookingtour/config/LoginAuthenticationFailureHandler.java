package edu.bookingtour.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String DEFAULT_MSG = "Tên đăng nhập, email hoặc mật khẩu không đúng!";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String message = DEFAULT_MSG;
        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            message = exception.getMessage();
        }
        request.getSession().setAttribute("error.message", message);
        response.sendRedirect(request.getContextPath() + "/login?error=true");
    }
}
