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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SQLFileOpenObjectAction extends SQLFileOpenAction {

	private IStructuredSelection structured;
	
	public void selectionChanged(IAction action, ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			structured = null;
			return;
		}
		structured = (IStructuredSelection) selection;
	}
	
	protected IJavaElement getSelectedJavaElement() throws CoreException {
		if (structured == null) {
			return null;
		}
		Object element = structured.getFirstElement();
		if (element instanceof ICompilationUnit) {
			ICompilationUnit unit = (ICompilationUnit) element;
			return unit.findPrimaryType();
		} else if (element instanceof IJavaElement) {
			return (IJavaElement) element;
		}
		return null;
	}

}
