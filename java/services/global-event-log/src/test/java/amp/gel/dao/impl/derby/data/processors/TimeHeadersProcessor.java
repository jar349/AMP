package amp.gel.dao.impl.derby.data.processors;

import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import amp.messaging.EnvelopeHelper;
import amp.messaging.MessageContext;
import amp.messaging.MessageContext.Directions;
import amp.messaging.IContinuationCallback;

/**
 * Processes event sequences and sets creation and receipt times in the envelope
 * headers. The processor starts at a given time, advances time, and continues
 * until it reaches the given stop time. Once time has ended, then an Exception
 * is thrown at the next event sequence. This signals the event generator to
 * stop.
 * 
 */
public class TimeHeadersProcessor implements EventSequenceProcessor {

	private static final DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat
			.date();

	private DateTime startDate;

	private DateTime stopDate = new DateTime();

	public void setStartDate(String startDate) {
		this.startDate = DATE_FORMATTER.parseDateTime(startDate);
		this.currentTime = this.startDate;
	}

	public void setStopDate(String stopDate) {
		this.stopDate = DATE_FORMATTER.parseDateTime(stopDate);
	}

	private DateTime currentTime;

	private Random random = new Random();

	public void processMessage(MessageContext eventContext,
			IContinuationCallback continuationCallback) {
		if (Directions.Out == eventContext.getDirection()) {
			EnvelopeHelper env = new EnvelopeHelper(eventContext.getEnvelope());

			DateTime creationTime = getCreationTime();
			env.setCreationTime(creationTime);

			DateTime receiptTime = getReceiptTime(creationTime);
			env.setReceiptTime(receiptTime);

			advanceTimeBetweenEvents();
		}
	}

	public void restartEventSequence() throws Exception {
		advanceTimeBetweenSequencesOfEvents();

		if (stopDate.isBefore(currentTime)) {
			throw new Exception("Time to stop generating event sequences!");
		}
	}

	private DateTime getReceiptTime(DateTime creationTime) {
		DateTime receiptTime = creationTime
				.plusMillis(randomTimeBetweenCreationTimeAndReceiptTime());
		return receiptTime;
	}

	private DateTime getCreationTime() {
		DateTime creationTime = new DateTime(currentTime.getMillis());
		return creationTime;
	}

	private void advanceTimeBetweenEvents() {
		currentTime = currentTime.plusMillis(randomTimeBetweenEvents());
	}

	private void advanceTimeBetweenSequencesOfEvents() {
		currentTime = currentTime
				.plusMillis(randomTimeBetweenSequencesOfEvents());
	}

	private int randomTimeBetweenEvents() {
		int randomNumberOfMinutes = random.nextInt(5) + 1;
		int millis = randomNumberOfMinutes * 60 * 1000;
		return millis;
	}

	private int randomTimeBetweenSequencesOfEvents() {
		int randomNumberOfMinutes = random.nextInt(30) + 1;
		int millis = randomNumberOfMinutes * 60 * 1000;
		return millis;
	}

	private int randomTimeBetweenCreationTimeAndReceiptTime() {
		int randomNumberOfSeconds = random.nextInt(60) + 1;
		int millis = randomNumberOfSeconds * 1000;
		return millis;
	}
}
