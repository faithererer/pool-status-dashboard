package com.zjc.controller;

import com.zjc.common.Result;
import com.zjc.dto.LoginRequest;
import com.zjc.dto.LoginResponse;
import com.zjc.dto.TokenInfo;
import com.zjc.service.AuthService;
import com.zjc.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(401, e.getMessage());
        }
    }
    
    /**
     * 用户登出
     * 
     * @param request HTTP请求
     * @return 响应结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            
            if (token == null) {
                return Result.error(400, "缺少认证令牌");
            }
            
            authService.logout(token);
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            return Result.error(500, "登出失败");
        }
    }
    
    /**
     * 验证令牌
     *
     * @param request HTTP请求
     * @return 令牌信息
     */
    @GetMapping("/validate")
    public Result<TokenInfo> verify(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            
            if (token == null) {
                return Result.error(401, "缺少认证令牌");
            }
            
            TokenInfo tokenInfo = authService.verifyToken(token);
            return Result.success("令牌有效", tokenInfo);
        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage());
            return Result.error(401, e.getMessage());
        }
    }
    
}