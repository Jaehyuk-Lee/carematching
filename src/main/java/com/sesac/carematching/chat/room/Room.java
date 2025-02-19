package com.sesac.carematching.chat.room;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_room")
public class Room {
    @Id
    @Column(name = "CRNO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "REQUESTER_UNO", nullable = false)
    private User requester;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "RECEIVER_UNO", nullable = false)
    private Caregiver caregiver;

    @NotNull
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

}
