package study.datajpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 메소드 명에서 find와 by 사이에 어떤 단어가 들어가도 쿼리에 영향을 끼치지 않음. 마음대로 네이밍 가능.

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    List<Member> findTop3By();

    // @Query(name = "Member.findByUsername") // 없어도 동작함
    // JpaRepository<T, ID> 에서 T에 해당하는 엔티티에서 NamedQuery를 먼저 탐색함
    // Named Query가 존재하면 Named Query를 실행하고 없으면 `메소드 이름으로 쿼리 생성` 방식으로 쿼리를 생성해서 실행
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> usernames);

    List<Member> findListByUsername(String username);   // 컬렉션

    Member findMemberByUsername(String username);   // 단건

    Optional<Member> findOptionalByUsername(String username);   // 단건 Optional

    // count 쿼리만 따로 정의할 수 있음. count 하는데는 굳이 join할 필요 없으므로 따로 정의하는 것이 좋음.
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);

    // @Modifying: JPA의 executeUpdate(). 수정하는 쿼리에는 꼭 넣어줘야 하는 어노테이션. 없으면 에러 발생.
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    // @EntityGraph: team을 fetch join한 것과 같은 효과
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    @EntityGraph("Member.all")
    List<Member> findNamedEntityGraphByUsername(@Param("username") String username);

    // 간단한 fetch join이 필요할 때는 EntityGraph를 사용
    // 복잡한 쿼리는 JPQL로 직접 fetch join 쿼리 작성
    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);
}
