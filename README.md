# Spring Boot Microservices
Willian Suane 
DevDojo Academy
Durante o curso foi implementado o back-end de uma aplicação utilizando a arquitetura microservices. Em linhas gerais, essa arquitetura consiste em gerar um coleção de pequenos serviços autônomos, onde cada um é implementado de forma independente uma única funcionalidade e se comunica de forma simples, geralmente por meio de uma API REST utilizando o protocolo HTTP.
Na arquitetura microservices típica existem serviços com diversas atribuições e além disso há regras estabelecidas para a comunicação. Em resumo, podemos dizer que os serviços não devem ser acessados de forma direta. Todas as requisições devem passar pelo router/gateway que se comunica com o service discovery que é o responsável pela identificação do serviço que está sendo solicitado. A aplicação cliente deve fazer a primeira requisição ao serviço de autenticação (auth).
O serviço de autenticação retorna um JWE Token que ficará guardado do lado do cliente para ser usado nas próximas requisições. A partir, o token será usado a cada nova requisição de serviço e o cliente deverá encaminhá-lo ao router/gateway.
O router/gateway, por sua vez, irá verifica se o token é válido e em caso positivo, o encaminha ao serviço (assinado (JWS) ou criptografado (JWE)).
Esse mecanismo é importante porque além de trazer segurança ao processo, também nos dá a possibilidade de escalar a aplicação de acordo com a necessidade.

Durante o curso foram implementados os seguintes módulos:
- core: módulo onde ficam as classes comuns às demais classes. Nesse módulo estão o model e o repository.
- course: microserviço. Nesse módulo fica api rest para acessar os recursos do microserviço (controller e o service)
- Serviço de Autenticação
- Microserviço
- Bibliotecas para criptografar e assinar o token
- auth
- discovery
- gateway
- token

Tecnologias Utilizadas:
- Zuul (Gateway da Netflix)
- Eureka Server (Service Discovery da Netflix)
- Spring Boot: starter do projeto
- Spring Framework: para prover a injeção de dependência
- Spring Data: Para facilitar o acesso e manipulação dos dados das entidades do banco
- Spring Web: Para implementar a api REST
- Spring Cloud: Para prover a orquestração de serviços: o Eurika Server (Registry, Config Server e Distributed Tracing)
- Spring Security: Para implementar o serviço de autorização e autenticação
- Nimbus-Jose: Token para Web Services REST
- Lombok: Biblioteca usada para reduzir o código boiler plate
- Maven: Gerenciamento de dependências
- MySql: Gerenciador de Banco de Dados SQL
- Docker: Container usado no projeto para prover o serviço de banco de dados
- Swagger Documentation: Para realizar a documentação dos serviços REST
- Postman: Cliente REST

Arquitetura
--------------
 Client Side
--------------
    |
    |REST
    |
---------------------------              --------------------------
 Router and Filter Gateway   <-Fetches->  Service Discovery Server
---------------------------              --------------------------  
único ponto de entrada para receber requisições REST


DMZ
---------------------------------------------    
----------------  ----------  ---------- ---------- ----------
 Authentication    service1    service1   service2   service3
    Service       ----------  ---------- ---------- ----------
----------------       |            |         |         |
       |               v            v         |         |
       v                --------------        v         v
      ( DB )                 (DB)            (DB)      (DB) 



# Service Discovery
Responsável pela identificação do serviço que será utilizado.

# Service Discovery Server
Service Discovery Eureka da NetFlix

Ao instanciar um serviço ele irá se registrar no Service Discovery Server. O Gateway também precisa se registrar no service discovery

GATEWAY(UP)
AUTH(UP)
SERVICE1(2)(UP)
SERVICE2(UP)
SERVICE3(UP)
....

# Segurança

1 - Client Side -Req-> Router and Filter Gateway -Req-> Auth Servie
A primeira requisição deve ser feita ao serviço de autenticação.

2 - Cliente Side <-Resp + JWE Token- Router and Filter Gateway <-Resp +JWE Token- Auth Service
O serviço de autenticação retorna um JWE Token (modificado base64 decode) que ficará guardado do lado do cliente para ser usado nas próximas requisições.

3 - Client Side -JWE-> Router and Filter Gateway -JWS or JWE-> Auth Servie
Toda vez que o cliente fizer uma requisição de serviço, irá mandar o token JWE ao Router.
O Router verifica se o token é válido e em caso positivo, o encaminha ao serviço assinado (JWS) ou criptografado (JWE). 
Quando criptografa, na verdade está assinando e criptografando.

JOSE ou Json Object Signing and Encryption: Especificação guarda-chuva que é formada pelas seguintes specs:
    JWT: Json Web Token, o token propriamente dito;
    JWE: Json Web Encryption, a criptografia para assinatura do token;
    JWA: Json Web Algorithms, sobre os algoritmos para assinatura do token;
    JWK: Json Web Keys, as chaves para assinatura;
    JWS: Json Web Signature, sobre a assinatura do token.

# Bora pro código

Ferramentas:
Intellij IDEA Ultimate

1 - Criar o projeto no Intellij
New Project / Spring Initializer
Dependencias do Projeto:
Core
- Spring Boot DevTools
- Lombok 
- Spring Configuration

Web
- Spring Web

SQL
- Spring Data JPA
- MySQL Driver
- H2 Database


# Criar o arquivo de configuração do banco de dados:
Vamos usar o Docker do MySql, por isso será necessário criar o arquivo stack.yml.
https://hub.docker.com/_/mysql

stack.yml
```
    # Use root/example as user/password credentials
    version: '3.1'

    services:

      db:
        image: mysql
        command: --default-authentication-plugin=mysql_native_password
        ports:
          - 3307:3306
        environment:
          MYSQL_USER: root
          MYSQL_ROOT_PASSWORD: root
        volumes:
        - microservices_devdojo:/var/lib/mysql

    volumes:
      microservices_devdojo:


```
Após a criação do arquivo de configuração rodar:

```
    sudo apt install docker-compose

    docker-compose -f stack.yml up
```
Nesse momento o compose vai fazer o download de tudo.
- Excluir a pasta static e templates na pasta src/main/java/resources
- Renomear o arquivo application.properties para application.yml
Nota: YAML significa YAML Is not Markup Language.


application.yml:
```
server:
  port: 8082

eureka:
  instance:
    prefer-ip-address: true
  client:
    service-url:
      defaultZone: http://localhost:8081/eureka/
    register-with-eureka: true


spring:
  application:
    name: course
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
  jmx:
    enabled: false
  datasource:
    url: jdbc:mysql://localhost:3307/devdojo?allowPublicKeyRetrieval=true&sslMode=DISABLED
    username: root
    password: root
jwt:
  config:
    type: signed

```
ERROR: In file './stack.yml', volume must be a mapping, not a string.
SOLVE:  Also for the "... not string" error, just append ":" after your volume name like below
volumes:
  db_data:

No IntelliJ, incluir um datasource na aba Database e criar o banco de dados da aplicação:
```
    create database devdojo;
```

# Setup Inicial do Projeto
# 1 - Controller:
Criar a classe CourseController
src/main/java/academy.devdojo.youtube.course.constroller

