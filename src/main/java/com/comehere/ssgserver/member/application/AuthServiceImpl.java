package com.comehere.ssgserver.member.application;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.comehere.ssgserver.common.security.CustomUserDetails;
import com.comehere.ssgserver.common.security.jwt.JWTUtil;
import com.comehere.ssgserver.member.domain.Agree;
import com.comehere.ssgserver.member.domain.Member;
import com.comehere.ssgserver.member.domain.Role;
import com.comehere.ssgserver.member.dto.SignInResponseDTO;
import com.comehere.ssgserver.member.infrastructure.AgreeRepository;
import com.comehere.ssgserver.member.infrastructure.MemberRepository;
import com.comehere.ssgserver.member.vo.JoinRequestVo;
import com.comehere.ssgserver.member.vo.SignInRequestVo;
import com.comehere.ssgserver.purchase.domain.Address;
import com.comehere.ssgserver.purchase.infrastructure.AddressRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	private final MemberRepository memberRepository;

	private final AddressRepository addressRepository;

	private final AgreeRepository agreeRepository;

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	private final AuthenticationManager authenticationManager;

	private final JWTUtil jwtUtil;

	private final long expiredMs = 1000 * 60 * 60; // 1시간

	@Override
	public void signUp(JoinRequestVo joinRequestVo) {

		validateDuplicateMember(joinRequestVo);
		Member member = this.createMember(joinRequestVo);
		log.info("member: {}", member);
	}

	@Override
	public SignInResponseDTO signIn(SignInRequestVo signInRequestVo) {

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
				signInRequestVo.getSignInId(), signInRequestVo.getPassword());

		// 인증 과정 수행
		Authentication authentication = authenticationManager.authenticate(authToken);
		// 인증 정보를 SecurityContext에 저장
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// 사용자 인증 정보 추출
		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		UUID userUuid = customUserDetails.getUserUuid();
		String username = customUserDetails.getUsername();
		String role = customUserDetails.getAuthorities().iterator().next().getAuthority();

		// JWT 토큰 생성
		String token = jwtUtil.createJwt(userUuid, role, expiredMs);

		// 인증 성공 응답 생성
		return SignInResponseDTO.builder()
				.uuid(userUuid)
				.name(username)
				.accessToken(token)
				.build();
	}

	private Member createMember(JoinRequestVo joinRequestVo) {

		UUID uuid = UUID.randomUUID();

		Member member = Member
				.builder()
				.signinId(joinRequestVo.getSigninId())
				.password(bCryptPasswordEncoder.encode(joinRequestVo.getPassword()))
				.name(joinRequestVo.getName())
				.phone(joinRequestVo.getPhone())
				.email(joinRequestVo.getEmail())
				.birthday(joinRequestVo.getBirthday())
				.gender(joinRequestVo.getGender())
				.uuid(uuid)
				.role(Role.valueOf("USER"))
				.build();

		Member savedMember = memberRepository.save(member);

		Address address = Address
				.builder()
				.member(member)
				.name(joinRequestVo.getName())
				.phone(joinRequestVo.getPhone())
				.zipcode(joinRequestVo.getAddressInfoVo().getZipcode())
				.address(joinRequestVo.getAddressInfoVo().getAddress())
				.detailAddress(joinRequestVo.getAddressInfoVo().getDetailAddress())
				.defaultAddress((boolean)true)
				.build();

		addressRepository.save(address); // 주소 저장

		Agree agree = Agree
				.builder()
				.email(joinRequestVo.getEmail())
				.ssgPointMktAgr1(joinRequestVo.getSsgPointAgreesVo().getSsgPointMktAgr1())
				.ssgPointMktAgr2(joinRequestVo.getSsgPointAgreesVo().getSsgPointMktAgr2())
				.ssgPointEmail(joinRequestVo.getSsgPointAgreesVo().getSsgPointEmail())
				.ssgPointSms(joinRequestVo.getSsgPointAgreesVo().getSsgPointSms())
				.ssgPointMail(joinRequestVo.getSsgPointAgreesVo().getSsgPointMail())
				.ssgPointCall(joinRequestVo.getSsgPointAgreesVo().getSsgPointCall())
				.ssgcomMktAgr1(joinRequestVo.getSsgcomAgreesVo().getSsgcomMktAgr1())
				.ssgcomEmail(joinRequestVo.getSsgcomAgreesVo().getSsgcomEmail())
				.ssgcomSms(joinRequestVo.getSsgcomAgreesVo().getSsgcomSms())
				.build();

		agreeRepository.save(agree); // 동의 저장

		log.info("member: {}", member);
		log.info("memberUUid : {}", uuid);

		return savedMember;
	}

	private void validateDuplicateMember(JoinRequestVo joinRequestVo) {
		memberRepository.findBySignInId(joinRequestVo.getSigninId())
				.ifPresent(m -> {
					throw new IllegalStateException("이미 존재하는 회원입니다.");
				});
	}
}
