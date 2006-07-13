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

import org.eclipse.core.resources.IResource;

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

}
