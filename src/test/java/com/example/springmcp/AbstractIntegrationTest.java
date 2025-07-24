
package com.example.springmcp;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.annotation.DirtiesContext;

import org.testcontainers.containers.wait.strategy.Wait;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("root")
            .withPassword("password");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management")
            .withQueue("test-queue");

    @Container
    static GenericContainer<?> chromadb = new GenericContainer<>(DockerImageName.parse("ghcr.io/chroma-core/chroma:0.4.18"))
            .withExposedPorts(8000)
            .withCommand("uvicorn chromadb.app:app --host 0.0.0.0 --port 8000")
            .waitingFor(Wait.forLogMessage(".*Uvicorn running on http://0.0.0.0:8000.*", 1));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);

        registry.add("spring.ai.vectorstore.chroma.url", () -> "http://" + chromadb.getHost() + ":" + chromadb.getMappedPort(8000));
    }
}
