package net.codeoasis.sce_exp;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;


import javax.swing.*;

public class LMToolWindowFactory implements ToolWindowFactory, DumbAware  {

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        LMPluginPanel panel = new LMPluginPanel(project);
        toolWindow.getComponent().add(panel);
    }

}

