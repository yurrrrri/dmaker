package com.fastcampus.dmaker.service;

import com.fastcampus.dmaker.code.StatusCode;
import com.fastcampus.dmaker.dto.DeveloperDetailDto;
import com.fastcampus.dmaker.entity.Developer;
import com.fastcampus.dmaker.exception.DMakerException;
import com.fastcampus.dmaker.repository.DeveloperRepository;
import com.fastcampus.dmaker.type.DeveloperLevel;
import com.fastcampus.dmaker.type.DeveloperSkillType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.fastcampus.dmaker.constant.DMakerConstant.MAX_JUNIOR_EXPERIENCE_YEARS;
import static com.fastcampus.dmaker.constant.DMakerConstant.MIN_SENIOR_EXPERIENCE_YEARS;
import static com.fastcampus.dmaker.dto.CreateDeveloper.*;
import static com.fastcampus.dmaker.exception.DMakerErrorCode.DUPLICATED_MEMBER_ID;
import static com.fastcampus.dmaker.exception.DMakerErrorCode.LEVEL_EXPERIENCE_YEARS_NOT_MATCHED;
import static com.fastcampus.dmaker.type.DeveloperLevel.*;
import static com.fastcampus.dmaker.type.DeveloperSkillType.FRONT_END;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DMakerServiceTest {

    @Mock
    private DeveloperRepository developerRepository;

    @InjectMocks
    private DMakerService dMakerService;

    private final Developer defaultDeveloper = Developer.builder()
            .developerLevel(SENIOR)
            .developerSkillType(FRONT_END)
            .experienceYears(MIN_SENIOR_EXPERIENCE_YEARS)
            .statusCode(StatusCode.EMPLOYED)
            .name("name")
            .age(30)
            .build();

    private Request getCreateRequest(DeveloperLevel level, DeveloperSkillType skillType, Integer experienceYears) {
        return Request.builder()
                .developerLevel(level)
                .developerSkillType(skillType)
                .experienceYears(experienceYears)
                .memberId("member1")
                .name("name")
                .age(30)
                .build();
    }

    @Test
    void test() {
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.of(defaultDeveloper));
        DeveloperDetailDto dto = dMakerService.getDeveloperDetail("memberId");
        assertEquals(SENIOR, dto.getDeveloperLevel());
        assertEquals(FRONT_END, dto.getDeveloperSkillType());
        assertEquals(MIN_SENIOR_EXPERIENCE_YEARS, dto.getExperienceYears());
    }

    @Test
    void createDeveloper_success() {
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.empty());
        given(developerRepository.save(any()))
                .willReturn(defaultDeveloper);

        ArgumentCaptor<Developer> captor =
                ArgumentCaptor.forClass(Developer.class);

        dMakerService.createDeveloper(getCreateRequest(SENIOR, FRONT_END, MIN_SENIOR_EXPERIENCE_YEARS));

        verify(developerRepository, times(1))
                .save(captor.capture());
        Developer savedDeveloper = captor.getValue();
        assertEquals(SENIOR, savedDeveloper.getDeveloperLevel());
        assertEquals(FRONT_END, savedDeveloper.getDeveloperSkillType());
        assertEquals(MIN_SENIOR_EXPERIENCE_YEARS, savedDeveloper.getExperienceYears());
    }

    @Test
    void createDeveloper_failed_with_duplicated() {
        Request request = getCreateRequest(SENIOR, FRONT_END, MIN_SENIOR_EXPERIENCE_YEARS);
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.of(defaultDeveloper));

        DMakerException exception = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(request));
        assertEquals(DUPLICATED_MEMBER_ID, exception.getDMakerErrorCode());
    }

    @Test
    void createDeveloper_fail_with_unmatched_level() {
        DMakerException exception = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(JUNIOR, FRONT_END, MAX_JUNIOR_EXPERIENCE_YEARS + 1))
        );
        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED, exception.getDMakerErrorCode());

        exception = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(
                        getCreateRequest(SENIOR, FRONT_END, MIN_SENIOR_EXPERIENCE_YEARS - 2))
        );
        assertEquals(LEVEL_EXPERIENCE_YEARS_NOT_MATCHED, exception.getDMakerErrorCode());
    }
}