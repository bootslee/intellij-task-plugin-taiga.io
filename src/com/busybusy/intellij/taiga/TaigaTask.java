package com.busybusy.intellij.taiga;

import com.busybusy.intellij.taiga.models.TaigaProject;
import com.busybusy.intellij.taiga.models.TaigaTaskStatus;
import com.intellij.openapi.util.IconLoader;
import com.intellij.tasks.Comment;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Tjones on 5/22/15.
 */
public class TaigaTask extends Task {
    TaigaProject mProject;
    TaigaRepository mRepository;
    com.busybusy.intellij.taiga.models.TaigaTask mTask;
    public static final String kTaskRefUrl = "https://tree.taiga.io/project/";
    public static final String kTaskRef = "/task/";

    public TaigaTask(@NotNull TaigaRepository repository, @NotNull com.busybusy.intellij.taiga.models.TaigaTask task) throws Exception
    {
        mRepository = repository;
        mTask = task;

        for (TaigaProject iter : mRepository.getProjectList())
        {
            if(iter.getProjectId().equals(mTask.getProjectId())) {
                mProject = iter;
                break;
            }

        }
    }

    @NotNull
    @Override
    public String getId() {
        return mTask.getRef();
    }

    @NotNull
    @Override
    public String getSummary() {
        return mTask.getSubject();
    }

    @Nullable
    @Override
    public String getDescription() {
        return mTask.getDescription();
    }

    @NotNull
    @Override
    public Comment[] getComments() {
        //Future Feature: Map this thing to the comment API of taiga
        return new Comment[0];
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/resources/taiga.png");
    }

    @NotNull
    @Override
    public TaskType getType() {
        return TaskType.OTHER;
    }

    @Nullable
    @Override
    public Date getUpdated() {
        return parseDateISO8601(mTask.getUpdatedAt());
    }

    @Nullable
    @Override
    public Date getCreated() {
        return parseDateISO8601(mTask.getCreatedAt());
    }

    @Override
    public boolean isClosed(){
        TaigaTaskStatus status = null;
        List<TaigaTaskStatus> statusList = mProject.getStatusList();
        for (TaigaTaskStatus aStatusList : statusList) {
            if (aStatusList.getTaigaId().equals(mTask.getStatus())) {
                status = aStatusList;
            }
        }

        return status == null || status.isClosed();
    }

    @Override
    public boolean isIssue() {
        return true;
    }

    @Nullable
    @Override
    public TaskRepository getRepository() {
        return mRepository;
    }

    @Nullable
    @Override
    public String getIssueUrl() {
        return kTaskRefUrl + mProject.getSlug() + kTaskRef + mTask.getRef();
    }

    public static Date parseDateISO8601(String input) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        if (input.endsWith("Z")) {
            input = input.substring(0, input.length() - 1) + "GMT-00:00";
        } else {
            int inset = 6;

            String s0 = input.substring(0, input.length() - inset);
            String s1 = input.substring(input.length() - inset, input.length());

            input = s0 + "GMT" + s1;
        }
        try {
            return df.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
