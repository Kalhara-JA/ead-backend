package com.store.microservices.notification_service.service;

import com.store.microservices.order_service.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "order-placed")
    public void listen(OrderPlacedEvent orderPlacedEvent) {
        log.info("Got Message from order-placed topic {}", orderPlacedEvent);
        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@gmail.com");
            messageHelper.setTo(orderPlacedEvent.getEmail().toString());
            messageHelper.setSubject("Your Order with Order Number " + orderPlacedEvent.getOrderNumber() + " has been placed successfully");
            messageHelper.setText(String.format("""
                    <html>
                        <body>
                            <h3>Dear %s %s, </h3>
                            <p>Your Order with Order Number %s has been placed successfully</p>
                            <p>Thank you for shopping with us</p>
                        </body>
                    </html>
                    """, orderPlacedEvent.getFirstName().toString(), orderPlacedEvent.getLastName().toString(), orderPlacedEvent.getOrderNumber()), true);
        };

        try {
            javaMailSender.send(mimeMessagePreparator);
            log.info("Order Notification Email Sent Successfully");
        } catch (MailException e) {
            log.error("Error while sending email", e);
            throw new RuntimeException("Error while sending email to " + orderPlacedEvent.getEmail());
        }
    }
}
