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
package org.seasar.s2daoplugin.cache.util;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class MethodUtil {

	public static final IMethod[] EMPTY_METHODS = new IMethod[0];

	public static boolean isPublic(IMethod method) {
		return Flags.isPublic(getFlags(method));
	}
	
	public static boolean isProtected(IMethod method) {
		return Flags.isProtected(getFlags(method));
	}
	
	public static boolean isPackagePrivate(IMethod method) {
		int flag = getFlags(method);
		return !Flags.isPublic(flag) && !Flags.isProtected(flag) && !Flags.isPrivate(flag);
	}
	
	public static boolean isPrivate(IMethod method) {
		return Flags.isPrivate(getFlags(method));
	}
	
	public static boolean isFinal(IMethod method) {
		return Flags.isFinal(getFlags(method));
	}
	
	public static boolean isStatic(IMethod method) {
		return Flags.isStatic(getFlags(method));
	}
	
	private static int getFlags(IMethod method) {
		try {
			if (method != null) {
				return method.getFlags();
			}
		} catch (JavaModelException ignore) {
		}
		return -1;
	}

}
