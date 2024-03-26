package com.comehere.ssgserver.member.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsgPointAgreesVo {

	private Boolean ssgPointMktAgr1;

	private Boolean ssgPointMktAgr2;

	private Boolean ssgPointEmail;

	private Boolean ssgPointSms;

	private Boolean ssgPointMail;

	private Boolean ssgPointCall;
}
