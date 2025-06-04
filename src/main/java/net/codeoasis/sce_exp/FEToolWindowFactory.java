package net.codeoasis.sce_exp;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

public class FEToolWindowFactory implements ToolWindowFactory, DumbAware  {

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        FEPluginPanel panel = new FEPluginPanel(project);
        toolWindow.getComponent().add(panel);
    }

}

