package org.openmrs.module.htmlformentry;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests voor audit-logging conform NEN-7510 8.15.
 *
 * Dekt:
 *  - Succesvolle acties worden gelogd (FORM_OPEN)
 *  - Mislukte acties worden gelogd (FORM_SUBMIT_FAILED)
 *  - Gevoelige gegevens (patiëntnaam) staan NIET in de logs
 */
public class AuditLoggingTest extends BaseModuleContextSensitiveTest {

    private TestAppender appender;
    private Logger rootLogger;

    @Before
    public void setUp() throws Exception {
        executeDataSet("org/openmrs/module/htmlformentry/include/HtmlFormEntryTest-data-openmrs-1.9.xml");

        appender = new TestAppender();
        appender.setThreshold(Level.ALL);

        rootLogger = LogManager.getRootLogger();
        rootLogger.addAppender(appender);
        rootLogger.setLevel(Level.ALL);
        Logger.getLogger("org.openmrs.module.htmlformentry").setLevel(Level.ALL);
    }

    @After
    public void tearDown() {
        rootLogger.removeAppender(appender);
    }

    /**
     * Test 1: Succesvolle actie — formulier openen logt een AUDIT FORM_OPEN entry.
     */
    @Test
    public void formOpen_shouldLogAuditEntry() throws Exception {
        Patient patient = Context.getPatientService().getPatient(2);

        new FormEntrySession(patient, null, Mode.ENTER, buildSimpleHtmlForm(), new MockHttpSession());

        assertTrue("AUDIT FORM_OPEN moet gelogd worden",
                appender.containsMessage("AUDIT | action=FORM_OPEN"));
    }

    /**
     * Test 2: FORM_OPEN log bevat patientId maar NIET de patiëntnaam.
     */
    @Test
    public void formOpen_shouldLogPatientIdButNotPatientName() throws Exception {
        Patient patient = Context.getPatientService().getPatient(2);
        String patientName = patient.getPersonName().getFullName();

        new FormEntrySession(patient, null, Mode.ENTER, buildSimpleHtmlForm(), new MockHttpSession());

        assertTrue("Log moet patientId bevatten",
                appender.containsMessage("patientId=" + patient.getPatientId()));
        assertFalse("Log mag GEEN patiëntnaam bevatten — gevoelige data",
                appender.containsMessage(patientName));
    }

    /**
     * Test 3: Mislukte validatie — validateSubmission logt FORM_SUBMIT_FAILED.
     */
    @Test
    public void validateSubmission_withErrors_shouldLogSubmitFailed() {
        FormSubmissionController controller = new FormSubmissionController();
        controller.addAction(new AlwaysFailingSubmissionAction());

        List<FormSubmissionError> errors = controller.validateSubmission(
                new FormEntryContext(Mode.ENTER), new MockHttpServletRequest());

        assertFalse("Er moeten validatiefouten zijn", errors.isEmpty());
        assertTrue("AUDIT FORM_SUBMIT_FAILED moet gelogd worden",
                appender.containsMessage("AUDIT | action=FORM_SUBMIT_FAILED"));
    }

    /**
     * Test 4: Succesvolle validatie — geen FORM_SUBMIT_FAILED in log.
     */
    @Test
    public void validateSubmission_withoutErrors_shouldNotLogSubmitFailed() {
        FormSubmissionController controller = new FormSubmissionController();

        controller.validateSubmission(
                new FormEntryContext(Mode.ENTER), new MockHttpServletRequest());

        assertFalse("FORM_SUBMIT_FAILED mag NIET gelogd worden bij succesvolle validatie",
                appender.containsMessage("AUDIT | action=FORM_SUBMIT_FAILED"));
    }

    /**
     * Test 5: Log bevat geen username — alleen userId.
     */
    @Test
    public void auditLog_shouldContainUserIdNotUsername() throws Exception {
        Patient patient = Context.getPatientService().getPatient(2);
        String username = Context.getAuthenticatedUser() != null
                ? Context.getAuthenticatedUser().getUsername() : null;

        new FormEntrySession(patient, null, Mode.ENTER, buildSimpleHtmlForm(), new MockHttpSession());

        assertTrue("Log moet userId bevatten",
                appender.containsMessage("userId="));
        if (username != null && !username.isEmpty()) {
            assertFalse("Log mag GEEN username bevatten (gevoelige data), alleen userId",
                    appender.containsMessage("userId=" + username));
        }
    }

    // ─── Hulpmethoden ──────────────────────────────────────────────────────────

    /**
     * Bouwt een minimale HtmlForm zoals RegressionTestHelper dat ook doet —
     * met Form en EncounterType zodat de FormEntrySession-constructor niet crasht.
     */
    private HtmlForm buildSimpleHtmlForm() {
        HtmlForm form = new HtmlForm();
        form.setXmlData("<htmlform></htmlform>");
        form.setForm(new Form(1));
        form.getForm().setEncounterType(new EncounterType(1));
        return form;
    }

    // ─── Hulpklasse: log-capture ────────────────────────────────────────────────

    static class TestAppender extends AppenderSkeleton {

        private final List<String> messages = new ArrayList<String>();

        @Override
        protected void append(LoggingEvent event) {
            messages.add(event.getRenderedMessage());
        }

        boolean containsMessage(String fragment) {
            for (String msg : messages) {
                if (msg != null && msg.contains(fragment)) return true;
            }
            return false;
        }

        @Override public void close() {}
        @Override public boolean requiresLayout() { return false; }
    }

    // ─── Hulpklasse: action die altijd faalt ────────────────────────────────────

    static class AlwaysFailingSubmissionAction
            implements org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction {

        @Override
        public Collection<FormSubmissionError> validateSubmission(
                FormEntryContext context, javax.servlet.http.HttpServletRequest submission) {
            List<FormSubmissionError> errors = new ArrayList<FormSubmissionError>();
            errors.add(new FormSubmissionError("test", "Geforceerde testfout"));
            return errors;
        }

        @Override
        public void handleSubmission(FormEntrySession session,
                javax.servlet.http.HttpServletRequest submission) {}
    }
}
