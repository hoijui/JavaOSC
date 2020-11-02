/*
 * Copyright (C) 2001-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

// this is the package we are in
package com.illposed.osc.ui;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.transport.OSCPort;
import com.illposed.osc.transport.OSCPortOut;
import com.illposed.osc.argument.OSCTimeTag64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * OscUI is a subClass of JPanel.
 */
public class OscUI extends JPanel {

	private static final int FREQ_MIN = 20;
	private static final int FREQ_MAX = 10000;

	// NOTE We make this static, because Logger is not Serializable,
	//   but JPanel (our super) is, and thus fields have to be too
	private static final Logger LOG = LoggerFactory.getLogger(OscUI.class);

	// declare some variables
	private final JFrame parent;
	private JTextField addressWidget;
	private JLabel portWidget;
	private JTextField textBox;
	private JTextField textBox2;
	private JTextField textBox3;
	private JTextField textBox4 = new JTextField(String.valueOf(1000), 8);
	private JLabel delayLabel;

	private JButton firstSynthButtonOn, secondSynthButtonOn, thirdSynthButtonOn;
	private JButton firstSynthButtonOff, secondSynthButtonOff, thirdSynthButtonOff;
	private JSlider slider, slider2, slider3;

	private OSCPortOut oscPort;

	// create a constructor
	// OscUI takes an argument of myParent which is a JFrame
	public OscUI(final JFrame myParent) {
		super();
		parent = myParent;
		makeDisplay();
		try {
			oscPort = new OSCPortOut();
		} catch (final Exception ex) {
			// this is just a demo program, so this is acceptable behavior
			LOG.error("Failed to create test OSC UDP out port", ex);
		}
	}

	// create a method for widget building
	private void makeDisplay() {

		// setLayout to be a BoxLayout
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// call these methods ???? to be defined later

		addOscServerAddressPanel();
		addGlobalControlPanel();
		addFirstSynthPanel();
		addSecondSynthPanel();
		addThirdSynthPanel();
	}

