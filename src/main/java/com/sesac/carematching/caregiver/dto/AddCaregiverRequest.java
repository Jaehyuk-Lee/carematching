package com.sesac.carematching.caregiver.dto;

import com.sesac.carematching.caregiver.*;
import com.sesac.carematching.user.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddCaregiverRequest {
    private User user;
    private String loc;
    private String servNeeded;
    private Byte workDays;
    private WorkTime workTime;
    private WorkForm workForm;
    private EmploymentType employmentType;
    private Integer salary;
    private Status status;

    public Caregiver toEntity() {
        return Caregiver.builder()
            .user(user)
            .loc(loc)
            .servNeeded(servNeeded)
            .workDays(workDays)
            .workTime(workTime)
            .workForm(workForm)
            .employmentType(employmentType)
            .salary(salary)
            .status(status)
            .build();
    }
}
