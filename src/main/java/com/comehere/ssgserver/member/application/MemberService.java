package com.comehere.ssgserver.member.application;

public interface MemberService {

	public boolean checkUserSignInIdDuplication(String signInId);

	public boolean checkUserEmailDuplication(String email);
}
