package com.cloud.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.cloud.gateway.config.AppConstants.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	public static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.cors().and().csrf().disable().authorizeRequests().anyRequest().authenticated().and()
				.addFilter(new JwtAuthenticationFilter()).addFilter(new JwtAuthorizationFilter(authenticationManager()))
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(SWAGGER_API_DOCS);
		web.ignoring().antMatchers(SWAGGER_JSON);
		web.ignoring().antMatchers(SWAGGER_UI_HTML);
		web.ignoring().antMatchers(SWAGGER_WEBJARS);
		web.ignoring().antMatchers(SWAGGER_RESOURCES);
		web.ignoring().antMatchers(HttpMethod.POST,"/v1/user/register");
		web.ignoring().antMatchers(HttpMethod.POST,"/v1/user/login");
		web.ignoring().antMatchers(HttpMethod.GET,"/v1/user");
	}
}
