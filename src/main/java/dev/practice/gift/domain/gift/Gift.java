package dev.practice.gift.domain.gift;

import common.exception.IllegalStatusException;
import common.exception.InvalidParamException;
import common.util.TokenGenerator;
import dev.practice.gift.domain.AbstractEntity;
import lombok.*;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.ZonedDateTime;

/**
 * @Entity:  JPA가 관리하는 클래스
 * @Table : 엔티티와 매핑할 테이블을 지정
 */
@Getter
@Entity
@ToString
@NoArgsConstructor
@Table(name="gifts")
public class Gift extends AbstractEntity {

    private static final String GIFT_PREFIX = "gt_";

    /**
     * @GeneratedValue : 기본키 자동 생성 전략
     * 기본키 생성을 데이터베이스에 위임 ( =AUtoIncrement)
     * @Id : 엔티티 객체의 식별자로 사용할 필드에 적용. 유니크한 컬럼에 매핑
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String giftToken;
    private Long buyerUserId;
    private String orderToken;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private PushType pushType;
    private String giftReceiverName;
    private String giftReceiverPhone;
    private String giftMessage;

    private String receiverName;
    private String receiverPhone;
    private String receiverZipcode;
    private String receiverAddress1;
    private String receiverAddress2;
    private String etcMessage;

    private ZonedDateTime paidAt;
    private ZonedDateTime pushedAt;
    private ZonedDateTime acceptedAt;
    private ZonedDateTime expiredAt;

    @Getter
    @AllArgsConstructor
    public enum Status {
        INIT("선물 주문 생성"),
        IN_PAYMENT("결제 중"),
        ORDER_COMPLETE("주문 완료"),
        PUSH_COMPLETE("선물 링크 발송 완료"),
        ACCEPT("선물 수락"),
        DELIVERY_PREPARE("상품준비"),
        IN_DELIVERY("배송중"),
        DELIVERY_COMPLETE("배송완료"),
        EXPIRATION("선물 수락 만료");

        private final String description;
    }

    @Getter
    @AllArgsConstructor
    public enum PushType {
        KAKAO("카카오톡"),
        LMS("문자");

        private final String description;
    }

    @Builder
    public Gift(
            Long buyerUserId,
            String orderToken,
            PushType pushType,
            String giftReceiverName,
            String giftReceiverPhone,
            String giftMessage
    ){
        if (buyerUserId == null) throw new InvalidParamException("Gift constructor buyerUserId is null");
        if (pushType == null) throw new InvalidParamException("Gift constructor pushType is null");
        if (StringUtils.isEmpty(giftReceiverName)) throw new InvalidParamException("Gift constructor giftReceiverName is empty");
        if (StringUtils.isEmpty(giftReceiverPhone)) throw new InvalidParamException("Gift constructor giftReceiverPhone is empty");
        if (StringUtils.isEmpty(giftMessage)) throw new InvalidParamException("Gift constructor giftMessage is empty");

        this.giftToken = TokenGenerator.randomCharacterWithPrefix(GIFT_PREFIX);
        this.buyerUserId = buyerUserId;
        this.orderToken = orderToken;
        this.status = Status.INIT;
        this.pushType = pushType;
        this.giftReceiverName = giftReceiverName;
        this.giftReceiverPhone = giftReceiverPhone;
        this.giftMessage = giftMessage;
        this.expiredAt = ZonedDateTime.now().plusDays(7);
    }

    public void inPayment(){
        if(this.status != Status.INIT) throw new IllegalStatusException("Gift paymentComplete");
        this.status = Status.IN_PAYMENT;
    }

    public void completePayment() {
        if(this.status != Status.IN_PAYMENT) throw new IllegalStatusException("Gift pushLink");
        this.status = Status.PUSH_COMPLETE;
        this.paidAt = ZonedDateTime.now();
    }

    public void accept(GiftCommand.Accept request) {
        var receiverName = request.getReceiverName();
        var receiverPhone = request.getReceiverPhone();
        var receiverZipcode = request.getReceiverZipcode();
        var receiverAddress1 = request.getReceiverAddress1();
        var receiverAddress2 = request.getReceiverAddress2();
        var etcMessage = request.getEtcMessage();

        if (!availableAccept()) throw new IllegalStatusException();
        if (StringUtils.isEmpty(receiverName)) throw new InvalidParamException("Gift accept receiverName is empty");
        if (StringUtils.isEmpty(receiverPhone)) throw new InvalidParamException("Gift accept receiverPhone is empty");
        if (StringUtils.isEmpty(receiverZipcode)) throw new InvalidParamException("Gift accept receiverZipcode is empty");
        if (StringUtils.isEmpty(receiverAddress1)) throw new InvalidParamException("Gift accept receiverAddress1 is empty");
        if (StringUtils.isEmpty(receiverAddress2)) throw new InvalidParamException("Gift accept receiverAddress2 is empty");
        if (StringUtils.isEmpty(etcMessage)) throw new InvalidParamException("Gift accept etcMessage is empty");

        this.status = Status.ACCEPT;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverZipcode = receiverZipcode;
        this.receiverAddress1 = receiverAddress1;
        this.receiverAddress2 = receiverAddress2;
        this.etcMessage = etcMessage;
        this.acceptedAt = ZonedDateTime.now();
    }

    public void expired() {
        this.status = Status.EXPIRATION;
        this.expiredAt = ZonedDateTime.now();
    }

    private boolean availableAccept() {
        if (this.expiredAt.isBefore(ZonedDateTime.now())) return false;
        return this.status == Status.ORDER_COMPLETE || this.status == Status.PUSH_COMPLETE;
    }
}

