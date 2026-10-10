package com.youthguide.api.user.validation;

import com.youthguide.api.user.domain.MemberInputPolicy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return MemberInputPolicy.isValidPassword(value);
	}
}
