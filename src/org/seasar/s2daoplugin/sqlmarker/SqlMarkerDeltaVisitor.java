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
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.seasar.s2daoplugin.S2DaoSqlFinder;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.cache.IComponentCache;
import org.seasar.s2daoplugin.util.JavaUtil;

public class SqlMarkerDeltaVisitor implements IResourceDeltaVisitor {

	private IProject project;
	private S2DaoSqlFinder finder = new S2DaoSqlFinder();
	
	public SqlMarkerDeltaVisitor(IProject project) {
		if (project == null) {
			throw new IllegalArgumentException();
		}
		this.project = project;
	}
	
	public boolean visit(IResourceDelta delta) throws CoreException {
		String extension = delta.getFullPath().getFileExtension();
		if ("java".equals(extension)) {
			handleJava(delta);
		} else if ("sql".equals(extension)) {
			handleSql(delta);
		}
		return true;
	}
	
	private void handleJava(IResourceDelta delta) throws CoreException {
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(project);
		if (cache == null) {
			return;
		}
		final IType type = JavaUtil.findPrimaryType(delta.getResource());
		if (!cache.contains(type)) {
			return;
		}
		switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						SqlMarkerUtil.mark(type);
					}
				};
				project.getWorkspace().run(runnable, null);
				break;
			
			case IResourceDelta.CHANGED:
				runnable = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						SqlMarkerUtil.remark(type);
					}
				};
				project.getWorkspace().run(runnable, null);
				break;
		}
	}
	
	private void handleSql(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (!(resource instanceof IFile)) {
			return;
		}
		final IMethod method = finder.findMethodFromSql((IFile) resource);
		if (method == null) {
			return;
		}
		switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						SqlMarkerUtil.mark(method);
					}
				};
				project.getWorkspace().run(runnable, null);
				break;
			
			case IResourceDelta.REMOVED:
				runnable = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						SqlMarkerUtil.unmark(method);
					}
				};
				project.getWorkspace().run(runnable, null);
				break;
		}
	}

}
