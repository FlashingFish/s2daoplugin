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
package org.seasar.s2daoplugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
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
import org.seasar.s2daoplugin.cache.IComponentCache;
import org.seasar.s2daoplugin.util.IDEUtil;

public abstract class SQLFileOpenAction
		implements IEditorActionDelegate, IObjectActionDelegate {

	private S2DaoSqlFinder finder = new S2DaoSqlFinder();
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
			if (!isS2DaoComponent(member)) {
				return;
			}
			IDEUtil.openEditors(findSqlFiles(member));
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	protected abstract IJavaElement getSelectedJavaElement() throws CoreException;
	
	private IFile[] findSqlFiles(IMember member) {
		IFile[] sqlFiles = S2DaoConstants.EMPTY_FILES;
		if (member instanceof IMethod) {
			sqlFiles = finder.findSqlFiles((IMethod) member);
			if (sqlFiles.length == 0) {
				if (confirmCreation()) {
					openSqlCreationWizard((IMethod) member);
				}
			}
		} else if (member instanceof IType) {
			sqlFiles = finder.findSqlFiles((IType) member);
		}
		return sqlFiles;
	}
	
	private boolean isS2DaoComponent(IMember member) throws CoreException {
		IProject project = member.getJavaProject().getProject();
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(project);
		if (cache == null) {
			return false;
		}
		IType type = getType(member);
		return type != null && type.isInterface() ? cache.contains(type) : false;
	}
	
	private IType getType(IMember member) {
		return member instanceof IType ? (IType) member :
			member instanceof IMethod ? member.getDeclaringType() : null;
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
		String message = Messages.getMessage("SQLFileOpenAction.creation.confirm.message");
		return MessageDialog.openConfirm(getShell(), title, message);
	}
	
	private void openSqlCreationWizard(IMethod method) {
		IFolder folder = finder.guessSqlStoredFolder(method);
		IResource resource = folder != null ? folder : method.getResource();
		
		SQLCreationWizard wizard = new SQLCreationWizard();
		wizard.setInitialFileName(S2DaoUtil.createSqlFileName(method));
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(resource));
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.open();
	}
	
	private Shell getShell() {
		return shell != null ? shell :
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

}
