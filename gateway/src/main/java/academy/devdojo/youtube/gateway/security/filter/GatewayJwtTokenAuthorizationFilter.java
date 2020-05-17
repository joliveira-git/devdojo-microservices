package academy.devdojo.youtube.gateway.security.filter;

import academy.devdojo.youtube.core.property.JwtConfiguration;
import academy.devdojo.youtube.security.filter.JwtTokenAuthorizationFilter;
import academy.devdojo.youtube.security.token.converter.TokenConverter;
import com.netflix.zuul.context.RequestContext;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static academy.devdojo.youtube.security.util.SecurityContextUtil.setSecurityContext;

public class GatewayJwtTokenAuthorizationFilter extends JwtTokenAuthorizationFilter {
    public GatewayJwtTokenAuthorizationFilter(JwtConfiguration jwtConfiguration, TokenConverter tokenConverter) {
        super(jwtConfiguration, tokenConverter);
    }

    @Override
    @SneakyThrows
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

        //Todas as requisições que o gateway recebe são do front-end ou externas e o token sempre virá criptografado...
        //descriptografa ...
        String signedToken = tokenConverter.decryptToken(token);
        //valida a assinatura ...
        tokenConverter.validateTokenSignature(signedToken);

        //Precisamos do securityContext para validar os Roles
        setSecurityContext(SignedJWT.parse(signedToken));

        if(jwtConfiguration.getType().equalsIgnoreCase("signed")) {
            // Se a propriedade tipo da configuração for signed (assinado), sobrescreve o header Authorization
            // Substitui o token que está criptografado por apenas um token assinado.
            RequestContext.getCurrentContext().addZuulRequestHeader("Authorization", jwtConfiguration.getHeader().getPrefix() + signedToken);
        }

        chain.doFilter(request, response);
    }
}
