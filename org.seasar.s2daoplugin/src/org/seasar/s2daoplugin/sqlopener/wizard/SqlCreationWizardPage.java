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
import org.seasar.s2daoplugin.util.IDEUtil;

public class SqlCreationWizardPage extends WizardNewFileCreationPage
		implements S2DaoConstants {

	private IWorkbench workbench;
	private String initialFileName;
	private Button defaultSuffix;
	private Button oracleSuffix;
	private Button db2Suffix;
	private Button mssqlSuffix;
	private Button mysqlSuffix;
	private Button postgresqlSuffix;
	private Button firebirdSuffix;
	private Button hsqlSuffx;
	private Button openFileCheckBox;
	
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
        
		buildSuffixRadio(group);
        openFileCheckBox = new Button(composite, SWT.CHECK);
        openFileCheckBox.setText(Messages.getMessage("wizard.SQLCreation.page1.openFile"));
        openFileCheckBox.setSelection(true);
        
        setPageComplete(validatePage());
	}
	
	public boolean finish() {
		IFile newFile = createNewFile();
		if (newFile == null) {
			return false;
		}
        if (openFileCheckBox.getSelection()) {
        	return IDEUtil.openEditor(workbench, newFile) != null;
        }
        return true;
	}
	
	public void handleEvent(Event event) {
		if (event.type == SWT.Selection) {
			Widget source = event.widget;
			String filename = initialFileName;
			if (defaultSuffix == source) {
				filename = S2DaoNamingConventions.changeSuffix(SUFFIX_DEFAULT, filename);
			} else if (oracleSuffix == source) {
				filename = S2DaoNamingConventions.changeSuffix(SUFFIX_ORACLE, filename);
			} else if (db2Suffix == source) {
				filename = S2DaoNamingConventions.changeSuffix(SUFFIX_DB2, filename);
			} else if (mssqlSuffix == source) {
				filename = S2DaoNamingConventions.changeSuffix(SUFFIX_MSSQL, filename);
			} else if (mysqlSuffix == source) {
				filename = S2DaoNamingConventions.changeSuffix(SUFFIX_MYSQL, filename);
			} else if (postgresqlSuffix == source) {
				filename = S2DaoNamingConventions.changeSuffix(SUFFIX_POSTGRESQL, filename);
			} else if (firebirdSuffix == source) {
				filename = S2DaoNamingConventions.changeSuffix(SUFFIX_FIREBIRD, filename);
			} else if (hsqlSuffx == source) {
				filename = S2DaoNamingConventions.changeSuffix(SUFFIX_HSQLDB, filename);
			}
			// super.createControlを実行中にhandleEventが呼ばれる。
			// 継承元のコントロールが作成途中にsetFileNameが呼ばれるとNullPointerExceptionが
			// スローされる。
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
	
	private void buildSuffixRadio(Group group) {
		// default
        defaultSuffix = new Button(group, SWT.RADIO);
        defaultSuffix.setText(Messages.getMessage("wizard.SQLCreation.page1.suffix.default"));
        defaultSuffix.setSelection(true);
        defaultSuffix.addListener(SWT.Selection, this);
        
        // oracle
        oracleSuffix = new Button(group, SWT.RADIO);
        oracleSuffix.setText(Messages.getMessage("wizard.SQLCreation.page1.suffix.oracle"));
        oracleSuffix.addListener(SWT.Selection, this);
        
        // db2
        db2Suffix = new Button(group, SWT.RADIO);
        db2Suffix.setText(Messages.getMessage("wizard.SQLCreation.page1.suffix.db2"));
        db2Suffix.addListener(SWT.Selection, this);
        
        // mssql
        mssqlSuffix = new Button(group, SWT.RADIO);
        mssqlSuffix.setText(Messages.getMessage("wizard.SQLCreation.page1.suffix.mssql"));
        mssqlSuffix.addListener(SWT.Selection, this);
        
        // mysql
        mysqlSuffix = new Button(group, SWT.RADIO);
        mysqlSuffix.setText(Messages.getMessage("wizard.SQLCreation.page1.suffix.mysql"));
        mysqlSuffix.addListener(SWT.Selection, this);
        
        // postgresql
        postgresqlSuffix = new Button(group, SWT.RADIO);
        postgresqlSuffix.setText(Messages.getMessage("wizard.SQLCreation.page1.suffix.postgresql"));
        postgresqlSuffix.addListener(SWT.Selection ,this);
        
        // firebird
        firebirdSuffix = new Button(group, SWT.RADIO);
        firebirdSuffix.setText(Messages.getMessage("wizard.SQLCreation.page1.suffix.firebird"));
        firebirdSuffix.addListener(SWT.Selection, this);
        
        // hsql
        hsqlSuffx = new Button(group, SWT.RADIO);
        hsqlSuffx.setText(Messages.getMessage("wizard.SQLCreation.page1.suffix.hsql"));
        hsqlSuffx.addListener(SWT.Selection, this);
	}

}
