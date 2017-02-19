package com.helospark.sparktemplatingplugin.repository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.sparktemplatingplugin.repository.domain.ScriptEntity;
import com.helospark.sparktemplatingplugin.repository.zip.ScriptZipper;

public class FileSystemBackedScriptRepository implements ScriptRepository {
    private static final String SPARK_TEMPLATING_TOOL_FILE_ENCODING = "UTF-8";
    private EclipseRootFolderProvider eclipseRootFolderProvider;
    private CommandNameToFilenameMapper commandNameToFilenameMapper;
    private ScriptZipper scriptZipper;

    public FileSystemBackedScriptRepository(EclipseRootFolderProvider eclipseRootFolderProvider,
            CommandNameToFilenameMapper commandNameToFilenameMapper, ScriptZipper scriptZipper) {
        this.eclipseRootFolderProvider = eclipseRootFolderProvider;
        this.commandNameToFilenameMapper = commandNameToFilenameMapper;
        this.scriptZipper = scriptZipper;
    }

    @Override
    public void saveNewScript(ScriptEntity entity) {
        try {
            File rootDirectory = eclipseRootFolderProvider.provideRootDirectory();
            File scriptFile = new File(rootDirectory, commandNameToFilenameMapper.mapToFilename(entity.getCommandName()));
            if (!scriptFile.exists()) {
                scriptFile.createNewFile();
            } else {
                System.out.println("File already exists, updating content " + scriptFile);
            }
            try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scriptFile), SPARK_TEMPLATING_TOOL_FILE_ENCODING))) {
                bufferedWriter.write(entity.getScript());
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot write to new file", e);
        }
    }

    @Override
    public List<ScriptEntity> loadAll() {
        try {
            File rootDirectory = eclipseRootFolderProvider.provideRootDirectory();
            File[] commands = rootDirectory.listFiles();
            return Arrays.stream(commands)
                    .map(file -> createScriptEntityFromFile(file))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Cannot get a listing of commands", e);
        }
    }

    @Override
    public Optional<ScriptEntity> loadByCommandName(String commandName) {
        File rootDirectory = eclipseRootFolderProvider.provideRootDirectory();
        String fileName = commandNameToFilenameMapper.mapToFilename(commandName);
        File scriptFile = new File(rootDirectory, fileName);
        Optional<ScriptEntity> result = Optional.empty();
        if (scriptFile.exists()) {
            result = Optional.of(createScriptEntityFromFile(scriptFile));
        }
        return result;
    }

    private ScriptEntity createScriptEntityFromFile(File file) {
        try {
            String data = new String(Files.readAllBytes(file.toPath()), SPARK_TEMPLATING_TOOL_FILE_ENCODING);
            String commandName = commandNameToFilenameMapper.mapToCommandName(file.getName());
            return new ScriptEntity(commandName, data);
        } catch (Exception e) {
            throw new RuntimeException("Cannot read file " + file);
        }
    }

    @Override
    public URI getUriForCommand(String commandName) {
        try {
            File rootDirectory = eclipseRootFolderProvider.provideRootDirectory();
            String fileName = commandNameToFilenameMapper.mapToFilename(commandName);
            File scriptFile = new File(rootDirectory, fileName);
            return scriptFile.toURI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
