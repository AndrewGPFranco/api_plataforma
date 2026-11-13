package com.agpf.plataforma.models.projects;

import com.agpf.plataforma.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "startup_script")
public class StartupScript extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "path_project", columnDefinition = "TEXT")
    private String pathProject;

    @Column(name = "script", columnDefinition = "TEXT")
    private String script;

}
