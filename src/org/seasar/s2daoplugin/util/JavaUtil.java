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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.s2daoplugin.S2DaoPlugin;

public class JavaUtil {

	private static final IPath[] EMPTY_PATHS = new IPath[0];
	
	public static IPath[] getSourceFolderPaths(IJavaProject project) {
		try {
			IClasspathEntry[] buildPaths = project.getRawClasspath();
			List sourcePathList = new LinkedList();
			for (int i = 0; i < buildPaths.length; i++) {
				if (buildPaths[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					sourcePathList.add(buildPaths[i].getPath());
				}
			}
			return (IPath[]) sourcePathList.toArray(new IPath[sourcePathList.size()]);
		} catch (JavaModelException e) {
			S2DaoPlugin.log(e);
			return EMPTY_PATHS;
		}
	}
	
	public static void visitSourceFolders(IJavaProject project, String packageName,
			IResourceVisitor visitor) {
		if (project == null || packageName == null || visitor == null) {
			return;
		}
		IPath[] srcPaths = getSourceFolderPaths(project);
		for (int i = 0; i < srcPaths.length; i++) {
			try {
				IPackageFragment pack =
					project.findPackageFragment(srcPaths[i].append(toPathName(packageName)));
				if (pack == null) {
					continue;
				}
				pack.getResource().accept(visitor, IResource.DEPTH_INFINITE, false);
			} catch (CoreException e) {
				S2DaoPlugin.log(e);
			}
		}
	}
	
	public static IType findPrimaryType(IResource resource) {
		if (resource == null || !"java".equals(resource.getFileExtension())) {
			return null;
		}
		Object obj = resource.getAdapter(IJavaElement.class);
		if (!(obj instanceof ICompilationUnit)) {
			return null;
		}
		return ((ICompilationUnit) obj).findPrimaryType();
	}
	
	private static String toPathName(String packageName) {
		return packageName.replace('.', '/');
	}

}