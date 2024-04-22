package com.hospital.demo.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class RequestHeaderInterceptor implements HandlerInterceptor {
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
	
		// You can validate few things before the request goes to the controller 
		// For example, check for Authorization header
		
//		if(StringUtils.isEmpty(request.getHeader("Authorization"))) {
//			// throw exception    
//		}
		
		System.out.println("preHandle() method invoked");
		
		System.out.println("---------------- Request Start ---------------");
		System.out.println("Request URL: " + request.getRequestURI());
		System.out.println("Method Type: " + request.getMethod());
//		System.out.println("Local Address: "+request.getLocalAddr());
//		System.out.println("Local Port: "+request.getLocalPort());
		
		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		System.out.println("postHandle() method invoked");
		// You can perform post-processing here if needed
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
		System.out.println("afterCompletion() method invoked");
		
		System.out.println("Request URL: " + request.getRequestURI());
		System.out.println("Method Type: " + request.getMethod());
		System.out.println("Status: " + response.getStatus());
		System.out.println("---------------- Request End ---------------");
		
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
}
