package org.iplantc.de.theme.base.client.diskResource.grid;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Created by jstroot on 1/26/15.
 * @author jstroot
 */
public interface GridViewDisplayStrings extends Messages{
    @Key("noItemsToDisplay")
    String noItemsToDisplay();

    @Key("pathFieldEmptyText")
    String pathFieldEmptyText();

    @Key("pathFieldLabel")
    SafeHtml pathFieldLabel();
}
