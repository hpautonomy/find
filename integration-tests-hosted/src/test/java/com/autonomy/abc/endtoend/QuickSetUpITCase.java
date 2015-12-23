package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.core.Is.is;

//CSA-1563
public class QuickSetUpITCase extends HostedTestBase {

    public QuickSetUpITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    GettingStartedPage gettingStarted;

    @Before
    public void setUp(){
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        getElementFactory().getPromotionsPage().deleteAllPromotions();

        body.getSideNavBar().switchPage(NavBarTabId.GETTING_STARTED);
        body = getBody();
        gettingStarted = getElementFactory().getGettingStartedPage();
    }

    @Test
    public void testQuickSetUp(){
        String site = "www.cnet.com";
        gettingStarted.addSiteToIndex(site);
        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Document \"http://" + site + "\" was uploaded successfully"));

        body.getTopNavBar().search(site);
        SearchPage searchPage = getElementFactory().getSearchPage();
        body = getBody();

        //Check promoting the correct document
        searchPage.selectAllIndexes();
        searchPage.findElement(By.xpath(".//label[text()[contains(., 'All')]]/div/ins")).click();
        new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()[contains(.,'default_index')]]"))).click();
        Waits.loadOrFadeWait();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        searchPage.getSearchResult(1).click();
        Waits.loadOrFadeWait();
        assertThat(getDriver().findElement(By.xpath("//*[text()='Reference']/../td")).getText(), is("http://" + site));
        getDriver().findElement(By.className("fa-close")).click();
        Waits.loadOrFadeWait();

        String promotionTitle = searchPage.createAPromotion();

        CreateNewPromotionsPage createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();

        String trigger = "trigger";

        createNewPromotionsPage.addSpotlightPromotion("", trigger);

        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
        assertThat(searchPage.getPromotedResult(1).getText(), is(promotionTitle));

        //Delete Promotion
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        PromotionsPage promotionsPage = getElementFactory().getPromotionsPage();
        promotionsPage.getPromotionLinkWithTitleContaining(trigger).findElement(By.className("promotion-delete")).click();
        Waits.loadOrFadeWait();
        getDriver().findElement(By.className("modal-action-button")).click();

        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Removed a spotlight promotion"));

        assertThat(promotionsPage.promotionsList().size(),is(0));
    }

    @After
    public void tearDown(){
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        getElementFactory().getPromotionsPage().deleteAllPromotions();
    }

}
