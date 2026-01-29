package com.digital.magazine.common.service;

import com.digital.magazine.common.enums.MailTargetType;
import com.digital.magazine.user.enums.AccountStatus;

public interface SendEmailService {

//	public void sendBulkMail(MailTargetType targetType, AccountStatus accountStatus, String subject, String content,
//			byte[] attachment, String fileName);

	public void sendBulkMail(MailTargetType targetType, AccountStatus accountStatus, String subject, String content,
			byte[] attachment, String fileName);

}
