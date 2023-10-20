package dev.lxqtpr.lindaSelfGuru.Domain.Projects;

import dev.lxqtpr.lindaSelfGuru.Core.Excreptions.ResourceNotFoundException;
import dev.lxqtpr.lindaSelfGuru.Core.Services.FileService;
import dev.lxqtpr.lindaSelfGuru.Domain.Notes.NoteRepository;
import dev.lxqtpr.lindaSelfGuru.Domain.Phrases.Dto.ResponsePhraseDto;
import dev.lxqtpr.lindaSelfGuru.Domain.Projects.Dto.CreateProjectDto;
import dev.lxqtpr.lindaSelfGuru.Domain.Projects.Dto.ResponseProjectDto;
import dev.lxqtpr.lindaSelfGuru.Domain.Projects.Dto.UpdateProjectDto;
import dev.lxqtpr.lindaSelfGuru.Domain.Songs.SongsRepository;
import dev.lxqtpr.lindaSelfGuru.Domain.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final FileService fileService;
    private final NoteRepository noteRepository;
    private final SongsRepository songsRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public ResponseProjectDto createProject(CreateProjectDto dto){
        var fileName = fileService.upload(dto.getVoiceRecording());
        var projectToSave = modelMapper.map(dto, ProjectEntity.class);
        projectToSave.setVoiceRecording(fileName);
        var note = noteRepository.findById(dto.getNoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Note with this id does not exist"));
        var song = songsRepository.findById(dto.getSongId())
                .orElseThrow(() -> new ResourceNotFoundException("Song with this id does not exist"));
        var user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User with this id does not exist"));
        projectToSave.setNote(note);
        projectToSave.setSong(song);
        projectToSave.setUser(user);
        return modelMapper.map(projectRepository.save(projectToSave), ResponseProjectDto.class);
    }

    public List<ResponseProjectDto> getAllUserProjects(Long userId){
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with this id does not exist"));
        return user.getProjects()
                .stream()
                .map(project -> modelMapper.map(project, ResponseProjectDto.class))
                .toList();
    }

    public ResponseProjectDto updateProject(UpdateProjectDto dto){
        var project = projectRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project with this id does not exist"));
        var note = noteRepository.findById(dto.getNoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Note with this id does not exist"));
        var song = songsRepository.findById(dto.getSongId())
                .orElseThrow(() -> new ResourceNotFoundException("Song with this id does not exist"));
        project.setName(dto.getName());
        project.setNote(note);
        project.setSong(song);
        return modelMapper.map(projectRepository.save(project), ResponseProjectDto.class);
    }
    public String deleteProject(Long projectId){
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project with this id does not exist"));
        fileService.deleteFile(project.getVoiceRecording());
        projectRepository.deleteById(projectId);
        return "Project was deleted";
    }
}
