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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class SQLFileOpenEditorAction extends SQLFileOpenAction {

	private ITextEditor editor;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		super.setActiveEditor(action, targetEditor);
		if (!(targetEditor instanceof ITextEditor)) {
			editor = null;
			return;
		}
		editor = (ITextEditor) targetEditor;
	}
	
	protected IJavaElement getSelectedJavaElement() throws CoreException {
		ICompilationUnit unit = getCompilationUnit();
		if (unit == null) {
			return null;
		}
		ITextSelection selection = getSelectedText();
		if (selection == null) {
			return null;
		}
		IJavaElement[] selectedElements =
			unit.codeSelect(selection.getOffset(), selection.getLength());
		if (selectedElements.length != 1) {
			return null;
		}
		return selectedElements[0];
	}
	
	private ICompilationUnit getCompilationUnit() {
		if (editor == null) {
			return null;
		}
		Object unit = editor.getEditorInput().getAdapter(IJavaElement.class);
		if (unit instanceof ICompilationUnit) {
			return (ICompilationUnit) unit;
		}
		return null;
	}
	
	private ITextSelection getSelectedText() {
		if (editor == null) {
			return null;
		}
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			return (ITextSelection) selection;
		}
		return null;
	}

}
