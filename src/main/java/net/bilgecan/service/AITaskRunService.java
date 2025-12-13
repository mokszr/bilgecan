package net.bilgecan.service;

import net.bilgecan.dto.AIResponseDetailsDto;
import net.bilgecan.dto.AITaskRunDto;
import net.bilgecan.dto.MembershipDto;
import net.bilgecan.entity.AIResponseDetails;
import net.bilgecan.entity.AITaskRun;
import net.bilgecan.entity.AITaskStatus;
import net.bilgecan.entity.AITaskTemplate;
import net.bilgecan.entity.FileProcessingPipeline;
import net.bilgecan.entity.Permission;
import net.bilgecan.entity.Workspace;
import net.bilgecan.entity.security.User;
import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.pipeline.fileprocessing.FileSystemInputFileProvider;
import net.bilgecan.pipeline.fileprocessing.FileSystemOutputFileWriter;
import net.bilgecan.repository.AITaskRunRepository;
import net.bilgecan.repository.AITaskTemplateRepository;
import net.bilgecan.repository.WorkspaceRepository;
import net.bilgecan.util.WorkspacePermissions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional(rollbackFor = Exception.class)
@Service
public class AITaskRunService {

    private AITaskRunRepository aiTaskRunRepository;
    private AITaskTemplateRepository aiTaskTemplateRepository;
    private SecurityService securityService;
    private WorkspaceRepository workspaceRepository;
    private MembershipService membershipService;
    private TranslationService translationService;
    private FileSystemInputFileProvider fileSystemInputFileProvider;
    private FileSystemOutputFileWriter fileSystemOutputFileWriter;


    public AITaskRunService(AITaskRunRepository aiTaskRunRepository, AITaskTemplateRepository aiTaskTemplateRepository, SecurityService securityService,
                            WorkspaceRepository workspaceRepository, MembershipService membershipService, TranslationService translationService) {
        this.aiTaskRunRepository = aiTaskRunRepository;
        this.aiTaskTemplateRepository = aiTaskTemplateRepository;
        this.securityService = securityService;
        this.workspaceRepository = workspaceRepository;
        this.membershipService = membershipService;
        this.translationService = translationService;
    }

    public void saveTaskRun(Long aiTaskTemplateId) {
        User currentUser = securityService.getCurrentUser();

        AITaskTemplate template = aiTaskTemplateRepository.findByIdAndOwnerAndWorkspaceIsNull(aiTaskTemplateId, currentUser);
        if (template == null) {
            return;
        }

        AITaskRun run = toRun(template);
        run.setStatus(AITaskStatus.PENDING);
        run.setOwner(currentUser);

        aiTaskRunRepository.save(run);
    }

