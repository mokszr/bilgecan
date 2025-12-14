package net.bilgecan.service;

import net.bilgecan.dto.FileProcessingPipelineDto;
import net.bilgecan.dto.InputSourceDto;
import net.bilgecan.dto.OutputTargetDto;
import net.bilgecan.entity.FileProcessingPipeline;
import net.bilgecan.entity.InputSource;
import net.bilgecan.entity.OutputTarget;
import net.bilgecan.entity.security.User;
import net.bilgecan.exception.AppLevelValidationException;
import net.bilgecan.pipeline.fileprocessing.InputSourceResolverService;
import net.bilgecan.repository.AITaskTemplateRepository;
import net.bilgecan.repository.FileProcessingPipelineRepository;
import net.bilgecan.repository.InputSourceRepository;
import net.bilgecan.repository.OutputTargetRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Transactional(rollbackFor = Exception.class)
@Service
public class FileProcessingPipelineService {

    private FileProcessingPipelineRepository fileProcessingPipelineRepository;
    private AITaskTemplateRepository aiTaskTemplateRepository;
    private OutputTargetRepository outputTargetRepository;
    private InputSourceRepository inputSourceRepository;
    private InputSourceResolverService inputSourceResolverService;
    private AITaskRunService aiTaskRunService;
    private SecurityService securityService;
    private TranslationService translationService;

    public FileProcessingPipelineService(FileProcessingPipelineRepository fileProcessingPipelineRepository,
                                         AITaskTemplateRepository aiTaskTemplateRepository,
                                         OutputTargetRepository outputTargetRepository,
                                         InputSourceRepository inputSourceRepository,
                                         InputSourceResolverService inputSourceResolverService,
                                         AITaskRunService aiTaskRunService,
                                         SecurityService securityService,
                                         TranslationService translationService) {
        this.fileProcessingPipelineRepository = fileProcessingPipelineRepository;
        this.aiTaskTemplateRepository = aiTaskTemplateRepository;
        this.outputTargetRepository = outputTargetRepository;
        this.inputSourceRepository = inputSourceRepository;
        this.inputSourceResolverService = inputSourceResolverService;
        this.aiTaskRunService = aiTaskRunService;
        this.securityService = securityService;
        this.translationService = translationService;
    }

    public Long saveFileProcessingPipeline(FileProcessingPipelineDto pipelineDto) {

        User currentUser = securityService.getCurrentUser();

        FileProcessingPipeline alreadySavedOne = fileProcessingPipelineRepository.findByOwnerAndName(currentUser, pipelineDto.getName());
        // new pipeline creation
        if (pipelineDto.getId() == null) {
            if (alreadySavedOne != null) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        } else {
            // update
            if (alreadySavedOne != null && !alreadySavedOne.getId().equals(pipelineDto.getId())) {
                throw new AppLevelValidationException(translationService.t("general.alreadySavedWithSameName"));
            }
        }

        if (pipelineDto.getAiTaskTemplateId() == null) {
            throw new AppLevelValidationException(translationService.t("fileProcessingPipeline.aiTaskShouldBeSelected"));
        }

        FileProcessingPipeline entity = mapToEntity(pipelineDto);
        entity.setTask(aiTaskTemplateRepository.findByIdAndOwnerAndWorkspaceIsNull(pipelineDto.getAiTaskTemplateId(), currentUser));
        entity.setOwner(currentUser);
        inputSourceRepository.save(entity.getInputSource());
        outputTargetRepository.save(entity.getOutputTarget());
        FileProcessingPipeline saved = fileProcessingPipelineRepository.save(entity);
        return saved.getId();
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<FileProcessingPipelineDto> findPaginated(Pageable pageable, String searchTerm) {
        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        if (StringUtils.isBlank(searchTerm)) {
            return mapToDto(fileProcessingPipelineRepository.findByOwner((User) authenticatedUser, pageable));
        }

        return mapToDto(fileProcessingPipelineRepository.findByNameContainingIgnoreCaseAndOwner(searchTerm, (User) authenticatedUser, pageable));
    }

    public void delete(FileProcessingPipelineDto aiTask) {
        User currentUser = securityService.getCurrentUser();
        long deletedCount = fileProcessingPipelineRepository.deleteByIdAndOwner(aiTask.getId(), currentUser);
        if (deletedCount <= 0) {
            throw new AppLevelValidationException("FileProcessingPipeline cannot be found");
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public FileProcessingPipelineDto getFileProcessingPipeline(Long AITaskId) {
        User currentUser = securityService.getCurrentUser();
        return toDto(fileProcessingPipelineRepository.findByIdAndOwner(AITaskId, currentUser));
    }

    public int execute(Long pipelineId) {
        User currentUser = securityService.getCurrentUser();
        FileProcessingPipeline fileProcessingPipeline = fileProcessingPipelineRepository.findByIdAndOwner(pipelineId, currentUser);

        List<String> files = inputSourceResolverService.getFiles(fileProcessingPipeline.getInputSource());

        aiTaskRunService.saveTaskRunForPipelineBatch(files, fileProcessingPipeline);
        return files.size();
    }

    private Page<FileProcessingPipelineDto> mapToDto(Page<FileProcessingPipeline> page) {
        return page.map(entity -> toDto(entity));
    }

    private FileProcessingPipelineDto toDto(FileProcessingPipeline entity) {
        if (entity == null) {
            return null;
        }
        FileProcessingPipelineDto dto = new FileProcessingPipelineDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAiTaskTemplateId(entity.getTask().getId());
        dto.setAiTaskTemplateName(entity.getTask().getName());
        dto.setInputSource(toDto(entity.getInputSource()));
        dto.setOutputTarget(toDto(entity.getOutputTarget()));

        return dto;
    }

    private InputSourceDto toDto(InputSource entity) {
        InputSourceDto dto = new InputSourceDto();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setConfigs(new HashMap<>(entity.getConfigs()));

        return dto;
    }

    private OutputTargetDto toDto(OutputTarget entity) {
        OutputTargetDto dto = new OutputTargetDto();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setConfigs(new HashMap<>(entity.getConfigs()));

        return dto;
    }

    private FileProcessingPipeline mapToEntity(FileProcessingPipelineDto dto) {
        FileProcessingPipeline entity = new FileProcessingPipeline();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setInputSource(mapToEntity(dto.getInputSource()));
        entity.setOutputTarget(mapToEntity(dto.getOutputTarget()));

        return entity;
    }

    private InputSource mapToEntity(InputSourceDto dto) {
        InputSource entity = new InputSource();
        entity.setType(dto.getType());
        entity.setConfigs(new HashMap<>(dto.getConfigs()));
        entity.setId(dto.getId());

        return entity;
    }

    private OutputTarget mapToEntity(OutputTargetDto dto) {
        OutputTarget entity = new OutputTarget();
        entity.setType(dto.getType());
        entity.setId(dto.getId());
        entity.setConfigs(new HashMap<>(dto.getConfigs()));

        return entity;
    }

}
