package com.agpf.plataforma.services.projects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StartupScriptServiceTest {

    @TempDir
    Path pastaLogs;

    @Test
    void criaArquivoDeLogNoDiretorio() throws IOException {
        Path log = pastaLogs.resolve("id.txt");

        Files.createFile(log);

        assertTrue(Files.exists(log));
        File[] arquivos = pastaLogs.toFile().listFiles();
        assertNotNull(arquivos);
        assertEquals(1, arquivos.length);
    }

}
