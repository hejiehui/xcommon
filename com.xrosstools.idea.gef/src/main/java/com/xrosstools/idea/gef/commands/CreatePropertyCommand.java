package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.util.DataTypeEnum;
import com.xrosstools.idea.gef.util.PropertyEntry;
import com.xrosstools.idea.gef.util.PropertyEntrySource;

public class CreatePropertyCommand extends InputTextCommand {
	private PropertyEntrySource properties;
	private String category;
	private String key;
	private DataTypeEnum type;
	private PropertyEntry newEntry;
	private PropertyEntry oldEntry;
	
	public CreatePropertyCommand(
			String category,
			PropertyEntrySource properties,
			DataTypeEnum type){
		this.category = category;
		this.properties = properties;
		this.type = type;
	}
	
	public void execute() {
		key = getInputText();
		if(newEntry == null)
			newEntry = new PropertyEntry(key, type.defaultValue()).setCategory(category);

		if(properties.containsKey(category, key))
			oldEntry = properties.unregister(category, key);

		properties.register(newEntry);
	}
	
    public String getLabel() {
        return "Add property";
    }

    public void redo() {
		if(oldEntry != null)
			properties.unregister(category, key);
		properties.register(category, newEntry);
    }

    public void undo() {
		properties.unregister(category, key);
		if(oldEntry != null)
			properties.register(category, oldEntry);
    }
}
