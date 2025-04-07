package net.codeoasis.sce_exp;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class SavaNegLM extends AnAction {

    private void sendNegSample(String selectLines, String projectName) {
        // Check if the user is already logged in (by checking locally stored data)
        if (!LoginManager.isLoggedIn()) {
            // Show login window
            LoginDialog loginDialog = new LoginDialog();
            if (loginDialog.showAndGet()) {  // If the user clicked "OK"
                String username = loginDialog.getUsername();
                String password = loginDialog.getPassword();
                OasisActivator.doOKAction(username, password);
            } else {

                return;  // Exit without sending the snippet
            }
        }


        try {
            Map<String, Object> postData = new HashMap<>();
            postData.put("project", projectName);
            postData.put("content", selectLines);
            postData.put("extract_lines", "0");
            postData.put("review", 0);
            postData.put("reviewer_id", LoginManager.getUserId());

            Gson gson = new Gson();
            String json = gson.toJson(postData);
            HttpClient client = HttpClient.newHttpClient();
            System.out.println(json);

            // Build the HTTP POST request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://www.codeoasis.net:8005/api/lm/"))  // Replace with your server's URL
                    .header("Content-Type", "application/json")  // Set Content-Type header
                    .header("Authorization", "Bearer " + LoginManager.getAccess())
                    .POST(HttpRequest.BodyPublishers.ofString(json))       // Attach the request body
                    .build();

            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                Map<String, Object> result = gson.fromJson(response.body().toString(), Map.class);
                System.out.println(result);
                OasisActivator.showNotification("Sample Saved !");
            } else if(response.statusCode() == 401) {
                OasisActivator.showUnLoginNotification();
            }else {
                OasisActivator.showNotification("Save failed! The error code:" + response.statusCode());
            }

        } catch (Exception xe) {
            System.out.println(xe);
        }

    }

    public static Project getProject(Document document) {
        Editor[] editors = EditorFactory.getInstance().getEditors(document);
        if (editors.length > 0) {
            return editors[0].getProject();
        }
        return null;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);

        if (editor != null) {
            SelectionModel selectionModel = editor.getSelectionModel();
            String selectedText = selectionModel.getSelectedText();
            Document document = editor.getDocument();
            Project project = getProject(document);
            String projectName = project != null ? project.getName() : null;

            if (selectedText != null) {
                // Send the selectedText to the server (implement this next)
//                sendSnippetToServer(selectedText, e);
                sendNegSample(selectedText, projectName);
            } else {
//                showNotification("No text selected!");
            }
        }
    }
}
