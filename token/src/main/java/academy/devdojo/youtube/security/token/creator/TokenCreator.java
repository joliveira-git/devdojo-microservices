package academy.devdojo.youtube.security.token.creator;

import academy.devdojo.youtube.core.model.ApplicationUser;
import academy.devdojo.youtube.core.property.JwtConfiguration;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TokenCreator {

    private final JwtConfiguration jwtConfiguration; //É necessário declarar como final para poder acessar as configurações, pois trata-se de um singleton e precisa ser declarado como variável de instância.

    // TODO: 3 - Assina o token
    @SneakyThrows
    public SignedJWT createSignedJWT(Authentication auth){
        log.info("Starting to create the signed JWT");
        ApplicationUser applicationUser = (ApplicationUser) auth.getPrincipal();

        // Gera as claims
        JWTClaimsSet jwtClaimSet = createJWTClaimSet(auth, applicationUser);

        // Gera o par de chaves (pública e privada)
        KeyPair rsaKeys = generateKeyPair();

        log.info("Building JWK from the RSA Keys");

        // Gera JSON Web Key a partir da chave pública
        JWK jwk = new RSAKey
                .Builder((RSAPublicKey) rsaKeys.getPublic())
                .keyID(UUID.randomUUID().toString())
                .build();

        // Gera o Token para ser assinado, passando a chave pública no header e informa o algoritmo, o tipo do objeto e o claimset.
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader
                        .Builder(JWSAlgorithm.RS256).jwk(jwk).type(JOSEObjectType.JWT)
                        .build(),jwtClaimSet);

        // Assina o token usado a chave privada
        RSASSASigner signer = new RSASSASigner(rsaKeys.getPrivate());
        signedJWT.sign(signer);

        // Retorna o token assinado ( ainda falta fazer a criptografia)

        log.info("Serialized token '{}'", signedJWT.serialize());
        return signedJWT;
    }

    // Monta o ClaimSet do Token
    private JWTClaimsSet createJWTClaimSet(Authentication auth, ApplicationUser applicationUser){
        log.info("Creating the JWTClaimSet Object for '{}'", applicationUser);
        return new JWTClaimsSet.Builder()
                .subject(applicationUser.getUsername())
                .claim("authorities", auth.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .claim("userId", applicationUser.getId())
                .issuer("http://academy.devdojo")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + (jwtConfiguration.getExpiration()*1000)))
                .build();
    }

    // Monta as chaves do Token
    @SneakyThrows
    private KeyPair generateKeyPair(){
        log.info("Generating RSA 2048 bits keys");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        return generator.genKeyPair();
    }

    // TODO: 4 - Criptografar o token
    public String encryptToken(SignedJWT signedJWT) throws JOSEException {
        log.info("Starting the encryptToken method");

        //Vamos usar criptografia direta usando uma chave privada que está armazenada nas propriedades (JwtConfiguration)
        DirectEncrypter directEncrypter = new DirectEncrypter(jwtConfiguration.getPrivateKey().getBytes());

        JWEObject jweObject = new JWEObject(new JWEHeader
                .Builder(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256)
                .contentType("JWT")
                .build(), new Payload(signedJWT));
        log.info("Encrypting token with system's private key");

        jweObject.encrypt(directEncrypter);

        log.info("Token encrypted");

        return jweObject.serialize(); //Retorna o token criptografado e serializado (no formato de string)
    }


}
