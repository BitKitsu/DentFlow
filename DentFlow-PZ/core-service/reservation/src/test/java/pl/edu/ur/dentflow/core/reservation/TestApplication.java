package pl.edu.ur.dentflow.core.reservation;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import pl.edu.ur.dentflow.core.clinic.infrastructure.StaffMemberRepository;
import pl.edu.ur.dentflow.core.clinic.infrastructure.TenantRepository;
import pl.edu.ur.dentflow.core.patient.infrastructure.PatientRepository;
import pl.edu.ur.dentflow.core.scheduling.infrastructure.BlockerRepository;

@SpringBootApplication(
    scanBasePackages = {
        "pl.edu.ur.dentflow.core.reservation",
        "pl.edu.ur.dentflow.core.notification"
    },
    exclude = MailSenderAutoConfiguration.class
)
@EntityScan({
    "pl.edu.ur.dentflow.core.reservation.domain",
    "pl.edu.ur.dentflow.core.notification.domain"
})
@EnableJpaRepositories({
    "pl.edu.ur.dentflow.core.reservation.infrastructure",
    "pl.edu.ur.dentflow.core.notification.infrastructure"
})
public class TestApplication {

    @Bean
    @Primary
    public BlockerRepository blockerRepository() {
        return Mockito.mock(BlockerRepository.class);
    }

    @Bean
    @Primary
    public TenantRepository tenantRepository() {
        return Mockito.mock(TenantRepository.class);
    }

    @Bean
    @Primary
    public StaffMemberRepository staffMemberRepository() {
        return Mockito.mock(StaffMemberRepository.class);
    }

    @Bean
    @Primary
    public PatientRepository patientRepository() {
        return Mockito.mock(PatientRepository.class);
    }

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
}
