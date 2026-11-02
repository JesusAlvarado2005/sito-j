package dev.jj.projects.project;

import dev.jj.projects.project.ProjectDto.TagDto;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
class ProjectController {

    private final ProjectService service;

    ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping("/projects")
    List<ProjectDto> list(@RequestParam(defaultValue = "en") @Pattern(regexp = "it|en") String lang,
                          @RequestParam(required = false) String tag,
                          @RequestParam(required = false) Boolean featured) {
        return service.list(lang, tag, featured);
    }

    @GetMapping("/projects/{slug}")
    ProjectDto detail(@PathVariable String slug,
                      @RequestParam(defaultValue = "en") @Pattern(regexp = "it|en") String lang) {
        return service.detail(slug, lang);
    }

    @GetMapping("/tags")
    List<TagDto> tags() {
        return service.usedTags();
    }
}
