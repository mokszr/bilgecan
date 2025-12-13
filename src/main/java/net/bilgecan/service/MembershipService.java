package net.bilgecan.service;

import net.bilgecan.dto.MembershipDto;
import net.bilgecan.dto.UserDto;
import net.bilgecan.entity.Membership;
import net.bilgecan.entity.WorkspaceRole;
import net.bilgecan.entity.security.User;
import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.repository.MembershipRepository;
import net.bilgecan.repository.UserRepository;
import net.bilgecan.repository.WorkspaceRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Transactional(rollbackFor = Exception.class)
@Service
public class MembershipService {

    private final SecurityService securityService;
    private MembershipRepository membershipRepository;
    private UserRepository userRepository;
    private WorkspaceRepository workspaceRepository;
    private TranslationService translations;

    public MembershipService(MembershipRepository membershipRepository, UserRepository userRepository, WorkspaceRepository workspaceRepository, SecurityService securityService, TranslationService translations) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.securityService = securityService;
        this.translations = translations;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void saveMembership(Long workspaceId, UserDto selectedUserDto, WorkspaceRole role) {

        Optional<Membership> storedMembership = membershipRepository.findByWorkspaceIdAndUserId(workspaceId, selectedUserDto.getId());
        if (storedMembership.isPresent()) {
            throw new AppLevelValidationException(translations.t("wsMember.alreadyMember"));
        }

        Membership membership = new Membership();
        membership.setRole(role);
        membership.setUser(userRepository.findById(selectedUserDto.getId()).get());
        membership.setJoinedAt(OffsetDateTime.now());
        membership.setWorkspace(workspaceRepository.findById(workspaceId).get());

        membershipRepository.save(membership);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(MembershipDto membershipDto) {
        membershipRepository.deleteById(membershipDto.getId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateMembership(MembershipDto membershipDto, WorkspaceRole selected) {
        membershipRepository.updateRoleById(membershipDto.getId(), selected);
    }

    public Page<MembershipDto> findPaginated(Pageable pageable, String searchTerm, Long workspaceId) {
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(membershipRepository.findByWorkspaceId(workspaceId, pageable));
        }
        return mapToDto(membershipRepository.findByUserUsernameContainingIgnoreCaseAndWorkspaceId(searchTerm, workspaceId, pageable));
    }

    public List<MembershipDto> findUserMemberships() {
        User currentUser = securityService.getCurrentUser();
        List<Membership> memberships = membershipRepository.findAllByUser(currentUser);
        return memberships.stream().map(this::toDto).toList();
    }

    public MembershipDto findMembershipForWorkspace(Long workspaceId) {
        User currentUser = securityService.getCurrentUser();
        Membership membership = membershipRepository.findByUserAndWorkspaceId(currentUser, workspaceId);
        return toDto(membership);
    }

    private Page<MembershipDto> mapToDto(Page<Membership> page) {
        return page.map(this::toDto);
    }

    private MembershipDto toDto(Membership entity) {
        if (entity == null) {
            return null;
        }
        MembershipDto dto = new MembershipDto();
        dto.setRole(entity.getRole());
        dto.setUsername(entity.getUser().getUsername());
        dto.setJoinedAt(entity.getJoinedAt());
        dto.setUserId(entity.getUser().getId());
        dto.setId(entity.getId());
        dto.setWorkspaceId(entity.getWorkspace().getId());
        dto.setWorkspaceName(entity.getWorkspace().getName());

        return dto;
    }


}
