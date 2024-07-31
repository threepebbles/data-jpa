package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import study.datajpa.entity.Member;

/**
 * @ 간단한 기능은 JpaRepository<T, ID>를 사용하고, 복잡한 쿼리를 JDBC Template, MyBatis, QueryDSL로 직접 구현하고 싶은 경우 아래와 같이 직접 구현체를 생성해서 사용
 * @ 1. MemberRepositoryCustom 인터페이스 생성
 * @ 2. MemberRepository가 MemberRepositoryCustom을 상속
 * @ 3. MemberRepositoryCustom을 상속받은 실제 구현체 작성
 * @ !주의사항
 * @ 구현체의 이름은 무조건 "인터페이스명 + Impl" 이어야 한다. (MemberRepositoryImpl혹은 MemberRepositoryCutomImpl)
 * @ 인터페이스명 + postfix 규칙은 바꿀 수 없다. (인터페이스명은 고정. 못 바꿈.)
 * @EnableJpaRepositories를 이용해서 인터페이스 구현체의 postfix 규칙을 커스터마이징할 수 있지만, 사이드 이펙트를 생각하면 그냥 디폴트 값(Impl)으로 사용하는 것을 권장.
 */
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m where m.age>=10", Member.class)
                .getResultList();
    }
}
