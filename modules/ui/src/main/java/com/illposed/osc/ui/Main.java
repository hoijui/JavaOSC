// SPDX-FileCopyrightText: 2003-2014 C. Ramakrishnan / Auracle
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

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
