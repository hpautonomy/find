package com.autonomy.abc.selenium.find;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OnPremNavBarSettings extends NavBarSettings {

    public OnPremNavBarSettings(final WebDriver driver) { super(driver);}

    public void goToSettings() {
        openSettings();
        header().findElement(By.cssSelector("li[data-pagename='settings'] a")).click();
    }

    public void goToDashboard(final String dashboardName) {
        openSideBar();
        header().findElement(By.cssSelector("ul.side-menu li[data-pagename='dashboards/" + dashboardName + "']")).click();
    }
}
