package com.xetus.freeipa.pwdportal;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.autoconfigure.EndpointCorsProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

import groovy.util.logging.Slf4j

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
  private EndpointCorsProperties corsProperties;
  
  @Autowired
  public SecurityConfig(EndpointCorsProperties corsProperties) {
    super();
    this.corsProperties = corsProperties
  }
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    
    /*
     * Ensure CORS is enabled. Note that this requires configuring the
     * {@link EndpointCorsProperties}, which can be done via configuration
     * files.
     */
    http
      .cors();
      
    /*
     * At this point, none of the endpoints actually require 
     * authentication, although we can protect the app with CORS and
     * CSRF
     */
    http
      .authorizeRequests().antMatchers("/api/**").permitAll()

    http.csrf().disable()
    /*
     * Enable CSRF protection since we're using API consumers. Note that the
     * cookie uses the default Spring configuration of being HTTP only since
     * the CSRF token value is delivered to the consumer on successful
     * authentication.
     *
     * While alone this would be dangerous (since injected javascript could
     * theoretically access the CSRF token value from the authentication
     * endpoint), ensuring the cookie is HTTP Only will protect against CSRF
     * attacks. Note that this relies on proper XSS escaping of user input
     * on the client in order for this to be effective.
     *
     * See the following for reference:
     * https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#csrf-cookie
     * http://stackoverflow.com/a/20518324/2807658
     */
      // TODO: ok to leave disbled, since no authenticated sessions?
      //http.csrf().csrfTokenRepository(new CookieCsrfTokenRepository().withHttpOnlyFalse());
  }
  
  
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
    configuration.setAllowedMethods(["GET","POST"]);
    configuration.setAllowedHeaders(Arrays.asList("*", "*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}
