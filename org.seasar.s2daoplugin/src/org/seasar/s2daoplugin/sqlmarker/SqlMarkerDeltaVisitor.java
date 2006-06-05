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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.KijimunaCore;
import org.seasar.s2daoplugin.S2DaoSqlFinder;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.cache.IComponentCache;
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
		String extension = delta.getFullPath().getFileExtension();
		if ("java".equalsIgnoreCase(extension)) {
			handleJava(delta);
		} else if ("sql".equalsIgnoreCase(extension)) {
			handleSql(delta);
		} else if (".project".equals(delta.getResource().getName())) {
			handleDotProject(delta);
		}
		return true;
	}
	
	private void handleJava(IResourceDelta delta) {
		final IType type = JavaUtil.findPrimaryType(delta.getResource());
		marker.unmark(type);
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(project);
		if (cache == null) {
			return;
		}
		if (!cache.contains(type)) {
			return;
		}
		switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				marker.mark(type);
				break;
			
			case IResourceDelta.CHANGED:
				marker.remark(type);
				break;
		}
	}
	
	private void handleSql(IResourceDelta delta) {
		IResource resource = delta.getResource();
		if (!(resource instanceof IFile) ||
				!JavaProjectUtil.isInSourceFolder(resource)) {
			return;
		}
		final IMethod method = finder.findMethodFromSql((IFile) resource);
		if (method == null) {
			return;
		}
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(project);
		if (cache == null) {
			return;
		}
		IType type = method.getDeclaringType();
		if (!cache.contains(type)) {
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
