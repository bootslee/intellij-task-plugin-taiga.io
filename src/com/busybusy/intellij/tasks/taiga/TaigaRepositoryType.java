package com.busybusy.intellij.tasks.taiga;

import com.intellij.openapi.util.IconLoader;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.impl.BaseRepositoryType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Tjones on 5/22/15.
 */
public class TaigaRepositoryType extends BaseRepositoryType<TaigaRepository> {
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
    public TaskRepository createRepository() {
        return new TaigaRepository(this);
    }

    @Override
    public Class<TaigaRepository> getRepositoryClass() {
        return TaigaRepository.class;
    }
}
