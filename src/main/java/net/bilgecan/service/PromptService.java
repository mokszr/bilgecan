package net.bilgecan.service;

import net.bilgecan.dto.MembershipDto;
import net.bilgecan.dto.PromptDto;
import net.bilgecan.entity.Permission;
import net.bilgecan.entity.Prompt;
import net.bilgecan.entity.security.User;
import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.repository.PromptRepository;
import net.bilgecan.repository.WorkspaceRepository;
import net.bilgecan.util.WorkspacePermissions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
@Service
public class PromptService {

    private PromptRepository promptRepository;
    private SecurityService securityService;
    private WorkspaceRepository workspaceRepository;
    private MembershipService membershipService;
    private TranslationService translationService;

    public PromptService(PromptRepository promptRepository, SecurityService securityService,
                         WorkspaceRepository workspaceRepository, MembershipService membershipService,
                         TranslationService translationService) {
        this.promptRepository = promptRepository;
        this.securityService = securityService;
        this.workspaceRepository = workspaceRepository;
        this.membershipService = membershipService;
        this.translationService = translationService;
    }

    public Long savePrompt(PromptDto promptDto) {

        User currentUser = securityService.getCurrentUser();

        Prompt alreadySavedOne = promptRepository.findByOwnerAndNameIgnoreCaseAndWorkspaceIsNull(currentUser, promptDto.getName());
        // new prompt creation
        if (promptDto.getId() == null) {
            if (alreadySavedOne != null) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        } else {
            // prompt update
            if (alreadySavedOne != null && !alreadySavedOne.getId().equals(promptDto.getId())) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        }

        Prompt entity = mapToEntity(promptDto);
        entity.setOwner(currentUser);
        Prompt saved = promptRepository.save(entity);
        return saved.getId();
    }

    public Long saveWorkspacePrompt(PromptDto promptDto, Long workspaceId) {

        User currentUser = securityService.getCurrentUser();
        MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(workspaceId);
        if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.EDIT)) {
            throw new AppLevelValidationException(translationService.t("general.noPermission"));
        }

        Prompt alreadySavedOne = promptRepository.findByWorkspaceIdAndNameIgnoreCase(workspaceId, promptDto.getName());
        // new prompt creation
        if (promptDto.getId() == null) {
            if (alreadySavedOne != null) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        } else {
            // prompt update
            if (alreadySavedOne != null && !alreadySavedOne.getId().equals(promptDto.getId())) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        }

        Prompt entity = mapToEntity(promptDto);
        entity.setOwner(currentUser);
        entity.setWorkspace(workspaceRepository.findById(workspaceId).get());
        Prompt saved = promptRepository.save(entity);
        return saved.getId();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<PromptDto> findPaginated(Pageable pageable, String searchTerm) {
        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(promptRepository.findByOwnerAndWorkspaceIsNull((User) authenticatedUser, pageable));
        }

        return mapToDto(promptRepository.findByNameContainingIgnoreCaseAndOwnerAndWorkspaceIsNull(searchTerm, (User) authenticatedUser, pageable));
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<PromptDto> findInWorkspacePaginated(Pageable pageable, String searchTerm, Long workspaceId) {

        MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(workspaceId);
        if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.VIEW)) {
            throw new AppLevelValidationException(translationService.t("general.noPermission"));
        }

        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(promptRepository.findByWorkspaceId(workspaceId, pageable));
        }

        return mapToDto(promptRepository.findByNameContainingIgnoreCaseAndWorkspaceId(searchTerm, workspaceId, pageable));
    }

    public void delete(PromptDto prompt) {
        User currentUser = securityService.getCurrentUser();
        long deletedCount = promptRepository.deleteByIdAndOwnerAndWorkspaceIsNull(prompt.getId(), currentUser);
        if (deletedCount <= 0) {
            throw new AppLevelValidationException("Prompt cannot be found");
        }
    }

    public void deleteInWorkspace(PromptDto prompt, Long workspaceId) {
        MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(workspaceId);
        if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.EDIT)) {
            throw new AppLevelValidationException(translationService.t("general.noPermission"));
        }

        long deletedCount = promptRepository.deleteByIdAndWorkspaceId(prompt.getId(), workspaceId);
        if (deletedCount <= 0) {
            throw new AppLevelValidationException("Prompt cannot be found");
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public PromptDto getPrompt(Long promptId, Long wsId) {
        User currentUser = securityService.getCurrentUser();
        Prompt entity = null;
        if (wsId == null) {
            entity = promptRepository.findByIdAndOwnerAndWorkspaceIsNull(promptId, currentUser);
        } else {
            MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(wsId);
            if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.USE)) {
                return null;
            }
            entity = promptRepository.findByIdAndWorkspaceId(promptId, wsId);
        }
        return toDto(entity);
    }

    private Page<PromptDto> mapToDto(Page<Prompt> promptPage) {
        return promptPage.map(prompt -> toDto(prompt));
    }

    private PromptDto toDto(Prompt prompt) {
        if (prompt == null) {
            return null;
        }
        PromptDto dto = new PromptDto();
        dto.setId(prompt.getId());
        dto.setName(prompt.getName());
        dto.setInput(prompt.getInput());
        dto.setType(prompt.getType());

        return dto;
    }

    private Prompt mapToEntity(PromptDto promptDto) {
        Prompt prompt = new Prompt();
        prompt.setId(promptDto.getId());
        prompt.setInput(promptDto.getInput());
        prompt.setName(promptDto.getName());
        prompt.setType(promptDto.getType());
        return prompt;
    }

}
