package academy.devdojo.youtube.security.filter;

import academy.devdojo.youtube.core.property.JwtConfiguration;
import academy.devdojo.youtube.security.token.converter.TokenConverter;
import academy.devdojo.youtube.security.util.SecurityContextUtil;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JwtTokenAuthorizationFilter extends OncePerRequestFilter { //Usamos OncePerRequestFilter para garantir que os métodos dessa classe serão executados apenas uma vez por requisição.

    protected final JwtConfiguration jwtConfiguration;
    protected final TokenConverter tokenConverter;

    @Override
    @SuppressWarnings("Duplicates")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(jwtConfiguration.getHeader().getName());

        //O filtro pode estar indo para requisições que não precisam ser autenticadas, por exemplo: login
        if(header == null || !header.startsWith(jwtConfiguration.getHeader().getPrefix())){
            chain.doFilter(request, response); // indica ao container que deve seguir o processamento
            return;
        }

        //Remove o prefixo
        String token = header.replace(jwtConfiguration.getHeader().getPrefix(), "").trim();

        SecurityContextUtil.setSecurityContext(StringUtils.equalsIgnoreCase("signed", jwtConfiguration.getType())?validate(token):decryptValidating(token));

        chain.doFilter(request, response);
    }

    @SneakyThrows
    // Para tokens criptografados
    private SignedJWT decryptValidating(String encryptedToken){
        //Descriptografa o token
        String signedToken = tokenConverter.decryptToken(encryptedToken);
        // Valida e gera um exceção quando for inválido.
        tokenConverter.validateTokenSignature(signedToken);
        return SignedJWT.parse(signedToken);
    }

    // Para tokens apenas assinados
    @SneakyThrows
    private SignedJWT validate(String signedToken){
        tokenConverter.validateTokenSignature(signedToken);
        return SignedJWT.parse(signedToken);
    }
}