```
    @RestController
    @RequestMapping("v1/admin/course")
    @Slf4j
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public class CourseController {

        private final CourseService courseService;

        @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
        public ResponseEntity<Iterable<Course>> list(Pageable pageable){
            return new ResponseEntity<>(courseService.list(pageable), HttpStatus.OK);
        }

    }
```
Para que serve cada anotação:
    @RestController: Anotação spring que indica que a classe é um controller do tipo REST.
    @RequestMapping: Anotação Spring que serve para mapear a rota que será usada na requsição
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE): Indica o tipo da requisição, que no caso é GET e passa o tipo da resposta que será produzida (JSON)
    @Slf4j: Anotação Lombok que auxilia na geração de logs. O Simple Logging Facade para Java fornece uma API de log Java por meio de um padrão de fachada simples. 
            O backend de log subjacente é determinado no tempo de execução adicionando a ligação desejada ao caminho de classe e pode ser o pacote de log Sun Java padrão java.util.logging, log4j, logback ou tinylog
    @RequiredArgsConstructor(onConstructor = @__(@Autowired)): Anotação Lombok RequiredArgsConstructor indica que um construtor deve ser gerado com a anotação @Autowired

# 2 - Service
Criar a classe CourseService
src/main/java/academy.devdojo.youtube.course.service

```
    @Service
    @Slf4j
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public class CourseService {

        private final CourseRepository courseRepository;

        public Iterable<Course> list(Pageable pageable){
            log.info("Listin all courses");
            return courseRepository.findAll(pageable);
        }
    }
```
Para que serve cada anotação:
    @Service: Anotação Spring que indica um serviço.
    @Slf4j: Anotação do Lombok para gerar um logger field
    @RequiredArgsConstructor(onConstructor = @__(@Autowired)): Injeção do tipo constructor com Lombok
    
# 3 - Repository
Criar a interface CourseRepository
src/main/java/academy.devdojo.youtube.course.repository

```
    @Repository
    public interface CourseRepository extends PagingAndSortingRepository<Course, Long> {
        
    }
```
Estende a classe PagingAndSortingRepository do Spring para auxiliar na paginação dos dados.

# 4 - Model
Criar a interface AbstractEntity:

```
    package academy.devdojo.youtube.course.endpoint.model;

    import java.io.Serializable;

    public interface AbstractEntity extends Serializable {

        Long getId();

    }
```

Criar a classe Course:
```
    package academy.devdojo.youtube.course.endpoint.model;

    import lombok.*;

    import javax.persistence.*;
    import javax.validation.constraints.NotNull;

    @Entity
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Table(name = "Course")
    public class Course implements AbstractEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @EqualsAndHashCode.Include
        private Long id;

        @NotNull(message = "The field titile is mandatory")
        @Column(nullable = false)
        private String title;

        @Override
        public Long getId() {
            return id;
        }
    }
```
Para que serve cada anotação:
    @Entity: Anotação javax.persistence que indica uma entidade. Ou seja, indica que uma tabela será mapeada para esta classe (ORM - Mapeamento Objeto-Relacional)
    @Getter e @Setter: Anotações que informam ao Lombok que devem ser gerados métodos acessors getters e setters para cada atributo da classe.
    @Builder: Anotação Lombok que permite usar o pattern Builder sem precisar implementar o código. Como resultado podemos fazer algo do tipo: 
        ```
        Widget testWidget = Widget.builder()
          .name("foo")
          .id(1)
          .build();
         
        ```        
        
        Como informação extra, podemos usar @Builder(toBuilder = true) para retornar um construtor inicializado com as propriedades da instância de onde for chamado. Exemplo:
        ```            
            Widget testWidget = Widget.builder()
              .name("foo")
              .id(1)
              .build();
             
            Widget.WidgetBuilder widgetBuilder = testWidget.toBuilder();
        ```
    @NoArgsConstructor: Anotação Lombok que gera um construtor sem argumentos
    @AllArgsConstructor: Anotação Lombok que gera um construtor usando todos os campos da classe como argumentos
    @ToString: Anotação Lombok que gera o método toString
    @EqualsAndHashCode: Anotação Lombok que gera os métodos hasCode e equal. O parâmetro onlyExplicitlyInclude=true, indica que só vão entrar na composição dos métodos os campos anotados com @EqualsAndHashCode.Include
    @Table: Anotação java persistence que permite indicar o nome da tabela, quando este for diferente do nome da classe (não necessário no exemplo acima).


A aplicação não tem contexto, ou seja, o contexto é vazio.
Tomcat started on port(s): 8080 (http) with context path ''

DICA: Para quem estiver com problemas no log do CourseService.java, por conta do Intellij não reconhecer o log do lombok, aqui está a solução: https://stackoverflow.com/a/42809311/7238350.
No meu caso bastou instalar o plugin do lombok.

 
Modulizar o Projeto

Criar um pom.xml na raiz do projeto, copiar o pom.xml do módulo course e fazer os seguintes ajustes:
<packaging>pom</packaging>: indica que é um pom do tipo parent. Os módulos vão herdar desse pom
incluir dependência para o starter do spring boot validation e remover o starter
indicar quais módulos farão parte do projeto:
<modules>
        <module>course</module>
</modules>

No pom.xml do módulo course fazer os seguintes ajustes:
Remover todas as dependências. O parent não vai ser mais do spring boot e sim o pom.xml da raiz. Deve-se fornecer o relative path indicando a pasta anterior.
    <parent>
        <groupId>academy.devdojo.youtube</groupId>
        <artifactId>devdojo-microservices</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../</relativePath>
    </parent>

No terminal, rodar "mvn clean install" para baixar as dependências e transformar o projeto em um módulo maven.

mvn clean install -DskipTests

Criar um novo módulo: Btn dir no projeto / New / Module / Maven / Next
Em parent, indicar o módulo pai.
Criar o pacote academy.devdojo.youtube.core.model na pasta core / src / main / java.
* O nome do pacote é importante.
Migrar a interface e a classe ref. ao model para a nova estrutura.

Feito isso, precisa jogar o core como dependência de curso.
Editar o pom.xml do módulo course e incluir a dependência:
```
        <dependency>
            <groupId>academy.devdojo.youtube</groupId>
            <artifactId>core</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```
rodar mvn clean install
o que faz: 
- apaga todas as pastas target, os jars 
- adiciona as dependências locais dentro do repositório (core-1.0-SNAPSHOT.Jar, etc)

ERRO: Erro indicando que o Spring não está encontrando as dependências para serem instanciadas. 
 Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'courseController' defined in file [/home/joliveira/Documents/devdojo-microservices/course/target/classes/academy/devdojo/youtube/course/endpoint/controller/CourseController.class]: Unsatisfied dependency expressed through constructor parameter 0; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'courseService' defined in file [/home/joliveira/Documents/devdojo-microservices/course/target/classes/academy/devdojo/youtube/course/endpoint/service/CourseService.class]: Unsatisfied dependency expressed through constructor parameter 0; nested exception is org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'academy.devdojo.youtube.core.repository.CourseRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}

