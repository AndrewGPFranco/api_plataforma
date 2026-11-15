package com.agpf.plataforma.models.projects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
@RedisHash("Scripts")
public class CacheScriptRedis {

    @Id
    private UUID id;
    private Long pid;
    private String path;
    @Indexed
    private String title;
    @Indexed
    private UUID projectId;
    /**
     * Horário de início do processo, usado para detectar PID reutilizado pelo SO.
     */
    private Instant inicio;

}
