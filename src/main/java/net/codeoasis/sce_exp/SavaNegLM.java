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
import java.util.HashMap;
import java.util.Map;
import com.intellij.psi.*;
import com.intellij.psi.util.*;


public class SavaNegLM extends AnAction {

    private void showNotification(String content) {
        Notification notification = new Notification(
                "Snippet Save Notification",  // Notification Group ID (can be used to categorize notifications)
                "SCE",               // Title of the notification
                content,                      // Content of the notification
                NotificationType.INFORMATION  // Type of notification (e.g., INFORMATION, WARNING, ERROR)
        );
        Notifications.Bus.notify(notification);
    }

//    private String findMethodName(int startOffset, int endOffset, Project project, Editor editor) {
//        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
//        if (psiFile == null) return "";
//
//        PsiElement startElement = psiFile.findElementAt(startOffset);
//        PsiElement endElement = psiFile.findElementAt(endOffset);
//        if (startElement == null || endElement == null) return "";
//
//        PsiElement commonParent = PsiTreeUtil.findCommonParent(startElement, endElement);
//        if (commonParent == null) return "";
//
//        PsiMethod method = PsiTreeUtil.getParentOfType(commonParent, PsiMethod.class);
//        if (method != null) {
//            String methodName = method.toString();
//            System.out.println("Enclosing method name: " + methodName);
//        }
//
//        return "";
//    }

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

            if (selectedText != null) {
                // Send the selectedText to the server (implement this next)
//                sendSnippetToServer(selectedText, e);
                int startOffset = selectionModel.getSelectionStart();
                int endOffset = selectionModel.getSelectionEnd();

//                findMethodName(startOffset, endOffset, project, editor);
                sendNegSample(selectedText, projectName);
            } else {
//                showNotification("No text selected!");
            }
        }
    }
}
