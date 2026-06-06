package pl.edu.ur.dentflow.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.mail.host=localhost",
    "spring.mail.port=25",
    "spring.mail.username=test",
    "spring.mail.password=test",
    "spring.mail.properties.mail.smtp.auth=false",
    "spring.mail.properties.mail.smtp.starttls.enable=false",
    "jwt.secret=test-secret-key-for-unit-tests-only-min-256-bits-long",
    "supabase.url=https://fake.supabase.co",
    "supabase.service-key=fake-key",
    "aws.access-key-id=test-access-key",
    "aws.secret-access-key=test-secret-key",
    "aws.endpoint-url=https://s3.test.com",
    "aws.region=us-east-1",
    "aws.bucket-name=test-bucket"
})
class CoreServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
