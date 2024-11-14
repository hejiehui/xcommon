package com.xrosstools.idea.gef.actions;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.OpenSourceUtil;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.xrosstools.idea.gef.ContextMenuProvider.*;

public class ImplementationUtil implements ImplementationMessages {
    public static final String SEPARATOR = "::";
    public static final String DEFAULT_METHOD = "#default";

    public static boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    public static String getClassName(String implementation) {
        return implementation.contains(SEPARATOR) ? implementation.split(SEPARATOR)[0] : implementation;
    }
    public static String getMethodName(String implementation) {
        return implementation.contains(SEPARATOR) ? implementation.split(SEPARATOR)[1] : DEFAULT_METHOD;
    }

    public static PsiClass findClass(Project project, String className) {
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));

        if (psiClass == null)
            return null;

        return psiClass;
    }

    public static PsiMethod findMethod(Project project, String className, String methodName) {
        PsiClass psiClass = findClass(project, className);
        if(psiClass == null)
            return null;

        PsiMethod[] methods = psiClass.findMethodsByName(methodName, false);

        return methods.length == 0 ? null : psiClass.findMethodsByName(methodName, false)[0];
    }

    public static void openImpl(Project project, String implementation) {
        String className = getClassName(implementation);
        String methodName = getMethodName(implementation);

        GlobalSearchScope scope = GlobalSearchScope.allScope (project);

        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, scope);
        if (null == psiClass) {
            Messages.showErrorDialog("Can not open " + className, "Error");
        }else {
            if(DEFAULT_METHOD.equals(methodName))
                OpenSourceUtil.navigate(psiClass);
            else {
                PsiMethod[] methods = psiClass.findMethodsByName(methodName, false);
                if(methods.length != 0)
                    OpenSourceUtil.navigate(methods[0]);
                else
                    Messages.showErrorDialog(String.format("Can't find \"%s\" for class %s", methodName, className), "Method not found");
            }
        }
    }

    public static String assignImpl(Project project, String currentImpl) {
        TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project).createAllProjectScopeChooser(currentImpl);
        PsiClass selected = null;
        chooser.showDialog();
        selected = chooser.getSelected();

        if(selected == null)
            return currentImpl;

        return selected.getQualifiedName();
    }

    public static List<PsiMethod> getMethods(Project project, String currentImpl) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(currentImpl, scope);

        if (psiClass == null)
            return Collections.emptyList();

        List<PsiMethod> methods = new ArrayList<>();
        for (PsiMethod m : psiClass.getMethods()) {
            if (m.isConstructor()) continue;

            methods.add(m);
        }

        return methods;
    }

    public static String replaceMethodName(String implementation, String methodName) {
        if(DEFAULT_METHOD.equals(methodName) || methodName == null || methodName.trim().length() == 0)
            return implementation.split(SEPARATOR)[0];
        else
            return implementation.split(SEPARATOR)[0] + SEPARATOR + methodName;
    }


    public static void buildImplementationMenu(Project project, JPopupMenu menu, IPropertySource source, String propertyName, boolean allowMethod) {
        String implementation = (String)source.getPropertyValue(propertyName);
        if(isEmpty(implementation))
            menu.add(createItem(new AssignImplementationAction(project, source, propertyName)));
        else{
            menu.add(createItem(new ChangeImplementationAction(project, source, propertyName)));
            menu.add(createItem(new RemoveImplementationAction(source, propertyName)));
            menu.add(createItem(new OpenImplementationAction(project, propertyName, implementation)));

            if(!allowMethod)
                return;

            String className = getClassName(implementation);
            String methodName = getMethodName(implementation);
            JMenu methodMenu = new JMenu(REFERENCE_METHOD_MSG + methodName);
            methodMenu.add(createItem(new ChangeMethodAction(source, propertyName, DEFAULT_METHOD, false)));
            for(PsiMethod m: getMethods(project, className)) {
                methodMenu .add(createItem(new ChangeMethodAction(source, propertyName, m.getName(), m.hasModifierProperty(PsiModifier.PRIVATE))));
            }
            menu.add(methodMenu);
        }
    }
}
