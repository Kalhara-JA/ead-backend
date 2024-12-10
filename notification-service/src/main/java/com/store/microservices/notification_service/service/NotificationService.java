package com.store.microservices.notification_service.service;

import com.store.microservices.order_service.event.OrderCancelEvent;
import com.store.microservices.order_service.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

/**
 * Service for handling notification events and sending email notifications.
 * Listens to Kafka topics for order-related events and sends appropriate emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    /**
     * Listener for the "order-placed" Kafka topic.
     * Sends an email notification when an order is placed.
     *
     * @param orderPlacedEvent the event containing order placement details
     */
    @KafkaListener(topics = "order-placed")
    public void listen(OrderPlacedEvent orderPlacedEvent) {
        log.info("Received message from order-placed topic: {}", orderPlacedEvent);
        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@gmail.com");
            messageHelper.setTo(orderPlacedEvent.getEmail());
            messageHelper.setSubject("Order Confirmation - Order Number " + orderPlacedEvent.getOrderNumber());
            messageHelper.setText(String.format("""
                    <html>
                        <body>
                            <h3>Dear %s %s, </h3>
                            <p>Your order with Order Number %s has been placed successfully.</p>
                            <p>Thank you for shopping with us!</p>
                        </body>
                    </html>
                    """, orderPlacedEvent.getFirstName(), orderPlacedEvent.getLastName(), orderPlacedEvent.getOrderNumber()), true);
        };

        try {
            javaMailSender.send(mimeMessagePreparator);
            log.info("Order placement notification email sent successfully.");
        } catch (MailException e) {
            log.error("Error while sending email for order placement", e);
            throw new RuntimeException("Error sending email to " + orderPlacedEvent.getEmail());
        }
    }

    /**
     * Listener for the "order-cancel" Kafka topic.
     * Sends an email notification when an order is canceled.
     *
     * @param orderCancelEvent the event containing order cancellation details
     */
    @KafkaListener(topics = "order-cancel")
    public void listen(OrderCancelEvent orderCancelEvent) {
        log.info("Received message from order-cancel topic: {}", orderCancelEvent);
        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@gmail.com");
            messageHelper.setTo(orderCancelEvent.getEmail());
            messageHelper.setSubject("Order Cancellation - Order Number " + orderCancelEvent.getOrderNumber());
            messageHelper.setText(String.format("""
                    <html>
                        <body>
                            <p>Your order with Order Number %s has been canceled successfully.</p>
                            <p>We hope to serve you again soon!</p>
                        </body>
                    </html>
                    """, orderCancelEvent.getOrderNumber()), true);
        };

        try {
            javaMailSender.send(mimeMessagePreparator);
            log.info("Order cancellation notification email sent successfully.");
        } catch (MailException e) {
            log.error("Error while sending email for order cancellation", e);
            throw new RuntimeException("Error sending email to " + orderCancelEvent.getEmail());
        }
    }
}