SOLUÇÃO:
Indicar na classe CourseApplication, onde o Spring deve scanear as dependências:
@EntityScan({"academy.devdojo.youtube.core.model"}) 
@ComponentScan("academy.devdojo.youtube")
@EnableJpaRepositories({"academy.devdojo.youtube.core.repository"})

Obs:
A anotação @ComponentScan é utilizada para criar automaticamente beans para cada classe anotada com @Component, @Service, @Controller, @RestController, @Repository, ... e os adiciona ao container do Spring (permitindo que eles sejam @Autowired).
A anotação @EntityScan, não cria beans. Ela identifica apenas quais classes devem ser usadas por um contexto de persistência específico.

Feito isso, rodar o comando mvn clean install novamente.

# Adicionar o Gateway (Zuul) e o Service Discovery (Eureka)

# Adicionar o Service Discovery

No projeto / Btn Dir / New / Model / Spring Initializr / 
Group: academy.devdojo.youtube
Artifact: discovery
Description: Service Discovery

Adicionar a dependência Spring Cloud Discovery / Eureka Server
O Intellij irá mostrar uma mensagem avisando que há múltiplos serviços disponíveis para serem rodados. Show run configurantions in Services

Em seguida, dentro do projeto discovery, trocar o nome do arquivo application.properties para application.yml.

Precisa dizer onde o discovery está hospedado (localhost) e dar um nome para a aplicação.

application.yml:
```
    spring:
      application:
        name: registry
    server:
      port: 8081
      
    eureka:
      client:
        register-with-eureka: false
        fetch-registry: false
        service-url: 
          defaultZone: http://localhost:${server.port}/eureka/
```
No Java, classe DiscoveryApplication, incluir a anotação @EnableEurekaServer do Spring Cloud

No pom.xml do projeto discovery, trocar o parent que está apontando para o projeto Spring Boot para o projeto .
- manter a dependência do Spring Cloud no projeto (pq é específico desse módulo)
- remover <dependencyManagement> do Spring Cloud e a propriedade <spring-cloud.version> para o pom.xml do projeto raiz.
- remover a dependência spring-boot-starter-test pois já existe no pom.xml do projeto raiz.
- incluir o repositório do Spring Milestones no pom.xml do projeto principal:
```
    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
        </repository>
    </repositories>
```
- Em seguinda, deve-se copiar o parent do pom.xml do módulo curso para o módulo discovery, sobrescrevendo o parent que aponta para o Spring Boot.

Adiconar dependências javax e jaxb no pom.xml do módulo discovery (se estiver usando java 9 ou superior pq foi removido do core):
- Java Architecture for XML Binding (JAXB) é uma biblioteca Java que possibilita a conversão de arquivo XML em objetos Java ou vice versa. Também fornece ferramentas para geração de arquivos XSD e de validação de XML. Outra característica do JAXB é a possibilidade de geração de classes Java através de um arquivo XSD.
- 

```
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>javax.xml.bind</groupId>
            <artifactId>jaxb-api</artifactId>
            <version>${jaxb-api.version}</version>
        </dependency>
        <dependency>
            <groupId>javax.activation</groupId>
            <artifactId>activation</artifactId>
            <version>${javax-activation.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jaxb</groupId>
            <artifactId>jaxb-runtime</artifactId>
            <version>${jaxb-runtime.version}</version>
        </dependency>
    </dependencies>

```
e incluir a versão nas propriedades:
```
    <properties>
        <java.version>1.8</java.version>
        <jaxb-api.version>2.3.1</jaxb-api.version>
        <jaxb-runtime.version>2.3.1</jaxb-runtime.version>
        <javax-activation.version>1.1.1</javax-activation.version>
    </properties>
```

Definir as configurações de build:
- o final name, que é o nome que será gerado para o arquivo .jar
- configuração dizendo que se trata de um projeto executável.
- adiconar plugin para compilação
```
    <build>
        <finalName>discovery</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <executable>true</executable>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
```
copiar as configurações de build para o projeto course


Spring Milestones:
Milestone (marco) é um termo de gerenciamento de projetos.
Para produzir um release final, o código passa por vários marcos à medida que os principais recursos são implementados.

Uma vez implementados todos os novos recursos, o código geralmente passa por vários estágios de pré-lançamento, como betas e candidatos a lançamento. Quando todo mundo está feliz, uma versão final é lançada e todo o processo começa novamente.

Na terra do Spring, esse processo segue:

- Mx: para uma versão Milestone, numerada sequencialmente
- RCx: para um Release Candidate, numerado sequencialmente
- GA: para o lançamento "Disponibilidade geral" - a versão final
Spring Milestone repo é um padrão do Maven rep

Em resumo: São recursos novos que estão sendo testados pela comunidade.

# Adicionar o Gateway
No projeto / Btn Dir / New / Model / Spring Initializr / 
Group: academy.devdojo.youtube
Artifact: gateway
Description: Gateway

Adicionar as dependências:
- Spring Cloud Discovery / Eureka Discovey Client
- Spring Cloud Routing / Zuul

Configurações do pom.xml
- Copiar o build do discovery
- Remove repositórios (se houver)
- Remove dependence management
- Remove spring-boot-starter-test
- Mover eureka client para o pom do projeto principal pq todos os microserviços vão precisar se registrar no eureka server.
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

- Copiar o parent do discovery
- Remove groupId
- Remove spring-cloud.version

Configurar a classe GatewayApplication:
Acrescentar as anotações: @EnableZuulProxy e @EnableEurekaClient

Configurar application.properties que deve ser renomeado para application.yml

Em resumo as configurações dos módulos ficarão assim:

1 - Microservice Course:
 eureka: Registra o Eureka Client do gateway no Eureka Server do Service discovery

application.yml:
```
    server:
      port: 8082

    eureka:
      instance:
        prefer-ip-address: true
      client:
        service-url:
          defaultZone: http://localhost:8081/eureka/
        register-with-eureka: true


    spring:
      application:
        name: course
      jpa:
        show-sql: false
        hibernate:
          ddl-auto: update
        properties:
          hibernate:
            dialect: org.hibernate.dialect.MySQL8Dialect
      jmx:
        enabled: false
      datasource:
        url: jdbc:mysql://localhost:3307/devdojo?allowPublicKeyRetrieval=true&sslMode=DISABLED
        username: root
        password: root
    jwt:
      config:
        type: signed

```

2 - Service Discovery: configura o cliente Eureka na porta 8181.
    Não queremos que o Eureka se auto-reguistre, por isso configuramos register-with-eureka: false 
    Devemos indicar onde o serviço cliente Eureka está hospedado: defaultZone: http://localhost:${server.port}/eureka/

application.yml:    
```
    spring:
      application:
        name: registry
    server:
      port: 8081

    eureka:
      client:
        register-with-eureka: false
        fetch-registry: false
        service-url:
          defaultZone: http://localhost:${server.port}/eureka/
```

