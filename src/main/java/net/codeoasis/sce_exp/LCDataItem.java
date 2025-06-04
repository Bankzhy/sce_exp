package net.codeoasis.sce_exp;
import com.google.gson.annotations.SerializedName;

public class LCDataItem {

    @SerializedName("lc_id")
    int lcId;

    String project;

    @SerializedName("class_name")
    String className;

    String path;

    int label;

    @SerializedName("extract_methods")
    String extractMethods;

    public LCDataItem(int lmId , String project, String className, String extractMethods, int label, String path) {
        this.lcId = lmId;
        this.project = project;
        this.className = className;
        this.extractMethods = extractMethods;
        this.label = label;
        this.path = path;
    }

    public Object[] toTableRow() {
        return new Object[]{project, className, label, extractMethods, path};
    }
}
