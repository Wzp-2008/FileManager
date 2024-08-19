package cn.wzpmc.filemanager.configurations;

import cn.wzpmc.filemanager.entities.User;
import cn.wzpmc.filemanager.utils.JwtUtils;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Configuration
@Component
public class AuthorizationConfiguration implements Filter {
    private final JwtUtils jwtUtils;
    @Autowired
    public AuthorizationConfiguration(JwtUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest request) {
            if (servletResponse instanceof HttpServletResponse response) {
                String header = request.getHeader("Authorization");
                if (header != null){
                    try {
                        this.jwtUtils.verifyToken(header);
                    } catch (TokenExpiredException e){
                        User decodeUser = this.jwtUtils.forceDecode(header);
                        if (decodeUser != null){
                            String token = this.jwtUtils.createToken(decodeUser);
                            CustomHttpServletRequestWrapper requestWrapper = new CustomHttpServletRequestWrapper(request);
                            requestWrapper.setAuthorization(token);
                            response.addHeader("Set-Authorization", token);
                            filterChain.doFilter(requestWrapper, response);
                            return;
                        }
                    } catch (JWTVerificationException ignored){}
                }
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private static final class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
        @Setter(AccessLevel.PRIVATE)
        private String authorization = null;

        private CustomHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (name.equals("Authorization")){
                return Collections.enumeration(Collections.singleton(authorization));
            }
            return super.getHeaders(name);
        }
    }
}