DiscoveryApplication.java:
```
    @SpringBootApplication
    @EnableEurekaServer
    public class DiscoveryApplication {

        public static void main(String[] args) {
            SpringApplication.run(DiscoveryApplication.class, args);
        }

    }
````

3 - Gateway: 
 eureka: Registra o Eureka Client do gateway no Eureka Server do Service discovery
 zuul: sensitive-headers: Cookie: armazernar authorization nos cookies. Se não usar essa configuração authorization será removido.
 ERRO: exception is java.lang.IllegalArgumentException: ContextPath must start with '/' and not end with '/'
       (O context-path do servlet precisa iniciar com /)

application.yml:
O único que deve fazer fetch-registry é o gateway
```
    spring:
      application:
        name: gateway
    server:
      port: 8080
      servlet:
        context-path: /gateway
    eureka:
      instance:
        prefer-ip-address: true
      client:
        service-url:
          defaultZone: http://localhost:8081/eureka/
        fetch-registry: true
        register-with-eureka: true

      zuul:
        sensitive-headers: Cookie
```

GatewayApplication:
```
    @SpringBootApplication
    @EnableZuulProxy
    @EnableEurekaClient
    public class GatewayApplication {

        public static void main(String[] args) {
            SpringApplication.run(GatewayApplication.class, args);
        }

    }
```

IMPORTANTE: 
Ordem de inicialização:
O service discovery precisa ser iniciado primeiro poque os demais serviços precisam se registrar.
Se a ordem não for respeitada, o serviço vai ficar enviando mensagens de erro e vai ficar tentando se conectar.

Ajustar a ordem de inicialização dos serviços em Edit Configurations. Observar a ordem na aba Services da ide.
Para rodas todos os serviços listados na ordem em que aparecem, basta clicar em Spring Boot, botão Run.

Para observar os registros feitos no Service Discovery, basta acessar http://localhost/8081 e observar o relatório do Spring.

A porta dos microserviços deve ser bloqueada para acesso externo e os mesmos devem ser acessados via gateway.
Lembrar de adicionar o application name do microserviço ao context da URL
No postman trocar a rota para acessar o serviço via gateway:
http://localhost:8080/gateway

# Serviço de Autorização (Auth Service)
Geração de Token criptografado

1 - Criar um novo módulo de serviço: auth
Dependencies: Security / Spring Security
2 - Ajustar o pom.xml
- Copiar o parent do módule course
- Remover <groupId>academy.devdojo.youtube</groupId>
- Remover a dependência para spring-boot-starter-test
- Copiar build do módulo course

Resultado final:
pom.xml:
```
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <parent>
            <groupId>academy.devdojo.youtube</groupId>
            <artifactId>devdojo-microservices</artifactId>
            <version>1.0-SNAPSHOT</version>
            <relativePath>../</relativePath> <!-- lookup parent from repository -->
        </parent>
        <artifactId>auth</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <name>auth</name>
        <description>Authorization Service</description>

        <properties>
            <java.version>1.8</java.version>
        </properties>

        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-security</artifactId>
            </dependency>
            <dependency>
                <groupId>org.springframework.security</groupId>
                <artifactId>spring-security-test</artifactId>
                <scope>test</scope>
            </dependency>
        </dependencies>

        <build>
            <finalName>auth</finalName>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <executable>true</executable>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.8.0</version>
                    <configuration>
                        <source>1.8</source>
                        <target>1.8</target>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </project>
```

3 - Criar a classe ApplicationUser para representar a autorização do usuário no módulo core.
Copiar a classe Course para a classe ApplicationUser.
4 Criar a interface AplicationUserRepository
5 - Como nós vamos trabalhar com um token, vamos criar algumas propriedades para serem armazenadas e compartilhadas.
Como criar propriedades no Spring:
- criar um pacote chamado property
- dentro de property criar uma classe chamada JwtConfiguration e anotar com @Configuration e @ConfigurationProperties passando um prefixo.
Todas as propriedade listadas nessa classe vão para o arquivo application.yml.

JwtConfiguration:
```
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
            private String prefix = "Bearer "; // o prefixo do token
        }
    }

```

AuthApplication.java:
Para indicar que o arquivo de configuração application.yml será montado dinamicamente
    @EnableConfigurationProperties(value = JwtConfiguration.class)
Para o serviço de autenticação reconhecer os modelos que foram definidos no módulo core:
    @EntityScan({"academy.devdojo.youtube.core.model"})
    @EnableJpaRepositories({"academy.devdojo.youtube.core.repository"})
```
package academy.devdojo.youtube.auth;

import academy.devdojo.youtube.core.property.JwtConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

