package com.youthguide.api.user.service;

import com.youthguide.api.global.exception.BusinessException;
import com.youthguide.api.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 인증번호 메일 발송.
 * spring.mail.host(환경변수 SPRING_MAIL_HOST)가 설정되면 실제 메일을 보내고,
 * 없으면 개발용으로 서버 콘솔에 인증번호를 출력한다.
 */
@Component
public class VerificationMailSender {

	private static final Logger log = LoggerFactory.getLogger(VerificationMailSender.class);

	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	private final String from;

	public VerificationMailSender(ObjectProvider<JavaMailSender> mailSenderProvider,
			@Value("${app.mail.from}") String from) {
		this.mailSenderProvider = mailSenderProvider;
		this.from = from;
	}

	public void sendVerificationCode(String to, String code) {
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (mailSender == null) {
			log.info("[개발용] 메일 서버 미설정. 이메일 인증번호 {} -> {}", maskEmail(to), code);
			return;
		}
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject("[청년 길잡이] 이메일 인증번호");
		message.setText("청년 길잡이 회원가입 인증번호는 [" + code + "] 입니다.\n"
				+ "5분 안에 회원가입 화면에 입력해 주세요.\n"
				+ "본인이 요청하지 않았다면 이 메일을 무시해 주세요.");
		try {
			mailSender.send(message);
		} catch (MailException e) {
			log.warn("인증 메일 발송 실패: {}", maskEmail(to), e);
			throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, e);
		}
	}

	/** 로그에 전체 이메일 주소가 남지 않게 앞 두 글자만 보인다. 예: ab***@example.com */
	static String maskEmail(String email) {
		int at = email.indexOf('@');
		if (at <= 0) {
			return "***";
		}
		String local = email.substring(0, at);
		String visible = local.length() <= 2 ? local.substring(0, 1) : local.substring(0, 2);
		return visible + "***" + email.substring(at);
	}
}
