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
 * Service responsible for handling notification events related to order placement and cancellation.
 * Listens to Kafka topics and sends email notifications to customers regarding their order status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    /**
     * Listens for order placement events from the "order-placed" topic, and sends a confirmation email to the customer.
     *
     * @param orderPlacedEvent the event object containing order placement details
     */
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

    /**
     * Listens for order cancellation events from the "order-cancel" topic, and sends a cancellation confirmation email to the customer.
     *
     * @param orderCancelEvent the event object containing order cancellation details
     */
    @KafkaListener(topics = "order-cancel")
    public void listen(OrderCancelEvent orderCancelEvent) {
        log.info("Got Message from order-cancel topic {}", orderCancelEvent);
        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@gmail.com");
            messageHelper.setTo(orderCancelEvent.getEmail().toString());
            messageHelper.setSubject("Your Order with Order Number " + orderCancelEvent.getOrderNumber() + " has been cancel successfully");
            messageHelper.setText(String.format("""
                    <html>
                        <body>
                            <p>Your Order with Order Number %s has been cancel successfully</p>
                            <p>Thank you for shopping with us</p>
                        </body>
                    </html>
                    """, orderCancelEvent.getOrderNumber()), true);
        };

        try {
            javaMailSender.send(mimeMessagePreparator);
            log.info("Order Notification Email Sent Successfully");
        } catch (MailException e) {
            log.error("Error while sending email", e);
            throw new RuntimeException("Error while sending email to " + orderCancelEvent.getEmail());
        }
    }
}
