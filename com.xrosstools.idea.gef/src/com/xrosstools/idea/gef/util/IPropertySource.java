package com.xrosstools.idea.gef.util;

import java.beans.PropertyChangeSupport;

public interface IPropertySource {
    PropertyChangeSupport getListeners();
    IPropertyDescriptor[] getPropertyDescriptors();
    Object getPropertyValue(Object id);
    void setPropertyValue(Object id, Object value);

    default IPropertyDescriptor[] getPropertyDescriptors(String category) {return getPropertyDescriptors();}
    default Object getPropertyValue(String category, Object id) {return getPropertyValue(id);}
    default void setPropertyValue(String category, Object id, Object value) {setPropertyValue(id, value);}
    default Object getEditableValue(){return this;}
    default boolean isPropertySet(Object id){return true;}
    default void resetPropertyValue(Object id){}
}
