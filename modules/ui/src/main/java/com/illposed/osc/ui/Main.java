/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class Main extends JFrame {

	public Main() {
		super("OSC");

		final OscUI myUi = new OscUI(this);
		setBounds(10, 10, 500, 350);
		setContentPane(myUi);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent evt) {
				myUi.doSendGlobalOff(1000, 1001, 1002);
				System.exit(0);
			}
		});

		setVisible(true);
	}


	public static void main(final String[] args) {
		new Main();
	}
}
