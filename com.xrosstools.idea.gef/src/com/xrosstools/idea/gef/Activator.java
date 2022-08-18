package com.xrosstools.idea.gef;

import java.util.HashMap;
import java.util.Map;

/**
 * This is actually ICON path factory
 */
public class Activator {
    private static Map<Class, String> reg = new HashMap<>();

	public static final String HOME = "/icons/";
	public static final String ICO = ".png";
	
	public static final String TREE = "tree";
	public static final String NODE = "node";
	public static final String CONNECTION = "connection";

	public static String getIconPath(String iconId) {
		return HOME + iconId + ICO;
	}

    public static String getIconPath(Class clazz) {
        String iconId = reg.get(clazz);
        return HOME + iconId + ICO;
    }

	public static void register(Class clazz, String iconId) {
        reg.put(clazz, iconId);
    }
}
