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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.s2daoplugin.S2DaoConstants;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.S2DaoResourceResolver;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.cache.cache.IComponentCache;
import org.seasar.s2daoplugin.cache.util.TypeUtil;

public class SqlMarkerUtil {

	private static ISqlMarkerCreator creator = new Creator();
	private static ISqlMarkerCreator creatorWithoutRunnable = new CreatorWithoutRunnable();
	
	public static ISqlMarkerCreator getCreator() {
		return creator;
	}
	
	public static ISqlMarkerCreator getCreatorWithoutRunnable() {
		return creatorWithoutRunnable;
	}
	
	public interface ISqlMarkerCreator {
		void remarkAll(IProject project);
		void remark(IType type);
		void markAll(IProject project);
		void mark(IType[] types);
		void mark(IType type);
		void mark(IMethod method);
		void unmarkAll(IProject project);
		void unmark(IType[] types);
		void unmark(IType type);
		void unmark(IMethod method);
	}
	
	private static abstract class AbstractCreator implements ISqlMarkerCreator {
		
		private S2DaoResourceResolver resolver = new S2DaoResourceResolver();
		
		protected ISqlMarker getSqlMarker(IMarker marker) {
			return marker != null ? new SqlMarker(marker) : null;
		}
		
		protected IFile[] getSqlFiles(IMethod method) {
			return resolver.findSqlFiles(method);
		}
		
		protected boolean hasSql(IMethod method) {
			return getSqlFiles(method).length != 0;
		}
		
		protected void run(IJavaElement element, IWorkspaceRunnable runnable) {
			if (element == null || element.getJavaProject() == null) {
				return;
			}
			run(element.getJavaProject().getProject(), runnable);
		}
		
		protected void run(IProject project, IWorkspaceRunnable runnable) {
			if (project == null || runnable == null) {
				return;
			}
			try {
				project.getWorkspace().run(runnable, null);
			} catch (CoreException e) {
				S2DaoPlugin.log(e);
			}
		}
		
		protected IMarker createMarker(IMethod method) {
			if (method.isBinary()) {
				return null;
			}
			if (hasMarker(method)) {
				return setSqlFiles(method);
			}
			try {
				IMarker marker = method.getResource().createMarker(
						S2DaoConstants.ID_SQL_MARKER);
				createMarkerAttributes(marker, method);
				return marker;
			} catch (CoreException e) {
				S2DaoPlugin.log(e);
				return null;
			}
		}
		
		protected IMarker setSqlFiles(IMethod method) {
			ISqlMarker sqlMarker = getSqlMarker(getMarker(method));
			if (sqlMarker == null) {
				return null;
			}
			sqlMarker.setSqlFiles(getSqlFiles(method));
			return sqlMarker.getMarker();
		}
		
		protected void createMarkerAttributes(IMarker marker, IMethod method)
				throws CoreException {
			int start = getStart(method);
			int end = start + getLength(method);
			ISqlMarker sqlMarker = getSqlMarker(marker);
			sqlMarker.setStart(start);
			sqlMarker.setEnd(end);
			setSqlFiles(method);
		}
		
		protected boolean hasMarker(IMethod method) {
			return getMarker(method) != null;
		}
		
		protected IMarker getMarker(IMethod method) {
			if (method == null || method.isBinary()) {
				return null;
			}
			IMarker[] markers = null;
			try {
				markers = method.getResource().findMarkers(
						S2DaoConstants.ID_SQL_MARKER, false, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				S2DaoPlugin.log(e);
				return null;
			}
			for (int i = 0; i < markers.length; i++) {
				try {
					int start = getStart(method);
					int end = start + getLength(method);
					if (start == getStart(markers[i]) && end == getEnd(markers[i])) {
						return markers[i];
					}
				} catch (CoreException e) {
					S2DaoPlugin.log(e);
				}
			}
			return null;
		}
		
		protected int getStart(IMethod method) throws JavaModelException {
			return method.getSourceRange().getOffset();
		}
		
		protected int getLength(IMethod method) throws JavaModelException {
			return method.getSourceRange().getLength();
		}

		protected int getStart(IMarker marker) {
			return marker.getAttribute(IMarker.CHAR_START, -1);
		}
		
		protected int getEnd(IMarker marker) {
			return marker.getAttribute(IMarker.CHAR_END, -1);
		}
		
	}
	