@SpringBootApplication
@EnableConfigurationProperties(value = JwtConfiguration.class)
@EntityScan({"academy.devdojo.youtube.core.model"})
@EnableJpaRepositories({"academy.devdojo.youtube.core.repository"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
```
6 -  Configuração: Onde vou dizer o que vai ser bloqueado e o que não vai ser bloqueado



O cross-site request forgery (CSRF ou XSRF), em português falsificação de solicitação entre sites, também conhecido como ataque de um clique (one-click attack) ou montagem de sessão (session riding), é um tipo de exploit malicioso de um website, no qual comandos não autorizados são transmitidos a partir de um usuário em quem a aplicação web confia.[1] Há muitos meios em que um site web malicioso pode transmitir tais comandos, tags de imagem especialmente criadas, formulários ocultos e XMLHttpRequests de JavaScript, por exemplo, podem funcionar sem a interação do usuário ou mesmo seu conhecimento. Diferente do cross-site scripting (XSS), que explora a confiança que um usuário tem para um site específico, o CSRF explora a confiança que um site tem no navegador de um usuário.


7 - Criar o Filtro que vai ser passado na configuração:

1 - Autenticação
2 - Gerar o Token
3 - Assinar o Token
4 - Criptografar o Token
5 - Retornar o Token 

```
    package academy.devdojo.youtube.auth.security.filter;

    import academy.devdojo.youtube.core.model.ApplicationUser;
    import academy.devdojo.youtube.core.property.JwtConfiguration;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.nimbusds.jose.*;
    import com.nimbusds.jose.crypto.DirectEncrypter;
    import com.nimbusds.jose.crypto.RSASSASigner;
    import com.nimbusds.jose.jwk.JWK;
    import com.nimbusds.jose.jwk.RSAKey;
    import com.nimbusds.jwt.JWT;
    import com.nimbusds.jwt.JWTClaimsSet;
    import com.nimbusds.jwt.SignedJWT;
    import lombok.RequiredArgsConstructor;
    import lombok.SneakyThrows;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

    import javax.servlet.FilterChain;
    import javax.servlet.ServletException;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    import java.security.KeyPair;
    import java.security.KeyPairGenerator;
    import java.security.interfaces.RSAPublicKey;
    import java.util.Collections;
    import java.util.Date;
    import java.util.UUID;
    import java.util.stream.Collectors;

    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    @Slf4j
    public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
        // TODO: 1 - Fazer autenticação, 2 - Gerar token, 3 - Assinar o token, 4 - Criptografar o token, 5 - Retornar o token

        private final AuthenticationManager authenticationManager;
        private final JwtConfiguration jwtConfiguration;

        @Override
        @SneakyThrows // Permite lançar exceção sem a necessidade de bloco try / catch. Efeito colateral: Dificulta rastrear o erro
        public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)  {

            log.info("Attempting authentication ...");

            // TODO: 1 - Faz a autenticação
            //Lê os dados de login e usa o mapper do Lombok para serializar o objeto JSON em um objeto ApplicationUser
            ApplicationUser applicationUser = new ObjectMapper().readValue(request.getInputStream(), ApplicationUser.class);
            if (applicationUser == null){
                throw new UsernameNotFoundException("Unable to retrieve the username or password");
            }
            log.info("Creating the authentication object for the user '{}' and calling UserDetailServiceImpl loadUserByUsername", applicationUser.getUsername());

            // TODO: 2 - Gera o token
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(applicationUser.getUsername(), applicationUser.getPassword(), Collections.emptyList());
            usernamePasswordAuthenticationToken.setDetails(applicationUser);

            return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        }

        //TODO: 5 - Retorna o token assinado e criptografado
        @SneakyThrows
        @Override
        protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication auth) throws IOException, ServletException {
            log.info("Authentication was successful for the user '{}', generating JWE token", auth.getName());

            SignedJWT signedJWT = createSignedJWT(auth);
            String encriptedToken = encryptToken(signedJWT);

            log.info("Token generated successfully, adding it to the response header");

            //Adicionar as informações abaixo para que o Javascript não tenha problemas para pegar o response header.
            //"XSRF-TOKEN": será usado pelo Spring
            response.addHeader("Access-Control-Expose-Headers", "XSRF-TOKEN " + jwtConfiguration.getHeader().getName());
            response.addHeader(jwtConfiguration.getHeader().getName(), jwtConfiguration.getHeader().getPrefix() + encriptedToken);
        }

        // TODO: 3 - Assina o token
        @SneakyThrows
        private SignedJWT createSignedJWT(Authentication auth){
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
        private String encryptToken(SignedJWT signedJWT) throws JOSEException {
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


```

@SneakyThrows: Anotação do Lombok que permite lançar exceções verificadas sem usar a declaração de throws. Isso é útil quando você precisa gerar uma exceção de um método dentro de interfaces muito restritivas como o Runnable.
É fato que as exceções verificadas podem (e geralmente o fazem) poluir o código com blocos try / catch desnecessários. Por outro lado lançamentos furtivos (Sneaky Throws) nos fazem perder o contexto em que a exceção foi lançada furtivamente, o que torna quase impossível identificar exceções observando os logs de rastreamento de pilha. Uma abordagem alternativa sobre métodos de higienização que lançam exceções verificadas é envolvê-las em um RuntimeException em primeiro lugar. Dessa forma, pode-se manter o código limpo e rastrear a pilha nos logs.

ObjectMapper: A classe Jackson ObjectMapper é usada para serializar objetos Java para JSON e desserializar a seqüência JSON em objetos Java


# Um pouco sobre Tokens

Os JWTs são muito úteis nos serviçoes Web RESTful, não apenas para autenticação sem estado, mas para todos os propósitos que requerem tokens, como por exemplo, para verificação de e-mail e senha esquecidos.
Spring Lemon: biblioteca que encapsula o código e a configuração não funcional necessários ao desenvolver serviços Web RESTful usando a estrutura Spring e Spring Boot.

JWT: JSON Web Token é um padrão que define uma forma segura de transmitir mensagens utilizando token compacto e self-contained no formato de um objeto JSON.
- Compacto: pode ser enviado através de um header HTTP, via URL, ou como parâmetro no corpo de uma requisição HTTP.
- Self-contained: por que seu payload possui toda a informação necessária para autenticar um usuário.

JSON Web Tokens são comumente utilizados quando precisamos de autenticação em aplicações com arquiteturas stateless (REST, por exemplo). JWTs nos permitem autenticar um usuário e garantir que as demais requisições erão feitas de forma autenticada, sendo possível restringir acessos a recursos e serviços com diferentes níveis de permissões.

Estrutura do JSON Web Token:
Um JWT é composto por três partes separadas por ponto:

hhh.ppp.sss

- H eader
  O header consiste em duas parte diferentes: o tipo do token (no caso JWT), e o nome do algoritmo resposável pelo hashing, HMAC SHA256 ou RSA.
```
    {
        "alg": "HS256",
        "typ": "JWT"
    }
```
Esse JSON será encoded via Base64Url e irá compor a primeira parte do token.

- P ayload
  O payload contém o que chamados de claims. Claims são atributos da entidade (no caso usuário) e metadados.
```
    {
        "sub": "1337",
        "name": "Lucas Bleme",
        "admin": true
    }
```


- S ignature
  Verifica se o remetente do JWT é quem diz ser para garantir que a mensagem não foi alterada durante o tráfego. Para criar a assinatura (signature), utiliza-se o header Base64 encoded, o payload também Base64 encoded, e o algoritmo especificado no header.
```
    HMACSHA256(
        base64UrlEncode(header) + "." + 
        base64UrlEncode(payload),
        secret)
```


Por fim, teremos uma string em Base64 separada por pontos, compondo o JSON Web Token.
```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTQ5MTUyMjg2NH0.OZQPWEgs-JaABOCEodulSiN-yd-T1gmZzrswY4kaNKNI_FyOVFJPaBsAqkcD3SgN010Y4VSFSNh6DXNqHwNMIw
```

Para testar esses conceitos pode-se usar o JWT Debugger e ver o token sendo formado na prática: https://jwt.io/

Existem dois tipos de JWTs:
- JWS: token com dados assinados assinados, mas não criptografados. Eles podem ser usados como tokens de autenticação nos quais o front-end ou qualquer cliente precisa ler os dados.
- JWE: token criptografado 

Java possui duas bibliotecas populares de código aberto para criação e análise de JWT:
JJWT (io.jsonwebtoken): 
 - Simples e muito fácil de usar. 
 - Suporta apenas JWS

Nimbus JOSE (nimbus-jose-jwt): 
 - Mais abrangente e possui recursos que não existem no JJWT. 
 - Suporta JWS e JWE. JWE é essencial para criar tokens a serem enviados por e-mail (ex. tokens de senha esquecida. 
 - O Spring Security 5 usa Nimbus JWT e suas dependências como spring-security-oauth2-client e spring-security-oauth2-jose incluem nimbus-jose-jwt.
 - Suporta vários algoritmos para assinar e criptografar tokens.

Criação / análise de tokens JWE usando chave compartilhada: se encaixa na maioria dos casos de uso ao desenvolver Serviços Web REST sem estado, por exemplo: autorização, validação de email, senha esquecida, etc.

- Adicionar dependência para Nimbus JOSE no pom.xml do parent.
- Criar o claimsSet. Claims são afirmações ou declarações, pequenas informações que do usuário que podem ser usadas para definir a política de segurançado sistema. 
Para testar use: jwt.io
Como a aplicação é totalmente stateless, deve-se passar no claimsSet tudo o que pode ser usado pelos microserviços.
Pode-se usar claims pré-definidas, como por exemplo subject e também claims customizadas, por exemplo: .claim("minhaclaim").



Os links abaixo foram usados como referência e contém mais informações sobre o assunto: 
    https://dzone.com/articles/using-nimbus-jose-jwt-in-spring-applications-why-a
    https://andreybleme.com/2017-04-01/autenticacao-com-jwt-no-spring-boot/


No response header estamos passando o prefixo Bearer. Bearer é um schema do HTTP Authentication.

Esquemas de autenticação
A estrutura geral de autenticação HTTP é usada por vários esquemas de autenticação. Os esquemas podem diferir na força da segurança e na disponibilidade deles no software cliente ou servidor.
Os esquemas de autenticação comuns incluem:

Basic: Básico (consulte RFC 7617 , credenciais codificadas em base64. Consulte abaixo para obter mais informações.)
Bearer: Portador (consulte RFC 6750 , tokens de portador para acessar recursos protegidos pelo OAuth 2.0),
Digest: Resumo (veja RFC 7616 , apenas o hash md5 é suportado no Firefox; veja o bug 472823 para suporte à criptografia SHA),
HOBA: (ver RFC 7486 , Secção 3, H TTP O rigin- B ound Um uthentication, baseado no de assinatura digital),
Mutual: Mútuo (ver RFC 8120 ),
AWS4-HMAC-SHA256:  (consulte os documentos da AWS ).


8 - Ajustar o arquivo application.yml
- Definir a porta
- Registrar o serviço no Eurika
- Identificar a aplicação Spring

application.yml:
```
    server:
      port: 8083

    eureka:
      instance:
        prefer-ip-address: true
      client:
        service-url:
          defaultZone: http://localhost:8081/eureka/
        register-with-eureka: true


    spring:
      application:
        name: auth
      jpa:
        show-sql: false
        hibernate:
          ddl-auto: update
        properties:
          hibernate:
            dialect: org.hibernate.dialect.MySQL8Dialect
      jmx:
        enabled: false
      datasource:
        url: jdbc:mysql://localhost:3307/devdojo?allowPublicKeyRetrieval=true&sslMode=DISABLED
        username: root
        password: root
    jwt:
      config:
        type: signed


```


9 - Gerar uma rotina de testes
- Rodar o teste para gerar a string para a senha que vamos usar para testes: Run Test
- Copiar a string gerada: $2a$10$1nBmIsEB5KR/NRoGPG2W9O6T8oJGA3nwQirnL5./xerkni1Js6YSW

```
package academy.devdojo.youtube.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class AuthApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void test(){
        System.out.println(new BCryptPasswordEncoder().encode("devdojo"));
    }

}

```

10 - Testar o login no Postman
Criar uma requisição post:

URL (acesso direto ao microserviço): localhost:8083/login
URL: localhost:8080/gateway/auth/login


Body: {"username":"joliveira", "password":"devdojo"}

Cadastrar um usuário na tabela application_user: password / role / username:
$2a$10$1nBmIsEB5KR/NRoGPG2W9O6T8oJGA3nwQirnL5./xerkni1Js6YSW	ADMIN	joliveira

IMPORTANTE: Ao realizar os testes, é necessário aguardar alguns instantes, porque os serviços precisam ser registrados no service discovery.

Copiar a string gerada no console do auth service e testar no site https://jwt.io/.
O token criptografado vai no response header

O token criptografado pode ser guardado no cookie do cliente para poder ser reutilizado.



# Módulo Token
Criar o módulo Token e retirar as funcionalidades ref. a criação, criptografia e conversão do Token do módulo Auth.
Será criado um Módulo Maven para esta finalidade.

Remover a anotação @EnableWebSecurity???
Remover private final UserDetailsService userDetailsService;
Remover protected void configure(AuthenticationManagerBuilder auth) throws Exception 
Remover public BCryptPasswordEncoder passwordEncoder()
Remover .addFilter


ERRO: jwtConfiguration nulo. É necessário declarar como final para poder acessar as configurações, pois trata-se de um singleton e precisa ser declarado como variável de instância.
    private final JwtConfiguration jwtConfiguration;

#Autorização

- Token Converter: Responsável por descriptografar o token
- JwtTokenAuthorizationFilter: Será usada em todas as requisições e vai ser responsável por validar as Roles.

Há dois tipos de configurações:
1 - O token vai ser enviado assinado e criptografado para todos os microserviços 
2 - O token vai ser enviado apenas assinado para os downstream services. Abaixo do gateway, apenas assinado. Acima do gateway assinado e criptografado.

- downstream (rio abaixo): Os serviços downstream são os que consomem o serviço upstream. Em particular, eles dependem do serviço upstream. Portanto, o front-end fica a jusante do back-end, porque depende do back-end. O back-end pode existir significativamente sem o front-end, mas o front-end não faz sentido sem o back-end.

- upstream (rio acima): De maneira mais geral, os serviços upstream não precisam saber ou se preocupar com a existência de serviços downstream. Os serviços downstream se preocupam com a existência de serviços upstream, mesmo que apenas os consumam opcionalmente.

- SecurityContextUtil: Classe utilitária para recuperar informações do usuário no Spring Security.
Spring Security possui o Security Context onde podemos guardar dados para depois recuperá-los a partir do principal
Coloque o usuário em um bean.



Na classe JwtTokenAuthorizationFilter estamos importando estaticamente a classe SecurityContextUtil: import static academy.devdojo.youtube.security.util.SecurityContextUtil.setSecurityContext;
O texto abaixo trata um pouco sobre import static:

O import static foi criado basicamente, para evitar o Constant Interface Antipattern.
O import static geralmente é uma boa idéia quando se está interagindo com classes que só contém métodos estaticos, tal como a java.lang.Math.
Não existe nenhum ganho ao escrever Math. o tempo todo. O código fica mais limpo e mais claro se essa escrita for simplesmente eliminada, através do import static. Especialmente se você chama esses métodos o tempo todo.
Na verdade, pelo conceito de OO, a classe Math do Java nem sequer deveria existir. Ela é usada para conseguir simular a programação estruturada no Java. E com o import static, é isso mesmo que acontece. Pense bem: Qual seria diferença de criar uma classe só com métodos estáticos e que não permite herança, ou de criar funções sem classe, dentro de um pacote java Math, como seria na programação estruturada? Nenhuma.
Não é recomendável usar o import static em métodos fábrica, ou métodos que cujo significado não pode ser definido sem que se leia o nome da classe que os contém. Por exemplo, o método getByName da classe InetAddress. Fazer um import static desse método só vai deixar o código confuso.

- Incluir o arquivo de configuração no módulo gateway.
Importante não esquecer da anotação @EnableWebSecurity. Como o próprio nome diz, é ela que habilita os recursos de segurança em nossa aplicação.
O gateway será responsável por enviar aos serviços downstream o token e o gateway vai decidir se o token será emitido apenas autenticado ou também criptografado


ATENÇÃO: ERRO 401 (Não autorizado) quando passa pelo gateway. Isso ocorre devido a configuração da login-URL. 
Incluir a configuração abaixo no application.yml. Observe que agora o login foi colocado dentro do contexto auth. 
Lembrando que a URL para acesso direto é: localhost:8083/login e a URL passando pelo gateway é:localhost:8080/gateway/auth/login e a URL que está definida na classe JwtConfiguration, na propriedade loginUrl é "/login/**", ou seja, sem contexto.

jwt:
  config:
    login-url: /auth/login


Ao realizar um teste com o serviço course, vamos ganhar ERRO 401 - Unauthorized, porque agora é necessário passar o header Authorization com o token retornado pelo serviço gateway.
Lembrando que é importante respeitar a URL definida na classe SecurityTokenConfig: "/course/v1/admin/**"


Guia rápido de anotações Spring: https://dzone.com/articles/a-guide-to-spring-framework-annotations

A cada novo teste é necessário fazer o login passando pelo gateway para gerar um novo token.
Se fizer um teste trocando a role de ADMIN para USER vamos ganhar o ERRO 403 - Forbidden, (acesso proibido) Requisição legal, mas o servidor está recusando a resposta.


# Criar Segurança para módulo de cursos e criar end-point para retornar os dados UI

- Criar end-point para retornar os dados do usuário:
@RestController
@RequestMapping("user")
public class UserInfoController{
...
}

A requisição vai falhar porque precisa atualizar o SecurityConfig do token 


Problema de Timeout na resposta do Gateway.
Hystrix tem um tempo de timeout de 1.5 a 2s, porém a primeira requisição demorar mais o que pode gerar o timeout.
Para resolver o problema é necessário incluir a seguinte configuração no application.yml do gateway:

Como está utilizando o load balancer do Eurika, precisamos utilizar Ribbon
Se estivesse colocando URL fixa dos end-points nós teríamos que usar o Hystrix

Ribbon e Hystrix são aplicativos Netflix OSS.
Ribbon é um load balancer. O Hystrix é uma aplicação circuit breaker.
O projeto Spring Cloud Hystrix está obsoleto. Portanto, novos aplicativos não devem usar este projeto.
O Spring Cloud Load Balancer será o sucessor do Ribbon e o sucessor do Spring Cloud Netflix Zuul é o Spring Cloud Gateway.

ERRO: java.text.ParseException:unexpected number of Base64URL parts, must be five
Quando tem o token criptografado são 5, e quando tem o token somente assinado são 3, então precisa dizer ao serviço de autenticação que o token está assinado e não criptografado, pois o sistema aguarda um token criptografado.

no application.yml do módulo auth incluir as seguintes linhas:
jwt:
  config:
    type: signed

essas linhas devem ser acrescentadas no arquivo application.yml de todos os micro serviços.

# Ajustar o módulo course:
Acrecentar a anotação @EnableConfigurationProperties na classe CourseApplication para dizer que esse módulo possui um arquivo de configurações.
Acrescentar a anotação @ComponentScan para localizar os beans spring


# Swagger Documentation
Swagger é uma ferramenta que auxilia o desenvolvedor de API's Rest a criar a documentação das suas API's de forma padronizada, facilitando o entendimento de cada serviço existente na API e sua estrutura, sem que haja a necessidade de abrir seu código-fonte.
Para gerar a documentação estamos utilizando o Springfox. O Springfox é um conjunto de bibliotecas que visa automatizar a geração de especificações e torná-las legíveis tanto por máquinas, quanto por pessoas para APIs JSON escritas usando o ecosistema Spring. O Springfox funciona examinando a aplicação, uma vez, em tempo de execução para inferir a semântica da API com base nas configurações, estrutura de classe e anotações java em tempo de compilação.
Esse projeto evoluiu do swagger-springmvc.

- Deve ser criado um arquivo de configuração no core para ficar disponível a todos os módulos.
A anotação @Bean foi utilizada para gerar um bean Spring a partir do resultado do método
Um pouco mais sobre @Bean pode ser visto em https://medium.com/@decioluckow/explorando-bean-spring-ioc-e640c53d29a9


```

package academy.devdojo.youtube.core.docs;

import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

public class BaseSwaggerConfig {
    private final String basePackage;

    public BaseSwaggerConfig(String basePackage){
        this.basePackage = basePackage;
    }

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .build()
                .apiInfo(metaData());
    }

    private ApiInfo metaData(){
        return new ApiInfoBuilder()
                .title("Another Awesome course from DevDojo Spring Boot Microservices")
                .description("Everybody is a Jedi noew")
                .version("1.0")
                .contact(new Contact("Julio Oliveira", "https://joliveira.com.br", "joliveira.net@gmail.com"))
                .license("Private stuff bro, belongs to DevDojo")
                .licenseUrl("http://devdojo.academy")
                .build();
    }
}



