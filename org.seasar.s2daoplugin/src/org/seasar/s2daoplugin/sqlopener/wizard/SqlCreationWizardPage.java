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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.seasar.s2daoplugin.Messages;
import org.seasar.s2daoplugin.S2DaoConstants;
import org.seasar.s2daoplugin.S2DaoNamingConventions;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.util.IDEUtil;

public class SqlCreationWizardPage extends WizardNewFileCreationPage
		implements S2DaoConstants {

	private IWorkbench workbench;
	private String initialFileName;
	private Button openFileCheckBox;
	private ISuffixRadio[] suffixRadios = new ISuffixRadio[0];
	
	public SqlCreationWizardPage(IWorkbench workbench, IStructuredSelection selection) {
		super("SQLCreationPage1", selection);
		setTitle(Messages.getMessage("wizard.SQLCreation.page1.title"));
		setDescription(Messages.getMessage("wizard.SQLCreation.page1.description"));
		this.workbench = workbench;
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite composite = (Composite) getControl();
		
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout(5, false));
		group.setText(Messages.getMessage("wizard.SQLCreation.page1.suffix.group")); 
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
        
		setUpSuffixRadios(group);
        openFileCheckBox = new Button(composite, SWT.CHECK);
        openFileCheckBox.setText(Messages.getMessage("wizard.SQLCreation.page1.openFile"));
        openFileCheckBox.setSelection(true);
        
        setPageComplete(validatePage());
	}
	
	public boolean finish() {
		IFile newFile = createNewFile();
		return newFile != null && openFileCheckBox.getSelection() ?
        	IDEUtil.openEditor(workbench, newFile) != null : true;
	}
	
	public void handleEvent(Event event) {
		if (event.type == SWT.Selection) {
			Widget source = event.widget;
			String filename = initialFileName;
			for (int i = 0; i < suffixRadios.length; i++) {
				if (suffixRadios[i].equals(source)) {
					filename = S2DaoNamingConventions.changeSuffix(
							suffixRadios[i].getSuffix(), filename);
					break;
				}
			}
			// super.createControlを実行中にhandleEventが呼ばれる。
			// 継承元のコントロールが作成途中にsetFileNameが呼ばれると
			// NullPointerExceptionがスローされる。
			if (isPageComplete()) {
				setFileName(filename);
			}
		}
		super.handleEvent(event);
	}
	
	public void setInitialFileName(String filename) {
		setFileName(filename);
		initialFileName = filename;
	}
	
	protected String getNewFileLabel() {
		return Messages.getMessage("wizard.SQLCreation.page1.newFileLabel");
	}
	
	private void setUpSuffixRadios(Group group) {
		Properties props = getDbmsSuffixProperties();
		suffixRadios = new SuffixRadio[DBMS_SUFFIXES.length];
		for (int i = 0; i < DBMS_SUFFIXES.length; i++) {
			String dbmsName = props.getProperty(DBMS_SUFFIXES[i]);
			ISuffixRadio radio = new SuffixRadio(group);
			radio.setSelection(i == 0);
			radio.setSuffix(DBMS_SUFFIXES[i]);
			radio.setDbmsName(dbmsName);
			radio.addSelectionListener(this);
			suffixRadios[i] = radio;
		}
	}
	
	private Properties getDbmsSuffixProperties() {
		String propName = getClass().getPackage().getName().toString()
				.replace('.', '/') + "/dbms_suffix.properties";
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				propName);
		Properties props = new Properties();
		try {
			props.load(is);
		} catch (IOException e) {
			S2DaoPlugin.log(e);
		}
		return props;
	}

}
