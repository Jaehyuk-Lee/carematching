package com.sesac.carematching.transaction;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverService;
import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.dto.TransactionAddDTO;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    // 결제 가능 시간
    private final static long MAX_PAYMENT_TIME = 30;

    private final TransactionRepository transactionRepository;
    private final CaregiverService caregiverService;
    private final UserService userService;

    @Transactional
    public Transaction saveTransaction(TransactionAddDTO transactionAddDTO) {
        Transaction transaction = new Transaction();
        Caregiver caregiver = caregiverService.findById(transactionAddDTO.getCno());
        User user = userService.findById(transactionAddDTO.getUno());

        if (user == null) {
            throw new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다: " + transactionAddDTO.getUno());
        }
        if (caregiver == null) {
            throw new IllegalArgumentException("해당 ID의 요양사를 찾을 수 없습니다: " + transactionAddDTO.getCno());
        }

        transaction.setCno(caregiver);
        transaction.setUno(user);
        transaction.setPrice(caregiver.getSalary());
        transaction.setStatus(Status.PENDING);

        return transactionRepository.save(transaction);
    }

    public TransactionGetDTO getValidTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (transaction.getStatus() != Status.PENDING) {
            throw new IllegalStateException("결제 대기 중인 주문이 아닙니다.");
        }

        // 결제 가능 시간 확인
        long minutesDifference = java.time.Duration.between(
            transaction.getCreatedAt(),
            java.time.Instant.now()
        ).toMinutes();
        if (minutesDifference > MAX_PAYMENT_TIME) {
            throw new IllegalStateException("결제 가능 시간이 만료되었습니다. 새로운 결제를 시도해주세요.");
        }

        TransactionGetDTO transactionGetDTO = new TransactionGetDTO();
        transactionGetDTO.setCaregiverName(transaction.getCno().getRealName());
        transactionGetDTO.setUserName(transaction.getUno().getUsername());
        transactionGetDTO.setPrice(transaction.getPrice());
        return transactionGetDTO;
    }
}
