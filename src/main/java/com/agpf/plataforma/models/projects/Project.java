package com.agpf.plataforma.models.projects;

import com.agpf.plataforma.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
public class Project extends BaseEntity {

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "started_at", nullable = false)
    private LocalDate startedAt;

    @Column(name = "repo_front_url", columnDefinition = "TEXT")
    private String repoFrontURL;

    @Column(name = "repo_back_url", columnDefinition = "TEXT")
    private String repoBackURL;

    @Column(name = "deploy_front_url", columnDefinition = "TEXT")
    private String deployFrontURL;

    @Column(name = "deploy_back_url", columnDefinition = "TEXT")
    private String deployBackURL;

    @Column(name = "localhost_url", nullable = false, length = 100)
    private String localhostURL;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "has_deploy", insertable = false, updatable = false)
    private boolean hasDeploy;

    @Column(name = "user_id")
    private Long userId;

}
