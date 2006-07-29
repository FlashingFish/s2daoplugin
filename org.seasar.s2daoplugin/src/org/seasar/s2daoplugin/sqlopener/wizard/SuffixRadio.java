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
package org.seasar.s2daoplugin.sqlopener.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

public class SuffixRadio implements ISuffixRadio {

	private Button radio;
	private String suffix;
	
	public SuffixRadio(Composite composite) {
		radio = new Button(composite, SWT.RADIO);
	}
	
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setDbmsName(String name) {
		radio.setText(name);
	}

	public void setSelection(boolean selected) {
		radio.setSelection(selected);
	}
	
	public void addSelectionListener(Listener listener) {
		radio.addListener(SWT.Selection, listener);
	}
	
	public Button getButton() {
		return radio;
	}

}
