package org.jboss.ddoyle.drools;

import java.util.concurrent.TimeUnit;

import org.drools.core.time.impl.PseudoClockScheduler;
import org.jboss.ddoyle.drools.model.SimpleEvent;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		LOGGER.debug("Initializing KieServices and KieContainer.");
		KieServices kieServices = KieServices.Factory.get();
		KieContainer kieContainer = kieServices.newKieClasspathContainer();
		KieSession kieSession = kieContainer.newKieSession();
		 
		testExpiration(kieSession);

		kieSession.dispose();
	}

	
	private static void testExpiration(final KieSession kieSession) {
		LOGGER.debug("Running testThree. Rule should fire.");
		PseudoClockScheduler clock = kieSession.getSessionClock();
		
		//Insert the clock as a fact into the session so we can reason over it to be able to implement event GC/retraction.
		FactHandle clockHandle = kieSession.insert(clock);

		//Insert first event and fire.
		SimpleEvent event1 = new SimpleEvent("1", 0);
		//We need to pass the clockHandle here, as we need to update the clock fact when we advance the time.
		insertAndFire(kieSession, clockHandle, event1);

		//Insert second event and fire.
		SimpleEvent event2 = new SimpleEvent("2", 31000);
		insertAndFire(kieSession, clockHandle, event2);
		
		if (kieSession.getFactCount() != 3) {
			throw new IllegalStateException("Should have 3 events in WorkingMemory.");
		}
		
		//One event should expire
		LOGGER.debug("First event should expire after 300 seconds.");
		advanceClock(kieSession, clockHandle, 270, TimeUnit.SECONDS);
		kieSession.fireAllRules();
		//One event should have been retracted.
		if (kieSession.getFactCount() != 2) {
			throw new IllegalStateException("Should have 2 events in WorkingMemory.");
		}
		
		LOGGER.debug("Second event should expire after 331 seconds.");
		//Second event should expire.
		advanceClock(kieSession, clockHandle, 270, TimeUnit.SECONDS);
		kieSession.fireAllRules();
		//And the second event should have been retracted. Now we should only have the clock in WM.
		if (kieSession.getFactCount() != 1) {
			throw new IllegalStateException("Should have 1 event in WorkingMemory.");
		}
	}

	private static FactHandle insertAndFire(KieSession kieSession, FactHandle clockHandle, SimpleEvent simpleEvent) {
		LOGGER.debug("Inserting event and firing rules.");
		PseudoClockScheduler clock = kieSession.getSessionClock();
		long currentClockTime = clock.getCurrentTime();
		long deltaTime = simpleEvent.getTimestamp() - currentClockTime;
		if (deltaTime > 0) {
			advanceClock(kieSession, clockHandle, deltaTime, TimeUnit.MILLISECONDS);
		}
		FactHandle handle = kieSession.insert(simpleEvent);
		kieSession.fireAllRules();
		LOGGER.debug("Rules fired.");
		return handle;
	}

	private static void advanceClock(KieSession kieSession, FactHandle clockHandle, long amount, TimeUnit unit) {
		PseudoClockScheduler clock = kieSession.getSessionClock();
		clock.advanceTime(amount, unit);
		//Because we use the clock as a fact in WM, we need to notify the engine when we advance the clock.
		kieSession.update(clockHandle, clock);
		LOGGER.debug("Current time: " + clock.getCurrentTime());
	}

	
}
