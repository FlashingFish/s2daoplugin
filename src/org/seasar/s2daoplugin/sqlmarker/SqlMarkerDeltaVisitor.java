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
package org.seasar.s2daoplugin.sqlmarker;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.kijimuna.core.KijimunaCore;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.S2DaoSqlFinder;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.cache.cache.IComponentCache;
import org.seasar.s2daoplugin.sqlmarker.SqlMarkerUtil.ISqlMarkerCreator;
import org.seasar.s2daoplugin.util.JavaProjectUtil;
import org.seasar.s2daoplugin.util.JavaUtil;

public class SqlMarkerDeltaVisitor implements IResourceDeltaVisitor {

	private IProject project;
	private ISqlMarkerCreator marker = SqlMarkerUtil.getCreator();
	private S2DaoSqlFinder finder = new S2DaoSqlFinder();
	
	public SqlMarkerDeltaVisitor(IProject project) {
		if (project == null) {
			throw new IllegalArgumentException();
		}
		this.project = project;
	}
	
	public boolean visit(IResourceDelta delta) throws CoreException {
		if (JavaUtil.isJavaFile(delta.getResource())) {
			handleJava(delta);
		} else if ("sql".equalsIgnoreCase(delta.getFullPath().getFileExtension())) {
			handleSql(delta);
		} else if (".project".equals(delta.getResource().getName())) {
			handleDotProject(delta);
		}
		return true;
	}
	
	private void handleJava(IResourceDelta delta) {
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(project);
		if (cache == null) {
			return;
		}
		IJavaElement element = JavaCore.create(delta.getResource());
		if (!(element instanceof ICompilationUnit)) {
			return;
		}
		IType[] types = null;
		try {
			types = ((ICompilationUnit) element).getAllTypes();
		} catch (JavaModelException e) {
			S2DaoPlugin.log(e);
			return;
		}
		for (int i = 0; i < types.length; i++) {
			marker.unmark(types[i]);
			if (!cache.contains(types[i])) {
				continue;
			}
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					marker.mark(types[i]);
					break;
				
				case IResourceDelta.CHANGED:
					marker.remark(types[i]);
					break;
			}
		}
	}
	
	private void handleSql(IResourceDelta delta) {
		IResource resource = delta.getResource();
		if (!(resource instanceof IFile) ||
				!JavaProjectUtil.isInSourceFolder(resource)) {
			return;
		}
		IMethod method = finder.findMethodFromSql((IFile) resource);
		if (method == null) {
			return;
		}
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(project);
		if (cache == null || !cache.contains(method.getDeclaringType())) {
			return;
		}
		switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				marker.mark(method);
				break;
			
			case IResourceDelta.REMOVED:
				marker.unmark(method);
				break;
		}
	}
	
	private void handleDotProject(IResourceDelta delta) throws CoreException {
		if (project.hasNature(KijimunaCore.ID_NATURE_DICON)) {
			marker.markAll(project);
		} else {
			marker.unmarkAll(project);
		}
	}

}
