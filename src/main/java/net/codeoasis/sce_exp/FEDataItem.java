package net.codeoasis.sce_exp;
import com.google.gson.annotations.SerializedName;

public class FEDataItem {

    @SerializedName("fe_id")
    int feId;

    String project;

    @SerializedName("class_name")
    String className;

    @SerializedName("method_name")
    String methodName;

    @SerializedName("target_class_name")
    String targetClassName;

    String path;

    int label;

    public FEDataItem(int feId , String project, String className, String methodName, String targetClassName, int label, String path) {
        this.feId = feId;
        this.project = project;
        this.className = className;
        this.methodName = methodName;
        this.targetClassName = targetClassName;
        this.label = label;
        this.path = path;
    }

    public Object[] toTableRow() {
        return new Object[]{project, className, methodName, targetClassName, label, path};
    }
}
