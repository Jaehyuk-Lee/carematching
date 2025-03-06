package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.CaregiverListDto;
import com.sesac.carematching.caregiver.dto.CaregiverDetailDto;
import com.sesac.carematching.caregiver.dto.BuildCaregiverDto;
import com.sesac.carematching.experience.Experience;
import com.sesac.carematching.experience.ExperienceRequest;
import com.sesac.carematching.experience.ExperienceService;
import com.sesac.carematching.user.role.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sesac.carematching.util.TokenAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caregivers")
public class CaregiverController {
    private final CaregiverService caregiverService;
    private final RoleService roleService;
    private final ExperienceService experienceService;
    private final TokenAuth tokenAuth;

    @GetMapping
    public ResponseEntity<List<CaregiverListDto>> CaregiverList() {
        List<CaregiverListDto> caregivers = caregiverService.findALlOpenCaregiver()
            .stream()
            .map(CaregiverListDto::new)
            .toList();
        return ResponseEntity.ok()
            .body(caregivers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaregiverDetailDto> findCaregiverById(@PathVariable Integer id) {
        Caregiver caregiver = caregiverService.findById(id);
        List<Experience> experiences = experienceService.findExperienceList(caregiver);

        return ResponseEntity.ok()
            .body(new CaregiverDetailDto(caregiver, experiences));
    }

    @GetMapping("/user")
    public ResponseEntity<CaregiverDetailDto> findCaregiverByUser(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Caregiver caregiver = caregiverService.findByUsername(username);
        List<Experience> experiences = experienceService.findExperienceList(caregiver);

        return ResponseEntity.ok()
            .body(new CaregiverDetailDto(caregiver, experiences));
    }

    @PostMapping("/build")
    public ResponseEntity<CaregiverDetailDto> buildCaregiver(HttpServletRequest request,
                                                             @RequestBody BuildCaregiverDto dto) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Caregiver caregiver = updateOrCreateCaregiver(username, dto);
        List<Experience> experiences = processExperienceList(caregiver, dto.getExperienceRequestList());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CaregiverDetailDto(caregiver, experiences));
    }

    private Caregiver updateOrCreateCaregiver(String username, BuildCaregiverDto dto) {
        return Optional.ofNullable(caregiverService.findByUsername(username))
            .map(existingCaregiver -> caregiverService.update(username, dto))
            .orElseGet(() -> caregiverService.save(username, dto));
    }

    private List<Experience> processExperienceList(Caregiver caregiver, List<ExperienceRequest> experienceRequestList) {
        if (experienceRequestList == null || experienceRequestList.isEmpty()) return null;

        List<Experience> existingExperiences = caregiver.getExperienceList();
        int existingSize = existingExperiences.size();
        int newSize = experienceRequestList.size();

        // 기존 경험 업데이트
        IntStream.range(0, Math.min(existingSize, newSize))
            .forEach(i -> experienceService.update(experienceRequestList.get(i), existingExperiences.get(i).getId()));

        // 추가 경험 저장 (새로운 데이터가 더 많은 경우)
        List<Experience> newExperiences = experienceRequestList.subList(existingSize, newSize).stream()
            .map(ex -> experienceService.save(ex, caregiver))
            .collect(Collectors.toList());

        // 기존 경험 + 새로 추가된 경험 리스트 반환
        List<Experience> allExperiences = new ArrayList<>(existingExperiences);
        allExperiences.addAll(newExperiences);

        return allExperiences;
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkCaregiver(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        boolean isRegistered = caregiverService.isCaregiverRegistered(username);
        return ResponseEntity.ok(isRegistered);
    }
}
