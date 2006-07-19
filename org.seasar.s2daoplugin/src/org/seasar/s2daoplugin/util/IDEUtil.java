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
package org.seasar.s2daoplugin.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.seasar.s2daoplugin.S2DaoPlugin;

public class IDEUtil {

	public static IEditorPart[] openEditors(IFile[] files) {
		if (files == null) {
			return null;
		}
		List editorList = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			IEditorPart editor = openEditor(files[i]);
			if (editor != null) {
				editorList.add(editor);
			}
		}
		return (IEditorPart[]) editorList.toArray(new IEditorPart[editorList.size()]);
	}
	
	public static IEditorPart openEditor(IFile file) {
		if (file == null) {
			return null;
		}
		return openEditor(PlatformUI.getWorkbench(), file);
	}
	
	public static IEditorPart openEditor(IWorkbench workbench, IFile file) {
		if (workbench == null || file == null) {
			return null;
		}
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			try {
				return IDE.openEditor(page, file);
			} catch (PartInitException e) {
				S2DaoPlugin.log(e);
			}
		}
		return null;
	}
}
