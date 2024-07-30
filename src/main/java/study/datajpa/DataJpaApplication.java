package study.datajpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableJpaRepositories(basePackages = "study.datajpa.repository")
// spring boot를 사용했다면 @EnableJpaRepositories를 사용하지 않아도,
// @SpringBootApplication가 있는 패키지를 포함하여 하위 패키지는 전부 자동으로 인식한다.
// 패키지 위치가 밖이라면, @EnableJpaRepositories 설정 필요
public class DataJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataJpaApplication.class, args);
    }

}
