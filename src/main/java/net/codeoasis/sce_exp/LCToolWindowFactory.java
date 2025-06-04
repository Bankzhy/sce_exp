package net.codeoasis.sce_exp;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

public class LCToolWindowFactory implements ToolWindowFactory, DumbAware  {

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        LCPluginPanel panel = new LCPluginPanel(project);
        toolWindow.getComponent().add(panel);
    }

}

