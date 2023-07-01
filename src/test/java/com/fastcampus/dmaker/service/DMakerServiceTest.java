package com.fastcampus.dmaker.service;

import com.fastcampus.dmaker.code.StatusCode;
import com.fastcampus.dmaker.dto.DeveloperDetailDto;
import com.fastcampus.dmaker.entity.Developer;
import com.fastcampus.dmaker.exception.DMakerException;
import com.fastcampus.dmaker.repository.DeveloperRepository;
import com.fastcampus.dmaker.repository.RetiredDeveloperRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.fastcampus.dmaker.dto.CreateDeveloper.*;
import static com.fastcampus.dmaker.exception.DMakerErrorCode.DUPLICATED_MEMBER_ID;
import static com.fastcampus.dmaker.type.DeveloperLevel.SENIOR;
import static com.fastcampus.dmaker.type.DeveloperSkillType.FRONT_END;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DMakerServiceTest {

    @Mock
    private DeveloperRepository developerRepository;

    @Mock
    private RetiredDeveloperRepository retiredDeveloperRepository;

    @InjectMocks
    private DMakerService dMakerService;

    private final Developer defaultDeveloper = Developer.builder()
            .developerLevel(SENIOR)
            .developerSkillType(FRONT_END)
            .experienceYears(12)
            .statusCode(StatusCode.EMPLOYED)
            .name("name")
            .age(30)
            .build();

    private final Request defaultCreateRequest = Request.builder()
            .developerLevel(SENIOR)
            .developerSkillType(FRONT_END)
            .experienceYears(12)
            .memberId("member1")
            .name("name")
            .age(30)
            .build();

    @Test
    public void test() {
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.of(defaultDeveloper));
        DeveloperDetailDto dto = dMakerService.getDeveloperDetail("memberId");
        assertEquals(SENIOR, dto.getDeveloperLevel());
        assertEquals(FRONT_END, dto.getDeveloperSkillType());
        assertEquals(12, dto.getExperienceYears());
    }

    @Test
    void createDeveloper_success() {
        Request request = defaultCreateRequest;
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Developer> captor =
                ArgumentCaptor.forClass(Developer.class);

        dMakerService.createDeveloper(request);

        verify(developerRepository, times(1))
                .save(captor.capture());
        Developer savedDeveloper = captor.getValue();
        assertEquals(SENIOR, savedDeveloper.getDeveloperLevel());
        assertEquals(FRONT_END, savedDeveloper.getDeveloperSkillType());
        assertEquals(12, savedDeveloper.getExperienceYears());
    }

    @Test
    void createDeveloper_failed_with_duplicated() {
        Request request = defaultCreateRequest;
        given(developerRepository.findByMemberId(anyString()))
                .willReturn(Optional.of(defaultDeveloper));

        DMakerException exception = assertThrows(DMakerException.class,
                () -> dMakerService.createDeveloper(request));
        assertEquals(DUPLICATED_MEMBER_ID, exception.getDMakerErrorCode());
    }
}