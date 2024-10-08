package com.xrosstools.idea.gef.actions;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CodeGenHelper {
    public static StringBuffer getTemplate(String filePath, Class helperClass){
        StringBuffer codeBuf = new StringBuffer();

        BufferedReader reader = new BufferedReader(new InputStreamReader(helperClass.getResourceAsStream(filePath)));
        String line;
        try {
            while((line = reader.readLine()) != null)
                codeBuf.append(line).append('\n');
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                reader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return codeBuf;
    }

    public static void appendDesc(StringBuffer buf, String template, String desc) {
        if(desc != null && desc.length() > 0) {
            buf.append(String.format(template, desc));
        }
    }

    public static void replace(StringBuffer codeBuf, String replacementMark, String replacement){
        int start;
        while((start = codeBuf.indexOf(replacementMark)) >= 0) {
            codeBuf.replace(start, start + replacementMark.length(), replacement);
        }
    }

    public static String toConstantName(String label) {
        label = label.toUpperCase();
        return label.contains(" ") ? label.replace(' ', '_') : label;
    }

    public static String toClassName(String label) {
        StringBuffer clazz = new StringBuffer();
        for(String s: label.split(" ")) {
            clazz.append(capitalize(s));
        }
        return clazz.toString();
    }

    public static String capitalize(String label) {
        char[] charArray = label.toCharArray();
        if (charArray.length > 0) {
            charArray[0] = Character.toUpperCase(charArray[0]);
        }
        return new String(charArray);
    }

    public static String getValue(String value) {
        return value == null? "" : value;
    }

    public static String findResourcesPath(Project project, VirtualFile file) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for (ContentEntry contentEntry : ModuleRootManager.getInstance(module).getContentEntries()) {
                for (SourceFolder sourceFolder : contentEntry.getSourceFolders()) {
                    if(sourceFolder.getFile() == null)
                        continue;

                    if (VfsUtilCore.isAncestor(sourceFolder.getFile(), file, false)) {
                        if (sourceFolder.isTestSource() || sourceFolder.getFile().getPath().contains("resources")) {
                            String resourceRoot = sourceFolder.getFile().getPath();
                            return file.getPath().substring(resourceRoot.length() + 1);//Started with '/'
                        }else{
                            return file.getPath();
                        }
                    }
                }
            }
        }
        return file.getPath();
    }

}
