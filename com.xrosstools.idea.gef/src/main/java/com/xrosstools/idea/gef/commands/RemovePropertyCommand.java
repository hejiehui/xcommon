package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.util.PropertyEntry;
import com.xrosstools.idea.gef.util.PropertyEntrySource;

public class RemovePropertyCommand extends Command {
	private String category;
	private PropertyEntrySource properties;
	private String key;
	private PropertyEntry value;
	
	public RemovePropertyCommand(
			String category,
			PropertyEntrySource properties,
			String key){
		this.category = category;
		this.properties = properties;
		this.key = key;
	}
	
	public void execute() {
		value = properties.unregister(category, key);
	}
	
    public String getLabel() {
        return "Remove property";
    }

    public void redo() {
        execute();
    }

    public void undo() {
    	properties.register(category, value);
    }
}
