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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.s2daoplugin.S2DaoPlugin;

public class JavaProjectUtil {

	public static boolean isInSourceFolder(IResource resource) {
		return resource != null && JavaCore.create(resource.getParent()) != null;
	}
	
	public static IPackageFragmentRoot findPackageFragmentRoot(IType type) {
		return type != null ? ascend(type.getPackageFragment()) : null;
	}
	
	public static IPackageFragmentRoot findPackageFragmentRoot(IResource resource) {
		 return resource != null ? ascend(JavaCore.create(resource.getParent())) : null;
	}
	
	public static IPackageFragmentRoot[] findPackageFragmentRootsSharedOutputLocation(
			IResource resource) {
		return findPackageFragmentRootsSharedOutputLocation(findPackageFragmentRoot(
				resource));
	}
	
	public static IPackageFragmentRoot[] findPackageFragmentRootsSharedOutputLocation(
			IPackageFragmentRoot root) {
		if (root == null) {
			return new IPackageFragmentRoot[0];
		}
		IJavaProject project = root.getJavaProject();
		Set result = new HashSet();
		try {
			IPath output1 = root.getRawClasspathEntry().getOutputLocation();
			if (output1 == null) {
				output1 = project.getOutputLocation();
			}
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getKind() == IPackageFragmentRoot.K_BINARY) {
					continue;
				}
				IPath output2 = roots[i].getRawClasspathEntry().getOutputLocation();
				if (output2 == null) {
					output2 = project.getOutputLocation();
				}
				if (output1.equals(output2)) {
					result.add(roots[i]);
				}
			}
		} catch (JavaModelException e) {
			S2DaoPlugin.log(e);
		}
		return (IPackageFragmentRoot[]) result.toArray(
				new IPackageFragmentRoot[result.size()]);
	}
	
	private static IPackageFragmentRoot ascend(IJavaElement element) {
		while (element instanceof IPackageFragment) {
			element = ((IPackageFragment) element).getParent();
		}
		return element instanceof IPackageFragmentRoot ?
				(IPackageFragmentRoot) element : null;
	}

}
