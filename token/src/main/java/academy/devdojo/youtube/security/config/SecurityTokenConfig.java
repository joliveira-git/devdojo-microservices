package academy.devdojo.youtube.security.config;

import academy.devdojo.youtube.core.property.JwtConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class SecurityTokenConfig extends WebSecurityConfigurerAdapter {
    protected final JwtConfiguration jwtConfiguration;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            //CSRF: falsificação de solicitação entre sites.  Diferente do cross-site scripting (XSS), que explora a confiança que um usuário tem para um site específico, o CSRF explora a confiança que um site tem no navegador de um usuário.
            .csrf().disable()
            //cors: Cross-origin resource sharing é uma especificação de uma tecnologia de navegadores que define meios para um servidor permitir que seus recursos sejam acessados por uma página web de um domínio diferente. Esse tipo de acesso seria de outra forma negado pela same origin policy
            .cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()) //Configura o CORS com valores básicos.
            .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //Cria sessão sem estado.
            .and()
                .exceptionHandling().authenticationEntryPoint( (req, resp, e) -> resp.sendError(HttpServletResponse.SC_UNAUTHORIZED))//Tratamento de exceção.
            .and()
            .authorizeRequests()
                .antMatchers(jwtConfiguration.getLoginUrl(), "/**/swagger-ui.html").permitAll() // permite que URL de login seja acessada.
                .antMatchers(HttpMethod.GET, "/**/swagger-resources/**","/**/webjars/springfox-swagger-ui/**","/**/v2/api-docs/**").permitAll() // permite que URL do swagger seja acessada.
                .antMatchers("/course/v1/admin/**").hasRole("ADMIN") // permite acessar url aos usuários que possuem direito de "ADMIN".
                .antMatchers("/auth/user/**").hasAnyRole("ADMIN","USER") // permite acessar url aos usuários que possuem direito de "ADMIN".
            .anyRequest().authenticated(); // Qualquer outra seção precisa estar autenticada.
    }
}
