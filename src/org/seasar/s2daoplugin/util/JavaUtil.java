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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

public class JavaUtil {

	public static IType findPrimaryType(IResource resource) {
		if (resource == null || !isJavaFile(resource)) {
			return null;
		}
		Object obj = resource.getAdapter(IJavaElement.class);
		if (!(obj instanceof ICompilationUnit)) {
			return null;
		}
		return ((ICompilationUnit) obj).findPrimaryType();
	}
	
	public static IPath getPackagePath(IFile file) {
		if (file == null) {
			return null;
		}
		IJavaProject project = JavaCore.create(file.getProject());
		IPath[] srcPaths = JavaProjectUtil.getSourceFolderPaths(project);
		for (int i = 0; i < srcPaths.length; i++) {
			if (srcPaths[i].matchingFirstSegments(file.getFullPath())
					== srcPaths[i].segmentCount()) {
				return file.getFullPath().removeFirstSegments(
						srcPaths[i].segmentCount()).removeLastSegments(1);
			}
		}
		return null;
	}
	
	public static boolean isJavaFile(IResource resource) {
		return resource != null &&
				"java".equalsIgnoreCase(resource.getFileExtension());
	}
	
    public static String getPackageName(String className) {
    	if (StringUtil.isEmpty(className)) {
    		return null;
    	}
        int pos = className.lastIndexOf('.');
        if (pos > 0) {
            return className.substring(0, pos);
        }
        return null;
    }

    public static String getShortClassName(String className) {
    	if (StringUtil.isEmpty(className)) {
    		return null;
    	}
        int i = className.lastIndexOf('.');
        if (i > 0) {
            return className.substring(i + 1);
        }
        return className;
    }

}
