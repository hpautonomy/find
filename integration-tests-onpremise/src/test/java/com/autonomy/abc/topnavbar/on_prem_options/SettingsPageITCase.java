package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.config.HostAndPorts;
import com.autonomy.abc.selenium.iso.OPISOElementFactory;
import com.autonomy.abc.selenium.iso.SettingsPage;
import com.autonomy.abc.selenium.util.Waits;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.*;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openqa.selenium.lift.Matchers.displayed;


public class SettingsPageITCase extends ABCTestBase {
	private final static Map<SettingsPage.Panel, HostAndPorts> HOSTS_AND_PORTS;
	private final static EnumSet<SettingsPage.Panel> SERVER_PANELS = EnumSet.of(SettingsPage.Panel.COMMUNITY, SettingsPage.Panel.CONTENT, SettingsPage.Panel.QMS, SettingsPage.Panel.QMS_AGENTSTORE, SettingsPage.Panel.STATSSERVER, SettingsPage.Panel.VIEW);

	private SettingsPage settingsPage;

	static {
		try {
			HOSTS_AND_PORTS = new ObjectMapper().convertValue(TestConfig.getRawBaseConfig().path("servers"), new TypeReference<Map<SettingsPage.Panel, HostAndPorts>>() {});
			System.out.println(HOSTS_AND_PORTS);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public SettingsPageITCase(final TestConfig config) {
		super(config);
	}

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.ON_PREM);
		return parameters(applicationTypes);
	}

	@Override
	public OPISOElementFactory getElementFactory() {
		return (OPISOElementFactory) super.getElementFactory();
	}

	@Before
	public void setUp() throws InterruptedException {
		settingsPage = getApplication().switchTo(SettingsPage.class);
	}

	@Test
	public void testSaveChangesModal() {
		settingsPage.saveChangesClick();
		final ModalView saveModal = ModalView.getVisibleModalView(getDriver());
		assertThat("Correct modal not open", saveModal.getText().contains("Confirm Save"));

		settingsPage.modalCancel().click();
		Waits.loadOrFadeWait();

		settingsPage.saveChangesClick();
		final ModalView saveModalAgain = ModalView.getVisibleModalView(getDriver());
		assertThat("Correct modal not open", saveModalAgain.getText().contains("Confirm Save"));
		settingsPage.modalSaveChanges().click();

		Waits.loadOrFadeWait();
		final ModalView confirmModal = ModalView.getVisibleModalView(getDriver());
		assertThat("Correct modal not open", confirmModal.getText().contains("Success! Configuration has been saved"));

		settingsPage.modalClose();
		assertThat("Modal not closed", !saveModal.getText().contains("Confirm Save"));

	}

	@Test
	public void testRevertChangesModal() {
		settingsPage.revertChangesClick();
		final ModalView revertModal = ModalView.getVisibleModalView(getDriver());
		assertThat("Correct modal not open", revertModal.getText().contains("Revert settings"));

		settingsPage.modalCancel().click();
		Waits.loadOrFadeWait();

		settingsPage.revertChangesClick();
		final ModalView revertModalAgain = ModalView.getVisibleModalView(getDriver());
		assertThat("Correct modal not open", revertModalAgain.getText().contains("Revert settings"));
		settingsPage.modalOKButton().click();
	}

	@Test
	public void testAllSettingsPanelsPresent() {
		for (final SettingsPage.Panel panel : SettingsPage.Panel.values()) {
			if (panel.equals(SettingsPage.Panel.LOCALE)) continue;

			assertThat(settingsPage.getPanelWithName(panel.getTitle()), displayed());
		}
	}

	@Test
	public void testRevertChangesPort() {
		settingsPage.saveChanges();
		final EnumMap<SettingsPage.Panel, Integer> originalPortValues = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel panel : SERVER_PANELS) {
			final WebElement portBox = settingsPage.portBox(panel);
			originalPortValues.put(panel, Integer.parseInt(portBox.getAttribute("value")));
			settingsPage.changePort(1000, panel);
			assertThat(panel.getTitle() + " port should be changed to 1000", portBox.getAttribute("value").equals(Integer.toString(1000)));
		}

