package study.datajpa.repository;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {
    // Closed Projections
    String getUsername();

    // Open Projections. SpEL문법 지원.
    @Value("#{target.username + ' ' + target.age}")
    String getUsernameAndAge();
}
