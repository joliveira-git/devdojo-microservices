package academy.devdojo.youtube.security.util;

import academy.devdojo.youtube.core.model.ApplicationUser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SecurityContextUtil {
    private SecurityContextUtil(){

    }
    //Gera um objeto ApplicationUser a partir das claims do token assinado e armazena no SecurityContext
    public static void setSecurityContext(SignedJWT signedJWT){
        try {
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            String username = claims.getSubject();
            if(username == null){
                throw new JOSEException("Username missing from JWT");
            }
            List<String> authorities = claims.getStringListClaim("authorities");
            ApplicationUser applicationUser = ApplicationUser
                    .builder()
                    .id(claims.getLongClaim("userId"))
                    .username(username)
                    .role(String.join(",",authorities))
                    .build();
            // Gera o token de autenticação
            UsernamePasswordAuthenticationToken auth =new UsernamePasswordAuthenticationToken(applicationUser, null, createAuthorities(authorities));
            auth.setDetails(signedJWT.serialize());
            // Adiciona os dados de autenticação no security context para que possam ser acessados pelos métodos dos services e controllers.
            SecurityContextHolder.getContext().setAuthentication(auth);
        }catch (Exception e){
            log.error("Error setting security context ", e);
            SecurityContextHolder.clearContext();
        }
    }

    private static List<SimpleGrantedAuthority> createAuthorities(List<String> authorities){
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