		settingsPage.revertChanges();
		final EnumMap<SettingsPage.Panel, Integer> finalPortValues = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
			finalPortValues.put(settingsPanel, Integer.parseInt(settingsPage.portBox(settingsPanel).getAttribute("value")));
		}

		assertThat(originalPortValues, is(finalPortValues));
	}

	@Test
	public void testRevertChangesHostname() {
		settingsPage.saveChanges();
		final EnumMap<SettingsPage.Panel, String> originalHostNames = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel panel : SERVER_PANELS) {
			final WebElement hostBox = settingsPage.hostBox(panel);
			originalHostNames.put(panel, hostBox.getAttribute("value"));
			settingsPage.changeHost("richard", panel);
			assertThat(panel.getTitle() + " host should be changed to richard", hostBox.getAttribute("value").equals("richard"));
		}

		settingsPage.revertChanges();
		final EnumMap<SettingsPage.Panel, String> finalHostNames = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
			assertThat(settingsPanel + " hostname should not equal richard", !settingsPage.hostBox(settingsPanel).getAttribute("value").equals("richard"));
			finalHostNames.put(settingsPanel, settingsPage.hostBox(settingsPanel).getAttribute("value"));
		}

		assertThat(originalHostNames, is(finalHostNames));
	}

	@Test
	public void testRevertChangesProtocol() {
		settingsPage.saveChanges();
		final EnumMap<SettingsPage.Panel, String> originalProtocol = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
			originalProtocol.put(settingsPanel, settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value"));
			settingsPage.selectProtocol("HTTPS", settingsPanel);
			assertThat(settingsPanel + " protocol should be changed to https", settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value").equals("HTTPS"));
		}
		settingsPage.revertChanges();
		final EnumMap<SettingsPage.Panel, String> finalProtocol = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
			assertThat(settingsPanel + " hostname should not equal https", !settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value").equals("HTTPS"));
			finalProtocol.put(settingsPanel, settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value"));
		}

		assertThat(originalProtocol, is(finalProtocol));
	}

	@Test
	public void testRevertToNewlySaved() {
		settingsPage.saveChanges();
		final List<SettingsPage.Panel> settingsPanels = Arrays.asList(SettingsPage.Panel.COMMUNITY, SettingsPage.Panel.QMS_AGENTSTORE);
		final List<String> originalHostNames = new ArrayList<>();

		for (final SettingsPage.Panel settingsPanel : settingsPanels) {
			originalHostNames.add(settingsPage.hostBox(settingsPanel).getAttribute("value"));
			settingsPage.changeHost("idol-admin-test-01", settingsPanel);
			assertThat(settingsPanel + " hostname should be changed to idol-admin-test-01", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("idol-admin-test-01"));
		}

		settingsPage.saveChanges();

		for (final SettingsPage.Panel settingsPanel : settingsPanels) {
			settingsPage.changeHost("andrew", settingsPanel);
			assertThat(settingsPanel + " hostname should be changed to andrew", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("andrew"));
		}

		settingsPage.revertChanges();

		for (final SettingsPage.Panel settingsPanel : settingsPanels) {
			assertThat(settingsPanel + " hostname should not be andrew", !settingsPage.hostBox(settingsPanel).getAttribute("value").equals("andrew"));
			assertThat(settingsPanel + " hostname should equal idol-admin-test-01", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("idol-admin-test-01"));
		}

		for (int i = 0; i < 2; i++) {
			settingsPage.changeHost(originalHostNames.get(i), settingsPanels.get(i));
		}
	}

	@Test
	public void testEnterBadHostAndPortNames() {
		settingsPage.saveChanges();
		settingsPage.changeHost("richard", SettingsPage.Panel.CONTENT);
		settingsPage.testConnection("Content");
	}

	@Test
	public void testBlankPortsAndHosts() {
		for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
			settingsPage.changeHost("", settingsPanel);
			settingsPage.testConnection(settingsPanel.getTitle());
			assertThat("Incorrect/No Error Message", settingsPage.getPanelWithName(settingsPanel.getTitle()).getText().contains("Host name must not be blank!"));

			settingsPage.changeHost("a", settingsPanel);
			settingsPage.portBox(settingsPanel).clear();
			settingsPage.testConnection(settingsPanel.getTitle());
			assertThat("Incorrect/No Error Message in panel " + settingsPanel.getTitle(), settingsPage.getPanelWithName(settingsPanel.getTitle()).getText().contains("Port must not be blank, and inside the range 1-65535"));
		}
	}

	@After
	public void setDefaultSettings() {
		for (final SettingsPage.Panel panel : SettingsPage.Panel.values()) {
			final HostAndPorts hostAndPort = HOSTS_AND_PORTS.get(panel);

			if (hostAndPort.getPortNumber() != 0) {
				settingsPage.changePort(hostAndPort.getPortNumber(), panel);
				settingsPage.changeHost(hostAndPort.getHostName(), panel);
				settingsPage.selectProtocol("HTTP", panel);
			}
		}

		settingsPage.saveChanges();
	}

}
