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
package org.seasar.s2daoplugin.property;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.seasar.s2daoplugin.Messages;
import org.seasar.s2daoplugin.S2DaoConstants;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.sqlmarker.SqlMarkerUtil;

public class S2DaoPropertyPage extends PropertyPage {

	private Button useS2DaoPlugin;
	
	public S2DaoPropertyPage() {
	}
	
	public boolean performOk() {
		S2DaoPlugin plugin = S2DaoPlugin.getDefault();
		try {
			IWorkspaceRunnable runnable = null;
			if (useS2DaoPlugin.getSelection()) {
				plugin.addS2DaoNature(getProject());
				runnable = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor)
							throws CoreException {
						SqlMarkerUtil.markAll(getProject());
					}
				};
			} else {
				plugin.removeS2DaoNature(getProject());
				runnable = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor)
							throws CoreException {
						SqlMarkerUtil.unmarkAll(getProject());
					}
				};
				S2DaoUtil.removeS2DaoComponentCache(getProject());
			}
			getProject().getWorkspace().run(runnable, null);
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
		return true;
	}
	
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Control composite = addControl(parent);
		useS2DaoPlugin.setSelection(hasS2DaoNature());
		return composite;
	}
	
	private Control addControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setData(data);
		
		useS2DaoPlugin = new Button(composite, SWT.CHECK);
		useS2DaoPlugin.setText(Messages.getMessage("PropertyPage.usePlugin"));
		useS2DaoPlugin.setFont(parent.getFont());
		return composite;
	}
	
	private IProject getProject() {
		return (IProject) getElement();
	}
	
	private boolean hasS2DaoNature() {
		try {
			return getProject().hasNature(S2DaoConstants.S2DAO_NATURE);
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
			return false;
		}
	}

}
