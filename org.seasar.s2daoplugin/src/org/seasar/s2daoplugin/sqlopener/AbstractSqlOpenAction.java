/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.s2daoplugin.sqlopener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.seasar.s2daoplugin.Messages;
import org.seasar.s2daoplugin.S2DaoNamingConventions;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.S2DaoResourceResolver;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.sqlopener.wizard.SqlCreationWizard;
import org.seasar.s2daoplugin.util.IDEUtil;

public abstract class AbstractSqlOpenAction implements IEditorActionDelegate,
		IObjectActionDelegate {

	private S2DaoResourceResolver resolver = new S2DaoResourceResolver();
	private Shell shell;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (targetPart == null) {
			shell = null;
			return;
		}
		shell = targetPart.getSite().getShell();
	}
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor == null) {
			shell = null;
			return;
		}
		shell = targetEditor.getSite().getShell();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	public void run(IAction action) {
		try {
			IMember member = getSelectedJavaMember();
			if (member == null) {
				return;
			}
			if (!isPluginEnabled(member.getJavaProject().getProject())) {
				return;
			}
			IMethod[] methods = findS2DaoInterceptorAppliedMethods(member);
			if (methods.length == 0) {
				return;
			}
			IFile[] sqls = findSqlFiles(methods);
			if (sqls.length == 0 && member instanceof IMethod) {
				if (confirmCreation()) {
					openSqlCreationWizard(methods[0]);
					return;
				}
			}
			IDEUtil.openEditors(sqls);
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	protected abstract IJavaElement getSelectedJavaElement() throws CoreException;
	
	private boolean isPluginEnabled(IProject project) throws CoreException {
		if (!S2DaoPlugin.isEnabled(project)) {
			String title = Messages.getMessage("SQLFileOpenAction.plugindisabled.title");
			String msg = Messages.getMessage("SQLFileOpenAction.plugindisabled.message");
			MessageDialog.openInformation(getShell(), title, msg);
			return false;
		}
		return true;
	}
	
	private IFile[] findSqlFiles(IMethod[] methods) {
		Set sqlFiles = new HashSet();
		for (int i = 0; i < methods.length; i++) {
			sqlFiles.addAll(Arrays.asList(resolver.findSqlFiles(methods[i])));
		}
		return (IFile[]) sqlFiles.toArray(new IFile[sqlFiles.size()]);
	}
	
	private IMethod[] findS2DaoInterceptorAppliedMethods(IMember member) throws JavaModelException {
		IMethod[] methods = getMethods(member);
		Set result = new HashSet();
		for (int i = 0; i < methods.length; i++) {
			if (S2DaoUtil.isS2DaoInterceptorAppliedMethod(methods[i])) {
				result.add(methods[i]);
			}
		}
		return (IMethod[]) result.toArray(new IMethod[result.size()]);
	}
	
	private IMethod[] getMethods(IMember member) throws JavaModelException {
		if (member instanceof IType) {
			return ((IType) member).getMethods();
		} if (member instanceof IMethod) {
			return new IMethod[] {(IMethod) member};
		}
		return new IMethod[0];
	}
	
	private IMember getSelectedJavaMember() throws CoreException {
		IJavaElement element = getSelectedJavaElement();
		if (element instanceof IMember) {
			return (IMember) element;
		}
		return null;
	}
	
	private boolean confirmCreation() {
		String title = Messages.getMessage("SQLFileOpenAction.creation.confirm.title");
		String msg = Messages.getMessage("SQLFileOpenAction.creation.confirm.message");
		return MessageDialog.openConfirm(getShell(), title, msg);
	}
	
	private void openSqlCreationWizard(IMethod method) {
		IFolder folder = resolver.resolveSqlStoredFolder(method);
		IResource resource = folder != null ? folder : method.getResource();
		
		SqlCreationWizard wizard = new SqlCreationWizard();
		wizard.setInitialFileName(S2DaoNamingConventions.createSqlFileName(method));
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(resource));
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.open();
	}
	
	private Shell getShell() {
		return shell != null ? shell :
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

}
