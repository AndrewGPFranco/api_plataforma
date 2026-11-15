package com.agpf.plataforma.repositories.projects;

import com.agpf.plataforma.models.projects.CacheScriptRedis;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface CacheScriptRedisRepository extends CrudRepository<CacheScriptRedis, UUID> {

    Optional<CacheScriptRedis> findByProjectIdAndTitle(UUID id, String title);

}