	private static class CreatorWithoutRunnable extends AbstractCreator {

		public void remarkAll(IProject project) {
			unmarkAll(project);
			markAll(project);
		}

		public void remark(IType type) {
			unmark(type);
			mark(type);
		}

		public void markAll(IProject project) {
			IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(project);
			if (cache == null) {
				return;
			}
			mark(cache.getAllAppliedTypes());
		}

		public void mark(IType[] types) {
			if (types == null) {
				return;
			}
			for (int i = 0; i < types.length; i++) {
				mark(types[i]);
			}
		}

		public void mark(IType type) {
			IMethod[] methods = TypeUtil.getMethods(type);
			for (int i = 0; i < methods.length; i++) {
				mark(methods[i]);
			}
		}

		public void mark(IMethod method) {
			if (hasSql(method) &&
					S2DaoUtil.isS2DaoInterceptorAppliedMethod(method)) {
				createMarker(method);
			}
		}

		public void unmarkAll(IProject project) {
			if (project == null) {
				return;
			}
			try {
				project.deleteMarkers(S2DaoConstants.ID_SQL_MARKER,
						false, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				S2DaoPlugin.log(e);
			}
		}

		public void unmark(IType[] types) {
			if (types == null) {
				return;
			}
			for (int i = 0; i < types.length; i++) {
				unmark(types[i]);
			}
		}

		public void unmark(IType type) {
			if (type == null || type.isBinary()) {
				return;
			}
			try {
				type.getResource().deleteMarkers(S2DaoConstants.ID_SQL_MARKER,
						false, IResource.DEPTH_ZERO);
				markAfterUnmark(type.getCompilationUnit().getAllTypes());
			} catch (CoreException e) {
				S2DaoPlugin.log(e);
			}
		}

		public void unmark(IMethod method) {
			ISqlMarker sqlMarker = getSqlMarker(getMarker(method)); 
			if (sqlMarker == null) {
				return;
			}
			if (sqlMarker.getAvailableSqlFileSize() > 0) {
				setSqlFiles(method);
			} else {
				try {
					sqlMarker.getMarker().delete();
				} catch (CoreException e) {
					S2DaoPlugin.log(e);
				}
			}
		}
		
		private void markAfterUnmark(IType[] types) {
			for (int i = 0; i < types.length; i++) {
				try {
					markAfterUnmark(types[i].getTypes());
				} catch (JavaModelException ignore) {
				}
				mark(types[i]);
			}
		}
		
	}

	private static class Creator extends AbstractCreator {

		private ISqlMarkerCreator enclosedCreator = new CreatorWithoutRunnable();
		
		public void remarkAll(final IProject project) {
			if (project == null) {
				return;
			}
			run(project, new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.remarkAll(project);
				}
			});
		}

		public void remark(final IType type) {
			if (type == null) {
				return;
			}
			run(type, new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.remark(type);
				}
			});
		}

		public void markAll(final IProject project) {
			if (project == null) {
				return;
			}
			run(project, new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.markAll(project);
				}
			});
		}

		public void mark(final IType[] types) {
			if (types == null || types.length == 0) {
				return;
			}
			run(types[0], new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.mark(types);
				}
			});
		}

		public void mark(final IType type) {
			if (type == null) {
				return;
			}
			run(type, new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.mark(type);
				}
			});
		}

		public void mark(final IMethod method) {
			if (method == null) {
				return;
			}
			run(method, new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.mark(method);
				}
			});
		}

		public void unmarkAll(final IProject project) {
			if (project == null) {
				return;
			}
			run(project, new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.unmarkAll(project);
				}
			});
		}

		public void unmark(final IType[] types) {
			if (types == null || types.length == 0) {
				return;
			}
			run(types[0], new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.unmark(types);
				}
			});
		}

		public void unmark(final IType type) {
			if (type == null) {
				return;
			}
			run(type, new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.unmark(type);
				}
			});
		}

		public void unmark(final IMethod method) {
			if (method == null) {
				return;
			}
			run(method, new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					enclosedCreator.unmark(method);
				}
			});
		}
		
	}

}
