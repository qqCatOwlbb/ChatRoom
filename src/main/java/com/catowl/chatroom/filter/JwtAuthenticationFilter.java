package com.catowl.chatroom.filter;

import com.catowl.chatroom.exception.ExceptionEnum;
import com.catowl.chatroom.exception.UnauthorizedException;
import com.catowl.chatroom.model.entity.SecurityUser;
import com.catowl.chatroom.model.entity.User;
import com.catowl.chatroom.utils.JwtUtil;
import com.catowl.chatroom.utils.RedisCache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if(!StringUtils.hasText(token)|| !token.startsWith("Bearer ")){
            //token为空，应该让其跳过下面的token解析代码，交由security内的拦截器来拦截
            filterChain.doFilter(request,response);
            //拦截器拦截完返回时会再次进入该if，同样要避免执行下面的token解析代码
            return;
        }
        token = token.substring(7);
        Claims claims;
        try{
            claims = jwtUtil.getClaimsFromToken(token);
        }catch (UnauthorizedException e){
            throw new UnauthorizedException(ExceptionEnum.TOKEN_INVALID);
        }
        Long userId =Long.parseLong(claims.getSubject());
        User simpleUser = new User();
        simpleUser.setId(userId);
        List<String> roles = claims.get("roles", List.class);
        List<GrantedAuthority> authorities = new ArrayList<>();
        if(roles != null){
            authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        SecurityUser securityUser = new SecurityUser(simpleUser, authorities);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(securityUser, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request,response);
    }
}
