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
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class JavaUtil {

	public static boolean isJavaFile(IResource resource) {
		return hasExtension(resource, "java");
	}
	
	public static boolean isClassFile(IResource resource) {
		return hasExtension(resource, "class");
	}
	
	private static boolean hasExtension(IResource resource, String extension) {
		return resource != null &&
				extension.equalsIgnoreCase(resource.getFileExtension());
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
    
    // 不完全。output locationが変わりかつ、classFile.existsがfalseなら一意に
    // 特定できるとは限らない。。。というか特定する術がない気が…
    public static String getFullyQualifiedNameFromClassFile(IClassFile classFile) {
    	if (classFile == null) {
    		return null;
    	}
    	IJavaProject project = classFile.getJavaProject();
    	IPath[] outputs = getOutputLocationPaths(project);
    	for (int i = 0; i < outputs.length; i++) {
    		if (classFile.getPath().matchingFirstSegments(outputs[i]) ==
    			outputs[i].segmentCount()) {
    			IPath path = classFile.getPath().removeFirstSegments(
    					outputs[i].segmentCount()).removeFileExtension();
				return path.removeFileExtension().toString()
						.replace('/', '.').replace('$', '.');
    		}
    	}
    	return null;
    }
    
    private static IPath[] getOutputLocationPaths(IJavaProject project) {
    	IPackageFragmentRoot[] roots;
		try {
			roots = project.getPackageFragmentRoots();
		} catch (JavaModelException e) {
			return null;
		}
		Set ret = new HashSet();
		try {
			ret.add(project.getOutputLocation());
		} catch (JavaModelException e) {
			return new IPath[0];
		}
    	for (int i = 0; i < roots.length; i++) {
    		if (roots[i].isArchive()) {
    			continue;
    		}
    		try {
	    		IPath output = roots[i].getRawClasspathEntry().getOutputLocation();
	    		if (output != null) {
	    			ret.add(output);
	    		}
    		} catch (JavaModelException ignore) {
    		}
    	}
    	return (IPath[]) ret.toArray(new IPath[ret.size()]);
    }

}
