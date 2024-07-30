package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
//@Rollback(false)
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Optional<Member> byId = memberRepository.findById(savedMember.getId());
        // 원래 null 체크 필요
        Member findMember = byId.get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        // 하나의 transaction 내에서는 객체의 동일성 보장 (1차 캐시의 기능)
        assertThat(findMember).isEqualTo(member);   // findMember == member
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findHelloBy() {
        List<Member> findMembers = memberRepository.findTop3By();
    }

    @Test
    public void testNamedQuery() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 30);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void testQuery() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 30);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void findByNames() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 30);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 30);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> aaa = memberRepository.findListByUsername("AAA");
        Member findMember = memberRepository.findMemberByUsername("abcdef");
        System.out.println("findMember = " + findMember);   // null
        Optional<Member> findOptional = memberRepository.findOptionalByUsername("abcdef");
        System.out.println("findOptional = " + findOptional);   // Optional.empty
    }

    @Test
    public void returnType_duplicate() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 30);
        memberRepository.save(member1);
        memberRepository.save(member2);

        assertThatThrownBy(() -> {
            // org.hibernate.NonUniqueResultException -> org.springframework.dao.IncorrectResultSizeDataAccessException 발생
            Optional<Member> findOptional = memberRepository.findOptionalByUsername("AAA");
        }).isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    public void paging() {
        //given
        int memberCount = 11;
        for (int i = 1; i <= memberCount; i++) {
            memberRepository.save(new Member("member" + i, 10));
        }
        int age = 10;
        int pageNumber = 3;
        int pageSize = 3;
        // username기준 내림차순 정렬 결과를 페이징
        // 한 페이지당 pageSize개 항목
        // pageNumber번째 페이지 조회
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
//        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        //then
        List<Member> content = page.getContent();
        assertThat(content.size()).isEqualTo(2);    // 조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(11);  // 전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(3);  // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(4);  // 전체 페이지 수
        assertThat(page.isFirst()).isFalse();   // 첫번째 페이지인가?
        assertThat(page.hasNext()).isFalse();   // 다음 페이지가 있는가?

//
//        PageRequest pageRequest2 = PageRequest.of(2, pageSize, Sort.by(Direction.DESC, "username"));
//        Page<Member> page2 = memberRepository.findByAge(age, pageRequest2);
//        assertThat(page2.getContent().size()).isEqualTo(3);
//        assertThat(page2.getTotalElements()).isEqualTo(11);
//        assertThat(page2.getNumber()).isEqualTo(2);
//        assertThat(page2.getTotalPages()).isEqualTo(4);
//        assertThat(page2.isFirst()).isFalse();
//        assertThat(page2.hasNext()).isTrue();
    }

    @Test
    public void slicing() {
        //given
        int memberCount = 11;
        for (int i = 1; i <= memberCount; i++) {
            memberRepository.save(new Member("member" + i, 10));
        }

        int age = 10;
        int pageNumber = 0;
        int pageSize = 3;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.DESC, "username"));

        //when
        // Slice는 limit+1개만큼 가져옴
        // select m1_0.member_id,m1_0.age,m1_0.team_id,m1_0.username from member m1_0 where m1_0.age=10 order by m1_0.username desc limit 4;
        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent();
//        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
//        assertThat(page.getTotalElements()).isEqualTo(11);
        assertThat(page.getNumber()).isEqualTo(0);
//        assertThat(page.getTotalPages()).isEqualTo(4);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void bulkUpdate() {
        //given
        for (int i = 1; i <= 5; i++) {
            memberRepository.save(new Member("member" + i, 10 * i));
        }

        //when
        // 20살 이상 -> 4명
        // 벌크 연산 주의사항
        // 벌크 연산(executeUpdate()) 실행 시에는 내부적으로 em.flush() 호출 후, 영속성 컨텍스트를 거치지 않고 DB로 바로 쿼리를 보낸다.
        // 따라서 벌크 연산 후 영속성 컨텍스트에 남아있는 엔티티는 DB와 값이 다르다는 것을 인지하고 있어야 한다.
        // 벌크 연산 후 em.clear()를 통해 캐시를 비워주는 것이 하나의 방법.
        // Spring Data JPA의 @Modifying(clearAutomatically = true)를 사용하면 em.clear()와 같은 효과를 볼 수 있음.
        int resultCount = memberRepository.bulkAgePlus(20);

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5); // clearAutomatically = true 했을 경우 51, 안했을 경우 50

        //then
        assertThat(resultCount).isEqualTo(4);
    }

    @Test
    @Rollback(false)
    public void findMemberLazy() {
        //given
        // member1 -> teamA
        // member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when
//        List<Member> members = memberRepository.findNamedEntityGraphByUsername("member1");
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");
        //then
    }
}