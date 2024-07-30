package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

@SpringBootTest
@Transactional
//@Rollback(false)
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

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
}