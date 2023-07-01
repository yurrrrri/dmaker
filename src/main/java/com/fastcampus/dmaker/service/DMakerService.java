package com.fastcampus.dmaker.service;

import com.fastcampus.dmaker.code.StatusCode;
import com.fastcampus.dmaker.dto.CreateDeveloper;
import com.fastcampus.dmaker.dto.DeveloperDetailDto;
import com.fastcampus.dmaker.dto.DeveloperDto;
import com.fastcampus.dmaker.dto.EditDeveloper;
import com.fastcampus.dmaker.entity.Developer;
import com.fastcampus.dmaker.entity.RetiredDeveloper;
import com.fastcampus.dmaker.exception.DMakerException;
import com.fastcampus.dmaker.repository.DeveloperRepository;
import com.fastcampus.dmaker.repository.RetiredDeveloperRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fastcampus.dmaker.exception.DMakerErrorCode.DUPLICATED_MEMBER_ID;
import static com.fastcampus.dmaker.exception.DMakerErrorCode.NO_DEVELOPER;

@RequiredArgsConstructor
@Service
public class DMakerService {

    private final DeveloperRepository developerRepository;
    private final RetiredDeveloperRepository retiredDeveloperRepository;

    @Transactional
    public CreateDeveloper.Response createDeveloper(CreateDeveloper.Request request) {
        validateCreateDeveloperRequest(request);
        return CreateDeveloper.Response.fromEntity(
                developerRepository.save(createDeveloperFromRequest(request))
        );
    }

    private Developer createDeveloperFromRequest(CreateDeveloper.Request request) {
        return Developer.builder()
                .developerLevel(request.getDeveloperLevel())
                .developerSkillType(request.getDeveloperSkillType())
                .experienceYears(request.getExperienceYears())
                .memberId(request.getMemberId())
                .statusCode(StatusCode.EMPLOYED)
                .name(request.getName())
                .age(request.getAge())
                .build();
    }

    private void validateCreateDeveloperRequest(@NonNull CreateDeveloper.Request request) {
        request.getDeveloperLevel().validateExperienceYears(request.getExperienceYears());

        developerRepository.findByMemberId(request.getMemberId())
                .ifPresent(developer -> {
                    throw new DMakerException(DUPLICATED_MEMBER_ID);
                });
    }

    @Transactional(readOnly = true)
    public List<DeveloperDto> getAllEmployedDevelopers() {
        return developerRepository.findByStatusCodeEquals(StatusCode.EMPLOYED)
                .stream().map(DeveloperDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeveloperDetailDto getDeveloperDetail(String memberId) {
        return DeveloperDetailDto.fromEntity(getDeveloperByMemberId(memberId));
    }

    @Transactional
    public DeveloperDetailDto editDeveloper(String memberId, EditDeveloper.Request request) {
        request.getDeveloperLevel().validateExperienceYears(request.getExperienceYears());
        return DeveloperDetailDto.fromEntity(
                getUpdatedDeveloperFromRequest(request, getDeveloperByMemberId(memberId))
        );
    }

    private Developer getUpdatedDeveloperFromRequest(EditDeveloper.Request request, Developer developer) {
        developer.setDeveloperLevel(request.getDeveloperLevel());
        developer.setDeveloperSkillType(request.getDeveloperSkillType());
        developer.setExperienceYears(request.getExperienceYears());

        return developer;
    }

    private Developer getDeveloperByMemberId(String memberId) {
        return developerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DMakerException(NO_DEVELOPER));
    }

    @Transactional
    public DeveloperDetailDto deleteDeveloper(String memberId) {
        Developer developer = developerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DMakerException(NO_DEVELOPER));
        developer.setStatusCode(StatusCode.RETIRED);

        RetiredDeveloper retiredDeveloper = RetiredDeveloper.builder()
                .memberId(memberId)
                .name(developer.getName())
                .build();
        retiredDeveloperRepository.save(retiredDeveloper);
        return DeveloperDetailDto.fromEntity(developer);
    }
}
