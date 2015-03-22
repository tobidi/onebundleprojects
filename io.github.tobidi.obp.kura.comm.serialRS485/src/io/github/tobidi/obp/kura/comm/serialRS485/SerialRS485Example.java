package io.github.tobidi.obp.kura.comm.serialRS485;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

import org.eclipse.kura.comm.CommConnection;
import org.eclipse.kura.comm.CommURI;
import org.osgi.service.io.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class SerialRS485Example {

	private static final Logger log = LoggerFactory
			.getLogger(SerialRS485Example.class);

	ConnectionFactory connectionFactory;

	private CommConnection connOne;

	private InputStream isOne;

	private OutputStream osOne;

	@Reference
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void unsetConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = null;
	}

	@Activate
	public void activate() {

		GPIOPinConfig pinConfig = new GPIOPinConfig(DeviceConfig.DEFAULT, 22,
				GPIOPinConfig.DIR_OUTPUT_ONLY, -1, GPIOPinConfig.TRIGGER_NONE,
				true);

		GPIOPin directionPin = null;

		try {
			directionPin = (GPIOPin) DeviceManager.open(GPIOPin.class,
					pinConfig);

		} catch (IOException e1) {
			log.error(e1.getMessage(), e1);
			return;
		}

		if (connectionFactory != null) {

			try {
				String uri = new CommURI.Builder("/dev/ttyAMA0")
						.withBaudRate(19200).withDataBits(CommURI.DATABITS_8)
						.withStopBits(CommURI.STOPBITS_1)
						.withParity(CommURI.PARITY_NONE).withTimeout(2000)
						.withFlowControl(CommURI.FLOWCONTROL_NONE).build()
						.toString();

				directionPin.setValue(false);
				connOne = (CommConnection) connectionFactory.createConnection(
						uri, 1, false);
				directionPin.setValue(true);

				isOne = connOne.openInputStream();
				osOne = connOne.openOutputStream();

				byte[] array = "ping\n\r".getBytes();

				// directionPin.setValue(true);
				osOne.write(array);
				osOne.flush();
				// directionPin.setValue(false);

				isOne.close();
				osOne.close();
				isOne = null;
				osOne = null;

				connOne.close();
				connOne = null;

				directionPin.close();

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			log.warn("No ConnectionFactory");
		}

	}

}
