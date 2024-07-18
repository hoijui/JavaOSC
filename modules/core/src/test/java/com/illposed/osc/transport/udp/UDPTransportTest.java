package com.illposed.osc.transport.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.BufferOverflowException;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

public class UDPTransportTest
{
	@Test
	void testSimultaneusReadWrite() throws IOException, OSCSerializeException, InterruptedException {
		final SocketAddress local = new InetSocketAddress(11111);
		final SocketAddress remote = new InetSocketAddress(22222);
		final UDPTransport transport = new UDPTransport(local, remote);
		final UDPTransport reverseTransport = new UDPTransport(remote, local);

		final Object lock = new Object();
		final Exception[] result = new Exception[1];

		final Thread readThread = new Thread(() -> {
			while (true)
			{
				try
				{
					transport.receive();

					// We should not get here
				}
				catch (IOException | OSCParseException | BufferOverflowException e)
				{
					result[0] = e;
				}
				finally
				{
					synchronized (lock)
					{
						lock.notifyAll();
					}
				}
			}
		});
		readThread.setDaemon(true);
		readThread.start();

		// Put the transport in the "I lastly send a message" state
		transport.send(new OSCMessage("/xremote"));

		// Trigger the bug
		synchronized (lock)
		{
			reverseTransport.send(new OSCMessage("/xremote"));
			lock.wait(1000);
		}

		// Assert the result
		if (result[0] != null)
		{
			throw new AssertionFailedError("Can not read in one thread and write in another", result[0]);
		}
	}
}
