package study.datajpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    List<Member> findTop3By();

    // @Query(name = "Member.findByUsername") // 없어도 동작함
    // JpaRepository<T, ID> 에서 T에 해당하는 엔티티에서 NamedQuery를 먼저 탐색함
    // Named Query가 존재하면 Named Query를 실행하고 없으면 `메소드 이름으로 쿼리 생성` 방식으로 쿼리를 생성해서 실행
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);
}
