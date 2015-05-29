package com.busybusy.intellij.taiga;

import com.busybusy.intellij.taiga.models.TaigaProject;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.tasks.impl.TaskUiUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

import static com.busybusy.intellij.taiga.TaigaRepository.UNSPECIFIED_PROJECT;

/**
 * Created by Tjones on 5/26/15.
 */
public class TaigaRepositoryEditor extends BaseRepositoryEditor<TaigaRepository> {

    private JBLabel mProjectLabel;
    private ComboBox mProjectBox;

    public TaigaRepositoryEditor(Project project, TaigaRepository repository, Consumer<TaigaRepository> changeListener) {
        super(project, repository, changeListener);

        installListener(mProjectBox);

        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        });
    }

    private void initialize() {
        final TaigaProject currentProject = myRepository.getSelectedProject();
        if (currentProject != null && myRepository.isConfigured()) {
            new FetchProjectsTask().queue();
        }
    }


    @Nullable
    @Override
    protected JComponent createCustomPanel() {
        mProjectLabel = new JBLabel("Project:", SwingConstants.RIGHT);
        mProjectBox = new ComboBox(300);
        mProjectBox.setRenderer(new TaskUiUtil.SimpleComboBoxRenderer("Set URL, username, and password first"));
        mProjectLabel.setLabelFor(mProjectBox);
        return new FormBuilder().addLabeledComponent(mProjectLabel, mProjectBox).getPanel();
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
        super.setAnchor(anchor);
        mProjectLabel.setAnchor(anchor);
    }

    @Override
    protected void afterTestConnection(boolean connectionSuccessful) {
        if (connectionSuccessful) {
            new FetchProjectsTask().queue();
        }
    }

    @Override
    public void apply() {
        super.apply();
        myRepository.setSelectedProject((TaigaProject)mProjectBox.getSelectedItem());
        myTestButton.setEnabled(myRepository.isConfigured());
    }

    private class FetchProjectsTask extends TaskUiUtil.ComboBoxUpdater<TaigaProject> {
        private FetchProjectsTask() {
            super(TaigaRepositoryEditor.this.myProject, "Downloading Taiga projects...", mProjectBox);
        }

        @Override
        public TaigaProject getExtraItem() {
            return UNSPECIFIED_PROJECT;
        }

        @Nullable
        @Override
        public TaigaProject getSelectedItem() {
            return myRepository.getSelectedProject();
        }

        @NotNull
        @Override
        protected List<TaigaProject> fetch(@NotNull ProgressIndicator indicator) throws Exception {
            return myRepository.getProjectList();
        }


    }
}