	// create a method for adding ServerAddress Panel to the OscUI Panel
	protected void addOscServerAddressPanel() {

		// variable addressPanel holds an instance of JPanel.
		// instance of JPanel received from makeNewJPanel method
		final JPanel addressPanel = makeNewJPanel1();
		addressPanel.setBackground(new Color(123, 150, 123));
		// variable addressWidget holds an instance of JTextField
		addressWidget = new JTextField("localhost");
		// variable setAddressButton holds an insatnce of JButton with
		// a "Set Address" argument for its screen name
		final JButton setAddressButton = new JButton("Set Address");
		setAddressButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				// perform the addressChanged method when action is received
				addressChanged();
			}
		});

		// variable portWidget holds an instance of JLabel with the OSCPortOut
		// as the text it looks like OSCPortOut has a method to get the default
		// SuperCollider port
		portWidget = new JLabel(Integer.toString(OSCPort.defaultSCOSCPort()));

		portWidget.setForeground(new Color(255, 255, 255));
		final JLabel portLabel = new JLabel("Port");
		portLabel.setForeground(new Color(255, 255, 255));

		// add the setAddressButton to the addressPanel
		addressPanel.add(setAddressButton);
		// portWidget = new JTextField("57110");
		// add the addressWidget to the addressPanel
		addressPanel.add(addressWidget);
		// add the JLabel "Port" to the addressPanel
		addressPanel.add(portLabel);
		// add te portWidget tot eh addressPanel
		addressPanel.add(portWidget);

		//??? add address panel to the JPanel OscUI
		add(addressPanel);
	}

	public void addGlobalControlPanel() {
		final JPanel globalControlPanel = makeNewJPanel();
		final JButton globalOffButton = new JButton("All Off");
		final JButton globalOnButton = new JButton("All On");
		textBox4 = new JTextField(String.valueOf(1000), 8);
		delayLabel = new JLabel("All Off delay in ms");
		delayLabel.setForeground(new Color(255, 255, 255));
		globalControlPanel.setBackground(new Color(13, 53, 0));

		globalOnButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				doSendGlobalOn(1000, 1001, 1002);
				firstSynthButtonOn.setEnabled(false);
				firstSynthButtonOff.setEnabled(true);
				slider.setEnabled(true);
				slider.setValue(2050);
				textBox.setEnabled(true);
				textBox.setText("440.0");
				secondSynthButtonOn.setEnabled(false);
				secondSynthButtonOff.setEnabled(true);
				slider2.setEnabled(true);
				slider2.setValue(2048);
				textBox2.setEnabled(true);
				textBox2.setText("440.0");
				thirdSynthButtonOn.setEnabled(false);
				thirdSynthButtonOff.setEnabled(true);
				slider3.setEnabled(true);
				slider3.setValue(2052);
				textBox3.setEnabled(true);
				textBox3.setText("440.0");
			}
		});
		// ??? have an anonymous class listen to the setAddressButton action
		globalOffButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				doSendGlobalOff(1000, 1001, 1002);
				firstSynthButtonOn.setEnabled(true);
				firstSynthButtonOff.setEnabled(false);
				slider.setEnabled(false);
				slider.setValue(0);
				textBox.setEnabled(false);
				textBox.setText("0");
				secondSynthButtonOn.setEnabled(true);
				secondSynthButtonOff.setEnabled(false);
				slider2.setEnabled(false);
				slider2.setValue(0);
				textBox2.setEnabled(false);
				textBox2.setText("0");
				thirdSynthButtonOn.setEnabled(true);
				thirdSynthButtonOff.setEnabled(false);
				slider3.setEnabled(false);
				slider3.setValue(0);
				textBox3.setEnabled(false);
				textBox3.setText("0");
			}
		});

		globalControlPanel.add(globalOnButton);
		globalControlPanel.add(globalOffButton);
		globalControlPanel.add(textBox4);
		globalControlPanel.add(delayLabel);
		add(globalControlPanel);
	}

	// create method for adding a the buttons and synths of the
	// first synth on one panel
	public void addFirstSynthPanel() {
		// the variable firstSynthPanel holds an instance of JPanel
		// created by the makeNewJPanel method
		final JPanel firstSynthPanel = makeNewJPanel();
		// the variable firstSynthButtonOn holds an instance of JButton labeled
		// "On"

		firstSynthPanel.setBackground(new Color(13, 23, 0));
		firstSynthButtonOn = new JButton("On");
		//firstSynthButtonOn.setBackground(new Color(123, 150, 123));
		// the variable firstSynthButtonOff holds an instance of JButton labeled
		// "Off"
		firstSynthButtonOff = new JButton("Off");
		firstSynthButtonOff.setEnabled(false);
		// the variable slider holds an instance of JSlider which is
		// set to be a Horizontal slider
		slider = new JSlider(JSlider.HORIZONTAL);
		// set the minimum value of the slider to FREQ_MIN
		slider.setMinimum(0);
		slider.setMaximum(FREQ_MAX);
		// set the inital value of the slider to 400
		//slider.setValue(1 / 5);
		slider.setEnabled(false);

		textBox = new JTextField(String.valueOf((1 / 5) * FREQ_MAX), 8);
		textBox.setEnabled(false);

		firstSynthButtonOn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				// when the on button is pushed, doSendOn method is invoked
				// send the arguments for frequency and node
				doSendOn(440, 1000);
				firstSynthButtonOn.setEnabled(false);
				firstSynthButtonOff.setEnabled(true);
				textBox.setText("440.0");
				textBox.setEnabled(true);
				slider.setValue(2050);
				slider.setEnabled(true);
			}
		});
		// when the on button is pushed, doSendOff method is invoked
		// send the argument for node
		firstSynthButtonOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				// when the action occurs the doSend1 method is invoked
				doSendOff(1000);
				firstSynthButtonOn.setEnabled(true);
				firstSynthButtonOff.setEnabled(false);
				slider.setEnabled(false);
				slider.setValue(0);
				textBox.setEnabled(false);
				textBox.setText("0");
			}
		});

		// when the slider is moved, doSendSlider method is invoked
		// send the argument for freq and node
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent evt) {
				final JSlider mySlider = (JSlider) evt.getSource();
				if (mySlider.getValueIsAdjusting()) {
					float freq = (float) mySlider.getValue();
					freq = (freq / FREQ_MAX) * (freq / FREQ_MAX);
					freq = freq * FREQ_MAX;
					freq = freq + FREQ_MIN;
					doPrintValue(freq);
					doSendSlider(freq, 1000);
				}
			}
		});

		// when the value in the text-box is changed, doSendSlider method is
		// invoked; send the argument for freq and node
		textBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent evt) {
				final JTextField field = (JTextField) evt.getSource();
				float freq = Float.valueOf(field.getText());
				if (freq > (FREQ_MIN + FREQ_MAX)) { freq = (FREQ_MIN + FREQ_MAX); doPrintValue(freq); }
				if (freq < FREQ_MIN) { freq = FREQ_MIN; doPrintValue(freq); }
				slider.setValue((int)(FREQ_MAX * Math.sqrt(((freq - FREQ_MIN) / FREQ_MAX))));
				doSendSlider(freq, 1000);
			}
		});


		// add firstSynthButtonOn to the firstSynthPanel
		firstSynthPanel.add(firstSynthButtonOn);
		// add firstSendButtonOff to the firstSynthPanel
		firstSynthPanel.add(firstSynthButtonOff);
		// add slider to the firstSynthPanel
		firstSynthPanel.add(slider);
		firstSynthPanel.add(textBox);

		// add the firstSynthpanel to the OscUI Panel
		add(firstSynthPanel);
	}

	///********************
	// create method for adding a the Second Synth Panel
	protected void addSecondSynthPanel() {
		// make a new JPanel called secondSynthPanel
		final JPanel secondSynthPanel = makeNewJPanel();
		secondSynthPanel.setBackground(new Color(13, 23, 0));
		// the variable secondSynthButtonOn holds an instance of JButton
		secondSynthButtonOn = new JButton("On");
		// the variable secondSynthButtonOff holds an instance of JButton
		secondSynthButtonOff = new JButton("Off");
		secondSynthButtonOff.setEnabled(false);
		// the variable slider2 holds an instance of JSlider positioned
		// horizontally
		slider2 = new JSlider(JSlider.HORIZONTAL);
		slider2.setMinimum(0);
		slider2.setMaximum(FREQ_MAX);
		slider2.setEnabled(false);

		textBox2 = new JTextField(String.valueOf((2 / 5) * FREQ_MAX), 8);
		textBox2.setEnabled(false);

		secondSynthButtonOn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				// when the action occurs the doSendOn method is invoked
				// with the arguments for freq and node
				doSendOn(440, 1001);
				secondSynthButtonOn.setEnabled(false);
				secondSynthButtonOff.setEnabled(true);
				slider2.setEnabled(true);
				slider2.setValue(2050);
				textBox2.setEnabled(true);
				textBox2.setText("440.0");
			}
		});
		// add the action for the Off button
		secondSynthButtonOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				// when the action occurs the doSendOff method is invoked
				// with the argument for node
				doSendOff(1001);
				secondSynthButtonOn.setEnabled(true);
				secondSynthButtonOff.setEnabled(false);
				slider2.setEnabled(false);
				slider2.setValue(0);
				textBox2.setEnabled(false);
				textBox2.setText("0");
			}
		});
		// add the action for the slider
		slider2.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent evt) {
				final JSlider mySlider2 = (JSlider) evt.getSource();
				if (mySlider2.getValueIsAdjusting()) {
					float freq = (float) mySlider2.getValue();
					freq = (freq / FREQ_MAX) * (freq / FREQ_MAX);
					freq = freq * FREQ_MAX;
					freq = freq + FREQ_MIN;
					doPrintValue2(freq);
					// arguments for freq and node
					doSendSlider(freq, 1001);
				}
			}
		});

		// when the value in the textbox is changed, doSendSlider method is
		// invoked; send the argument for freq and node
		textBox2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent evt) {
				final JTextField field = (JTextField) evt.getSource();
				float freq = Float.valueOf(field.getText());
				if (freq > (FREQ_MIN + FREQ_MAX)) {
					freq = (FREQ_MIN + FREQ_MAX);
					doPrintValue2(freq);
				} else if (freq < FREQ_MIN) {
					freq = FREQ_MIN;
					doPrintValue2(freq);
				}
				slider2.setValue((int)(FREQ_MAX * Math.sqrt(((freq - FREQ_MIN) / FREQ_MAX))));
				doSendSlider(freq, 1001);
			}
		});

		// ******************
		// add Buttons and Slider to secondSynthPanel
		secondSynthPanel.add(secondSynthButtonOn);
		secondSynthPanel.add(secondSynthButtonOff);
		secondSynthPanel.add(slider2);
		secondSynthPanel.add(textBox2);
		// add the secondSynthPanel2 to the OscUI Panel
		add(secondSynthPanel);

	}

	protected void addThirdSynthPanel() {
		final JPanel thirdSynthPanel = makeNewJPanel();
		thirdSynthPanel.setBackground(new Color(13, 23, 0));
		thirdSynthButtonOn = new JButton("On");
		thirdSynthButtonOff = new JButton("Off");
		thirdSynthButtonOff.setEnabled(false);
		slider3 = new JSlider(JSlider.HORIZONTAL);
		slider3.setMinimum(0);
		slider3.setMaximum(FREQ_MAX);
		slider3.setEnabled(false);

		textBox3 = new JTextField(String.valueOf((1 / 25) * FREQ_MAX), 8);
		textBox3.setEnabled(false);

		thirdSynthButtonOn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				// when the action occurs the doSendOn method is invoked
				// with arguments for freq and node
				doSendOn(440, 1002);
				thirdSynthButtonOn.setEnabled(false);
				thirdSynthButtonOff.setEnabled(true);
				slider3.setEnabled(true);
				slider3.setValue(2050);
				textBox3.setEnabled(true);
				textBox3.setText("440.0");
			}
		});

		thirdSynthButtonOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				// when the action occurs the doSendOff method is invoked
				// with argument for node
				doSendOff(1002);
				thirdSynthButtonOn.setEnabled(true);
				thirdSynthButtonOff.setEnabled(false);
				slider3.setEnabled(false);
				slider3.setValue(0);
				textBox3.setEnabled(false);
				textBox3.setText("0");
			}
		});

		slider3.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent evt) {
				//  JSlider source = (JSlider) e.getSource();
				final JSlider mySlider3 = (JSlider) evt.getSource();
				//if (source.getValueIsAdjusting()) {
				if (mySlider3.getValueIsAdjusting()) {
					// int freq = (int)source.getValue();
					float freq = (float) mySlider3.getValue();
					freq = (freq / FREQ_MAX) * (freq / FREQ_MAX);
					freq = freq * FREQ_MAX;
					freq = freq + FREQ_MIN;
					doPrintValue3(freq);
					// when the action occurs the doSendSlider method is invoked
					// with arguments for freq and node
					doSendSlider(freq, 1002);
				}
			}
		});

		// when the value in the textbox is changed, doSendSlider method is
		// invoked; send the argument for freq and node
		textBox3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent evt) {
				final JTextField field = (JTextField) evt.getSource();
				float freq = Float.valueOf(field.getText());
				if (freq > (FREQ_MIN + FREQ_MAX)) {
					freq = (FREQ_MIN + FREQ_MAX);
					doPrintValue3(freq);
				} else if (freq < FREQ_MIN) {
					freq = FREQ_MIN;
					doPrintValue3(freq);
				}
				slider3.setValue((int)(FREQ_MAX * Math.sqrt(((freq - FREQ_MIN) / FREQ_MAX))));
				doSendSlider(freq, 1002);
			}
		});


		// ******************
		// add thirdSynthButtons and slider to the thirdSynthPanel
		thirdSynthPanel.add(thirdSynthButtonOn);
		thirdSynthPanel.add(thirdSynthButtonOff);
		thirdSynthPanel.add(slider3);
		thirdSynthPanel.add(textBox3);
		// add the sendButtonPanel2 to the OscUI Panel
		add(thirdSynthPanel);

	}

	// here is the make new JPanel method
	protected JPanel makeNewJPanel() {
		// a variable tempPanel holds an instance of JPanel
		final JPanel tempPanel = new JPanel();
		// set the Layout of tempPanel to be a FlowLayout aligned left
		tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// function returns the tempPanel
		return tempPanel;
	}

	// here is the make new JPanel method
	protected JPanel makeNewJPanel1() {
		// a variable tempPanel holds an instance of JPanel
		final JPanel tempPanel1 = new JPanel();
		// set the Layout of tempPanel to be a FlowLayout aligned left
		tempPanel1.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// function returns the tempPanel
		return tempPanel1;
	}

	// actions
	// create a method for the addressChanged action (Set Address)
	public void addressChanged() {
		// the variable OSCPortOut tries to get an instance of OSCPortOut
		// at the address indicated by the addressWidget
		try {
			oscPort =
				new OSCPortOut(InetAddress.getByName(addressWidget.getText()));
			// if the oscPort variable fails to be instantiated then sent
			// the error message
		} catch (final Exception ex) {
			showError("Couldn't set address");
		}
	}

	// if "Set Address" has not been performed, then give the message to set
	// it first
	private void checkAddress() {
		// if "Set Address" has not been performed then give the message to set
		// it first
		if (null == oscPort) {
			showError("Please set an address first");
		}
	}

	// create a method for the doSend action (Send)
	public void doSendOn(final float freq, final int node) {
		checkAddress();

		// send an OSC message to start the synth "pink" on node 1000.
		final List<Object> args = new ArrayList<>(6);
		args.add("javaosc-example");
		args.add(node);
		args.add(1);
		args.add(0);
		args.add("freq");
		args.add(freq);
		// a comma is placed after /s_new in the code
		final OSCMessage msg = new OSCMessage("/s_new", args);

		// Object[] args2 = {new Symbol("amp"), 0.5};
		// OscMessage msg2 = new OscMessage("/n_set", args2);
		//oscPort.send(msg);

		// try to use the send method of oscPort using the msg in nodeWidget
		// send an error message if this doesn't happen
		try {
			oscPort.send(msg);
		} catch (final Exception ex) {
			showError("Couldn't send");
		}
	}

	// create a method for the doSend1 action (Send)
	public void doSendOff(final int node) {
		checkAddress();

		// send an OSC message to free the node 1000
		final List<Object> args = new ArrayList<Object>(1);
		args.add(node);
		final OSCMessage msg = new OSCMessage("/n_free", args);

		// try to use the send method of oscPort using the msg in nodeWidget
		// send an error message if this doesn't happen
		try {
			oscPort.send(msg);
		} catch (final Exception ex) {
			showError("Couldn't send");
		}
	}

	public void doPrintValue(final float freq) {
		textBox.setText(String.valueOf(freq));
	}

	public void doPrintValue2(final float freq) {
		textBox2.setText(String.valueOf(freq));
	}

	public void doPrintValue3(final float freq) {
		textBox3.setText(String.valueOf(freq));
	}

	// create a method for the doSend3 action (Send)
	public void doSendSlider(final float freq, final int node) {
		checkAddress();

		// send an OSC message to set the node 1000
		final List<Object> args = new ArrayList<>(3);
		args.add(node);
		args.add("freq");
		args.add(freq);
		final OSCMessage msg = new OSCMessage("/n_set", args);

		// try to use the send method of oscPort using the msg in nodeWidget
		// send an error message if this doesn't happen
		try {
			oscPort.send(msg);
		} catch (final Exception ex) {
			showError("Couldn't send");
		}
	}

	public void doSendGlobalOff(final int node1, final int node2, final int node3) {
		checkAddress();

		final List<Object> args1 = new ArrayList<>(1);
		args1.add(node1);
		final OSCMessage msg1 = new OSCMessage("/n_free", args1);

		final List<Object> args2 = new ArrayList<>(1);
		args2.add(node2);
		final OSCMessage msg2 = new OSCMessage("/n_free", args2);

		final List<Object> args3 = new ArrayList<>(1);
		args3.add(node3);
		final OSCMessage msg3 = new OSCMessage("/n_free", args3);

		// create a timeStamped bundle of the messages
		final List<OSCPacket> packets = new ArrayList<>(3);
		packets.add(msg1);
		packets.add(msg2);
		packets.add(msg3);
		final Date newDate = new Date();
		long time = newDate.getTime();
		final Integer delayTime = Integer.valueOf(textBox4.getText());
		time = time + delayTime.longValue();
		newDate.setTime(time);

		final OSCBundle bundle = new OSCBundle(packets, OSCTimeTag64.valueOf(newDate));

		try {
			oscPort.send(bundle);
		} catch (final Exception e) {
			showError("Couldn't send");
		}

	}

	public void doSendGlobalOn(final int node1, final int node2, final int node3) {
		checkAddress();

		final List<Object> args1 = new ArrayList<>(4);
		args1.add("javaosc-example");
		args1.add(node1);
		args1.add(1);
		args1.add(0);
		final OSCMessage msg1 = new OSCMessage("/s_new", args1);

		final List<Object> args2 = new ArrayList<>(4);
		args2.add("javaosc-example");
		args2.add(node2);
		args2.add(1);
		args2.add(0);
		final OSCMessage msg2 = new OSCMessage("/s_new", args2);

		final List<Object> args3 = new ArrayList<>(4);
		args3.add("javaosc-example");
		args3.add(node3);
		args3.add(1);
		args3.add(0);
		final OSCMessage msg3 = new OSCMessage("/s_new", args3);

		try {
			oscPort.send(msg1);
		} catch (final Exception ex) {
			showError("Couldn't send");
		}

		try {
			oscPort.send(msg2);
		} catch (final Exception ex) {
			showError("Couldn't send");
		}

		try {
			oscPort.send(msg3);
		} catch (final Exception ex) {
			showError("Couldn't send");
		}
	}

	// create a showError method
	protected void showError(final String anErrorMessage) {
		// tell the JOptionPane to showMessageDialog
		JOptionPane.showMessageDialog(parent, anErrorMessage);
	}
}
