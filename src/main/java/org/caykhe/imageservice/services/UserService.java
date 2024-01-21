package org.caykhe.imageservice.services;

import lombok.RequiredArgsConstructor;
import org.caykhe.imageservice.dtos.ApiException;
import org.caykhe.imageservice.dtos.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.springframework.http.HttpStatus.*;


@Service
@RequiredArgsConstructor
public class UserService {
    private final RestTemplate restTemplate;
    
    @Value("${user.service.url}")
    private String userServiceUrl;
    
    public Optional<User> verifyToken(String token) {
        String url = userServiceUrl + "/auth/verify"; 
        
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", token);
            return execution.execute(request, body);
        });
        
        try {
            User user = restTemplate.getForObject(url, User.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException.NotFound e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode.equals(UNAUTHORIZED)) { // 401
                throw new ApiException("Có lỗi trong quá trình xác thực. Vui lòng thử lại sau!", UNAUTHORIZED);
            } else if (statusCode.equals(FORBIDDEN)) { // 403
                throw new ApiException("Yêu cầu không được phép", FORBIDDEN);
            } else if (statusCode.equals(NOT_ACCEPTABLE)) { //406
                throw new ApiException("Có lỗi xảy ra. Vui lòng đăng nhập lại!", NOT_ACCEPTABLE);
            } else if (statusCode.equals(PRECONDITION_FAILED)) { // 412
                throw new ApiException("Phiên truy cập đã hết hạn. Vui lòng đăng nhập lại!", PRECONDITION_FAILED);
            } else {
                throw new ApiException("Có lỗi xảy ra. Vui lòng thử lại sau!", INTERNAL_SERVER_ERROR);
            }
        }
    }

    public Optional<User> getByUsername(String username) {
        String url = userServiceUrl + "/users/" + username;
        try {
            User user = restTemplate.getForObject(url, User.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
