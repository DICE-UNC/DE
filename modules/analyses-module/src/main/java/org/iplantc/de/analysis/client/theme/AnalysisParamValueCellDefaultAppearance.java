package org.iplantc.de.analysis.client.theme;

import org.iplantc.de.analysis.client.views.widget.cells.AnalysisParamValueCell;
import org.iplantc.de.client.models.analysis.AnalysisParameter;
import org.iplantc.de.client.models.apps.integration.ArgumentType;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class AnalysisParamValueCellDefaultAppearance implements AnalysisParamValueCell.AnalysisParamValueCellAppearance {

    public interface AnalysisParamValueCellStyles extends CssResource {
        String inputType();

        String other();
    }

    public interface AnalysisParamValueCellResources extends ClientBundle {
        @Source("AnalysisParamValueCell.css")
        AnalysisParamValueCellStyles styles();
    }

    interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div class='{0}'>{1}</div>")
        SafeHtml cell(String className, SafeHtml displayValue);
    }

    private final AnalysisParamValueCellResources resources;
    private final Template template;

    public AnalysisParamValueCellDefaultAppearance() {
        this(GWT.<AnalysisParamValueCellResources> create(AnalysisParamValueCellResources.class));
    }

    protected AnalysisParamValueCellDefaultAppearance(AnalysisParamValueCellResources resources) {
        this.resources = resources;
        resources.styles().ensureInjected();
        this.template = GWT.create(Template.class);
    }

    @Override
    public void render(Cell.Context context, AnalysisParameter value, SafeHtmlBuilder sb) {
        String info_type = value.getInfoType();
        // At present,reference genome info types are not supported by DE viewers
        boolean valid_info_type = isValidInputType(info_type);
        SafeHtml displayValue = null;
        if (value.getDisplayValue() != null) {
            displayValue = SafeHtmlUtils.fromString(value.getDisplayValue());
        }

        if (displayValue == null) {
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            displayValue = builder.appendEscaped("").toSafeHtml();
        }
        if ((ArgumentType.Input.equals(value.getType())
                || ArgumentType.FileInput.equals(value.getType()) || ArgumentType.FolderInput.equals(value.getType()))
                && valid_info_type) {
            sb.append(template.cell(resources.styles().inputType(), displayValue));
        } else {
            sb.append(template.cell(resources.styles().other(), displayValue));
        }

    }

    /**
     * FIXME The AnalysisParameter bean needs to use the enum instead of a string.
     * 
     * @param info_type
     * @return
     */
    public boolean isValidInputType(String info_type) {
        return !info_type.equalsIgnoreCase("ReferenceGenome") && !info_type.equalsIgnoreCase("ReferenceSequence") && !info_type.equalsIgnoreCase("ReferenceAnnotation");
    }
}