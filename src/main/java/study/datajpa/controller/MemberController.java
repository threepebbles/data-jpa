package study.datajpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    // 도메인 클래스 컨버터
    // 외부에 PK를 공개해서 조회하게 하는 경우가 많진 않음 
    // HTTP 요청으로 Member 엔티티의 PK를 받지만, 도메인 클래스 컨버터가 중간에 동작해서 Member 엔티티 객체를 파라미터로 반환. 반환된 엔티티는 영속성 컨텍스트에서 관리되는 대상이 아니므로 조회용으로만 써야 한다. 변경하려면 Transational 달고 변경할 수는 있지만 로직이 지저분해지므로 권장하지 않음.
    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    // ex) localhost:8080/members?page=1&size=5&sort=username,desc&sort=id,desc
    // default: page=0, size=20
    @GetMapping("/members")
    public Page<MemberDto> list(
            // local 설정. global 설정은 appliation.yml에서 가능
            @PageableDefault(page = 1, size = 5, sort = "username", direction = Direction.DESC) Pageable pageable) {

        Page<Member> page = memberRepository.findAll(pageable);

        Page<MemberDto> toMap = page.map(member -> new MemberDto(member));
        return toMap;
    }

    @PostConstruct
    public void init() {
//        for (int i = 0; i < 100; i++) {
//            memberRepository.save(new Member("user" + i, i));
//        }
    }
}
