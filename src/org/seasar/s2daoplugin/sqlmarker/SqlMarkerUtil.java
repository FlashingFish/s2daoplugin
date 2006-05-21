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

import java.util.HashMap;
import java.util.Map;

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
import org.seasar.s2daoplugin.S2DaoSqlFinder;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.cache.IComponentCache;

public class SqlMarkerUtil {

	private static SqlMarkerCreator creator = new Creator();
	private static SqlMarkerCreator creatorWithoutRunnable = new CreatorWithoutRunnable();
	
	public static SqlMarkerCreator getCreator() {
		return creator;
	}
	
	public static SqlMarkerCreator getCreatorWithoutRunnable() {
		return creatorWithoutRunnable;
	}
	
	
	public interface SqlMarkerCreator {
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
	
	private static abstract class AbstractCreator implements SqlMarkerCreator {
		
		private S2DaoSqlFinder finder = new S2DaoSqlFinder();
		
		protected boolean hasSql(IMethod method) {
			return finder.findSqlFiles(method).length != 0;
		}
		
		protected void run(IJavaElement element, IWorkspaceRunnable runnable) {
			run(element.getJavaProject().getProject(), runnable);
		}
		
		protected void run(IProject project, IWorkspaceRunnable runnable) {
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
			try {
				IMarker marker = method.getResource().createMarker(S2DaoConstants.SQL_MARKER);
				marker.setAttributes(createMarkerAttributes(method));
				return marker;
			} catch (CoreException e) {
				S2DaoPlugin.log(e);
				return null;
			}
		}
		
		protected Map createMarkerAttributes(IMethod method) throws JavaModelException {
			int start = getStart(method);
			int end = start + getLength(method);
			Map map = new HashMap();
			map.put(IMarker.CHAR_START, new Integer(start));
			map.put(IMarker.CHAR_END, new Integer(end));
			return map;
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
			if (type == null) {
				return;
			}
			try {
				IMethod[] methods = type.getMethods();
				for (int i = 0; i < methods.length; i++) {
					mark(methods[i]);
				}
			} catch (JavaModelException e) {
				S2DaoPlugin.log(e);
			}
		}

		public void mark(IMethod method) {
			if (method == null) {
				return;
			}
			if (hasSql(method)) {
				createMarker(method);
			}
		}

		public void unmarkAll(IProject project) {
			if (project == null) {
				return;
			}
			try {
				project.deleteMarkers(S2DaoConstants.SQL_MARKER,
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
			if (type == null) {
				return;
			}
			try {
				type.getResource().deleteMarkers(S2DaoConstants.SQL_MARKER,
						false, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				S2DaoPlugin.log(e);
			}
		}

		public void unmark(IMethod method) {
			if (method == null) {
				return;
			}
			IMarker[] markers = null;
			try {
				markers = method.getResource().findMarkers(
						S2DaoConstants.SQL_MARKER, false, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				S2DaoPlugin.log(e);
				return;
			}
			for (int i = 0; i < markers.length; i++) {
				try {
					int start = getStart(method);
					int end = start + getLength(method);
					if (start == getStart(markers[i]) && end == getEnd(markers[i])) {
						markers[i].delete();
						return;
					}
				} catch (CoreException e) {
					S2DaoPlugin.log(e);
				}
			}
		}
		
	}

	private static class Creator extends AbstractCreator {

		private SqlMarkerCreator enclosedCreator = new CreatorWithoutRunnable();
		
		public void remarkAll(final IProject project) {
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
