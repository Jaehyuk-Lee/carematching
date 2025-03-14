package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.BuildCaregiverDto;
import com.sesac.carematching.caregiver.dto.CaregiverListDto;
import com.sesac.carematching.caregiver.experience.ExperienceRepository;
import com.sesac.carematching.caregiver.review.ReviewRepository;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import com.sesac.carematching.util.S3UploadService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CaregiverService {
    private final CaregiverRepository caregiverRepository;
    private final UserRepository userRepository;
    private final S3UploadService s3UploadService;
    private final ReviewRepository reviewRepository;

    @Transactional
    public Caregiver save(String username, BuildCaregiverDto dto) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("User must not be null"));
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and User ID must not be null");
        }
        Caregiver caregiver = caregiverRepository.save(toEntity(dto, user));

        return caregiver;
    }

    public Caregiver findById(Integer id) {
        return caregiverRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Caregiver not found with id: " + id));
    }

    public Optional<Integer> findUserIdByCaregiverId(Integer caregiverId) {
        return caregiverRepository.findById(caregiverId)
            .map(caregiver -> caregiver.getUser().getId());  // Caregiver의 User ID 반환
    }

    public Caregiver findByUserId(Integer userId) {
        return caregiverRepository.findByUserId(userId).orElseThrow(()-> new EntityNotFoundException("Caregiver not found with id: " + userId));
    }

    public Caregiver findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(()->new IllegalArgumentException("User is null"));
        Caregiver caregiver = caregiverRepository.findByUser(user)
            .orElse(null);
        return caregiver;
    }

    @Transactional
    public Caregiver update(String username, BuildCaregiverDto dto) {
        Caregiver caregiver = findByUsername(username);
        caregiver.setRealName(dto.getRealName());
        caregiver.setLoc(dto.getLoc());
        caregiver.setServNeeded(dto.getServNeeded());
        caregiver.setWorkDays(dto.getWorkDays());
        caregiver.setWorkTime(dto.getWorkTime());
        caregiver.setWorkForm(dto.getWorkForm());
        caregiver.setEmploymentType(dto.getEmploymentType());
        caregiver.setSalary(dto.getSalary());
        caregiver.setStatus(dto.getStatus());

        // 새 이미지가 제공된 경우: 기존 이미지가 있다면 S3에서 삭제 후 새 URL로 교체
        if (dto.getCaregiverImage() != null && !dto.getCaregiverImage().isEmpty()) {
            if (caregiver.getCaregiverImage() != null && !caregiver.getCaregiverImage().isEmpty()) {
                s3UploadService.deleteCaregiverImageFile(caregiver.getCaregiverImage());
            }
            caregiver.setCaregiverImage(dto.getCaregiverImage());
        }
        return caregiverRepository.save(caregiver);
    }

    public List<Caregiver> findAllOpenCaregiver() {
        return caregiverRepository.findByStatusAndRoleName(Status.OPEN, "ROLE_USER_CAREGIVER");
    }

    public boolean isCaregiverRegistered(String username) {
        User user = userRepository.findByUsername(username)
            .orElse(null);
        if (user == null) {
            return false;
        }
        return caregiverRepository.existsByUser(user);
    }

    private Caregiver toEntity(BuildCaregiverDto dto, User user) {
        return Caregiver.builder()
            .user(user)
            .loc(dto.getLoc())
            .realName(dto.getRealName())
            .servNeeded(dto.getServNeeded())
            .workDays(dto.getWorkDays())
            .workTime(dto.getWorkTime())
            .workForm(dto.getWorkForm())
            .employmentType(dto.getEmploymentType())
            .salary(dto.getSalary())
            .status(dto.getStatus())
            .caregiverImage(dto.getCaregiverImage())
            .build();
    }
}
