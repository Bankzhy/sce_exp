package net.codeoasis.sce_exp;

import com.google.gson.Gson;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavaPosLM extends AnAction {

    private void showNotification(String content) {
        Notification notification = new Notification(
                "Snippet Save Notification",  // Notification Group ID (can be used to categorize notifications)
                "SCE",               // Title of the notification
                content,                      // Content of the notification
                NotificationType.INFORMATION  // Type of notification (e.g., INFORMATION, WARNING, ERROR)
        );
        Notifications.Bus.notify(notification);
    }


    private void sendPosSample(String selectLines, String extractLines, int startLine, int endLine, String projectName) {
        String[] extractLinesList = extractLines.split(";");
        List<String> formatExtractLinesList = new ArrayList<>();
        for (String extractLine : extractLinesList) {

            String[] extractLineSplits = extractLine.split("-");
            int extractStartLine = Integer.parseInt(extractLineSplits[0]);
            int extractEndLine = Integer.parseInt(extractLineSplits[1]);

            extractStartLine = extractStartLine - startLine;
            extractEndLine = extractEndLine - startLine;

            String newExtractLine = extractStartLine + "-" + extractEndLine;
            formatExtractLinesList.add(newExtractLine);
        }
        String formatExtractLines = String.join(";", formatExtractLinesList);

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
            postData.put("extract_lines", formatExtractLines);
            postData.put("review", 1);
            postData.put("reviewer_id", LoginManager.getUserId());

            Gson gson = new Gson();
            String json = gson.toJson(postData);
            HttpClient client = HttpClient.newHttpClient();

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
//                System.out.println(result);
                showNotification("Sample Saved !");
            } else if(response.statusCode() == 401) {
                OasisActivator.showUnLoginNotification();
            }else {
                showNotification("Save failed! The error code:" + response.statusCode());
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

            int startOffset = selectionModel.getSelectionStart();
            int endOffset = selectionModel.getSelectionEnd();
            int startLine = document.getLineNumber(startOffset);
            int endLine = document.getLineNumber(endOffset);
//            System.out.println(startLine);
//            System.out.println(endLine);

            if (selectedText != null) {
                PosLMDialog posLMDialog = new PosLMDialog();
                if (posLMDialog.showAndGet()) {
                    String extractLines = posLMDialog.getExtractLine();
//                    System.out.println("extractLines: " + extractLines);
                    sendPosSample(selectedText, extractLines, startLine, endLine, projectName);
                } else {

                }
            } else {
//                showNotification("No text selected!");
            }
        }
    }
}
