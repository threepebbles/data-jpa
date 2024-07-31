package study.datajpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @EnableJpaAuditing(modifyOnCreate = false): 업데이트 값은 null로 설정 (권장하지 않음)
@EnableJpaAuditing
@SpringBootApplication
//@EnableJpaRepositories(basePackages = "study.datajpa.repository")
// spring boot를 사용했다면 @EnableJpaRepositories를 사용하지 않아도,
// @SpringBootApplication가 있는 패키지를 포함하여 하위 패키지는 전부 자동으로 인식한다.
// 패키지 위치가 밖이라면, @EnableJpaRepositories 설정 필요
public class DataJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataJpaApplication.class, args);
    }

    // @CreatedBy, @LastModifiedBy가 호출될 때마다 auditorProvider를 호출해서 결과물을 가져감
    @Bean
    public AuditorAware<String> auditorProvider() {
        // spring security
        // http 세션 혹은 JWT에서 수정자 이름 정보를 추출해와서 반환해주면 됨
        return () -> Optional.of(UUID.randomUUID().toString());
    }

}
