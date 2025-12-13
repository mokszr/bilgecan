package net.bilgecan.service;

import net.bilgecan.dto.WorkspaceDto;
import net.bilgecan.entity.Workspace;
import net.bilgecan.entity.security.User;
import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.repository.MembershipRepository;
import net.bilgecan.repository.WorkspaceRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class WorkspaceService {

    private final WorkspaceRepository repository;
    private SecurityService securityService;
    private MembershipRepository membershipRepository;
    private TranslationService translationService;

    public WorkspaceService(WorkspaceRepository repository, MembershipRepository membershipRepository, SecurityService securityService, TranslationService translationService) {
        this.repository = repository;
        this.securityService = securityService;
        this.membershipRepository = membershipRepository;
        this.translationService = translationService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void saveWorkspace(WorkspaceDto dto) {
        User currentUser = securityService.getCurrentUser();

        Workspace alreadySavedOne = repository.findByNameIgnoreCaseOrSlugIgnoreCase(dto.getName(), dto.getSlug());

        if (dto.getId() == null) {
            if (alreadySavedOne != null) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        } else {

            if (alreadySavedOne != null && !alreadySavedOne.getId().equals(dto.getId())) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        }

        Workspace entity = mapToEntity(dto);
        repository.save(entity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(WorkspaceDto workspaceDto) {
        repository.deleteById(workspaceDto.getId());
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<WorkspaceDto> findPaginated(Pageable pageable, String searchTerm) {
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(repository.findAll(pageable));
        }
        return mapToDto(repository.findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(searchTerm, searchTerm, pageable));
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public WorkspaceDto getWorkspace(Long workspaceId) {
        Optional<Workspace> byId = repository.findById(workspaceId);
        if (byId.isEmpty()) {
            throw new AppLevelValidationException("no workspace found!");
        }
        return toDto(byId.get());
    }

    private Page<WorkspaceDto> mapToDto(Page<Workspace> page) {
        return page.map(this::toDto);
    }

    private WorkspaceDto toDto(Workspace entity) {
        WorkspaceDto dto = new WorkspaceDto();
        dto.setName(entity.getName());
        dto.setSlug(entity.getSlug());
        dto.setId(entity.getId());
        dto.setSettings(new HashMap<>(entity.getSettings()));

        return dto;
    }

    private Workspace mapToEntity(WorkspaceDto dto) {
        Workspace entity = new Workspace();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setSlug(dto.getSlug());
        entity.setSettings(dto.getSettings());

        return entity;
    }


}
