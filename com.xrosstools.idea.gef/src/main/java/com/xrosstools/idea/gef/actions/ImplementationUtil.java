package com.xrosstools.idea.gef.actions;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.OpenSourceUtil;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.xrosstools.idea.gef.ContextMenuProvider.*;

/**
 * To avoid Slow Operation is prohibited in EDT:
 * For method that returns value, do time-consuming job without backend thread. E.g. findClass
 * For void method do time-consuming job in backend thread. E.g. openImpl
 */
public class ImplementationUtil implements ImplementationMessages {
    public static final String SEPARATOR = "::";
    public static final String DEFAULT_METHOD = "#default";
    public static final String PROPERTY_KEY_PREFIX = "PROP_KEY";

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String getClassName(String implementation) {
        return implementation.contains(SEPARATOR) ? implementation.split(SEPARATOR)[0] : implementation;
    }
    public static String getMethodName(String implementation) {
        return implementation.contains(SEPARATOR) ? implementation.split(SEPARATOR)[1] : DEFAULT_METHOD;
    }

    public static String replaceMethodName(String implementation, String methodName) {
        if(DEFAULT_METHOD.equals(methodName) || methodName == null || methodName.trim().isEmpty())
            return implementation.split(SEPARATOR)[0];
        else
            return implementation.split(SEPARATOR)[0] + SEPARATOR + methodName;
    }

    public static PsiClass findClass(Project project, String className) {
        return ApplicationManager.getApplication().runReadAction((Computable<PsiClass>) () -> {
            return JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
        });
    }

    public static List<PsiMethod> getMethods(Project project, String currentImpl) {
        PsiClass psiClass = findClass(project, currentImpl);

        if (psiClass == null)
            return Collections.emptyList();

        List<PsiMethod> methods = new ArrayList<>();
        for (PsiMethod m : psiClass.getMethods()) {
            if (m.isConstructor()) continue;

            methods.add(m);
        }

        return methods;
    }

    public static List<PsiMethod> getMethodsInEDT(Project project, String currentImpl) {
        final List<PsiMethod>[] result = new List[1];
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            // 后台线程执行
            result[0] = ApplicationManager.getApplication().runReadAction((Computable<List<PsiMethod>>) () -> {
                return getMethods(project, currentImpl);
            });
        }, "Loading Methods", true, project);
        return result[0];
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

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            PsiClass psiClass = findClass(project, className);
            ApplicationManager.getApplication().invokeLater(() -> {
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
            });
        });
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

    public static void buildImplementationMenu(Project project, JPopupMenu menu, IPropertySource source, String propertyName, boolean allowMethod) {
        String implementation = (String)source.getPropertyValue(propertyName);
        if(isEmpty(implementation))
            menu.add(createItem(new AssignImplementationAction(project, source, propertyName)));
        else{
            menu.add(createItem(new ChangeImplementationAction(project, source, propertyName)));
            menu.add(createItem(new RemoveImplementationAction(source, propertyName)));
            menu.add(createItem(new OpenImplementationAction(project, propertyName, implementation)));

            if(allowMethod)
                buildReferenceMethodMenu(project, menu, source, propertyName, implementation);
        }
    }

    public static void buildReferenceMethodMenu(Project project, JPopupMenu menu, IPropertySource source, String propertyName, String implementation) {
        String className = getClassName(implementation);
        String methodName = getMethodName(implementation);

        JMenu methodMenu = new JMenu(REFERENCE_METHOD_MSG + methodName);
        methodMenu.add(createItem(new ChangeMethodAction(source, propertyName, DEFAULT_METHOD, false)));
        for(PsiMethod m: getMethodsInEDT(project, className)) {
            methodMenu .add(createItem(new ChangeMethodAction(source, propertyName, m.getName(), m.hasModifierProperty(PsiModifier.PRIVATE))));
        }
        menu.add(methodMenu);
    }

    public static List<String> getPropertyKeysInEDT(Project project, String implementation) {
        final List<String>[] result = new List[1];
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            // 后台线程执行
            result[0] = ApplicationManager.getApplication().runReadAction((Computable<List<String>>) () -> {
                return getPropertyKeys(project, implementation);
            });
        }, "Loading Predefined Keys", true, project);
        return result[0];
    }

    public static List<String> getPropertyKeys(Project project, String implementation) {
        List<String> propKeys = new ArrayList<>();
        if (isEmpty(implementation))
            return propKeys;

        String className = getClassName(implementation);
        PsiClass type = findClass(project, className);

        if (null == type) return propKeys;

        for (PsiField f : type.getFields()) {
            if (f.getNameIdentifier().getText().startsWith(PROPERTY_KEY_PREFIX) && f.getType().getPresentableText().equals("String")) {
                String text = f.getText();
                int start = text.indexOf('"');
                if (start <= 0)
                    continue;

                int end = text.indexOf('"', start + 1);
                text = text.substring(start + 1, end);
                propKeys.add(text);
            }
        }

        return propKeys;
    }
}
