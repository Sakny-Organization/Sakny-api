package com.sakny.common.dto;

import com.sakny.common.model.*;
import lombok.Data;

@Data
public class RoommateFilterRequest {
    private Gender gender;
    private Integer minBudget;
    private Integer maxBudget;
    private SmokingStatus smoking;
    private PetStatus pets;
    private SleepSchedule sleepSchedule;
    private RoommateType roommateType;
}
