package net.bilgecan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bilgecan.dto.AITaskDto;
import net.bilgecan.dto.MembershipDto;
import net.bilgecan.entity.AITaskTemplate;
import net.bilgecan.entity.Permission;
import net.bilgecan.entity.security.User;
import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.repository.AITaskTemplateRepository;
import net.bilgecan.repository.WorkspaceRepository;
import net.bilgecan.util.WorkspacePermissions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Transactional(rollbackFor = Exception.class)
@Service
public class AITaskTemplateService {

    private AITaskTemplateRepository aiTaskTemplateRepository;

    private SecurityService securityService;
    private WorkspaceRepository workspaceRepository;
    private MembershipService membershipService;
    private TranslationService translationService;

    public AITaskTemplateService(AITaskTemplateRepository aiTaskTemplateRepository, SecurityService securityService,
                                 WorkspaceRepository workspaceRepository, MembershipService membershipService, TranslationService translationService) {
        this.aiTaskTemplateRepository = aiTaskTemplateRepository;
        this.securityService = securityService;
        this.workspaceRepository = workspaceRepository;
        this.membershipService = membershipService;
        this.translationService = translationService;
    }

    public Long saveAITask(AITaskDto aiTaskDto) {

        User currentUser = securityService.getCurrentUser();

        AITaskTemplate alreadySavedOne = aiTaskTemplateRepository.findByOwnerAndNameIgnoreCaseAndWorkspaceIsNull(currentUser, aiTaskDto.getName());
        // new AITask creation
        if (aiTaskDto.getId() == null) {
            if (alreadySavedOne != null) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        } else {
            // AITask update
            if (alreadySavedOne != null && !alreadySavedOne.getId().equals(aiTaskDto.getId())) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        }

        validateJsonSchema(aiTaskDto.getJsonSchema());

        AITaskTemplate entity = mapToEntity(aiTaskDto);
        entity.setOwner(currentUser);
        AITaskTemplate saved = aiTaskTemplateRepository.save(entity);
        return saved.getId();
    }

    public Long saveWorkspaceAITask(AITaskDto aiTaskDto, Long workspaceId) {

        User currentUser = securityService.getCurrentUser();
        MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(workspaceId);
        if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.EDIT)) {
            throw new AppLevelValidationException(translationService.t("general.noPermission"));
        }

        AITaskTemplate alreadySavedOne = aiTaskTemplateRepository.findByWorkspaceIdAndNameIgnoreCase(workspaceId, aiTaskDto.getName());
        // new AITask creation
        if (aiTaskDto.getId() == null) {
            if (alreadySavedOne != null) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        } else {
            // AITask update
            if (alreadySavedOne != null && !alreadySavedOne.getId().equals(aiTaskDto.getId())) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        }

        validateJsonSchema(aiTaskDto.getJsonSchema());

        AITaskTemplate entity = mapToEntity(aiTaskDto);
        entity.setOwner(currentUser);
        entity.setWorkspace(workspaceRepository.findById(workspaceId).get());
        AITaskTemplate saved = aiTaskTemplateRepository.save(entity);
        return saved.getId();
    }

    private void validateJsonSchema(String jsonSchema) {
        if (StringUtils.isBlank(jsonSchema)) {
            return;
        }
        try {
            new ObjectMapper().readValue(jsonSchema, Map.class);
        } catch (Exception e) {
            throw new AppLevelValidationException("JsonSchema is not valid!");
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<AITaskDto> findPaginated(Pageable pageable, String searchTerm) {
        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(aiTaskTemplateRepository.findByOwnerAndWorkspaceIsNull((User) authenticatedUser, pageable));
        }

        return mapToDto(aiTaskTemplateRepository.findByNameContainingIgnoreCaseAndOwnerAndWorkspaceIsNull(searchTerm, (User) authenticatedUser, pageable));
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<AITaskDto> findInWorkspacePaginated(Pageable pageable, String searchTerm, Long workspaceId) {

        MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(workspaceId);
        if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.VIEW)) {
            throw new AppLevelValidationException(translationService.t("general.noPermission"));
        }

        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(aiTaskTemplateRepository.findByWorkspaceId(workspaceId, pageable));
        }

        return mapToDto(aiTaskTemplateRepository.findByNameContainingIgnoreCaseAndWorkspaceId(searchTerm, workspaceId, pageable));
    }

    public void delete(AITaskDto aiTask) {
        User currentUser = securityService.getCurrentUser();
        long deletedCount = aiTaskTemplateRepository.deleteByIdAndOwnerAndWorkspaceIsNull(aiTask.getId(), currentUser);
        if (deletedCount <= 0) {
            throw new AppLevelValidationException("AITask cannot be found");
        }
    }

    public void deleteInWorkspace(AITaskDto aiTask, Long workspaceId) {
        MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(workspaceId);
        if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.EDIT)) {
            throw new AppLevelValidationException(translationService.t("general.noPermission"));
        }

        long deletedCount = aiTaskTemplateRepository.deleteByIdAndWorkspaceId(aiTask.getId(), workspaceId);
        if (deletedCount <= 0) {
            throw new AppLevelValidationException("AITask cannot be found");
        }
    }

    private Page<AITaskDto> mapToDto(Page<AITaskTemplate> AITaskPage) {
        return AITaskPage.map(AITaskTemplate -> toDto(AITaskTemplate));
    }

    private AITaskDto toDto(AITaskTemplate aiTaskTemplate) {
        if (aiTaskTemplate == null) {
            return null;
        }
        AITaskDto dto = new AITaskDto();
        dto.setId(aiTaskTemplate.getId());
        dto.setName(aiTaskTemplate.getName());
        dto.setPrompt(aiTaskTemplate.getPrompt());
        dto.setSystemPrompt(aiTaskTemplate.getSystemPrompt());
        dto.setModelRoute(aiTaskTemplate.getModelRoute());
        dto.setOutputVarName(aiTaskTemplate.getOutputVarName());
        dto.setJsonSchema(aiTaskTemplate.getJsonSchema());
        dto.setInputFileMimeType(aiTaskTemplate.getInputFileMimeType());
        dto.setInputFilePath(aiTaskTemplate.getInputFilePath());
        dto.setEnableRag(aiTaskTemplate.isEnableRag());
        return dto;
    }

    private AITaskTemplate mapToEntity(AITaskDto aiTaskDto) {
        AITaskTemplate aiTaskTemplate = new AITaskTemplate();
        aiTaskTemplate.setId(aiTaskDto.getId());
        aiTaskTemplate.setPrompt(aiTaskDto.getPrompt());
        aiTaskTemplate.setName(aiTaskDto.getName());
        aiTaskTemplate.setSystemPrompt(aiTaskDto.getSystemPrompt());
        aiTaskTemplate.setModelRoute(aiTaskDto.getModelRoute());
        aiTaskTemplate.setOutputVarName(aiTaskDto.getOutputVarName());
        aiTaskTemplate.setJsonSchema(aiTaskDto.getJsonSchema());
        aiTaskTemplate.setInputFileMimeType(aiTaskDto.getInputFileMimeType());
        aiTaskTemplate.setInputFilePath(aiTaskDto.getInputFilePath());
        aiTaskTemplate.setEnableRag(aiTaskDto.isEnableRag());
        return aiTaskTemplate;
    }

}
