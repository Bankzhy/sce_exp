package net.codeoasis.sce_exp;
import com.google.gson.annotations.SerializedName;

public class LMDataItem {

    @SerializedName("lm_id")
    int lmId;

    String project;

    @SerializedName("class_name")
    String className;

    @SerializedName("method_name")
    String methodName;

    @SerializedName("extract_lines")
    String extractLines;

    String path;

    int label;

    public LMDataItem(int lmId ,String project, String className, String methodName, int label, String path, String extractLines) {
        this.lmId = lmId;
        this.project = project;
        this.className = className;
        this.methodName = methodName;
        this.label = label;
        this.path = path;
        this.extractLines = extractLines;
    }

    public Object[] toTableRow() {
        return new Object[]{project, className, methodName, label, extractLines, path};
    }
}
