package com.pu.programus.sign;

import com.pu.programus.exception.SignException;
import com.pu.programus.jwt.JwtTokenProvider;
import com.pu.programus.member.Member;
import com.pu.programus.member.MemberRepository;
import com.pu.programus.sign.dto.SignInDto;
import com.pu.programus.sign.dto.SignUpDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SignService {

    private final Logger LOGGER = LoggerFactory.getLogger(SignService.class);
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public boolean checkRegisteredByUid(String uid) {
        Optional<Member> member = memberRepository.findByUid(uid);
        return member.isPresent();
    }

    public void signUp(SignUpDto signUpDto) throws SignException{

        LOGGER.info("[signUp] 중복 검사");
        Optional<Member> optionalMember = memberRepository.findByUid(signUpDto.getId());
        if (optionalMember.isPresent())
            throw new SignException(HttpStatus.BAD_REQUEST, "중복된 ID 입니다.");
        
        LOGGER.info("[signUp] User 엔티티 생성");
        Member member = createMember(signUpDto);

        LOGGER.info("[signUp] userRepository 저장");
        Member savedMember = memberRepository.save(member);

        if(savedMember.getUsername().isEmpty())
            throw new SignException(HttpStatus.BAD_REQUEST, "DB 저장에 실패했습니다.");
    }

    /**
     * @param signInDto
     * @return token
     * @throws SignException
     */
    public String signIn(SignInDto signInDto) throws SignException {
        LOGGER.info("[signIn] 회원 정보 확인");
        Optional<Member> optionalMember = memberRepository.findByUid(signInDto.getId());
        if (optionalMember.isEmpty())
            throw new SignException(HttpStatus.BAD_REQUEST, "아이디가 존재하지 않거나 비밀번호가 일치하지 않습니다.");

        Member member = optionalMember.get();
        LOGGER.info("[signIn] Id : {}", signInDto.getId());

        LOGGER.info("[signIn] 패스워드 비교 수행");
        if(!passwordEncoder.matches(signInDto.getPassword(), member.getPassword()))
            throw new SignException(HttpStatus.BAD_REQUEST, "아이디가 존재하지 않거나 비밀번호가 일치하지 않습니다.");

        return makeToken(member);
    }

    public void signOut(String uid) throws SignException{
        LOGGER.info("[signOut] 회원 탈퇴 정보 확인");
        Optional<Member> optionalMember = memberRepository.findByUid(uid);

        if(optionalMember.isEmpty())
            throw new SignException(HttpStatus.BAD_REQUEST, "존재하지 않는 아이디입니다.");

        Member member = optionalMember.get();
        LOGGER.info("[getSignOutResult] userRepository 삭제");
        memberRepository.delete(member);
    }

    public String makeTokenByUid(String uid) throws SignException {
        Optional<Member> optionalMember = memberRepository.findByUid(uid);

        if(optionalMember.isEmpty())
            throw new SignException(HttpStatus.BAD_REQUEST, "존재하지 않는 아이디입니다.");

        return makeToken(optionalMember.get());
    }

    public String makeToken(Member member) throws SignException {
        if (member == null)
            throw new SignException(HttpStatus.BAD_REQUEST, "아이디가 존재하지 않거나 비밀번호가 일치하지 않습니다.");

        return jwtTokenProvider.createToken(String.valueOf(member.getUid()), member.getRoles());
    }

    private Member createMember(SignUpDto signUpDto) {
        Member member = Member.builder()
                        .uid(signUpDto.getId())
                        .userName(signUpDto.getName())
                        .password(passwordEncoder.encode(signUpDto.getPassword()))
                        .roles(Collections.singletonList("ROLE_USER"))
                        .build();
        return member;
    }
}
