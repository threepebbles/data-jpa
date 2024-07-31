package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
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
    // 벌크 연산 주의사항
    // 벌크 연산(executeUpdate()) 실행 시에는 내부적으로 em.flush() 호출 후, 영속성 컨텍스트를 거치지 않고 DB로 바로 쿼리를 보낸다.
    // 따라서 벌크 연산 후 영속성 컨텍스트에 남아있는 엔티티는 DB와 값이 다르다는 것을 인지하고 있어야 한다.
    // 벌크 연산 후 em.clear()를 통해 캐시를 비워주는 것이 하나의 방법.
    // Spring Data JPA의 @Modifying(clearAutomatically = true)를 사용하면 em.clear()와 같은 효과를 볼 수 있음.
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

    // 변경 감지 기능 필요 없고 100% 조회용으로만 사용할 것이다 하면 최적화 방법이 있음.
    // 이걸로 인해 얻을 수 있는 성능 이점이 그렇게 크진 않음.
    // redis 없이 관계형 DB사용하면서 조회 성능을 조금씩만 더 최적화하고 싶을 때 사용
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    // Spring Data JPA에서 제공하는 Lock 기능. 데이터베이스 방언에 따라 동작방식이 다름.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

    List<UsernameOnly> findProjectionsByUsername(@Param("username") String username);

    <T> List<T> findProjectionsDtoByUsername(@Param("username") String username, Class<T> type);

    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);

    @Query(value = "select m.member_id as id, m.username, t.name as teamName from member m left join team t",
            countQuery = "select count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);
}