    public void saveTaskRunInWorkspace(Long id, Long workspaceId) {
        User currentUser = securityService.getCurrentUser();
        MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(workspaceId);
        if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.USE)) {
            throw new AppLevelValidationException(translationService.t("general.noPermission"));
        }

        AITaskTemplate template = aiTaskTemplateRepository.findByIdAndWorkspaceId(id, workspaceId);
        if (template == null) {
            return;
        }

        AITaskRun run = toRun(template);
        run.setWorkspace(workspaceRepository.findById(workspaceId).get());
        run.setStatus(AITaskStatus.PENDING);
        run.setOwner(currentUser);

        aiTaskRunRepository.save(run);
    }


    public void saveTaskRunForPipelineBatch(List<String> files, FileProcessingPipeline fileProcessingPipeline) {
        User currentUser = securityService.getCurrentUser();
        AITaskTemplate template = fileProcessingPipeline.getTask();
        String mimeType = fileProcessingPipeline.getInputSource().getConfigs().get(FileSystemInputFileProvider.MIME_TYPE);

        List<AITaskRun> runList = new ArrayList<>();
        for (String inputFilePath : files) {
            AITaskRun run = toRun(template);
            run.setStatus(AITaskStatus.PENDING);
            run.setOwner(currentUser);
            run.setFileProcessingPipeline(fileProcessingPipeline);
            run.setInputFilePath(inputFilePath);
            run.setInputFileMimeType(mimeType);
            run.setInputFileName(new File(inputFilePath).getName());
            runList.add(run);
        }

        aiTaskRunRepository.saveAll(runList);
    }

    private AITaskRun toRun(AITaskTemplate template) {
        AITaskRun run = new AITaskRun();
        run.setRawPrompt(template.getPrompt());
        run.setResolvedPrompt(template.getPrompt());
        run.setRawSystemPrompt(template.getSystemPrompt());
        run.setResolvedSystemPrompt(template.getSystemPrompt());
        run.setPriority(template.getDefaultPriority());
        run.setModelRoute(template.getModelRoute());
        run.setTemplate(template);
        run.setTemplateName(template.getName());
        run.setJsonSchema(template.getJsonSchema());
        run.setInputFileMimeType(template.getInputFileMimeType());
        run.setInputFilePath(template.getInputFilePath());
        run.setEnableRag(template.isEnableRag());

        return run;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<AITaskRunDto> findPaginated(Pageable pageable, String searchTerm) {
        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(aiTaskRunRepository.findVisibleForUser((User) authenticatedUser, pageable));
        }
        //TODO make it use searchTerm
        return mapToDto(aiTaskRunRepository.findVisibleForUser((User) authenticatedUser, pageable));
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public AITaskRunDto findById(Long id) {
        Optional<AITaskRun> byId = aiTaskRunRepository.findById(id);
        return byId.map(this::toDto).orElse(null);
    }

    private Page<AITaskRunDto> mapToDto(Page<AITaskRun> page) {
        return page.map(aiTaskRun -> toDto(aiTaskRun));
    }

    private AITaskRunDto toDto(AITaskRun aiTaskRun) {
        if (aiTaskRun == null) {
            return null;
        }
        AITaskRunDto dto = new AITaskRunDto();
        dto.setId(aiTaskRun.getId());
        dto.setStatus(aiTaskRun.getStatus());
        dto.setResolvedPrompt(aiTaskRun.getResolvedPrompt());
        dto.setError(aiTaskRun.getError());
        dto.setResolvedSystemPrompt(aiTaskRun.getResolvedSystemPrompt());
        dto.setFinishedAt(aiTaskRun.getFinishedAt());
        dto.setStartedAt(aiTaskRun.getStartedAt());
        dto.setTemplateName(aiTaskRun.getTemplateName());
        dto.setModelRoute(aiTaskRun.getModelRoute());
        dto.setAiResponseDetails(toDetailsDto(aiTaskRun.getAiResponseDetails()));
        dto.setJsonSchema(aiTaskRun.getJsonSchema());
        dto.setInputFileMimeType(aiTaskRun.getInputFileMimeType());
        dto.setInputFilePath(aiTaskRun.getInputFilePath());
        dto.setLeaseOwner(aiTaskRun.getLeaseOwner());
        dto.setEnableRag(aiTaskRun.isEnableRag());
        return dto;
    }

    private AIResponseDetailsDto toDetailsDto(AIResponseDetails aiResponseDetails) {
        if (aiResponseDetails == null) {
            return null;
        }
        AIResponseDetailsDto dto = new AIResponseDetailsDto();
        dto.setDoneAi(aiResponseDetails.getDoneAi());
        dto.setCompletionTokensAi(aiResponseDetails.getCompletionTokensAi());
        dto.setPromptTokensAi(aiResponseDetails.getPromptTokensAi());
        dto.setTotalTokensAi(aiResponseDetails.getTotalTokensAi());
        dto.setFinishReasonAi(aiResponseDetails.getFinishReasonAi());
        dto.setOutputTextAi(aiResponseDetails.getOutputTextAi());
        dto.setLoadDurationAi(aiResponseDetails.getLoadDurationAi());
        dto.setPromptEvalDurationAi(aiResponseDetails.getPromptEvalDurationAi());
        dto.setTotalDurationAi(aiResponseDetails.getTotalDurationAi());
        dto.setEvalDurationAi(aiResponseDetails.getEvalDurationAi());
        return dto;
    }

    public void delete(AITaskRunDto aiTask) {
        User currentUser = securityService.getCurrentUser();
        Optional<AITaskRun> byId = aiTaskRunRepository.findById(aiTask.getId());
        if (byId.isEmpty()) {
            throw new AppLevelValidationException("AITaskRun cannot be found");
        }
        Workspace workspace = byId.get().getWorkspace();
        if (workspace != null) {
            MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(workspace.getId());
            if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.USE)) {
                throw new AppLevelValidationException(translationService.t("general.noPermission"));
            }
            long deletedCount = aiTaskRunRepository.deleteByIdAndWorkspaceId(aiTask.getId(), workspace.getId());
            validateDeletionResult(deletedCount);

        } else {
            long deletedCount = aiTaskRunRepository.deleteByIdAndOwner(aiTask.getId(), currentUser);
            validateDeletionResult(deletedCount);
        }
    }

    private void validateDeletionResult(long deletedCount) {
        if (deletedCount > 0) {
            System.out.println("Deleted successfully!");
        } else {
            throw new AppLevelValidationException("AITaskRun cannot be found");
        }
    }

    public boolean cancel(AITaskRunDto aiTask) {
        Optional<AITaskRun> byId = aiTaskRunRepository.findById(aiTask.getId());
        if (byId.isEmpty()) {
            throw new AppLevelValidationException("AITaskRun cannot be found");
        }
        Workspace workspace = byId.get().getWorkspace();
        if (workspace != null) {
            MembershipDto membershipForWorkspace = membershipService.findMembershipForWorkspace(workspace.getId());
            if (membershipForWorkspace == null || !WorkspacePermissions.can(membershipForWorkspace.getRole(), Permission.USE)) {
                throw new AppLevelValidationException(translationService.t("general.noPermission"));
            }
            int canceledCount = aiTaskRunRepository.cancel(aiTask.getId());
            return canceledCount > 0;
        } else {
            int canceledCount = aiTaskRunRepository.cancel(aiTask.getId());
            return canceledCount > 0;
        }

    }

    public Optional<File> getFullInputFile(AITaskRunDto dto) {
        Optional<AITaskRun> byId = aiTaskRunRepository.findById(dto.getId());
        if (byId.isEmpty()) {
            return Optional.empty();
        }

        AITaskRun aiTaskRun = byId.get();
        File inputFile = new File(fileSystemInputFileProvider.getRootInputFileDirectoryPath() + File.separator + aiTaskRun.getInputFilePath());
        if (inputFile.exists()) {
            return Optional.of(inputFile);
        }
        FileProcessingPipeline fileProcessingPipeline = aiTaskRun.getFileProcessingPipeline();
        String archiveDirectoryName = fileProcessingPipeline.getOutputTarget().getConfigs().get(FileSystemOutputFileWriter.ARCHIVE_DIRECTORY_NAME);

        File archivedFile = new File(fileSystemOutputFileWriter.getRootArchiveFileDirectoryPath()
                + File.separator + archiveDirectoryName
                + File.separator + inputFile.getName());
        if (archivedFile.exists()) {
            return Optional.of(archivedFile);
        }

        return Optional.empty();
    }

    @Autowired
    public void setFileSystemOutputFileWriter(FileSystemOutputFileWriter fileSystemOutputFileWriter) {
        this.fileSystemOutputFileWriter = fileSystemOutputFileWriter;
    }

    @Autowired
    public void setFileSystemInputFileProvider(FileSystemInputFileProvider fileSystemInputFileProvider) {
        this.fileSystemInputFileProvider = fileSystemInputFileProvider;
    }
}
