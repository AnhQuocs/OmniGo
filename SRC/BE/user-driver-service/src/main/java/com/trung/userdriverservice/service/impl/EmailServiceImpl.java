package com.trung.userdriverservice.service.impl;

import com.trung.userdriverservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:trung8d2005@gmail.com}")
    private String fromEmail;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, int expiresInSeconds) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "OmniGo Ride & Food");
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực OTP OmniGo");

            String htmlContent = buildOtpEmailTemplate(otpCode, expiresInSeconds);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Đã gửi email chứa mã OTP thành công tới {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email OTP tới {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendPasswordChangedAlert(String toEmail, String fullName, LocalDateTime changeTime) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "OmniGo Security");
            helper.setTo(toEmail);
            helper.setSubject("Thông báo bảo mật: Mật khẩu tài khoản OmniGo của bạn đã được thay đổi");

            String timeStr = changeTime != null ? changeTime.format(FORMATTER) : LocalDateTime.now().format(FORMATTER);
            String htmlContent = buildPasswordChangedTemplate(fullName, timeStr);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Đã gửi email cảnh báo đổi mật khẩu thành công tới {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email cảnh báo đổi mật khẩu tới {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String buildOtpEmailTemplate(String otpCode, int expiresInSeconds) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f4f5f7; margin: 0; padding: 20px; color: #1e293b; }
                    .container { max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
                    .header { background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); padding: 30px 20px; text-align: center; color: #ffffff; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 0.5px; }
                    .content { padding: 32px 28px; text-align: center; }
                    .content p { font-size: 15px; line-height: 1.6; color: #475569; margin: 0 0 20px; }
                    .otp-box { background: #f0fdf4; border: 2px dashed #10b981; border-radius: 10px; padding: 18px 24px; font-size: 32px; font-weight: 800; letter-spacing: 6px; color: #047857; display: inline-block; margin: 10px 0 20px; }
                    .warning { font-size: 13px; color: #ef4444; font-weight: 600; margin-top: 15px; }
                    .footer { background: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>OmniGo Ride & Food</h1>
                    </div>
                    <div class="content">
                        <p>Xin chào quý khách,</p>
                        <p>Bạn đang thực hiện thao tác xác thực tài khoản trên ứng dụng OmniGo. Dưới đây là mã xác thực OTP của bạn:</p>
                        <div class="otp-box">%s</div>
                        <p class="warning">⚠️ Mã OTP này có hiệu lực trong vòng <strong>%d giây</strong>.</p>
                        <p style="font-size: 13px; color: #64748b;">Tuyệt đối không chia sẻ mã này với bất kỳ ai (kể cả nhân viên hỗ trợ OmniGo) để bảo vệ tài khoản của bạn.</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 OmniGo Ecosystem. Mọi quyền được bảo lưu.</p>
                        <p>Email này được gửi tự động, vui lòng không phản hồi.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(otpCode, expiresInSeconds);
    }

    private String buildPasswordChangedTemplate(String fullName, String timeStr) {
        String displayName = (fullName != null && !fullName.isBlank()) ? fullName : "Quý khách";
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f4f5f7; margin: 0; padding: 20px; color: #1e293b; }
                    .container { max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
                    .header { background: linear-gradient(135deg, #0284c7 0%%, #0369a1 100%%); padding: 30px 20px; text-align: center; color: #ffffff; }
                    .header h1 { margin: 0; font-size: 22px; font-weight: 700; }
                    .content { padding: 32px 28px; }
                    .content p { font-size: 15px; line-height: 1.6; color: #475569; margin: 0 0 16px; }
                    .alert-card { background: #eff6ff; border-left: 4px solid #0284c7; padding: 14px 18px; border-radius: 4px; margin: 16px 0; }
                    .alert-card p { margin: 0; font-size: 14px; color: #1e3a8a; }
                    .footer { background: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Cảnh Báo Bảo Mật OmniGo</h1>
                    </div>
                    <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Mật khẩu tài khoản OmniGo của bạn vừa được thay đổi thành công vào lúc:</p>
                        <div class="alert-card">
                            <p><strong>Thời gian:</strong> %s</p>
                            <p><strong>Thiết bị/Kênh:</strong> Ứng dụng di động OmniGo</p>
                        </div>
                        <p>Nếu bạn là người thực hiện thay đổi này, bạn có thể yên tâm bỏ qua email này.</p>
                        <p style="color: #ef4444; font-weight: 600;">Nếu BẠN KHÔNG thực hiện thay đổi này, tài khoản của bạn có thể đã bị truy cập trái phép. Vui lòng sử dụng tính năng "Quên mật khẩu" ngay lập tức hoặc liên hệ tổng đài hỗ trợ OmniGo.</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 OmniGo Ecosystem. Mọi quyền được bảo lưu.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(displayName, timeStr);
    }
}

