package com.agpf.plataforma.repositories.projects;

import com.agpf.plataforma.models.projects.StartupScript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StartupScriptRepository extends JpaRepository<StartupScript, UUID> {

    List<StartupScript> findByProjectIdIn(Collection<UUID> projectIds);

    Optional<StartupScript> findFirstByProjectIdAndPathProject(UUID projectId, String pathProject);

}
