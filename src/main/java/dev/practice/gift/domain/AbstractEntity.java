package dev.practice.gift.domain;

import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.ZonedDateTime;

/**
 * MappedSuperclass : 공통매핑정보가 필요할 때 사용한다.
 * cratedAt와 updatedAt는 많은객체에서 공통적으로 사용하는 정보이기때문에,
 * 슈퍼클래스로 만들고 하위객체에서 상속받아 사용하도록한다.
 *
 * EntityListeners :
 * Audit은 Spring Data JPA에서 시간에 대해서 자동으로 값을 넣어주는 기능
 * Audit을 이용하면 업데이트시 자동으로 시간을 매핑하여 데이터베이스에 넣어줌
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractEntity {

    // 엔티티가 생성되어 저장될때, 시간이 자동 저장됨
    @CreationTimestamp
    private ZonedDateTime createdAt;

    // 조회한 Entity가 값을 변경할 때 시간이 자동 저장됨
    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
