package com.billiards.store.model;

import java.time.LocalDateTime;

import com.billiards.store.common.base.BaseEntity;
import com.billiards.store.common.enumerated.SessionStatus;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "play_session")
@EntityListeners(AuditingEntityListener.class)
public class PlaySession extends BaseEntity {
	@Column(unique = true, nullable = false, updatable = false)
    private String sessionCode;
    
    private String tableCode;
    
    @CreatedDate
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus sessionStatus;
    
}
