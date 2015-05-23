package com.busybusy.intellij.taiga;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.tasks.config.TaskRepositoryEditor;
import com.intellij.tasks.impl.BaseRepositoryType;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Tjones on 5/22/15.
 */
public class TaigaRepositoryType extends BaseRepositoryType<TaigaRepository> {

    public TaigaRepositoryType() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Taiga.io";
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/resources/taiga.png");
    }

    @NotNull
    @Override
    public TaigaRepository createRepository() {
        return new TaigaRepository(this);
    }

    @Override
    public Class<TaigaRepository> getRepositoryClass() {
        return TaigaRepository.class;
    }

    @NotNull
    @Override
    public TaskRepositoryEditor createEditor(TaigaRepository repository, Project project, Consumer<TaigaRepository> changeListener) {
        return new BaseRepositoryEditor<TaigaRepository>(project, repository, changeListener);
    }


}
