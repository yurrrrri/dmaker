package com.fastcampus.dmaker.dto;

import com.fastcampus.dmaker.entity.Developer;
import com.fastcampus.dmaker.type.DeveloperLevel;
import com.fastcampus.dmaker.type.DeveloperSkillType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class EditDeveloper {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request {

        @NotNull
        private DeveloperLevel developerLevel;
        @NotNull
        private DeveloperSkillType developerSkillType;
        @NotNull
        @Max(20)
        private Integer experienceYears;

    }
}
