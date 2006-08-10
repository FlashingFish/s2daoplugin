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
package org.seasar.s2daoplugin.sqlopener.wizard;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.seasar.s2daoplugin.S2DaoNamingConventions;
import org.seasar.s2daoplugin.S2DaoResourceResolver;

public class SqlCreationWizardUtil {

	private static final S2DaoResourceResolver resolver =
		new S2DaoResourceResolver();
	
	public static int open(IMethod method) {
		return open(method, PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell());
	}
	
	public static int open(IMethod method, Shell shell) {
		IFolder folder = resolver.resolveSqlStoredFolder(method);
		IResource resource = folder != null ? folder : method.getResource();
		SqlCreationWizard wizard = new SqlCreationWizard();
		wizard.setInitialFileName(S2DaoNamingConventions.createSqlFileName(method));
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(resource));
		WizardDialog dialog = new WizardDialog(shell, wizard);
		return dialog.open();
	}

}
