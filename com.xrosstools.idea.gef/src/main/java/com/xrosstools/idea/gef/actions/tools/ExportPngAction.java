package com.xrosstools.idea.gef.actions.tools;

import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.gef.GefIcons;

import java.awt.event.ActionEvent;

public class ExportPngAction extends Action {
    public static final String ID = "export_png";
    public static final String NAME = "Export";

    public ExportPngAction() {
        setText(NAME);
        setTooltip("Export diagram to PNG file");
        setIcon(GefIcons.EXPORT_PNG);
    }

    @Override
    public void actionPerformed(ActionEvent anActionEvent) {
        getPlatformFacade().saveAsPNG();
    }
}
