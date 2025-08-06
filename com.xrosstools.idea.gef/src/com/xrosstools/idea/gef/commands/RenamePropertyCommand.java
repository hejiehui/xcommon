package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.util.PropertyEntry;
import com.xrosstools.idea.gef.util.PropertyEntrySource;

public class RenamePropertyCommand extends InputTextCommand {
	private String category;
	private PropertyEntrySource properties;
	private String newName;
	private String oldName;
	private PropertyEntry newEntry;
	private PropertyEntry oldEntry;

	public RenamePropertyCommand(
			String category,
			PropertyEntrySource properties,
			String oldName){
		this.category = category;
		this.properties = properties;
		this.oldName = oldName;
	}
	
	public void execute() {
		newName = getInputText();
		oldEntry = properties.unregister(category, oldName);
		newEntry = new PropertyEntry(newName, oldEntry.get()).setCategory(category);
		properties.register(newEntry);
		newEntry.set(oldEntry.get());
	}
	
    public String getLabel() {
        return "Rename property";
    }

    public void redo() {
		properties.unregister(category, oldName);
		properties.register(category, newEntry);
    }

    public void undo() {
		properties.unregister(category, newName);
		properties.register(category, oldEntry);
    }
}
