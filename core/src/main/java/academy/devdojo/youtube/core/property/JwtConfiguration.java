package academy.devdojo.youtube.core.property;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt.config") // Todas as propriedades listadas aqui vão para o arquivo application.yml com o prefixo jwt.config
@Getter
@Setter
@ToString
public class JwtConfiguration {
    private String loginUrl = "/login/**"; //Vamos usar essa URL na hora de fazer a segurança com IntentMatchers
    @NestedConfigurationProperty
    private Header header = new Header();
    private int expiration = 3600; //expiration do token
    private String privateKey = "f39bYJ4ch5lHo09iChokKi4DbJVicjnY"; //chave para criptografar (64bytes). Generate random string. http://www.unit-conversion.info/texttools/random-string-generator/
    private String type = "encrypted";
    @Getter
    @Setter
    public static class Header{
        private String name = "Authorization"; // nome header que vai conter o token.
        private String prefix = "Bearer "; // o prefixo do token (Bearer ou portador, é um schemma do HTTP Authentication)
    }
}
