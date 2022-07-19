package com.zhouyue.waimai.filter;

import com.alibaba.fastjson.JSON;
import com.zhouyue.waimai.pojo.R;
import com.zhouyue.waimai.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经登录
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        // 1. 获取本次请求的 url
        String requestURI = httpServletRequest.getRequestURI();
        log.info("本次请求路径: " + requestURI);
        // 2. 设置不需要过滤的请求
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/static/backend/**",
                "/static/front/**",
                "/user/sendMsg",
                "/user/login",
                "/dish/list"
        };
        // 2. 判断本次请求是否要处理
        if (check(urls, requestURI)){
            // 3. 如果不需要处理，直接放行
            log.info("本次请求不需要处理");
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        // 4-1. 判断登录状态
        if (httpServletRequest.getSession().getAttribute("empId") != null){
            log.info("用户已登录");
            Long id = (Long) httpServletRequest.getSession().getAttribute("empId");
            BaseContext.setCurrentId(id);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        // 4-2. 判断移动端用户登录状态
        if (httpServletRequest.getSession().getAttribute("user") != null){
            log.info("用户已登录");
            Long id = (Long) httpServletRequest.getSession().getAttribute("user");
            BaseContext.setCurrentId(id);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        // 5. 如果未登录通过输出流方式向客户端页面响应，前端有拦截器判断
        httpServletResponse.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 判断请求的 url 是否需要处理
     * @param requestUri
     * @return
     */
    private boolean check(String[] urls, String requestUri){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestUri);
            if (match){
                return true;
            }
        }
        return false;
    }
}