```
- Feito isso, deve realizar a configuração para cada um dos microserviços.
A classe deve receber a anotação @EnableSwagger2 e o método construtor deverá passar o pacote ref. a API REST definida no controller

```
package academy.devdojo.youtube.course.docs;

import academy.devdojo.youtube.core.docs.BaseSwaggerConfig;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig extends BaseSwaggerConfig {

    public SwaggerConfig() {
        super("academy.devdojo.youtube.course.endpoint.controller");
    }
}


```
- Em seguida, a classe controller deve receber a anotação @Api, indicando ao Swagger que a classe deverá ser documentada. Além disso, cada um dos métodos que está sendo exposto precisa ganhar uma anotação @ApiOperation passando um texto explicativo.
- A documentação da API será gerada automaticamente e ficará disponível a partir da URL do recurso, acrescentando-se a página swagger-ui.html
exemplo: localhost:8080/gateway/auth/swagger-ui.html


--------------------------------------------------------------------------------------------
#EXTRA: DOCKER
--------------------------------------------------------------------------------------------

#MySql no Docker

no terminal:
1 - verificar as imagens no docker.
```
$ docker images
```
2 - Adicionar a imagem do MySql
```
$ docker pull mysql
```
3 - Criar container com a imagem
```
$ docker run --name mysqldb -e MYSQL_ROOT_PASSWORD=root -p 3307:3306 -d mysql
```
cria um container com nome mysqldb passando a senha root para a constante MYSQL_ROOT_PASSWORD e associa a porta 3307 da máquina host com a porta 3307 do container (pq a 3306 já está sendo usada por outra instância do MySql) e no final vincula o container a imagem docker mysql.

4 - listar todos os containers
```
$ docker ps -a
```
5- Acessar administração MySql
docker exec -it mysqldb mysql -p

-p para solicitar a senha


# Comandos Úteis do Docker

Vejamos quais os principais comandos que podemos usar para limpar o nosso ambiente do Docker.

# Exibindo os recursos do ambiente

A primeira coisa a fazer é identificar os recursos, contêiners, imagens, volumes e redes existentes no seu ambiente Docker.

Para isso podemos usar os seguintes comandos :

    docker container ls	 (Lista os contêineres que estão em execução. (docker ps))
    docker container ls -a	 (Lista todos os contêineres. (docker ps -a))
    docker image ls	 Lista as imagens (docker images)
    docker volume ls	 Lista os volumes
    docker network ls	 Lista as redes
    docker info	 Lista a quantidade de contêineres e imagens e informações do ambiente
Dessa forma temos contêineres que pode estar em execução e os que estão parados, e, temos também imagens não usadas, imagens que foram usadas para criar contêineres e imagens pendentes que não possuem relacionamento com nenhuma imagem tagueada.(as dangling images).

Uma imagem não usada significa que ela não foi atribuída ou usada em um contêiner. Por outro lado, uma imagem pendente significa que você criou um novo build da imagem, mas não recebeu um novo nome.

Então as velhas imagens que você tem se tornam a "imagem pendente". Essas imagens antigas são aquelas sem tag e exibem "<none>" em seu nome quando você executa o comando : docker images

Removendo todos os recursos

O Docker oferece um comando conveniente para remover contêineres , networks e imagens não usadas :

docker system prune

Este comando remove :

Todos os contêineres parados
Todas as redes não usadas pelo menos por um contâiner
Todas as imagens pendentes (dangling images)
Todo o cache build pendente


Note que é solicitada a confirmação para continuar o processamento [y/N].

Para remover também os volumes e imagens não utilizadas e sobrescrever o prompt de confirmação podemos usar o comando:

docker system prune --all --force --volumes

Este comando remove:

Todos os contêineres parados
Todas as redes não usadas pelo menos por um contâiner
Todos os volumes não usados por pelo menos um contêiner
Todas as imagens sem nenhum contêiner associado
Todo o cache build pendente
E não solicita confirmação. Portanto cuidado ao user o comando.

Também podemos aplicar o comando prune a cada recurso individualmente.

Assim:

docker container prune - remove todos os contêineres não usados;
docker image prune - remove todas as imagens não usadas;
docker volume prune - remove todos os volumes não usados;
docker network prune - remove todas as redes não utilizadas;
Removendo recursos individualmente

Outra opção é remover cada recurso individualmente. Para isso podemos usar os seguintes comandos:

docker container rm <id>   ou   docker rm <id>
docker image rm <id>         ou   docker rmi <id>
docker volume <id>
docker network <id>
Remove o recurso identificado pelo <id> ou nome especificado.

Para containeres, imagens e volumes temos ainda a opção -f que força a remoção do recurso.

docker container rm <id> -f
docker image rm <id> -f
docker volume <id> -f
Essa abordagem é mais segura, mas dependendo da quantidade de recursos, terá que ser repetida várias vezes.

Podemos ainda usar uma combinação de comandos para remover mais de um recurso de uma vez.

Contêineres:  docker container rm $(docker container ls -a -q)
 
Imagens :      docker image rm $(docker image ls -a -q)
 
Volumes :      docker volume rm $(docker volume ls -q)
 
Networks :    docker network rm $(docker network ls -q)
Onde:

 -a  : signfica todos os recursos
 -q  : significa o ID numérico do recurso

Assim, para parar todos os contêineres podemos usar o comando:

docker container stop $(docker container ls -a -q)

E podemos também vincular dois comandos para limpar todo o ambiente:

docker container stop $(docker container ls -a -q) && docker system prune -a -f --volumes

Dessa forma, você têm essas opções para limpar o seu ambiente, podendo também criar scripts com combinação de comandos para serem executados automaticamente.


# O que é e como usar o Docker Stack.

 
O Docker Stack é, de forma simplificada, a evolução do Docker Compose, com foco em clusters (utilizando swarm). Com o Docker Stack você faz um script e configura nele quantos containers devem subir para cada serviço e o Swarm aloca estes serviços dentro do cluster.

A grosso modo, com o Docker Compose você cria um script com as camadas da sua aplicação, que serão aplicadas em um node. Com o Docker Stack você faz a mesma coisa, mas aplicando o resultado em um cluster.

Play-With-Docker: cluster: Para ver saber como criar um cluster, procure por Docker Swarm.
Em um cluster pode-se estabelecer qual nó (node) será o manager e quais serão os workers.

Antes de utilizar o Docker Stack propriamente dito, precisamos de um arquivo, que segue o mesmo padrão do Docker Compose. 
No node manager, crie um arquivo chamado docker-stack.yml, conforme o exemplo abaixo:

```
    YAML
    version: "3"
    services:

      wordpress:
        image: wordpress
        depends_on:
          - mysql:mysql
        ports:
          - 8080:80
        networks:
          - frontend
        deploy:
          replicas: 6
          restart_policy:
            condition: on-failure

      mysql:
        image: mysql
        environment:
          - MYSQL_ROOT_PASSWORD=password
        networks:
          - frontend
        deploy:
          placement:
            constraints: [node.role == manager]

    networks:
      frontend:
