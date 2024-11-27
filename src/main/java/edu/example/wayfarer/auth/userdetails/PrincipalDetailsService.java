package edu.example.wayfarer.auth.userdetails;


import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 우리 프로젝트에서는 username이 email과 동치라서 아래와 같이 내비뒀습니다.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("올바르지 않은 email"));
        return new PrincipalDetails(member);
    }
}
