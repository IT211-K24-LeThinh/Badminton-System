package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

//    @Value("${spring.mail.password}")
//    private String pass;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
//            log.info("aaaaaaaaaaaaaaaaa {}", pass);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Đặt lại mật khẩu - Badminton System");

            String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;

            String htmlContent = """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f6fbf7;">
            <div style="background: #e8f5e9; padding: 28px; border-radius: 10px 10px 0 0; border: 1px solid #d6ead8;">
                <h1 style="color: #2e7d32; margin: 0; text-align: center; font-size: 24px;">
                    Badminton System
                </h1>
            </div>

            <div style="background: #ffffff; padding: 30px; border: 1px solid #d6ead8; border-top: none; border-radius: 0 0 10px 10px;">
                <h2 style="color: #1f2937; margin-top: 0; font-size: 22px;">
                    Yêu cầu đặt lại mật khẩu
                </h2>

                <p style="color: #4b5563; line-height: 1.6; font-size: 15px;">
                    Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình.
                    Vui lòng nhấp vào nút bên dưới để đặt lại mật khẩu:
                </p>

                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background: #43a047; color: #ffffff; padding: 14px 40px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 15px;">
                        Đặt lại mật khẩu
                    </a>
                </div>

                <p style="color: #4b5563; line-height: 1.6; font-size: 15px;">
                    Hoặc sao chép link này vào trình duyệt:<br>
                    <a href="%s" style="color: #2e7d32; word-break: break-all;">%s</a>
                </p>

                <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">

                <p style="color: #6b7280; font-size: 12px; line-height: 1.5; margin-bottom: 0;">
                    Link này sẽ hết hạn sau 1 giờ. Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                </p>
            </div>
        </div>
        """.formatted(resetLink, resetLink, resetLink);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Password reset email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Chào mừng đến với Badminton System!");

            String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; border-radius: 10px 10px 0 0;">
                        <h1 style="color: white; margin: 0; text-align: center;">🎉 Chào mừng!</h1>
                    </div>
                    <div style="background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 10px 10px;">
                        <h2 style="color: #333;">Xin chào %s!</h2>
                        <p style="color: #666; line-height: 1.6;">
                            Tài khoản của bạn đã được tạo thành công tại <strong>Badminton System</strong>.
                            Bạn có thể bắt đầu đặt sân cầu lông ngay bây giờ!
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <p style="color: #666; line-height: 1.6;">
                                Truy cập ngay để khám phá các sân cầu lông gần bạn:
                            </p>
                            <a href="http://localhost:8080" style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 14px 40px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                                Khám phá ngay
                            </a>
                        </div>
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">
                            Nếu bạn không tạo tài khoản này, vui lòng liên hệ với chúng tôi.
                        </p>
                    </div>
                </div>
                """.formatted(username);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Welcome email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }
}