```
O resultado final é bem parecido com o do Docker Compose, mas com algumas diferenças:

No exemplo acima, para o deploy do frontend (wordpress), está definido que ele possuirá 6 replicas, ou seja, existirão 6 containers para o front-end;
No banco de dados, foi incluída uma constraint no deploy (placement: constraints: [node.role == manager]), que força este serviço a subir apenas no node que está marcado como manager.
Ao final do arquivo, foi incluído um network chamado frontend. Ele foi adicionado tanto para o wordpress quanto para o MySql. Este network funciona como uma espécie de “rede privada”. Apenas quem utilizar esta rede será capaz de se comunicar com os containers do seu cluster. Isso é interessante para criar um nível a mais de isolamento.
No mais, as mesmas regras se aplicam. 

Bom, agora que já temos o arquivo necessário, vamos utiliza-lo!

Shell
docker stack deploy --compose-file docker-stack.yml wp_app
1
docker stack deploy --compose-file docker-stack.yml wp_app
No comando acima, estamos indicando que vamos utilizar o stack para fazer um deploy, tendo o arquivo docker-stack.yml como base. O wp_app no final da linha indica o prefixo que será utilizado nos serviços que subirem para esta stack, ou seja, o network que chamamos de frontend subirá com o nome wp_app_frontend, o wordpress com o nome wp_app_wordpress e o banco de dados com o nome wp_app_mysql.


Após executar o comando acima, o Docker irá gerar 6 containers com o wordpress e 1 com o MySql. Para sabermos se deu certo, podemos utilizar alguns comandos básicos (parte 2), aprendidos em posts anteriores:

Shell
docker service wp_app_wordpress
1
docker service wp_app_wordpress
O comando acima mostra todos os containers que estão rodando o wordpress e em qual node eles estão.

Agora vamos conferir o banco de dados:

Shell
docker service wp_app_mysql
1
docker service wp_app_mysql
Devido a constraint (restrição) que colocamos, ao executar este comando, a lista deve conter apenas um container e este deve estar rodando no node manager.


O nome do serviço que você deve utilizar neste comando é o prefixo definido no comando docker stack deploy (wp_app, no nosso caso) + nome definido para cada serviço no arquivo docker-stack.yml. (que foi o nome logo cima do item image:)


Você deve ter reparado que não colei aqui os resultados dos comandos. Desta vez pulei esta etapa, pois a visualização não fica muito boa.


Posts para referência:
Introdução ao Docker:
https://raccoon.ninja/pt/dev-pt/introducao-ao-docker/

Comandos básicos do Docker:
https://raccoon.ninja/pt/dev-pt/comandos-basicos-do-docker/

Um pouco sobre Docker Compose:
https://raccoon.ninja/pt/dev-pt/um-pouco-sobre-docker-compose/

Trabalhando com clusters no Docker Swarm
https://raccoon.ninja/pt/dev-pt/trabalhando-com-clusters-no-docker-swarm/

O que é e como usar o Docker Stack:
https://raccoon.ninja/pt/uncategorized-pt/o-que-e-e-como-usar-o-docker-stack/
